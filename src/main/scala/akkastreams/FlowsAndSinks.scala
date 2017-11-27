package akkastreams

import akka.{Done, NotUsed}
import akka.stream.ThrottleMode
import akka.stream.scaladsl.{Flow, Sink, Source}

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.Random

object FlowsAndSinks extends App with Base {

  import system.dispatcher
  val upperCaseFlow: Flow[String, String, _] = Flow.fromFunction(s => s.toUpperCase)


  /**
    * Ex1: Take the file `files/cool-people.txt` and transform all the names into upper case, and save the result
    * in files/cool-people-uppercase.txt
    */

  /**
    * Ex2: run the fast source akkastreams.SourceExamples.s3() against a fast sink that prints out all the numbers one by one (limit the nr of items printed to 40)
    */

  /**
    * Ex3: run the fast source against a slow sink that can consume 1 item per second. Use slowSink defined below
    */

  /**
    * Ex 4: Run calls to a slow service that returns random numbers. See how it behaves increasing the parallelism
    */
  def e4 = Source(List("one", "two", "three", "four")).mapAsync(1)(slowAppendRandomNumber).runWith(Sink.foreach(println))

  /**
    * Ex 5: Zip a slow source and a fast source and send the pairs to a sink that prints the incoming elements (pairs)
    */
  def e5 = {
    val fastSource = Source(1 to 50)
    val slowSource = Source(1 to 10).via(slowFlow)


  }

  def slowFlow[A]: Flow[A, A, NotUsed] = Flow[A].throttle(1, 1 second, 1, ThrottleMode.Shaping)
  def slowSink[A]: Sink[A, NotUsed] = slowFlow.to(Sink.foreach(println))

  def slowAppendRandomNumber(s: String): Future[String] =
    akka.pattern.after((3000 + Random.nextInt(2000)) seconds, system.scheduler)(Future.successful(s + Random.nextInt()))

}
