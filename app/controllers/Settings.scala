package controllers

import model.{Permissions, State}

object Settings extends SecuredController {
  def settings = AuthAction { implicit req => user =>
    val userPermissions = Permissions.forUser(user.id)
    Ok(views.html.settings(userPermissions.permitTrackingTo, userPermissions.mayTrack))
  }

  def addPermission(subject: String) = AuthAction { implicit req => user =>
    Permissions.forUser(user.id).grantPermissionTo(subject)
      Ok(subject)
  }
}
