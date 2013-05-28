package model

import language._

import dao.{NotificationAreaDao, Position}
import play.api._
import concurrent._
import java.util.Date
import libs.concurrent.Execution.Implicits._

object Notifications {

  def checkAndNotify(position: Position) = future(check(position)) onSuccess {
    case Some(c) => notifyUsers(c)
  }

  private implicit def toPoint(position: Position) = Point(position.longitude, position.latitude)

  private def notifyUsers(containment: Containment) =
    for {
      area <- containment.areas
        if area.lastAppearance.map(checkIfAnHourElapsed(_, containment.position.timestamp)) getOrElse true
    } AppMailer.send(
      sender = "lavrovvv@gmail.com",
      recipient = area.interestedUser,
      subject = s"${containment.position.userId} is in ${area.name}",
      text = s"lat=${containment.position.latitude}, lon=${containment.position.longitude}"
    )

  private def check(position: Position) = {
    NotificationAreaDao.byTrackedUser(position.userId).filter(_.area.contains(position)) match {
      case Nil => None
      case areaList =>
        areaList.map(_.id)
          .collect {case Some(id) => id}
          .foreach(NotificationAreaDao.updateLastAppearance(_, position.timestamp))
        Some {
          Containment(position, areaList)
        }
    }
  }

  private def checkIfAnHourElapsed(from: Date, to: Date) = to.getTime - from.getTime > 3600000
}

object AppMailer {
  import play.api.Play.current
  import com.typesafe.plugin._

  def send(sender: String, recipient: String, subject: String, text: String) = {
    Logger.info(s"Sending mail to $recipient")
    val mail = use[MailerPlugin].email
    mail.addFrom(sender).addRecipient(recipient).setSubject(subject).send(text)
  }
}

case class Point(longitude: BigDecimal, latitude: BigDecimal)

case class NotificationArea(id: Option[String], name: String, interestedUser: String, trackedObject: String, lastAppearance: Option[Date], area: Area)

case class Containment(position: Position, areas: List[NotificationArea])

trait Area {
  def contains(point: Point): Boolean
}

case class Rectangle(leftTop: Point, rightBottom: Point) extends Area {

  // TODO replace it with an appropriate library
  def contains(point: Point): Boolean =
    leftTop.longitude < point.longitude && leftTop.latitude > point.latitude &&
    rightBottom.longitude > point.longitude && rightBottom.latitude < point.latitude
}