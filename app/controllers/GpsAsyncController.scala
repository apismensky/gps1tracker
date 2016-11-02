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

  // curl -H "Content-Type: application/json" -X POST -d '{"i":"00000003","e":"06036.5161E","n":"5650.1863N","b":"98"}' http://localhost:9000/gps
  def createFromJson = Action.async(parse.json) { request =>
    Json.fromJson[Coordinates](request.body) match {
      case JsSuccess(coordinates, _) =>
        for {
          coords <- coordinatedFuture
          lastError <- {
            val ts = System.currentTimeMillis / 1000
            val _id = coordinates.i + "-" + ts.toString

            val newRecord = Json.obj(
              "_id" -> _id,
              "ts" -> ts.toInt,
              "i" -> coordinates.i,
              "e" -> coordinates.e,
              "n" -> coordinates.n,
              "b" -> coordinates.b
            )

            coords.insert(newRecord)
          }
        } yield {
          Logger.debug(s"Successfully inserted with LastError: $lastError")
          Created("Created 1 record")
        }
      case JsError(errors) =>
        Future.successful(BadRequest("Could not build a coord from the json provided. " + Errors.show(errors)))
    }
  }

 // curl http://localhost:9000/gps
  def list() = Action.async {
     coordinatedFuture.flatMap {
      _.find(Json.obj()).cursor[JsObject](ReadPreference.primary).collect[List]().map(list => Ok(Json.toJson(list)))
    }
  }

  // curl http://localhost:9000/gps/6254614
  def getLastEntryById(id: String) = Action.async { request =>
    coordinatedFuture.flatMap(gpsRecordsCollection => {
      val projection: JsObject = Json.obj("i" -> id, "$or" -> JsArray(Seq(Json.obj("e" -> Json.obj("$ne" -> "000000000")), Json.obj("n" -> Json.obj("$ne" ->"000000000")))))

      gpsRecordsCollection.find(projection).sort(Json.obj("ts" -> -1)).one[JsObject](ReadPreference.primary).map(record => Ok(Json.toJson(record)))
    })
  }

  // curl http://localhost:9000/gps/6254614/1473065721
  def getLastEntriesByIdAndTimestamp(id: String, timestamp: Int) = Action.async { request =>
    val nowTimestamp: Int = (System.currentTimeMillis / 1000).toInt

    coordinatedFuture.flatMap(gpsRecordsCollection => {
      gpsRecordsCollection.find(Json.obj("i" -> id, "ts" -> Json.obj("$gte" -> timestamp, "$lte" -> nowTimestamp))).cursor[JsObject](ReadPreference.primary).collect[List]().map(records => Ok(Json.toJson(records)))
    })
  }
}


