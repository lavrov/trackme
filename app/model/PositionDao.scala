package model

import play.api.db.DB
import play.api.Play.current
import anorm._
import java.util.{Date, UUID}
import play.api.Logger
import SqlParser._
import Column._

object PositionDao {
  def all = DB.withConnection( implicit conn =>
    SQL("select * from Position")
      .as(get[java.math.BigDecimal]("longitude")~get[java.math.BigDecimal]("latitude")~date("timestamp")*)
      .map { case lng~lat~time => Position(lng, lat, time)}
  )

  def savePosition(position: Position) = DB.withConnection{ implicit conn =>
    SQL("insert into Position values ({id}, {lng}, {lat}, {time})")
      .on(
        'id -> UUID.randomUUID.toString,
        'lng -> position.longitude.bigDecimal,
        'lat -> position.latitude.bigDecimal,
        'time -> position.timestamp
      )
      .executeInsert()
    Logger.info(s"Inserted: $position")
  }
}

case class Position(
                     longitude: BigDecimal,
                     latitude: BigDecimal,
                     timestamp: Date
                   )