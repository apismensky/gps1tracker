package controllers

import play.api.mvc._

import org.mongodb.scala._
import org.mongodb.scala.model.Aggregates._
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Projections._
import org.mongodb.scala.model.Sorts._
import org.mongodb.scala.model.Updates._
import org.mongodb.scala.model._

class GpsController extends Controller {

  def save = Action { request =>

  	val record: String = request.body;

  	val mongoClient: MongoClient = MongoClient()

  	val database: MongoDatabase = mongoClient.getDatabase("gps1db");

  	val collection: MongoCollection[Document] = database.getCollection("gps1records")

  	val timestamp: Long = System.currentTimeMillis / 1000

  	val doc: Document = Document("_id" -> timestamp, "record" -> record)

  	val observable: Observable[Completed] = collection.insertOne(doc)

      observable.subscribe(new Observer[Completed] {
        override def onNext(result: Completed): Unit = println("Inserted")
        override def onError(e: Throwable): Unit = println(" \n\nFailed " + e + "\n\n")
        override def onComplete(): Unit = println("Completed")
      })

    mongoClient.close();

    println("Body: " + request.body)

    Created("OK")
  }

}
