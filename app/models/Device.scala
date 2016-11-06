package models

import play.api.libs.json.Json

case class Device(id: String, nickname: String)

object Device {
  implicit val formatter = Json.format[Device]
}