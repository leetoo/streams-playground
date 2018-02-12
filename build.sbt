name := "streaming-technologies-playground"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq (
  "com.typesafe.akka" %% "akka-stream-kafka" % "0.18",
  "org.typelevel" %% "cats-core" % "1.0.1",
  "org.scalaz" %% "scalaz-core" % "7.2.19"
)
