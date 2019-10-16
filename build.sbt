// The simplest possible sbt build file is just one line:

scalaVersion := "2.13.1"
name := "wdentities"
organization := "es.weso"
version := "1.0"

lazy val catsVersion            = "2.0.0"
lazy val circeVersion           = "0.12.0-RC3"
lazy val http4sVersion          = "0.21.0-M4"
lazy val http4sJDKClientVersion = "0.2.0-M3"
lazy val jsoupVersion           = "1.12.1"

lazy val catsCore          = "org.typelevel"              %% "cats-core"              % catsVersion
lazy val catsKernel        = "org.typelevel"              %% "cats-kernel"            % catsVersion
lazy val catsMacros        = "org.typelevel"              %% "cats-macros"            % catsVersion
lazy val catsEffect        = "org.typelevel"              %% "cats-effect"            % catsVersion

lazy val circeCore         = "io.circe"                   %% "circe-core"             % circeVersion
lazy val circeGeneric      = "io.circe"                   %% "circe-generic"          % circeVersion
lazy val circeParser       = "io.circe"                   %% "circe-parser"           % circeVersion

lazy val http4sDsl         = "org.http4s"                 %% "http4s-dsl"             % http4sVersion
lazy val http4sBlazeServer = "org.http4s"                 %% "http4s-blaze-server"    % http4sVersion
lazy val http4sBlazeClient = "org.http4s"                 %% "http4s-blaze-client"    % http4sVersion
lazy val http4sCirce       = "org.http4s"                 %% "http4s-circe"           % http4sVersion
lazy val http4sTwirl       = "org.http4s"                 %% "http4s-twirl"           % http4sVersion
lazy val http4sJdkclient   = "org.http4s"                 %% "http4s-jdk-http-client" % http4sJDKClientVersion
lazy val jsoup             = "org.jsoup"                  % "jsoup"                  % jsoupVersion

libraryDependencies ++= Seq(
  catsCore, 
  circeCore,
  circeGeneric,
  circeParser,
  http4sDsl,
  http4sBlazeServer,
  http4sBlazeClient,
  http4sCirce,
  http4sTwirl,
  http4sJdkclient,
  jsoup
)

