package controllers

import play.api.mvc._
import play.api.libs.iteratee.{Enumerator, Enumeratee, Iteratee}
import play.api.libs.json.Json._
import model._
import dao.{Position, PositionDao}
import play.api.Logger
import play.api.libs.json.{JsValue, Writes}
import RequestHelper._

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

  def map = AuthAction { implicit req => user =>
    Ok(views.html.map(Permissions.forUser(user.id).mayTrack, State.TrackingObject.get(user)))
  }

  def ws = WebSocket.using { implicit request =>
    val user = loggedInUser.get
    val trackingObject = request.getQueryString("trackingObject").get
    if(model.State.TrackingObject.set(user, trackingObject)) {

      val in = Iteratee.consume[String]().mapDone( _ =>
        Logger.info("WebSocket disconnected")
      )

      val out = Enumerator(PositionDao.forUser(trackingObject).lastPoint: _*) >-
        ObjectTracker.enumerator &> Enumeratee.filter[Position](_.userId == trackingObject) &> makeString

      Logger.info("WebSocket connected")

      (in, out)
    }
    else {
      throw new Exception(s"You may not track $trackingObject")
    }
  }
}