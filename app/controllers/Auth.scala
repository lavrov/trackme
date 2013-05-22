package controllers

import play.api.mvc._
import play.api.libs.openid.OpenID
import model.security.Security._
import play.api.libs.concurrent.Execution.Implicits._
import play.api.Logger

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
    request.session.get("sessionId").foreach(deleteSession)
    Redirect(routes.Application.index).withNewSession
  }

  def openIDCallback = Action { implicit request =>
    AsyncResult(
      OpenID.verifiedId map { info =>
        val email = info.attributes("email")
        val sessionId = createSession(email)
        Logger.info(s"New session: $sessionId")
        Redirect(routes.Map.map).withSession("sessionId" -> sessionId)
      } recover {
        case t =>
          Logger.info("Auth rejected")
          Redirect(routes.Application.index)
      }
    )
  }
}
