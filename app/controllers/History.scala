package controllers

import play.api.mvc._
import play.api.libs.json.Json._
import play.api.data.Form
import play.api.data.Forms._

import org.joda.time._
import java.util.Date

import model.PositionDao


object History extends SecuredController {
  import PositionMarshaller._

  val dateFormat = "yyyy-MM-dd HH:mm"

  val timeIntervalForm = Form(
    tuple(
      "intervalType" -> nonEmptyText,
      "beginning" -> optional(date(dateFormat)),
      "end" -> optional(date(dateFormat))
    )
  )

  def postInterval = AuthAction { implicit req => _ =>
    def result(b: Date, e: Date) = Ok(toJson(
      PositionDao.betweenInterval(b, e)
        .map(toJson(_))
    ))
    timeIntervalForm.bindFromRequest.fold(
    form => BadRequest("Incorrect form data"), {
      case ("custom", Some(b), Some(e)) => result(b, e)
      case (intervalType, _, _) => intervalType match {
        case "lastHour" => result(DateTime.now.minus(Duration.standardHours(1)).toDate, new Date)
        case "lastDay" => result(DateTime.now.minusDays(1).toDate, new Date)
      }
      case _ => BadRequest("Incorrect form data")
    })
  }
}
