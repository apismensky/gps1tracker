package controllers

import play.api.mvc._

class GpsController extends Controller {

  def save = Action { request =>
    println("Body: " + request.body)
    Created("Done")
  }

}
