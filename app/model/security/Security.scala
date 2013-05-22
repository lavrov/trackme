package model.security

import play.api.db.DB
import play.api.Play.current
import anorm._
import SqlParser._
import java.util.UUID

object Security {
  private val sessionParser = (str("id")~str("email")).map {
    case id~email => Session(id, email)
  }

  def createSession(email: String) = DB.withConnection { implicit conn =>
    val id = UUID.randomUUID.toString
    SQL("insert into Session values ({id}, {email})")
    .on (
      'id -> id,
      'email -> email
    ).execute()
    id
  }

  def deleteSession(sessionId: String) = DB.withConnection { implicit conn =>
    SQL("delete from Session where id = {id}")
    .on('id -> sessionId).execute()
  }

  def restoreUser(sessionId: String) = DB.withConnection { implicit conn =>
    SQL("select * from Session s where id = {id}").on('id -> sessionId).as(sessionParser singleOpt).map(_.email).map(User.apply)
  }
}
