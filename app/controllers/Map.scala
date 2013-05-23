package controllers

import play.api.mvc._
import play.api.libs.iteratee.{Enumerator, Enumeratee, Iteratee}
import play.api.libs.json.Json._
import model._
import play.api.Logger
import play.api.libs.json.{JsValue, Writes}
import RequestHelper._
import model.Position

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

object Map extends SecuredController {
  import PositionMarshaller._

  private val makeString = Enumeratee.map[Position](positionToString)

  def positionToString(position: Position) = stringify(toJson(position))

  def map = AuthAction { implicit req => _ =>
    Ok(views.html.map())
  }

  def ws = WebSocket.using { implicit request =>
    Logger.info("WebSocket connected")
    val user = loggedInUser.get
    val in = Iteratee.consume[String]().mapDone( _ =>
      Logger.info("WebSocket disconnected")
    )

    val mayTrack = Permissions.forUser(user.id).mayTrack.toSet

    val out = Enumerator(PositionDao.forUser(user.id).lastPoint: _*) >-
      ObjectTracker.enumerator &> Enumeratee.filter[Position](p => mayTrack contains p.userId) &> makeString
    (in, out)
  }
}