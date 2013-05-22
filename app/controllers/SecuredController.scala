package controllers

import RequestHelper._
import play.api.mvc._
import model.security.User

trait SecuredController extends Controller {
  def AuthAction(body: Request[AnyContent] => User => Result) = Action { implicit request =>
    loggedInUser.fold[Result](Redirect(routes.Auth.login))(body(request))
  }
}