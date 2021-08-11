package trg_assessment

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol
import trg_assessment.ReqProcActor.{ GetData, JsonResult }

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val actionPerformedJsonFormat = jsonFormat1(JsonResult)
}
