package controllers

import javax.inject._

import modules.Helpers
import org.mindrot.jbcrypt.BCrypt
import play.api.libs.json._
import play.api.mvc._
import play.modules.reactivemongo._
import reactivemongo.api.ReadPreference
import reactivemongo.play.json._
import reactivemongo.play.json.collection._

import scala.concurrent.{ExecutionContext, Future}


@Singleton
class AuthAsyncController @Inject()(val reactiveMongoApi: ReactiveMongoApi)(implicit exec: ExecutionContext)
  extends Controller with MongoController with ReactiveMongoComponents {

  final val userProjection: JsObject = Json.obj("_id" -> 1, "firstName" -> 1, "lastName" -> 1, "devices" -> 1, "email" -> 1)

  def adminsCollectionFuture: Future[JSONCollection] = database.map(_.collection[JSONCollection]("admins"))

  def login = Action.async(parse.json) { request =>

    val helpers = new Helpers(reactiveMongoApi = reactiveMongoApi)

    for {
      adminsCollection <- adminsCollectionFuture
      plainPassword <- {
        val password = (request.body \ "password").as[String]

        helpers.decryptUserPassword(encryptedPassword = password)
      }
      result <- {
        val username = (request.body \ "username").as[String]

        adminsCollection.find(Json.obj("username" -> username)).one[JsObject](ReadPreference.primary).map {
          case Some(user: JsObject) => {
            val password = BCrypt.hashpw(plainPassword, (user \ "salt").as[String])

            if (password.equals((user \ "password").as[String])) {
              // Generate token
              val token = helpers.generateToken(user = user)
              Ok(Json.obj("msg" -> "Success", "token" -> token))
            } else {
              BadRequest(Json.obj("msg" -> "Wrong credentials"))
            }
          }
          case None => BadRequest(Json.obj("msg" -> "Username doesn't exist"))
        }
      }
    } yield {
      result
    }

  }

}


//      val keyBytes = Files.readAllBytes(new File("private_key.der").toPath)
//
//      keysCollectionFuture.flatMap(keysCollection =>
//        keysCollection.insert(Json.obj("privateKey" -> keyBytes))
//      )
