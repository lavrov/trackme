package model.dao

import org.specs2.mutable._

import model.{Point, Rectangle, NotificationArea}
import test.TestHelper._


class NotificationAreaDaoSpec extends Specification {
  val trackedUser = "trackedUser@mail.com"

  "NotificationAreaDaoSpec" should {
    "create new area" in {
      fakeApp {
        val idOpt =
          NotificationAreaDao.create(
            NotificationArea(
              None,
              "new area",
              "interesetUser@mail.com",
              trackedUser,
              None,
              Rectangle(Point(10, 10), Point(10, 10))
            )
          )
        NotificationAreaDao.byTrackedUser(trackedUser) must beLike {
          case List(area) => idOpt must beSome.which(_ == area.id.get)
        }
      }
    }
  }
}
