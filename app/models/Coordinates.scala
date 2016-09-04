package models

import play.api.libs.json.Json

case class Coordinates(i: String,
                       e: String,
                       n: String,
                       b: String)

object Coordinates {
  implicit val formatter = Json.format[Coordinates]
}
