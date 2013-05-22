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
    SQL("insert input Session values ({id}, {email})")
    .on (
      'id -> UUID.randomUUID.toString,
      'email -> email
    ).executeInsert(sessionParser single).id
  }

  def deleteSession(sessionId: String) = DB.withConnection { implicit conn =>
    SQL("delete from Session where id = {id}")
    .on('id -> sessionId).execute()
  }

  def restoreUser(sessionId: String) = DB.withConnection { implicit conn =>
    SQL("select * from Session s where id = {id}").on('id -> sessionId).as(sessionParser singleOpt).map(_.email).map(User.apply)
  }
}
