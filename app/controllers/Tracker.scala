package controllers

import play.api.mvc.{Action, Controller}
import model.{Position, ObjectTracker}
import java.util.Date

object Tracker extends Controller {

  def update(longitude: String, latitude: String) = Action {
    ObjectTracker.broadcaster.push(Position(BigDecimal(longitude), BigDecimal(latitude), new Date))
    Ok("Position updated")
  }

  def ogtUpdate(restUrl: String) = ogtupdate

  def ogtupdate = Action(parse.urlFormEncoded) { implicit request =>
    val points =
      request.body.collect {
        case (name, Seq(head, _ *)) =>
          name.partition(_.isDigit) -> head
      }
      .groupBy {
        case ((number, _), _) => number
      }
      .mapValues(_.map(t => t._1._2 -> t._2))
      .map {
        case (number, parameterMap) =>
          Position(
            BigDecimal(parameterMap("longitude")),
            BigDecimal(parameterMap("latitude")),
            new Date()
          )
      }
    points.foreach(ObjectTracker.broadcaster.push)
    Ok(s"Received ${points.size} points. Data: ${request.body}")
  }
}
