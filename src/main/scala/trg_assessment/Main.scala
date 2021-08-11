package trg_assessment

import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.routing.RoundRobinPool
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import org.apache.hadoop.fs.FileSystem
import org.apache.spark.sql.functions.{ col, input_file_name, when }
import org.apache.spark.sql.{ SaveMode, SparkSession }
import org.slf4j.LoggerFactory
import trg_assessment.Util._

import scala.concurrent.duration.Duration
import scala.concurrent.{ Await, ExecutionContext, Future }
import scala.util.{ Failure, Success }

object Main extends App with Routes {
  val mainLog = LoggerFactory.getLogger(Main.getClass)

  mainLog.info("Asking for a Spark Session ... ")

  implicit val spark = SparkSession.builder
    .master("local")
    .appName("trg-assessment")
    .getOrCreate

  mainLog.info(s"Spark Session ${spark.sparkContext.applicationId} got correctly.")

  implicit val fs: FileSystem =
    FileSystem.get(spark.sparkContext.hadoopConfiguration)

  implicit val system: ActorSystem = ActorSystem("trg-assessment-as")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContext = system.dispatcher

  val conf = ConfigFactory.load

  //street files
  val streets = spark.read.option("header", "true").option("delimiter", ",").schema(streetSchema)
    .csv(conf.getString("data.street-data-path"))
    .filter("crimeId is not null")
    .withColumn("districtName", extractDistrict(input_file_name))
    .select(streetColsOfInterest: _*)

  //outcomes files
  val outcomes = spark.read.option("header", "true").option("delimiter", ",").schema(outcomesSchema)
    .csv(conf.getString("data.outcomes-data-path"))
    .filter("crimeId is not null")
    .select("crimeId", "outcomeType")

  //Saving data & Kpis
  val data = streets.join(outcomes, List("crimeId"), "left")
    .withColumn(
      "lastOutcome",
      when(col("outcomeType").isNotNull, col("outcomeType"))
        .otherwise(col("lastOutcomeCategory")))
    .drop("outcomeType", "lastOutcomeCategory")
    .cache

  data
    .write.mode(SaveMode.Overwrite)
    .parquet(conf.getString("data.base-data-path"))

  //differentCrimes
  data.select("crimeType").distinct
    .write.mode(SaveMode.Overwrite)
    .parquet(conf.getString("data.kpis-different-crimes-path"))

  //crimesByDistrict
  data.groupBy("districtName").count.orderBy(col("count").desc)
    .write.mode(SaveMode.Overwrite)
    .parquet(conf.getString("data.kpis-crimes-by-district-path"))

  //crimesByCrimeType
  data.groupBy("crimeType").count.orderBy(col("count").desc)
    .write.mode(SaveMode.Overwrite)
    .parquet(conf.getString("data.kpis-crimes-by-crime-type-path"))

  //http-server
  val apiParallelism = conf.getInt("api.max-request-in-par")
  val processingActor: ActorRef = system
    .actorOf(RoundRobinPool(apiParallelism).props(Props(new ReqProcActor())), "processingRouter")

  lazy val routes: Route = processingRoutes

  val serverBinding: Future[Http.ServerBinding] =
    Http().bindAndHandle(routes, conf.getString("http-server.interface"), conf.getInt("http-server.port"))

  serverBinding.onComplete {
    case Success(bound) =>
      println(s"Server online at http://${bound.localAddress.getHostString}:${bound.localAddress.getPort}/")
    case Failure(e) =>
      Console.err.println(s"Server could not start!")
      e.printStackTrace()
      system.terminate()
  }

  Await.result(system.whenTerminated, Duration.Inf)
}
