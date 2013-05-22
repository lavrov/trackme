package controllers

import play.api.mvc.{RequestHeader, Request}
import model.security.Security._

object Imports {
  implicit def reqToCtx[A](req: Request[A]) =  Context(req, loggedInUser(req))

  def loggedInUser(implicit req: RequestHeader) = req.session.get("sessionId").flatMap(restoreUser)
}
