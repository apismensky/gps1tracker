package controllers

import com.google.inject.Inject
import play.api.mvc._
import org.mongodb.scala._
import play.api.Configuration

class GpsController @Inject()(config: Configuration) extends Controller {

  def save = Action { request =>

    val json = request.body.asJson.getOrElse(throw new IllegalArgumentException("Invalid request body - JSON expected"))
    val dbUrl = config.getString("mongodb.uri").getOrElse(throw new IllegalStateException("Configure mongodb.uriin application.conf"))

    //println(s"dbUrl: $dbUrl")
  	val mongoClient = MongoClient(dbUrl)

  	val database= mongoClient.getDatabase("heroku_60trxdkd")
  	val collection = database.getCollection("gps1records")
  	val ts = System.currentTimeMillis / 1000
    println("JSON:"+json.toString)
    val i = (json \ "i").as[Long]
    val e = (json \ "e").as[Long]
    val n = (json \ "n").as[Long]
    val b = (json \ "b").as[Long]
    val id = i + "-" + ts.toString
    val doc = Document("_id" -> id, "ts" -> ts.toInt, "i" -> i, "e" -> e, "n" -> n, "b" -> b)
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

    val builder = StringBuilder.newBuilder
    var done = false;

    collection.find().subscribe(
      (doc: Document) => builder.append(doc.toJson()+", \n"),              // onNext
      (error: Throwable) => println(s"Query failed: ${error.getMessage}"), // onError
      () => done = true // Complete
    )

    while(!done) { Thread.sleep(100) }

    //println(builder.toString())

    Created(builder.toString())

  }

}
