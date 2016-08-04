package controllers

import play.api.mvc.Controller
import play.mvc.Result
import play.mvc.Results._

object HealthController {
  val PlayVersion = play.core.PlayVersion
  val Versions = s"play version: ${PlayVersion.current}, " +
    s"scala version: ${PlayVersion.scalaVersion}, " +
    s"java version: ${System.getProperty("java.version")}, " +
    s"sbt version: ${PlayVersion.sbtVersion}"

}

class HealthController  extends Controller {
  import HealthController._
  def buildInfo(): Result = ok(Versions)
}