package controllers

import play.api.mvc._
import play.api.libs.iteratee.{Enumeratee, Iteratee}
import play.api.libs.json.Json
import model.{Position, ObjectTracker}
import play.api.Logger

object MapPage extends Controller {

  def positionToString(position: Position) = Json.stringify(Json.toJson(
    Map("longitude" -> position.longitude, "latitude" -> position.latitude)
  ))

  def map = Action { implicit request =>
    Ok(views.html.map("Map"))
  }

  def ws = WebSocket.using { request =>
    Logger.info("WebSocket connected")

    val in = Iteratee.consume[String]().mapDone( _ =>
      Logger.info("WebSocket disconnected")
    )

    val out = ObjectTracker.enumerator &> Enumeratee.map[Position](positionToString)

    (in, out)
  }
}
