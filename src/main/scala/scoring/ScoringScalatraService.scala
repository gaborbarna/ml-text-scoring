package scoring

import akka.actor.{ActorRef, Actor, ActorSystem}
import akka.util.Timeout
import org.scalatra._
import org.json4s.{DefaultFormats, Formats}
import org.scalatra.json._
import scalate.ScalateSupport
import scala.concurrent.{ExecutionContext, Await}
import grizzled.slf4j.Logger


class ScoringScalatraService(system: ActorSystem, scoringActor: ActorRef) extends ScoringStack with JacksonJsonSupport with FutureSupport {

  import _root_.akka.pattern.ask
  implicit val defaultTimeout = Timeout(10000)
  protected implicit val jsonFormats: Formats = DefaultFormats
  protected implicit def executor: ExecutionContext = system.dispatcher
  val logger = Logger(classOf[ScoringActor])


  before() {
    contentType = formats("json")
  }

  post("/index") {
    val id = ES.index_text(params("text"), Some(params("score").toInt))
    IndexInstance(id)
  }
  
  post("/train") {
    Validator.validateTrainInstance(params) match {
      case Left(s) => Map("success" -> false, "error" -> s)
      case Right(trainInstance) => {
	scoringActor ! trainInstance
	Map("success" -> true)
      }
    }
  }

  post("/classify") {
    Validator.validateClassifyInstance(params) match {
      case Left(s) => Map("success" -> false, "error" -> s)
      case Right(classifyInstance) => {
	val future = scoringActor ? classifyInstance
	Await.result(future, defaultTimeout.duration).asInstanceOf[Option[Double]] match {
	  case Some(d) => Map("success" -> true, "score" -> d)
	  case None => Map("success" -> false)
	}
      }
    }
  }
}
