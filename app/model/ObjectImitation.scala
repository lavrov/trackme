package model

import scala.concurrent.duration._

import play.api._
import concurrent.ExecutionContext.Implicits._
import dao.{PositionDao, Position}
import java.util.Date
import akka.actor.{ActorRef, Props, Actor}

object ObjectImitation {

  def start(tracker: ActorRef) =
    libs.concurrent.Akka.system(Play.current).actorOf {
      Props(new ObjectActor(tracker))
    }


  class ObjectActor(tracker: ActorRef) extends Actor {
    val userId = "icartracker.lvv@gmail.com"
    var current = Position(35, 55, new Date, userId)

    context.system.scheduler.schedule(5.second, 5.second, self, UpdatePosition)

    def receive = {
      case UpdatePosition =>
        current = current.copy(current.longitude + 0.0005, current.latitude + 0.0005, new Date)
        tracker ! ObjectTracker.Push(current)
    }

    override def postStop() {
      super.postStop()
      PositionDao.forUser(userId).deleteAll
      Logger.info(s"Positions of $userId deleted")
    }
  }

  case object UpdatePosition
}
