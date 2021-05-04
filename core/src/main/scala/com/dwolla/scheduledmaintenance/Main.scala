package com.dwolla.scheduledmaintenance

import org.scalajs.dom.experimental.serviceworkers.FetchEvent
import org.scalajs.dom.experimental._
import io.circe.literal._
import stubs.Globals

import scala.scalajs.js

object Main {
  def main(args: Array[String]): Unit = {
    Globals.addEventListener("fetch", (event: FetchEvent) => {
      event.respondWith(handleRequest())
    })
  }

  private def handleRequest(): Response =
    new Response(
      json"""{
               "code": "ScheduledMaintenance",
               "message": "Services are temporarily unavailable while we perform scheduled maintenance"
             }""".noSpaces,
      ResponseInit(
        _status = 503,
        _statusText = "Service Unavailable (scheduled maintenance)",
        _headers = js.Dictionary(
          "content-type" -> "application/json",
          "Retry-After" -> "Mon, 10 May 2021 04:00:00 GMT"
        )))
}
