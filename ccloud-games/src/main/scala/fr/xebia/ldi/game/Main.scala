package fr.xebia.ldi.game

import akka.NotUsed
import akka.actor.{ActorRef, ActorSystem}
import akka.kafka.ProducerMessage.Message
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.scaladsl.{Sink, Source}
import akka.stream.{ActorMaterializer, DelayOverflowStrategy, OverflowStrategy}
import com.typesafe.config.{Config, ConfigFactory}
import fr.xebia.ldi.common.schema.{Hand, Hero}
import fr.xebia.ldi.game.actor.RoomActor
import fr.xebia.ldi.game.actor.RoomActor.Start
import fr.xebia.ldi.game.model.Hand.{Player, PlayerOne, PlayerTwo}
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import org.slf4j.LoggerFactory

import scala.annotation.tailrec
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}
import scala.collection.JavaConverters._
import fr.xebia.ldi.game.model.Hero.Heroes

/**
  * Created by loicmdivad.
  */
object Main extends App {

  implicit val system: ActorSystem = ActorSystem()

  implicit val materializer: ActorMaterializer = ActorMaterializer()

  implicit val dispatcher: ExecutionContextExecutor = system.dispatcher

  lazy val logger = LoggerFactory.getLogger(getClass)

  val conf = ConfigFactory.load()

  import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde

  val heroesSerde: SpecificAvroSerde[Hero] = new SpecificAvroSerde[Hero]
  heroesSerde.configure(schemaRegistryPros(conf).asJava, false)

  val handsSerde = new SpecificAvroSerde[Hand]
  handsSerde.configure(schemaRegistryPros(conf).asJava, false)


  retryCallSchemaRegistry(conf, 3) match {

    case Failure(ex: Throwable) =>
      logger error "Fail to call the schema registry. shutting down now."
      logger error ex.getMessage
      materializer.shutdown()
      system.terminate()
      throw ex

    case Success(payload) =>
      logger info s"Successfully call the schema registry: $payload"

      val handSettings: ProducerSettings[String, Hand] =
        ProducerSettings(system, new StringSerializer(), handsSerde.serializer())

      val characterSettings: ProducerSettings[String, Hero] =
        ProducerSettings(system, new StringSerializer(), heroesSerde.serializer())

      Source.fromIterator(() => Heroes.toIterator)
        .map[ProducerRecord[String, Hero]] { character =>
        new ProducerRecord("HEROES", character.name, character.toJava)
      }
        .buffer(1, OverflowStrategy.backpressure)
        .runWith(Producer.plainSink(characterSettings))

      val handSrc: Source[Message[String, Hand, NotUsed], ActorRef] =
        Source
          .actorRef(10, OverflowStrategy.fail)
          .delay(1 millis, DelayOverflowStrategy.fail)
          .mapAsync(4) { hand: model.Hand =>
            Future(
              Message[String, Hand, NotUsed](
                new ProducerRecord(branchTopic(hand.player), hand.id, hand.toJava),
                NotUsed
              )
            )
          }

      implicit val actorRef: ActorRef = handSrc
        .via(Producer.flexiFlow(handSettings))
        .to(Sink.ignore)
        .run()

      val room = RoomActor.reference(Heroes)

      logger info "Ready ? GO !"
      room ! Start
  }

  def schemaRegistryKey: String = "schema.registry.url"

  def branchTopic(player: Player): String = {
    player match {
      case PlayerOne => "HIT-PLAYERONE"
      case PlayerTwo => "HIT-PLAYERTWO"
      case _ => "HIT-ERROR-EMISSION"
    }
  }

  def schemaRegistryPros(config: Config): Map[String, String] =
    Map(schemaRegistryKey -> config.getString(s"confluent.$schemaRegistryKey"))

  @tailrec
  def retryCallSchemaRegistry(conf: Config, countdown: Int): Try[String] = {
    val registryUrl = conf.getString(s"confluent.$schemaRegistryKey")
    Try(scala.io.Source.fromURL(registryUrl).mkString) match {
      case result: Success[String] =>
        logger info "Successfully call the Schema Registry."
        result
      case result: Failure[String] if countdown <= 0 =>
        logger error "Fail to call the Schema Registry for the last time."
        result
      case result: Failure[String] if countdown > 0 =>
        logger error "Fail to call the Schema Registry, retry in 15 secs."
        Thread.sleep(15.second.toMillis)
        retryCallSchemaRegistry(conf, countdown -1)
    }
  }
}
