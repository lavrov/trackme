package controllers

import play.api.libs.json.Json._
import model.State._

object StateController extends SecuredController {
  def get = AuthAction { implicit req => user =>
    Ok(toJson(
      collection.immutable.Map("currentObject" -> TrackingObject.get(user).getOrElse(""))
    ))
  }
}
