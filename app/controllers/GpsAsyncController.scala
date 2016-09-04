package controllers

import javax.inject._

import models.Coordinates
import play.api.Logger
import play.api.libs.json._
import play.api.mvc._
import play.modules.reactivemongo._
import reactivemongo.api.ReadPreference
import reactivemongo.play.json._
import reactivemongo.play.json.collection._
import utils.Errors

import scala.concurrent.{ExecutionContext, Future}


@Singleton
class GpsAsyncController @Inject()(val reactiveMongoApi: ReactiveMongoApi)(implicit exec: ExecutionContext)
  extends Controller with MongoController with ReactiveMongoComponents {

  def coordinatedFuture: Future[JSONCollection] = database.map(_.collection[JSONCollection]("gps1records"))

  def create(i: String,
             e: String,
             n: String,
             b: String) = Action.async {
    for {
      coordinates <- coordinatedFuture
      lastError <- coordinates.insert(Coordinates(i, e, n, b))
    } yield
      Ok("Mongo LastError: %s".format(lastError))
  }

  // curl -H "Content-Type: application/json" -X POST -d '{"i":22,"e":33,"n":663,"b":46}' http://localhost:9000/coordinates/
  def createFromJson = Action.async(parse.json) { request =>
    Json.fromJson[Coordinates](request.body) match {
      case JsSuccess(coordinates, _) =>
        for {
          coords <- coordinatedFuture
          lastError <- coords.insert(coordinates)
        } yield {
          Logger.debug(s"Successfully inserted with LastError: $lastError")
          Created("Created 1 record")
        }
      case JsError(errors) =>
        Future.successful(BadRequest("Could not build a coord from the json provided. " + Errors.show(errors)))
    }
  }

 // curl http://localhost:9000/coordinates
  def list() = Action.async {
     coordinatedFuture.flatMap {
      _.find(Json.obj()).cursor[Coordinates](ReadPreference.primary).collect[List]()
    } map { c => Ok(Json.toJson(c))  }
  }
}


