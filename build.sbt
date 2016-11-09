name := """gps1tracker"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  "org.reactivemongo" %% "play2-reactivemongo" % "0.11.14",
  "org.mongodb.scala" %% "mongo-scala-driver" % "1.1.1",
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test,
  "org.mindrot" % "jbcrypt" % "0.3m",
  "com.typesafe.play.modules" %% "play-modules-redis" % "2.5.0"
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"
resolvers += "google-sedis-fix" at "http://pk11-scratch.googlecode.com/svn/trunk"

scalacOptions in ThisBuild ++= Seq("-feature", "-language:postfixOps")
