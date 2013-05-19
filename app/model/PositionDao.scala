package model

import play.api.db.DB
import play.api.Play.current
import anorm._
import java.util.{Date, UUID}
import play.api.Logger
import SqlParser._
import Column._

object PositionDao {
  val PositionParser = (get[java.math.BigDecimal]("longitude")~get[java.math.BigDecimal]("latitude")~date("timestamp")*)

  def all = DB.withConnection( implicit conn =>
    SQL("select * from Position")
      .as(PositionParser)
      .map { case lng~lat~time => Position(lng, lat, time)}
  )

  def lastPoint = DB.withConnection( implicit conn =>
    SQL("select top 1 * from Position p where p.timestamp > {time} order by p.timestamp desc")
      .on(
        'time -> new Date(System.currentTimeMillis() - 600000L)
      )
      .as(PositionParser)
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