lazy val scalavista = (project in file("."))
    .enablePlugins(JavaAppPackaging)
    .settings(
      name := "scalavista-server",
      version := "0.1.4",
      maintainer := "christophbunte@gmail.com",
      scalaVersion := "2.12.8",
      crossScalaVersions := Seq("2.11.12", "2.12.8"),
      fork := true,
      scalacOptions ++= Seq(
        "-deprecation",
        "-feature", 
        "-unchecked",
        "-Ywarn-dead-code",
        "-Ywarn-value-discard",
        //"-Ywarn-unused:imports",
        //"-Ywarn-unused:patvars",
        //"-Ywarn-unused:privates",
        //"-Ywarn-unused:locals",
        //"-Ywarn-unused:params",
        "-Xlint:_"
      ),
      libraryDependencies ++= Seq(
        "org.scala-lang" % "scala-compiler" % scalaVersion.value,
        "org.scala-lang" % "scala-library" % scalaVersion.value,
        "org.scala-lang" % "scala-reflect" % scalaVersion.value,
        "com.typesafe.akka" %% "akka-http" % "10.1.5",
        //"com.typesafe.akka" %% "akka-http-testkit" % "10.1.5" % Test,
        "com.typesafe.akka" %% "akka-actor" % "2.5.18",
        //"com.typesafe.akka" %% "akka-testkit" % "2.5.18" % Test,
        "com.typesafe.akka" %% "akka-stream" % "2.5.18",
        //"com.typesafe.akka" %% "akka-stream-testkit" % "2.5.18" % Test,
        "io.spray" %% "spray-json" % "1.3.5",
        "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.5",
        "com.github.pathikrit" %% "better-files" % "3.7.0",
        "org.rogach" %% "scallop" % "3.1.5"
      ),
      //mainClass in assembly := Some("org.scalavista.Launcher"),
      //assemblyJarName in assembly := s"scalavista-server-${version.value}_${scalaBinaryVersion.value}.jar"
    )
