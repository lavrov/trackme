package controllers

import play.api.mvc.{Action, Controller}
import model.{Position, ObjectTracker}

object Tracker extends Controller {

  def update(longitude: Double, latitude: Double) = Action {
    ObjectTracker.broadcaster.push(Position(longitude, latitude))
    Ok("Position updated")
  }

}
