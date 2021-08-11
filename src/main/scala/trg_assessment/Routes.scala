package trg_assessment

import akka.actor.{ ActorRef, ActorSystem }
import akka.event.Logging
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{ concat, path, pathPrefix }
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.directives.MethodDirectives.get
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.util.Timeout
import trg_assessment.ReqProcActor.{ GetCrimesByCrimeType, GetCrimesByDistrict, GetData, GetDifferentCrimes, JsonResult }
import akka.pattern.ask

import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

trait Routes extends JsonSupport {
  implicit def system: ActorSystem

  lazy val log = Logging(system, classOf[Routes])

  def processingActor: ActorRef

  implicit lazy val timeout = Timeout(5.seconds)

  lazy val processingRoutes: Route =
    pathPrefix("trg-assessment") {
      concat(
        path("debug") {
          get {
            log.info("Get Request Received")
            complete(StatusCodes.OK, "OK, it works !!")
          }
        },

        path("get-data") {
          withRequestTimeout(2.hours) {
            get {
              parameter("numRows".as[Int]) {
                numRows =>
                  val res: Future[JsonResult] =
                    processingActor
                      .ask(GetData(numRows))(2.hours)
                      .mapTo[JsonResult]
                  onSuccess(res) { performed =>
                    complete((StatusCodes.OK, performed))
                  }
              }
            }
          }
        },

        path("different-crimes") {
          withRequestTimeout(2.hours) {
            get {
              val res: Future[JsonResult] =
                processingActor
                  .ask(GetDifferentCrimes)(5.minutes)
                  .mapTo[JsonResult]
              onSuccess(res) { performed =>
                complete((StatusCodes.OK, performed))
              }
            }
          }
        },

        path("crimes-by-district") {
          withRequestTimeout(2.hours) {
            get {
              parameter("numRows".as[Int]) {
                numRows =>
                  val res: Future[JsonResult] =
                    processingActor
                      .ask(GetCrimesByDistrict(numRows))(2.hours)
                      .mapTo[JsonResult]
                  onSuccess(res) { performed =>
                    complete((StatusCodes.OK, performed))
                  }
              }
            }
          }
        },

        path("crimes-by-crime-type") {
          withRequestTimeout(2.hours) {
            get {
              val res: Future[JsonResult] =
                processingActor
                  .ask(GetCrimesByCrimeType)(2.hours)
                  .mapTo[JsonResult]
              onSuccess(res) { performed =>
                complete((StatusCodes.OK, performed))
              }
            }
          }
        })
    }
}
