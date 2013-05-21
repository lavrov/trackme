package controllers

import play.api.mvc._
import be.objectify.deadbolt.scala.{DynamicResourceHandler, DeadboltHandler}
import be.objectify.deadbolt.core.models.{Permission, Role, Subject}
import java.util

trait SecuredController {
  self: Controller =>

  def SubjectAction[A](bodyParser: BodyParser[A])(block: BodyContext => Result)
                      (implicit deadboltHandler: DefaultDeadboltHandler.type = DefaultDeadboltHandler) =
    Action(bodyParser) { implicit request =>
      deadboltHandler.beforeAuthCheck(request) match {
        case Some(result) => result
        case _ => {
          deadboltHandler.getSubject(request) match {
            case Some(handler) => block(new BodyContext(request, Some(handler)))
            case None => deadboltHandler.onAuthFailure(request)
          }
        }
      }
    }

  def SubjectAction(block: BodyContext => Result): Action[AnyContent] =
    SubjectAction(BodyParsers.parse.anyContent)(block)
}

object DeadboltActions extends be.objectify.deadbolt.scala.DeadboltActions

object DefaultDeadboltHandler extends DeadboltHandler {
  def beforeAuthCheck[A](request: Request[A]): Option[Result] = None

  def getSubject[A](request: Request[A]): Option[User] = request.session.get("sessionId").map(User.apply)

  def onAuthFailure[A](request: Request[A]): Result = Default.Redirect(controllers.routes.Auth.login).flashing("error" -> "Auth needed")

  def getDynamicResourceHandler[A](request: Request[A]): Option[DynamicResourceHandler] = None
}

case class User(id: String) extends Subject {
  def getRoles: util.List[Role] = ???

  def getPermissions: util.List[Permission] = ???

  def getIdentifier: String = id
}

