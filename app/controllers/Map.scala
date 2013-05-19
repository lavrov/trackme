package controllers

import play.api.mvc._
import play.api.libs.iteratee.{Enumerator, Enumeratee, Iteratee}
import play.api.libs.json.Json._
import model.{PositionDao, Position, ObjectTracker}
import play.api.Logger

object Map extends Controller {

  def positionToString(position: Position) = stringify(toJson(
    collection.immutable.Map(
      "longitude" -> toJson(position.longitude),
      "latitude" -> toJson(position.latitude),
      "time" -> toJson(position.timestamp.getTime)
    )
  ))

  def map = Action { implicit request =>
    Ok(views.html.map("Map"))
  }

  def ws = WebSocket.using { request =>
    Logger.info("WebSocket connected")

    val in = Iteratee.consume[String]().mapDone( _ =>
      Logger.info("WebSocket disconnected")
    )

    val out = Enumerator(PositionDao.lastPoints: _*) >- ObjectTracker.enumerator &> Enumeratee.map[Position](positionToString)

    (in, out)
  }
}