package fr.xebia.ldi.game.model

import fr.xebia.ldi.game.model.Hand.Player
import fr.xebia.ldi.common.schema.{Hand => JHand}
import io.github.todokr.Emojipolation._
import org.scalacheck.Arbitrary
import org.scalacheck.Gen.oneOf

/**
  * Created by loicmdivad.
  */
case class Hand(id: String, character: String, hit: String, player: Player) {

  def toJava: JHand = new JHand(id, character, hit)
}

object Hand {

  val Hits: Seq[String] = Vector(
    emoji":leaves:",
    emoji":scissors:",
    emoji":moyai:",
  )

  implicit lazy val HitArbitrary: Arbitrary[String] = Arbitrary(oneOf(Hits))

  sealed trait Player

  case object PlayerOne extends Player
  case object PlayerTwo extends Player

}
