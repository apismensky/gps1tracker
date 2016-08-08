package models

import play.api.libs.json.Json

case class Coordinates(i: Int,
                       e: Int,
                       n: Int,
                       b: Int)

object Coordinates {
  implicit val formatter = Json.format[Coordinates]
}
