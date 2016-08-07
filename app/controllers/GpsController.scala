package controllers

import com.google.inject.Inject
import play.api.mvc._
import org.mongodb.scala._
import play.api.Configuration

class GpsController @Inject()(config: Configuration) extends Controller {

  def save = Action { request =>

    val json = request.body.asJson.getOrElse(throw new IllegalArgumentException("Invalid request body - JSON expected"))
    val dbUrl = config.getString("mongodb.uri").getOrElse(throw new IllegalStateException("Configure mongodb.uriin application.conf"))

    println(s"dbUrl: $dbUrl")
  	val mongoClient = MongoClient(dbUrl)

  	val database= mongoClient.getDatabase("heroku_60trxdkd")
  	val collection = database.getCollection("gps1records")
  	val timestamp = System.currentTimeMillis / 1000
    println("JSON:"+json.toString)
    val id = (json \ "i").as[Int]
    val lat = (json \ "e").as[Int]
    val lon = (json \ "n").as[Int]
    val bat = (json \ "b").as[Int]
    val doc = Document("_id" -> id, "ts" -> timestamp, "lat" -> lat, "lon" -> lon, "bat" -> bat)
    val observable = collection.insertOne(doc)

    observable.subscribe(new Observer[Completed] {
        override def onNext(result: Completed): Unit = println("Inserted")
        override def onError(e: Throwable): Unit = println(" \n\nFailed " + e + "\n\n")
        override def onComplete(): Unit = println("Completed")
    })

    //mongoClient.close()
    Created("OK")
  }

  def read = Action { request =>

    val dbUrl = config.getString("mongodb.uri").getOrElse(throw new IllegalStateException("Configure mongodb.uriin application.conf"))
    val mongoClient = MongoClient(dbUrl)

    val database= mongoClient.getDatabase("heroku_60trxdkd")
    val collection = database.getCollection("gps1records")


    collection.find().subscribe(
      (doc: Document) => println(doc.toJson()),                           // onNext
      (error: Throwable) => println(s"Query failed: ${error.getMessage}") // onError
    )

    Created("Done")
  }

}
