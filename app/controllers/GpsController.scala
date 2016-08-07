package controllers

import com.google.inject.Inject
import play.api.mvc._
import org.mongodb.scala._
import play.api.Configuration

class GpsController @Inject()(config: Configuration) extends Controller {

  def save = Action { request =>
    val json = request.body.asJson

    val dbUrl = config.getString("mongodb.uri").getOrElse(throw new IllegalStateException("Configure mongodb.uriin application.conf"))

    println(s"dbUrl: $dbUrl")
  	val mongoClient = MongoClient(dbUrl)

  	val database= mongoClient.getDatabase("heroku_60trxdkd")
  	val collection = database.getCollection("gps1records")
  	val timestamp = System.currentTimeMillis / 1000
    val id = (json.get \ "i").as[String]
    val lat = (json.get \ "e").as[String]
    val lon = (json.get \ "n").as[String]
    val bat = (json.get \ "b").as[String]
    val doc = Document("_id" -> id, "ts" -> timestamp, "lat" -> lat, "lon" -> lon, "bat" -> bat)
    val observable = collection.insertOne(doc)

      observable.subscribe(new Observer[Completed] {
        override def onNext(result: Completed): Unit = println("Inserted")
        override def onError(e: Throwable): Unit = println(" \n\nFailed " + e + "\n\n")
        override def onComplete(): Unit = println("Completed")
      })

    //mongoClient.close()
    println("Body: " + request.body)
    Created("OK")
  }

}
