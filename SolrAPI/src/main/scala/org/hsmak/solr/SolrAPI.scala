package org.hsmak.solr

import java.io.InputStream
import java.net.URL

import org.springframework.core.io.ClassPathResource
import play.api.libs.json.Json

import scalaj.http.{Http, HttpRequest, MultiPart}


case class SolrEndpoint(protocol: String, hostname: String, port: Int)

class FormatEnum extends Enumeration {
  val json, xml = Value
}

/**
  * Created by hsmak on 3/30/17.
  */
case class SolrAPI(solrEndpoint: SolrEndpoint, collection: String, format: (String) => String) {

  def updateHandlerWithCSV(fileName: String, inputStream: InputStream, literalFields: Seq[(String, String)]): String = {

    val path = s"/solr/$collection/update"
    val url = new URL(solrEndpoint.protocol, solrEndpoint.hostname, solrEndpoint.port, path)

    val arrayBytes = Stream.continually(inputStream.read).takeWhile(_ != -1).map(_.toByte).toArray
    val multiPart = MultiPart(fileName, fileName, "application/csv", arrayBytes)

    val params = Seq(("commit", "true"), ("wt", "json")) ++ literalFields

    val request = Http(url.toString)
    val response = request
      .params(params)
      .postMulti(multiPart)
      .asString

    format(response.body)
  }

  def updateHandler(req: HttpRequest): String = {
    ""
  }

  def queryHandler(): Boolean = {
    true
  }

  def collectionHandler(): Boolean = {
    true
  }
}

object tt extends App {

  w.main(Array())

  object w extends App {
    println("tttttt")
  }
}

object gettingstarted {

  val protocol = "http"
  val hostname = "localhost"
  val port = 8983
  val collection = "gettingstarted"

  val endPoint = new SolrEndpoint(protocol, hostname, port)
  val solrAPI = new SolrAPI(endPoint, collection, formatJSON)

  def formatJSON(str: String): String = {
    Json.prettyPrint(Json.parse(str))
  }

  object uploadCSV extends App {

    val response = solrAPI.updateHandlerWithCSV("test.csv", getInputStream("./test.csv"), Seq(("literal.type", "ManagedAnswer")))
    println(response)

    def getInputStream(filePath: String): InputStream = {
      new ClassPathResource(filePath).getInputStream();
    }
  }

  object uploadManagedAnswersCSV extends App {
    println("test")
  }

  object uploadTopicHubCSV extends App {
    println("test")
  }

  object uploadTakeActionCSV extends App {
    println("test")
  }

}