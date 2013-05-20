package controllers

import play.api.mvc._
import play.api.libs.json.Json._
import play.api.data.Form
import play.api.data.Forms._

import model.PositionDao


object History extends Controller {
  import PositionMarshaller._

  val dateFormat = "yyyy-MM-dd HH:mm"

  val timeIntervalForm = Form(
    tuple(
      "beginning" -> date(dateFormat),
      "end" -> date(dateFormat)
    )
  )

  def postInterval = Action { implicit request =>
    timeIntervalForm.bindFromRequest.fold(
    form => BadRequest("Incorrect form data"),
    {
      case (b, e) =>
        Ok(toJson(
          PositionDao.betweenInterval(b, e)
            .map(toJson(_))
        ))
    }
    )
  }
}
