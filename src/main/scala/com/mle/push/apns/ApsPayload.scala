package com.mle.push.apns

import play.api.libs.json.Json._
import play.api.libs.json.{JsObject, JsValue, Json, Writes}

/**
 * @author Michael
 */
case class APSPayload(alert: Either[String, AlertPayload],
                      badge: Option[Int] = None,
                      sound: Option[String] = None,
                      silent: Boolean = false) {
  def json: JsObject = {
    val alertJson = alert.fold(s => toJson(s), ap => toJson(ap))
    val silentKeyValue = if (silent) Json.obj("content-available" -> 1) else Json.obj()
    Json.obj("alert" -> alertJson, "badge" -> toJson(badge), "sound" -> toJson(sound)) ++ silentKeyValue
  }
}

object APSPayload {
  implicit val json = new Writes[APSPayload] {
    override def writes(o: APSPayload): JsValue = o.json
  }
}