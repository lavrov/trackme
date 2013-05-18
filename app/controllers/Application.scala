package controllers

import play.api.mvc._

object Application extends Controller {
  
  def about = Action { implicit request =>
    Ok(views.html.about())
  }
}