package controllers

import javax.inject._

import models.{Device, User}
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
class AdminAsyncController @Inject()(val reactiveMongoApi: ReactiveMongoApi)(implicit exec: ExecutionContext)
  extends Controller with MongoController with ReactiveMongoComponents {

  def index = Action { request =>
    Ok(views.html.admin())
  }

  def listOfUsers = Action { request =>
    Ok(views.html.admin_list())
  }

  def editUser(id: String) = Action { request =>
    Ok(views.html.admin_edit(id))
  }



}


