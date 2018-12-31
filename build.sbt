  name := "AkkaPlayground"

version := "1.0"

scalaVersion := "2.12.8"

val akkaVersion = "2.5.19"
val akkaHttpVersion = "10.1.6"

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % akkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-agent" % akkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-camel" % akkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-cluster" % akkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-cluster-metrics" % akkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-cluster-sharding" % akkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-cluster-tools" % akkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-contrib" % akkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-http" % akkaHttpVersion
libraryDependencies += "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion
libraryDependencies += "com.typesafe.akka" %% "akka-multi-node-testkit" % akkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-osgi" % akkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-persistence" % akkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-persistence-tck" % akkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-remote" % akkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-slf4j" % akkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-stream" % akkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-testkit" % akkaVersion

libraryDependencies += "io.spray" %%  "spray-json" % "1.3.2"
libraryDependencies += "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion
libraryDependencies += "com.hunorkovacs" %% "koauth" % "2.0.0"
libraryDependencies += "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.9.8"
libraryDependencies += "de.heikoseeberger" %% "akka-http-play-json" % "1.23.0"
