package model

import dao._

object Permissions {
  import TrackingPermissionDao._
  def forUser(userId: String) = new {
    def mayTrack = bySubject(userId)
    def permitTrackingTo = byObject(userId)
    def grantPermissionTo(subject: String) = create(TrackingPermission(subject, userId))
    def withdrawPermissionFrom(subject: String) = delete(TrackingPermission(subject, userId))
  }
}
