package controllers

import javax.inject._

import models.{Device, User}
import org.mindrot.jbcrypt.BCrypt
import org.sedis.Pool
import play.api.libs.json._
import play.api.mvc._
import play.modules.reactivemongo._
import reactivemongo.api.ReadPreference
import reactivemongo.bson.BSONObjectID
import reactivemongo.play.json._
import reactivemongo.play.json.collection._

import scala.concurrent.{ExecutionContext, Future}


@Singleton
class UserAsyncController @Inject()(val reactiveMongoApi: ReactiveMongoApi, sedisPool: Pool)(implicit exec: ExecutionContext)
  extends Controller with MongoController with ReactiveMongoComponents {

  final val userProjection: JsObject = Json.obj("_id" -> 1, "firstName" -> 1, "lastName" -> 1, "devices" -> 1, "email" -> 1)

  def usersCollectionFuture: Future[JSONCollection] = database.map(_.collection[JSONCollection]("users"))

  def index = Action.async { request =>
    usersCollectionFuture.flatMap(usersCollection => {

      val user = sedisPool.withJedisClient(client => client.get(request.headers.get("X-ACCESS-TOKEN").get))

      if (user != null) {
        usersCollection
          .find(Json.obj(), userProjection)
          .cursor[JsObject](ReadPreference.primary)
          .collect[Vector]()
          .map(list => {
            val resultVector = list.map(user => user ++ Json.obj("_id" -> (user \ "_id").as[BSONObjectID].stringify))

            Ok(Json.obj("objects" -> Json.toJson(resultVector)))
          })
      } else {
        Future.successful(BadRequest(Json.obj("msg" -> "Unauthorized")))
      }

    })
  }

  def create = Action.async(parse.json) { request =>
    val user = sedisPool.withJedisClient(client => client.get(request.headers.get("X-ACCESS-TOKEN").get))

    if (user != null) {
      Json.fromJson[User](request.body) match {
        case JsSuccess(userData, _) => {
          for {
            usersCollection <- usersCollectionFuture
            userId <- {
              usersCollection.find(
                Json.obj("email" -> userData.email),
                Json.obj("_id" -> 1)
              ).one[JsObject](ReadPreference.primary).map(s => s)
            }
            result <- {
              userId match {
                case Some(_) => Future.successful(BadRequest(Json.obj("msg" -> "User Exists")))
                case None => {
                  val salt = BCrypt.gensalt()
                  val password = BCrypt.hashpw(userData.password, salt)

                  val newUser = Json.obj(
                    "firstName" -> userData.firstName,
                    "lastName" -> userData.lastName,
                    "email" -> userData.email,
                    "password" -> password,
                    "salt" -> salt,
                    "devices" -> JsArray(Seq()))

                  usersCollection.insert(newUser).map(writeResult => Ok(Json.obj("msg" -> "User Created")))

                }
              }
            }
          } yield {
            result
          }
        }
        case JsError(errors) => {
          Future.successful(BadRequest(Json.obj("msg" -> "Invalid Json Input")))
        }
      }
    } else {
      Future.successful(BadRequest(Json.obj("msg" -> "Unauthorized")))
    }
  }

  def getUser(id: String) = Action.async { request =>
    val user = sedisPool.withJedisClient(client => client.get(request.headers.get("X-ACCESS-TOKEN").get))

    if (user != null) {
      for {
        usersCollection <- usersCollectionFuture
        userOption <- usersCollection.find(Json.obj("_id" -> BSONObjectID(id)), userProjection).one[JsObject](ReadPreference.primary).map(s => s)
      } yield {
        val user = userOption match {
          case Some(user: JsObject) => user
          case None => Json.obj()
        }

        Ok(user)
      }
    } else {
      Future.successful(BadRequest(Json.obj("msg" -> "Unauthorized")))
    }
  }

  def updateUserDevices(id: String) = Action.async(parse.json) { request =>
    val user = sedisPool.withJedisClient(client => client.get(request.headers.get("X-ACCESS-TOKEN").get))

    if (user != null) {
      (request.body \ "devices").asOpt[JsArray] match {
        case None => Future.successful(BadRequest(Json.obj("msg" -> "Invalid Json Input - No devices")))
        case Some(devices: JsArray) => {
          Json.fromJson[Vector[Device]](devices) match {
            case JsSuccess(deviceVector: Vector[Device], _) => {
              for {
                usersCollection <- usersCollectionFuture
                result <- {
                  usersCollection.findAndUpdate(
                    Json.obj("_id" -> BSONObjectID(id)),
                    Json.obj("$set" -> Json.obj("devices" -> deviceVector)),
                    fetchNewObject = true,
                    fields = Some(userProjection))
                  .map(_.result[JsObject])
                }
              } yield {
                val user = result match {
                  case Some(user: JsObject) => user
                  case None => Json.obj()
                }

                Ok(user)
              }
            }
            case JsError(errors) => {
              Future.successful(BadRequest(Json.obj("msg" -> "Invalid Json Input - Malformed device structure")))
            }
          }
        }
      }
    } else {
      Future.successful(BadRequest(Json.obj("msg" -> "Unauthorized")))
    }
  }

}


