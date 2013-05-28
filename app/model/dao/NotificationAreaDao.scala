package model.dao

import language._

import play.api._
import db.DB
import Play.current
import anorm._
import java.util.{Date, UUID}
import SqlParser._
import Column._
import model.{Point, Rectangle, NotificationArea}

object NotificationAreaDao {
  private val table = "NotificationArea"

  private val parser = (
      str("id")~
      str("name")~
      str("interestedUser")~
      str("trackedObject")~
      get[Option[Date]]("lastAppearance")~
      get[java.math.BigDecimal]("leftTopLongitude")~
      get[java.math.BigDecimal]("leftTopLatitude")~
      get[java.math.BigDecimal]("rightBottomLongitude")~
      get[java.math.BigDecimal]("rightBottomLatitude")
    ) map {
    case id~name~interestedUser~trackedObject~lastAppearance~ltLong~ltLat~rbLong~rbLat =>
      NotificationArea(Some(id), name, interestedUser, trackedObject, lastAppearance,
        Rectangle(
          Point(ltLong, ltLat), Point(ltLong, ltLat)
        )
      )
  }

  def byTrackedUser(userId: String) = DB.withConnection( implicit conn =>
    SQL(s"select * from $table na where na.trackedObject = {userId}").onParams(userId).as(parser *)
  )

  def updateLastAppearance(id: String, date: Date) = DB.withConnection(implicit conn =>
    SQL(s"update $table set lastAppearance = {date} where id = {id}").onParams(date, id).executeUpdate()
  )

  def create(area: NotificationArea) = DB.withConnection(implicit conn =>
    area.area match {
      case Rectangle(Point(ltLong, ltLat), Point(rbLong, rbLat)) =>
        val id = area.id getOrElse UUID.randomUUID.toString
        SQL(s"insert into $table values({id}, {name}, {iu}, {to}, {la}, {ltLng}, {ltLat}, {rbLng}, {rbLat})")
          .onParams(
            id,
            area.name,
            area.interestedUser,
            area.trackedObject,
            area.lastAppearance,
            ltLong.bigDecimal, ltLat.bigDecimal,
            rbLong.bigDecimal, rbLat.bigDecimal
        )
        .execute()
        Some(id)
      case area =>
        Logger.error("Cant save area type " + area.getClass.getSimpleName)
        None
    }
  )
}
