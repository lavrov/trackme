package model

import play.api._
import db.DB
import Play.current
import anorm._
import SqlParser._

object TrackingPermissionDao {
  def create(p: TrackingPermission) = DB.withConnection( implicit conn =>
    SQL("insert into TrackingPermission values({subject}, {object})").on('subject -> p.subject, 'object -> p.obj).execute()
  )

  def delete(p: TrackingPermission) = DB.withConnection( implicit conn =>
    SQL("delete from TrackingPermission where subject = {subject} and object = {object}").on('subject -> p.subject, 'object -> p.obj).execute()
  )

  def bySubject(subject: String) = DB.withConnection( implicit conn =>
    SQL("select t.object from TrackingPermission t where t.subject = {subject}").on('subject -> subject).as(scalar[String]*)
  )

  def byObject(obj: String) = DB.withConnection( implicit conn =>
    SQL("select t.subject from TrackingPermission t where t.object = {object}").on('object -> obj).as(scalar[String]*)
  )
}

object Permissions {
  import TrackingPermissionDao._
  def forUser(userId: String) = new {
    def mayTrack = bySubject(userId)
    def permitTrackingTo = byObject(userId)
    def grantPermissionTo(subject: String) = create(TrackingPermission(subject, userId))
    def withdrawPermissionFrom(subject: String) = delete(TrackingPermission(subject, userId))
  }
}

case class TrackingPermission(subject: String, obj: String)
