package controllers

import play.api.mvc.Request
import model.security.User

case class Context[+A](req: Request[A], user: Option[User])