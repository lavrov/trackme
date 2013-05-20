package controllers

import play.api.mvc._
import play.api.libs.iteratee.{Enumerator, Enumeratee, Iteratee}
import play.api.libs.json.Json._
import model.{PositionDao, Position, ObjectTracker}
import play.api.Logger
import play.api.libs.json.{JsValue, Writes}

object PositionMarshaller {
  implicit object PositionWrites extends Writes[Position] {
    def writes(position: Position): JsValue = toJson(
      collection.immutable.Map(
        "longitude" -> toJson(position.longitude),
        "latitude" -> toJson(position.latitude),
        "time" -> toJson(position.timestamp.getTime)
      )
    )
  }
}

object Map extends Controller {
  import PositionMarshaller._

  def positionToString(position: Position) = stringify(toJson(position))

  def map = Action { implicit request =>
    Ok(views.html.map("Map"))
  }

  def ws = WebSocket.using { request =>
    Logger.info("WebSocket connected")

    val in = Iteratee.consume[String]().mapDone( _ =>
      Logger.info("WebSocket disconnected")
    )

    val out = Enumerator(PositionDao.lastPoint: _*) >- ObjectTracker.enumerator &> Enumeratee.map[Position](positionToString)

    (in, out)
  }
}