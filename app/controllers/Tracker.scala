package controllers

import play.api.mvc.{Result, Action, Controller}
import model.{Position, ObjectTracker}
import java.util.Date
import play.api.Logger

object Tracker extends Controller {

  def push(longitude: String, latitude: String) = Action { implicit req =>
    ObjectTracker.broadcaster.push(Position(BigDecimal(longitude), BigDecimal(latitude), new Date, req.getQueryString("object") getOrElse "lavrovvv@gmail.com"))
    Ok("Position updated")
  }

  def ogtPush(restUrl: String) = ogtpush

  def ogtpush = Action(parse.urlFormEncoded) { implicit request =>
    Logger.debug(s"OGT data received: ${request.body}")
    val result =
      for {
        tag <- request.body.get("tag")
        userId <- tag.headOption.map {
          case "test" => "lavrovvv@gmail.com"
          case other => other
        }
      } yield {
        val points = request.body.collect {
          case (PointParameterKey(index, name), values) if values.nonEmpty =>
            (index -> name) -> values.head
        }
        .groupBy {
          case ((index, _), _) => index
        }
        .mapValues(_.map {
          case ((_, parameter), value) => parameter -> value
        })
        .map {
          case (index, parameterMap) =>
            Position(
              BigDecimal(parameterMap("longitude")),
              BigDecimal(parameterMap("latitude")),
              new Date(),
              userId
            )
        }
        points.foreach(ObjectTracker.broadcaster.push)
        points
      }
    result.fold[Result](BadRequest("User id isn't set"))(points => Ok(s"Received ${points.size} points."))
  }
}

object PointParameterKey {
  def unapply(in: String) = in.partition(_.isDigit) match {
    case (index, name) if !index.isEmpty => Some(index.toInt -> name)
    case _ => None
  }
}
