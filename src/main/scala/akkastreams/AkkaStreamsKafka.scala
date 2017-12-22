package akkastreams

import akka.Done
import akka.actor.ActorSystem
import akka.kafka.ConsumerMessage.CommittableOffsetBatch
import akka.kafka.{ConsumerSettings, ProducerMessage, ProducerSettings, Subscriptions}
import akka.kafka.scaladsl.{Consumer, Producer}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, ByteArraySerializer, StringDeserializer, StringSerializer}

import scala.concurrent.Future

trait Topics {
  val sourceTopic = "akka-streams-playground-source"
  val targetTopic = "akka-streams-playground-target"
}

object CreateNumbers extends App with Topics {

  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer = ActorMaterializer()


  val producerSettings = ProducerSettings(system, new ByteArraySerializer, new StringSerializer)
    .withBootstrapServers("localhost:9092")

  val randomIndex = scala.util.Random.nextInt(1000)
  /**
    * Write 50 numbers as Strings to `sourceTopic` topic
    */
  val done: Future[Done] = Source(1 to 50)
    .map(n => s"Run-$randomIndex-${n.toString}")
    .map { elem =>
      new ProducerRecord[Array[Byte], String](sourceTopic, elem)
    }
    .runWith(Producer.plainSink(producerSettings))
  done.onComplete(println)(system.dispatcher)



}

object TransformNumbers extends App with Topics {

  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer = ActorMaterializer()

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
    .withGroupId("akka-streams-kafka-consumer-group")
    .withProperty("auto.offset.reset", "earliest")

  Consumer.committableSource(consumerSettings, Subscriptions.topics(sourceTopic))
//    .take(10) //N.B.: With Take the offset does not get committed to Kafka
    .map { msg =>
      println(s"consuming $msg from topic $sourceTopic. " +
        s"\nPartition: ${msg.record.partition}, Cons Grp: ${msg.committableOffset.partitionOffset.key.groupId}, offset: ${msg.committableOffset.partitionOffset.offset}")
      ProducerMessage.Message(new ProducerRecord[Array[Byte], String](
        targetTopic,
        msg.record.value + "-processed"
      ), msg.committableOffset
      )
    }
    .via(Producer.flow(producerSettings))
    .map{ producerMessage => {
        println(s"produced $producerMessage")
        producerMessage.message.passThrough
      }
    }
//    .batch(max = 20, first => CommittableOffsetBatch.empty.updated(first)) { (batch, elem) =>
//      println("updating CommittableOffsetBatch")
//      batch.updated(elem)
//    }
    .map( coBatch => {
        println(s"committing offset: ${coBatch.partitionOffset.offset}")
        coBatch.commitScaladsl()
      }
    )
    .runWith(Sink.ignore)
//  .runWith(Producer.commitableSink(producerSettings))

  println("consuming...")

}