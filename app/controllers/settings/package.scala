package controllers

import model.Permissions
import model.dao.NotificationAreaDao

package object settings extends SecuredController {
  def settings = AuthAction { implicit req => user =>
    val userPermissions = Permissions.forUser(user.id)
    Ok(views.html.settings(userPermissions.permitTrackingTo, userPermissions.mayTrack, NotificationAreaDao.byInterestedUser(user.id)))
  }

  def addPermission(subject: String) = AuthAction { implicit req => user =>
    Permissions.forUser(user.id).grantPermissionTo(subject)
    Ok(subject)
  }
}
