package akkastreams

import akka.Done
import akka.actor.ActorSystem
import akka.kafka.ProducerMessage.Message
import akka.kafka.{ConsumerSettings, ProducerMessage, ProducerSettings, Subscriptions}
import akka.kafka.scaladsl.{Consumer, Producer}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import akkastreams.CreateNumbers.system
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, ByteArraySerializer, StringDeserializer, StringSerializer}

import scala.concurrent.Future

object CreateNumbers extends App {

  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer = ActorMaterializer()

  val sourceTopic = "akka-kafka-topic"
  val targetTopic = "akka-kafka-target"

  val producerSettings = ProducerSettings(system, new ByteArraySerializer, new StringSerializer)
    .withBootstrapServers("localhost:9092")

  /**
    * Write 100 numbers as Strings to `sourceTopic` topic
    */
  val done: Future[Done] = Source(1 to 100)
    .map(_.toString)
    .map { elem =>
      new ProducerRecord[Array[Byte], String](sourceTopic, elem)
    }
    .runWith(Producer.plainSink(producerSettings))
  done.onComplete(println)(system.dispatcher)



}

object TransformNumbers extends App {

  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer = ActorMaterializer()

  val sourceTopic = "akka-kafka-topic"
  val targetTopic = "akka-kafka-target"
  val producerSettings = ProducerSettings(system, new ByteArraySerializer, new StringSerializer)
    .withBootstrapServers("localhost:9092")

  /**
    * Consume 10 numbers from a.m. topic and deliver them to `targetTopic` with "-processed" appended
    * - Consumer's offset should be committed (at least 1ce delivery)
    * - If I rerun the stream, it should deliver the next 10 numbers
    *
    */
  val consumerSettings = ConsumerSettings(system, new ByteArrayDeserializer, new StringDeserializer)
    .withBootstrapServers("localhost:9092")
    .withGroupId("0")

  Consumer.committableSource(consumerSettings, Subscriptions.topics(sourceTopic))
    .take(10)
    .map { msg =>
      println(s"consuming $msg from topic $sourceTopic")
      ProducerMessage.Message(new ProducerRecord[Array[Byte], String](
        targetTopic,
        msg.record.value + "-processed"
      ), msg.committableOffset
      )
    }.runWith(Producer.commitableSink(producerSettings))

  println("consuming...")

}