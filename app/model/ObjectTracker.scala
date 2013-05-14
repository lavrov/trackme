package model

import akka.actor.Actor
import play.api.libs.iteratee.{Iteratee, Enumeratee, Concurrent, Enumerator}

object ObjectTracker {
  val (enumerator, broadcaster) = Concurrent.broadcast[Position]

  enumerator(
    Iteratee.foreach[Position](PositionDao.savePosition(_))
  )
}