package fr.xebia.ldi.game.actor

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import fr.xebia.ldi.game.actor.CharacterActor.GetReady
import fr.xebia.ldi.game.actor.RoomActor.{Start, Stop}
import fr.xebia.ldi.game.model.Hero
import org.scalacheck.Gen.Choose
import org.slf4j.{Logger, LoggerFactory}

/**
  * Created by loicmdivad.
  */
case class RoomActor(characters: Vector[Hero])(implicit system: ActorSystem, producer: ActorRef) extends Actor {

  var roomReference: Option[ActorRef] = None

  val characterRefs: Vector[ActorRef] = defineRefs

  val roomId: Option[Long] = Choose.chooseLong.choose(0, 3000).sample

  val logger: Logger = LoggerFactory.getLogger(getClass)

  override def receive: Receive = {
    case Stop => characterRefs.foreach(_ ! Stop)
    case Start => characterRefs.foreach(_ ! roomId.map(id => GetReady(s"Room#$id", characterRefs)).orNull)
    case msg => logger debug s"Receive an incompatible message: $msg"
  }

  def defineRefs(implicit system: ActorSystem): Vector[ActorRef] =
    characters.map(character => system.actorOf(Props.apply(classOf[CharacterActor], character, producer)))
}

object RoomActor {

  sealed trait Signal
  case object Start extends Signal
  case object Stop extends Signal
  case object Pause extends Signal

  def reference(characters: Vector[Hero])(implicit system: ActorSystem, producer: ActorRef): ActorRef =
      system.actorOf(Props.apply(classOf[RoomActor], characters, system, producer))

}
