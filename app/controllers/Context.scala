package controllers

import play.api.mvc.{Request, RequestHeader}

sealed abstract class Context(val req: RequestHeader, val me: Option[User]) {
  implicit def request = req
}

final class BodyContext(val body: Request[_], m: Option[User])
  extends Context(body, m){
  override implicit def request: Request[_] = body
}

final class HeaderContext(r: RequestHeader, m: Option[User])
  extends Context(r, m)

object Context {

  implicit def toContext(req: RequestHeader) = Context(req, None)

  def apply(req: RequestHeader, me: Option[User]): HeaderContext =
    new HeaderContext(req, me)

  def apply(req: Request[_], me: Option[User]): BodyContext =
    new BodyContext(req, me)
}
