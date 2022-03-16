package es.weso

import cats.effect._
import cats.implicits._
import org.http4s._
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.implicits._
import org.http4s.dsl.io._
import org.http4s.client.blaze._
import org.http4s.client._

import scala.concurrent.ExecutionContext.global
import cats.effect.{ContextShift, Timer}
import org.http4s.client.jdkhttpclient.JdkHttpClient

import scala.concurrent.ExecutionContext.Implicits.global
import io.circe._, io.circe.syntax._
import scala.jdk.CollectionConverters._

object EntityResolver {

  val MAX = 500

  def parse(entityNumber: Int, body: String): IO[Option[Json]] = {
    import org.jsoup.Jsoup
    import org.jsoup.helper.Validate
    import org.jsoup.nodes.Document
    import org.jsoup.nodes.Element
    import org.jsoup.select.Elements

    IO {
      val doc: Document = Jsoup.parse(body)
      val element: Element = doc.getElementById("mw-content-text")
      val child = element.child(0)
      if (child.hasClass("noarticletext")) None
      else { // child = <table> <thead> ... </thead> <tbody> <tr> ... </tr> </table>
        val conceptUri = s"https://www.wikidata.org/wiki/Special:EntitySchemaText/E${entityNumber}"
        val webUri = s"https://www.wikidata.org/wiki/EntitySchema:E${entityNumber}"
        val rows: Elements = child.select("tbody tr")
        val initial: Map[String, Json] = Map()
        def cmb(currentMap: Map[String,Json], e: Element): Map[String,Json] = {
          val label = e.getElementsByAttributeValue("class","entityschema-label").text()
          val lang = e.getElementsByAttributeValue("class","entityschema-label").first().attr("lang")
          val descr = e.getElementsByAttributeValue("class","entityschema-description").text()
          val aliases = e.getElementsByAttributeValue("class","entityschema-aliases").text()
          val json = Json.fromFields(List(
            ("lang", Json.fromString(lang)),
            ("label", Json.fromString(label)),
            ("descr", Json.fromString(descr)),
            ("aliases", Json.fromString(aliases))
          ))
          currentMap.updated(lang,json)
        }

        val labels = rows.asScala.toList.foldLeft(initial)(cmb)

        Some(Json.fromFields(
          List(
            ("id", Json.fromString("E" + entityNumber)),
            ("conceptUri", Json.fromString(conceptUri)),
            ("webUri", Json.fromString(webUri)),
            ("title",Json.fromString(doc.title())),
            ("labels", labels.asJson))
          )
        )
      }
    }
  }

  def showJson(maybeJson: Option[Json]): String = maybeJson match {
    case None => "<>"
    case Some(json) => json.hcursor.downField("title").as[String].getOrElse("<No title>")
  }

  def getUri(entityNumber: Int, uri: Uri, client: Client[IO]): IO[Option[Json]] = for {
    // _ <- IO { println(s"Access to $uri") }
    body <- client.get(uri)(_ .bodyAsText.compile.toVector.map(_.mkString))
    maybeJson <- parse(entityNumber, body)
    _ <- IO { println(s"Content for entity number $entityNumber ${showJson(maybeJson)}")}
  } yield maybeJson

  def resolve(i: Int, client: Client[IO]): IO[Option[Json]] = {
    val uri =uri"https://www.wikidata.org/wiki/".withPath(s"/wiki/EntitySchema:E${i}")
    getUri(i, uri,client)
  }

  def run(client: Client[IO]): IO[Json] = {
    val ls = 1 to MAX
    for {
     maybeJsons <- ls.map(resolve(_,client)).toList.sequence
    } yield Json.fromValues(maybeJsons.flatten)
  }
}

object Utils {
  def writeFile(name: String, contents: String): IO[Unit] = {
    import java.nio.file._
    IO {
      val path = Paths.get(name)
      Files.write(path, contents.getBytes)
    }
  }
}

object Main extends IOApp {

  def run(args: List[String]): IO[ExitCode] = {
    val fileName = "entities.json"
    for {
      client <- JdkHttpClient.simple[IO]
      json <- EntityResolver.run(client)
      _ <- Utils.writeFile(fileName, json.spaces2)
      _ <- IO { println(s"JSON generated at file $fileName\n${json.spaces2}")}
    } yield ExitCode.Success
  }


}
