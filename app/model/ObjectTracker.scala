package model

import dao.{Position, PositionDao}
import play.api.libs._
import iteratee.{Iteratee, Concurrent}
import akka.actor.{Props, Actor}
import concurrent.Execution.Implicits._

object ObjectTracker {

  val (enumerator, broadcaster) = Concurrent.broadcast[Position]

  enumerator(
    Iteratee.foreach[Position]{ p =>
      Notifications.checkAndNotify(p)
      PositionDao.savePosition(p)
    }
  )

  //import play.api.Play.current
  //ObjectImitation.start(concurrent.Akka.system.actorOf(Props[Tracker]))

  class Tracker extends Actor {
    def receive = {
      case Push(position) => broadcaster.push(position)
    }
  }

  case class Push(position: Position)

}