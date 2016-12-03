name := "tcpOSGI"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq("biz.aQute.bnd" % "bndlib" % "2.4.0"
  ,"org.scalatest" %% "scalatest" % "2.2.4" % "test"
  ,"org.scala-lang" % "scala-reflect" % scalaVersion.value
  ,"org.scala-lang" % "scala-library" % scalaVersion.value
  ,"org.scala-lang.modules" % "scala-parser-combinators_2.11" % "1.0.4"
  ,"com.typesafe.akka" % "akka-actor_2.11" % "2.4.9"
  ,"com.typesafe" % "config" % "1.3.0"
  ,"com.typesafe.akka" % "akka-osgi_2.11" % "2.4.10"
  ,"org.osgi" % "org.osgi.core" % "4.2.0" % "provided"
  ,"org.osgi" % "org.osgi.compendium" % "4.2.0" % "provided"
  )

unmanagedBase := baseDirectory.value / "lib"

osgiSettings

OsgiKeys.exportPackage := Seq("com.osgi","com.helpers")

OsgiKeys.bundleActivator := Option("com.osgi.Activator")

OsgiKeys.importPackage := Seq("*")

