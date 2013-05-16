package controllers

import play.api.mvc.{Action, Controller}
import model.{Position, ObjectTracker}
import java.util.Date

object Tracker extends Controller {

  def update(longitude: String, latitude: String) = Action {
    ObjectTracker.broadcaster.push(Position(BigDecimal(longitude), BigDecimal(latitude), new Date))
    Ok("Position updated")
  }

}
