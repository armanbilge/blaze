import sbt._
import Keys._
import scala.util.Properties

//import spray.revolver.RevolverPlugin._

object ApplicationBuild extends Build {

  /* Projects */
  lazy val main = Project("blaze",
                    new File("."),
                    settings = buildSettings
                  )

  /* global build settings */
  //lazy val buildSettings = Defaults.defaultSettings ++  publishing ++ Revolver.settings ++ Seq(
  lazy val buildSettings = Defaults.defaultSettings ++ 
                           dependencies ++
                           publishing ++ Seq(
    organization := "org.http4s",

    version := "0.2.0-SNAPSHOT",

    scalaVersion := "2.10.4",

    crossScalaVersions := Seq("2.10.4", "2.11.0"),

    description := "NIO Framework for Scala",

    homepage := Some(url("https://github.com/http4s/blaze")),

    startYear := Some(2014),

    licenses := Seq(("Apache 2.0", url("http://www.apache.org/licenses/LICENSE-2.0"))),

    scmInfo := Some(
      ScmInfo(
        url("https://github.com/http4s/blaze"),
        "scm:git:https://github.com/http4s/blaze.git",
        Some("scm:git:git@github.com:http4s/blaze.git")
      )
    ),

    //mainClass in Revolver.reStart := Some("org.http4s.blaze.examples.NIO1HttpServer"),
//    javaOptions in run += "-Djavax.net.debug=all",    // SSL Debugging
//    javaOptions in run += "-Dcom.sun.net.ssl.enableECC=false",
//    javaOptions in run += "-Djsse.enableSNIExtension=false",
    fork in run := true

      // Adds NPN to the boot classpath for Spdy support
//    javaOptions in run <++= (managedClasspath in Runtime) map { attList =>
//      for {
//        file <- attList.map(_.data)
//        path = file.getAbsolutePath if path.contains("jetty.npn")
//      } yield { println(path); "-Xbootclasspath/p:" + path}
//    }

  )

  /* dependencies */
  lazy val dependencies = Seq(
    libraryDependencies += scalatest % "test",
    libraryDependencies += (scalaBinaryVersion.value match {
      case "2.10" => scalaloggingSlf4j_2_10
      case "2.11" => scalaloggingSlf4j_2_11
    }),
    libraryDependencies ++= (scalaBinaryVersion.value match {
      case "2.10" => Seq.empty
      case "2.11" => Seq(scalaXml)
    }),
    libraryDependencies += logbackClassic,
    libraryDependencies += npn_api,
    libraryDependencies += npn_boot
  )
   
  lazy val scalatest = "org.scalatest" %% "scalatest" % "2.1.3"


  lazy val scalaloggingSlf4j_2_10 = "com.typesafe"          %% "scalalogging-slf4j"      % "1.1.0"
  lazy val scalaloggingSlf4j_2_11 = "com.typesafe.scala-logging" %% "scala-logging-slf4j" % "2.1.0"
  lazy val logbackClassic      = "ch.qos.logback" %  "logback-classic"    % "1.0.9"

  lazy val scalaXml = "org.scala-lang.modules" %% "scala-xml" % "1.0.1"

  // Needed for Spdy Support. Perhaps it should be a sub-project?
  // Interesting note: Http2.0 will use the TSLALPN extension which, unfortunately,
  // is also not implemented in java SSL yet.
  lazy val npn_api             = "org.eclipse.jetty.npn"     % "npn-api"  % npn_version
  lazy val npn_boot            = "org.mortbay.jetty.npn"     % "npn-boot" % npn_version

  lazy val npn_version = "8.1.2.v20120308"

  /* publishing */
  lazy val publishing = Seq(
    publishMavenStyle := true,
    publishTo <<= version { (v: String) =>
      val nexus = "https://oss.sonatype.org/"
      if (v.trim.endsWith("SNAPSHOT")) Some(
        "snapshots" at nexus + "content/repositories/snapshots"
      )
      else Some("releases" at nexus + "service/local/staging/deploy/maven2")
    },
    publishArtifact in Test := false,
    pomIncludeRepository := { _ => false },

    Seq("SONATYPE_USER", "SONATYPE_PASS") map Properties.envOrNone match {
      case Seq(Some(user), Some(pass)) =>
        credentials += Credentials("Sonatype Nexus Repository Manager", "oss.sonatype.org", user, pass)
      case _ => credentials in ThisBuild ~= identity
    },

    pomExtra := (
      <developers>
        <developer>
          <id>bryce-anderson</id>
          <name>Bryce L. Anderson</name>
          <email>bryce.anderson22@gamil.com</email>
          <!-- <url></url> -->
        </developer>
        <developer>
          <id>rossabaker</id>
          <name>Ross A. Baker</name>
          <email>baker@alumni.indiana.edu</email>
          <!-- <url></url> -->
        </developer>
      </developers>
    )
  )
            
}