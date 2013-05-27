package model

import dao.{Position, PositionDao}
import play.api.libs._
import iteratee.{Iteratee, Concurrent}
import akka.actor.{Props, Actor}

object ObjectTracker {
  import play.api.Play.current

  val (enumerator, broadcaster) = Concurrent.broadcast[Position]

  enumerator(
    Iteratee.foreach[Position](PositionDao.savePosition(_))
  )

  ObjectImitation.start(concurrent.Akka.system.actorOf(Props[Tracker]))

  class Tracker extends Actor {
    def receive = {
      case Push(position) => broadcaster.push(position)
    }
  }

  case class Push(position: Position)
}