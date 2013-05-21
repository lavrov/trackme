package controllers

import play.api.mvc._
import Context._

object Application extends Controller {

  def index = Action { implicit request =>
    Ok(views.html.index(request))
  }

  def about = Action { implicit request =>
    Ok(views.html.about(request))
  }
}