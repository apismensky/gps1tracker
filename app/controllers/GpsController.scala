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

  	val mongoClient: MongoClient = MongoClient()

  	val database: MongoDatabase = mongoClient.getDatabase("gps1db");

  	val collection: MongoCollection[Document] = database.getCollection("gps1records")

    mongoClient.close()

    println("Body: " + request.body)
    
    Created("OK")
  }

}
