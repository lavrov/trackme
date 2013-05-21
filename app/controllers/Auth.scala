package controllers

import play.api.mvc._
import play.api.libs.openid.OpenID
import play.api.libs.concurrent.Execution.Implicits._

object Auth extends Controller {

  def login = Action { implicit request =>
    AsyncResult(
      OpenID.redirectURL("https://www.google.com/accounts/o8/id", routes.Auth.openIDCallback.absoluteURL(),
        List("email" -> "http://schema.openid.net/contact/email"))
        .map(url => Redirect(url))
        .recover {
          case t => Redirect(routes.Auth.login)
        }
    )
  }

  def logout = Action { implicit request =>
    Redirect(routes.Application.index).withNewSession
  }

  def openIDCallback = Action { implicit request =>
    AsyncResult(
      OpenID.verifiedId.map(
        info => Redirect(routes.Map.map).withSession("sessionId" -> info.attributes("email"))
      ).recover {
        case t => Redirect(routes.Application.index)
      }
    )
  }
}
