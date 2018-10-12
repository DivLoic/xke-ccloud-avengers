package fr.xebia.ldi.game.actor

import java.util.UUID

import akka.actor.{Actor, ActorRef, ActorSystem, Cancellable, Props}
import fr.xebia.ldi.game.actor.CharacterActor.{Accepted, Challenge, Declared, GetReady}
import fr.xebia.ldi.game.model.Hand.{HitArbitrary, PlayerOne, PlayerTwo}
import fr.xebia.ldi.game.model.{Hand, Hero}
import org.scalacheck.Gen.oneOf
import org.scalacheck.{Arbitrary, Gen}
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.duration._

/**
  * Created by loicmdivad.
  */
case class CharacterActor(character: Hero, producer: ActorRef) extends Actor {

  var roomReference: Option[ActorRef] = None

  var challengers: Vector[ActorRef] = Vector.empty[ActorRef]

  var optRoomId: Option[String] = Option.empty

  def logger: Logger = LoggerFactory.getLogger(getClass)

  override def receive: Receive = {
    case GetReady(id, heroes) => (optRoomId, challengers) match {
      case (None, Vector()) =>
        optRoomId = Some(id)
        challengers = heroes
        schedule
      case _ =>
    }

    case Challenge() =>
      challenge
      schedule

    case Accepted(id) => logger debug s"challenger accepted the duel n°$id"

    case Declared(id) => logger debug s"just receive a duel: $id from ${sender.path}"
      Gen.frequency((10, true), (1, false)).sample.foreach { accepted =>
        if(accepted)
          producer ! generateHand(id)
          sender ! Accepted(id)
      }

    case msg => logger info s"Receive an incompatible message. $msg"
  }

  def schedule: Cancellable =
    context.system.scheduler.scheduleOnce(timeOff, self, Challenge())(context.system.dispatcher)

  def timeOff: FiniteDuration = Gen.choose(0.5 seconds, 5 seconds).sample.getOrElse(2 seconds)

  def challenge: Option[Unit] = for {
      challenged <- oneOf(challengers.filter(_.path != self.path)).sample
      uuid <- Arbitrary.arbUuid.arbitrary.sample
      roomId <- optRoomId
    } yield {
    producer ! generateHand(roomId, uuid)
    challenged ! Declared(generateId(roomId, uuid))
  }

  def generateName: String = "  " + character.name.padTo(18, " ").mkString

  def generateId(room: String, id: UUID): String = s"$room&${id.toString.substring(0, 5)}"

  def generateHand(id: String): Hand =
    HitArbitrary.arbitrary.map(hit => Hand(id, generateName, hit, PlayerTwo)).sample.get

  def generateHand(room: String, id: UUID): Hand =
  HitArbitrary.arbitrary.map(hit => Hand(generateId(room, id), generateName, hit, PlayerOne)).sample.get

}

object CharacterActor {

  def reference(characters: Hero)(implicit system: ActorSystem, producerActor: ActorRef): ActorRef =
    system.actorOf(Props.apply(classOf[CharacterActor], characters, producerActor))

  sealed trait Duel
  case class Declared(duelId: String) extends Duel
  case class Accepted(duelId: String) extends Duel

  sealed trait Order
  case class Challenge() extends Order
  case class GetReady(roomId: String, refs: Vector[ActorRef]) extends Order
}