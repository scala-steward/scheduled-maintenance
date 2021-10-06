package com.dwolla.scheduledmaintenance

import dev.holt.javatime.literals._
import io.circe.literal._
import io.circe.parser.parse
import org.scalajs.dom.experimental._

import java.time.Instant
import java.time.format.DateTimeFormatter
import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js

class ScheduledMaintenanceResponseTest extends munit.FunSuite with FetchPolyfill {

  private val defaultRequest =
    new Request("https://hydragents.xyz/test", new RequestInit() {
      method = HttpMethod.GET
    })

  test("the response should be a 503 status code") {
    val output = Main.handleRequest(defaultRequest)

    assert(output.status == 503)
    assert(output.statusText == "Service Unavailable (scheduled maintenance)")
  }

  test("the response should contain an appropriate JSON body") {
    val output = Main.handleRequest(defaultRequest)

    output.text().toFuture.map { body =>
      assert(parse(body) == Right(
        json"""{
               "code": "ScheduledMaintenance",
               "message": "Services are temporarily unavailable while we perform scheduled maintenance"
             }"""))
    }
  }

  test("the response should contain an appropriate Retry-After header") {
    val expected = offsetDateTime"""2021-05-16T00:00:00-05:00""".toInstant

    val output = Main.handleRequest(defaultRequest)

    val actual: Instant =
      Option(output.headers.get("Retry-After"))
        .map(DateTimeFormatter.RFC_1123_DATE_TIME.parse)
        .map(Instant.from)
        .orNull

    assert(expected == actual)
  }

  test("if the request asks for HTML, give it HTML") {
    val req = new Request("https://hydragents.xyz/test", new RequestInit() {
      method = HttpMethod.GET
      headers = new Headers(js.Dictionary(
        "Accept" -> "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"
      ))
    })

    val output = Main.handleRequest(req)

    assert(output.status == 503)
    assert(output.headers.get("Content-type") == "text/html")
  }
}
