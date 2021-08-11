package trg_assessment

import akka.actor.{ Actor, ActorLogging, Props }
import com.typesafe.config.ConfigFactory
import io.circe._
import io.circe.generic.AutoDerivation
import io.circe.parser._
import io.circe.syntax._
import org.apache.hadoop.fs.{ FileSystem, Path }
import org.apache.spark.sql.SparkSession
import spray.json.{ JsArray, JsBoolean, JsNull, JsNumber, JsObject, JsString, JsValue }
import trg_assessment.json.outputJson._

import scala.language.postfixOps

object ReqProcActor extends AutoDerivation {

  final case class JsonResult(json: JsValue)
  final case class GetData(numRows: Int)
  final case object GetDifferentCrimes
  final case class GetCrimesByDistrict(numRows: Int)
  final case object GetCrimesByCrimeType

  def props: Props = Props[ReqProcActor]

  def jsonToJsValue(cj: Json): JsValue =
    cj.fold(
      JsNull,
      JsBoolean(_),
      jn => jn.toBigDecimal.fold[JsValue](JsString(jn.toString))(JsNumber(_)),
      JsString(_),
      js => JsArray(js.map(jsonToJsValue)),
      jo => JsObject(jo.toMap.mapValues(jsonToJsValue)))

  def apply(implicit fs: FileSystem, spark: SparkSession): Props =
    Props(new ReqProcActor())
}

class ReqProcActor()(implicit fs: FileSystem, spark: SparkSession) extends Actor with ActorLogging {
  import ReqProcActor._
  import spark.implicits._

  log.info(s"[${context.self}]: ReqProcActor routee created")

  val conf = ConfigFactory.load

  def receive: Receive = {
    case GetData(numRows) =>
      val dataPath = conf.getString("data.base-data-path")

      if (fs.exists(new Path(dataPath))) {
        val res = BaseData(spark.read.parquet(dataPath)
          .as[Data]
          .take(numRows)
          .toList) asJson

        sender ! JsonResult(jsonToJsValue(res))

      } else {
        sender ! JsonResult(jsonToJsValue(PathDoesNotExist(s"Path $dataPath does not exist").asJson))
      }

    case GetDifferentCrimes =>
      val dataPath = conf.getString("data.kpis-different-crimes-path")

      if (fs.exists(new Path(dataPath))) {
        //it is supposed that the list of possible crimes is a small list
        val res = DifferentCrimes(spark.read.parquet(dataPath)
          .collect
          .map(_.getString(0))
          .toList) asJson

        sender ! JsonResult(jsonToJsValue(res))

      } else {
        sender ! JsonResult(jsonToJsValue(PathDoesNotExist(s"Path $dataPath does not exist").asJson))
      }

    case GetCrimesByDistrict(numRows: Int) =>
      val dataPath = conf.getString("data.kpis-crimes-by-district-path")

      if (fs.exists(new Path(dataPath))) {
        val res = ListCrimesByDistrict(spark.read.parquet(dataPath)
          .as[CrimesByDistrict]
          .take(numRows)
          .toList) asJson

        sender ! JsonResult(jsonToJsValue(res))

      } else {
        sender ! JsonResult(jsonToJsValue(PathDoesNotExist(s"Path $dataPath does not exist").asJson))
      }

    case GetCrimesByCrimeType =>
      val dataPath = conf.getString("data.kpis-crimes-by-crime-type-path")

      if (fs.exists(new Path(dataPath))) {
        //it is supposed that the list of possible crimes is a small list
        val res = ListCrimesByCrimeType(spark.read.parquet(dataPath)
          .as[CrimesByCrimeType]
          .collect
          .toList) asJson

        sender ! JsonResult(jsonToJsValue(res))

      } else {
        sender ! JsonResult(jsonToJsValue(PathDoesNotExist(s"Path $dataPath does not exist").asJson))
      }
  }
}
