package models

import play.api.libs.json.Json

case class User(firstName: String,
                lastName: String,
                email: String,
                password: String)

object User {
  implicit val formatter = Json.format[User]
}