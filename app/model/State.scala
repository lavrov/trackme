package model

import dao.TrackingPermissionDao
import play.api._
import db.DB
import Play.current
import anorm._
import SqlParser._

import security.User

object State {
  object TrackingObject {
    def get(user: User) = DB.withConnection { implicit conn =>
      SQL("select p.object from TrackingState s inner join TrackingPermission p on s.permissionId = p.id where s.email = {user}")
        .onParams(user.id).as(scalar[String] singleOpt)
    }

    def set(forUser: User, obj: String) = DB.withConnection( implicit conn =>
      TrackingPermissionDao.readId(forUser.id, obj).map { permissionId =>
        SQL("delete from TrackingState where email = {user}").onParams(forUser.id).execute()
        SQL("insert into TrackingState values({user}, {permission})").onParams(forUser.id, permissionId).execute()
      }.isDefined
    )
  }
}
