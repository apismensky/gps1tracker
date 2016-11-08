package controllers

import javax.inject._

import models.{Device, User}
import modules.Helpers
import org.mindrot.jbcrypt.BCrypt
import play.api.libs.json._
import play.api.mvc._
import play.modules.reactivemongo._
import reactivemongo.api.ReadPreference
import reactivemongo.bson.BSONObjectID
import reactivemongo.play.json._
import reactivemongo.play.json.collection._

import scala.concurrent.{ExecutionContext, Future}


@Singleton
class AuthAsyncController @Inject()(val reactiveMongoApi: ReactiveMongoApi)(implicit exec: ExecutionContext)
  extends Controller with MongoController with ReactiveMongoComponents {

  final val userProjection: JsObject = Json.obj("_id" -> 1, "firstName" -> 1, "lastName" -> 1, "devices" -> 1, "email" -> 1)

  def adminsCollectionFuture: Future[JSONCollection] = database.map(_.collection[JSONCollection]("admins"))

  def login = Action.async(parse.json) { request =>
    adminsCollectionFuture.flatMap(adminsCollection => {

      val password = (request.body \ "password").as[String]
      val helpers = new Helpers
      val plainPassword = helpers.decryptUserPassword(encryptedPassword = password)

      Future.successful(Ok(Json.obj("password" -> plainPassword)))

    })
  }

}


