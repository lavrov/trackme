package model.dao

import play.api._
import db.DB
import Play.current
import anorm._
import SqlParser._

object TrackingPermissionDao {
  def create(p: TrackingPermission) = DB.withConnection( implicit conn =>
    SQL("insert into TrackingPermission (subject, object) values({subject}, {object})").on('subject -> p.subject, 'object -> p.obj).execute()
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

  def readId(subject: String, obj: String) = DB.withConnection( implicit conn =>
    SQL("select p.id from TrackingPermission p where p.subject = {s} and p.object = {o}").onParams(subject, obj).as(scalar[String] singleOpt)
  )
}

case class TrackingPermission(subject: String, obj: String)
