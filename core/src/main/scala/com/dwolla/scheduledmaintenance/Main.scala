package com.dwolla.scheduledmaintenance

import dev.holt.javatime.literals.offsetDateTime
import io.circe.literal._
import org.scalajs.dom.experimental._
import org.scalajs.dom.experimental.serviceworkers.FetchEvent
import stubs.Globals

import java.time.format.DateTimeFormatter
import java.time.{OffsetDateTime, ZoneOffset}
import scala.scalajs.js

object Main {
  def main(args: Array[String]): Unit =
    Globals.addEventListener("fetch",
      (event: FetchEvent) => event.respondWith(handleRequest(event.request))
    )

  //noinspection SameParameterValue
  private def formatForHttpHeader(odt: OffsetDateTime): String =
    odt.toInstant.atOffset(ZoneOffset.UTC).format(DateTimeFormatter.RFC_1123_DATE_TIME)

  private[scheduledmaintenance] def handleRequest(req: Request): Response = {
    val accept: js.UndefOr[ByteString] = req.headers.get("Accept").flatMap {
      case null => js.undefined
      case other => other
    }

    if(accept.exists(_.contains("text/html")))
      buildResponse("text/html",
        """<html>
          |  <head><title>Dwolla :: Scheduled Maintenance</title></head>
          |  <body>
          |    <h1>Dwolla</h1>
          |    <p>
          |      Services are temporarily unavailable while we perform scheduled
          |      maintenance. See <a href="https://status.dwolla.com">status.dwolla.com</a>
          |      for more information.
          |    </p>
          |  </body>
          |</html>
          |""".stripMargin)
    else
      buildResponse("application/json",
        json"""{
                 "code": "ScheduledMaintenance",
                 "message": "Services are temporarily unavailable while we perform scheduled maintenance"
               }""".noSpaces)
  }

  private def buildResponse(contentType: String, body: String): Response =
    new Response(
      body,
      ResponseInit(
        _status = 503,
        _statusText = "Service Unavailable (scheduled maintenance)",
        _headers = js.Dictionary(
          "content-type" -> contentType,
          "Retry-After" -> formatForHttpHeader(offsetDateTime"""2021-05-10T23:00:00-05:00"""),
        )))
}
