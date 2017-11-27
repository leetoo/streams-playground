package akkastreams

import akka.NotUsed
import akka.stream.scaladsl.{Flow, Sink, Source}

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._


object SourceExamples extends App with Base {

  /**
    * N.B. Source is typed
    */
  val sourceFromIterator: Source[Int, NotUsed] = Source.fromIterator(() => List(1,2,3,4).iterator)

  val sourceFromList: Source[Int, NotUsed] = Source(List(1,2,3,4))

  sourceFromIterator.via(Flow.fromFunction(identity)).to(Sink.foreach(println)).run()


  /**
    * Ex1: Create source from an Int element = 42
    */
  def s1: Source[Int, _] = ???

  /**
    * Ex2: Create source from Future, Future.successful(42)
    */
  def s2: Source[Int, _] = ???

  /**
    * Ex3: Create source that repeats 42 forever
    */
  def s3: Source[Int, _] = ???

  /**
    * Ex4: Create a source that repeats 42 every second, indefinitely
    */
  def s4: Source[Int, _] = ???

  /**
    * Ex 5: Create a source that emits a string for every row of the file `file/cool-people.txt`
    */
  def s5: Source[String, _] = ???

  def accList[A, M](source: Source[A, M]): Future[Seq[A]] = {
    source.take(40).runWith(Sink.seq)
  }

  def run[A, M](source: Source[A, M]) = Await.result(accList(source), 10 seconds)

}
