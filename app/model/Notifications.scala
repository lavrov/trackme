package model

import dao.Position
import play.api._
import concurrent._
import java.util.Date

object Notifications {
  implicit def toPoint(position: Position) = Point(position.longitude, position.latitude)

  def checkAndNotify(position: Position)(implicit execctx: ExecutionContext) = future(check(position)) onSuccess {
    case Some(c) => notifyUsers(c)
  }

  def notifyUsers(containment: Containment) =
    for {
      area <- containment.areas
        if area.lastAppearance.map(checkIfAnHourElapsed(_, containment.position.timestamp)) getOrElse true
    } AppMailer.send(
      sender = "lavrovvv@gmail.com",
      recipient = area.interestedUser,
      subject = s"${containment.position.userId} is in ${area.name}",
      text = s"lat=${containment.position.latitude}, lon=${containment.position.longitude}"
    )

  def check(position: Position) = {
    notificationsArea(position.userId).filter(_.area.contains(position)) match {
      case Nil => None
      case areaList =>
        updateLastAppearance(areaList, position)
        Some {Containment(position, areaList)}
    }
  }

  def checkIfAnHourElapsed(from: Date, to: Date) = to.getTime - from.getTime > 3600000

  def updateLastAppearance(areas: List[NotificationArea], position: Position) {}

  def notificationsArea(userId: String): List[NotificationArea] = List(
    NotificationArea(
      "Moscow",
      "lavrovvv@gmail.com",
      "icartracker.lvv@gmail.com",
      Some(new Date),
      Rectangle(Point(37.303187, 55.911117), Point(37.903315, 55.57724))
    )
  )
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

case class NotificationArea(name: String, interestedUser: String, trackedUser: String, lastAppearance: Option[Date], area: Area)

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