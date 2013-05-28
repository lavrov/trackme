package controllers.settings

import controllers.SecuredController
import play.api.data._
import Forms._
import model.dao.NotificationAreaDao
import model.{Point, Rectangle, NotificationArea}
import scala.concurrent._
import play.api.libs.concurrent.Execution.Implicits._

package object notifications extends SecuredController {
  val form = Form(
    tuple(
      "name" -> nonEmptyText,
      "trackingObject" -> nonEmptyText,
      "leftTopLongitude" -> bigDecimal(9, 6),
      "leftTopLatitude" -> bigDecimal(9, 6),
      "rightBottomLatitude" -> bigDecimal(9, 6),
      "rightBottomLatitude" -> bigDecimal(9, 6)
    )
  )

  def add = AuthAction { implicit req => user =>
    form.bindFromRequest.fold(
      _ => BadRequest("Wrong parameters"),
      {
        case (name, tracingObject, ltLng, ltLat, rbLng, rbLat) => Async { future {
          NotificationAreaDao.create {
            NotificationArea(None, name, user.id, tracingObject, None,
              Rectangle(Point(ltLng,ltLat), Point(rbLng, rbLat))
            )
          }
          Ok("Notification added")
        }}
      }
    )
  }
}
