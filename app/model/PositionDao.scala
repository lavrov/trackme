package model

import play.api.db.DB
import play.api.Play.current
import anorm._
import java.util.{Date, UUID}
import play.api.Logger
import SqlParser._
import Column._

object PositionDao {
  val PositionParser = (
      get[java.math.BigDecimal]("longitude")~get[java.math.BigDecimal]("latitude")~date("timestamp")~str("userId")
    )
    .map{ case lng~lat~time~userId => Position(lng, lat, time, userId) }.*

  def all = DB.withConnection( implicit conn =>
    SQL("select * from Position")
      .as(PositionParser)
  )

  def savePosition(position: Position) = DB.withConnection { implicit conn =>
    SQL("insert into Position values ({id}, {lng}, {lat}, {time}, {userId})")
      .on(
      'id -> UUID.randomUUID.toString,
      'lng -> position.longitude.bigDecimal,
      'lat -> position.latitude.bigDecimal,
      'time -> position.timestamp,
      'userId -> position.userId
    )
      .executeInsert()
    Logger.info(s"Inserted: $position")
  }

    def forUser(userId: String) = new {


      def lastPoint = DB.withConnection( implicit conn =>
        SQL("select top 1 * from Position p where p.userId = {userId} and p.timestamp > {time} order by p.timestamp desc")
          .on(
            'userId -> userId,
            'time -> new Date(System.currentTimeMillis() - 600000L)
          )
          .as(PositionParser)
      )

      def betweenInterval(beginning: Date, end: Date) = DB.withConnection( implicit conn =>
        SQL("select * from Position p where p.userId = {userId} and p.timestamp > {beginning} and p.timestamp < {end} order by p.timestamp desc")
          .on(
            'userId -> userId,
            'beginning -> beginning,
            'end -> end
          )
          .as(PositionParser)
      )
  }
}

case class Position(
                     longitude: BigDecimal,
                     latitude: BigDecimal,
                     timestamp: Date,
                     userId: String
                   )