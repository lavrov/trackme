package model

import play.api.db.DB
import play.api.Play.current
import anorm._

object PositionDao {
  def savePosition(position: Position) = DB.withConnection( implicit conn =>
    SQL("insert into Position (longitude, latitude) values ({0}, {1})")
      .on("0" -> position.longitude, "1" -> position.latitude)
      .execute()
  )
}

case class Position(longitude: Double, latitude: Double)