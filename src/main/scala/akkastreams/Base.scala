package akkastreams

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

trait Base {

  implicit val system: ActorSystem = ActorSystem()
  implicit val mat: ActorMaterializer = ActorMaterializer()

}
