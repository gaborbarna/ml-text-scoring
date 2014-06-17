package scoring

import akka.actor.{ActorRef, Actor, ActorSystem}
import akka.event.Logging
import grizzled.slf4j.Logger


class ScoringActor extends Actor {

  val logger = Logger(classOf[ScoringActor])
  val classifier = new Classifier(10)
  val max_try_count = 1000

  def receive = {
    case TrainInstance(text: String, score: Int) => {
      sender ! train(text, score)
    }
    case ClassifyInstance(text: String) => {
      sender ! classify(text)
    }
    case _ => logger.error("what")
  }

  def train(text: String, score: Int) = {
    val id = ES.index_text(text, Some(score))
    TermVectorParser.parse(ES.getTermVector(id)) match {
      case Some(m) => classifier.train(m, score)
      case None => ()
    }
  }

  def classify(text: String): Option[Double] = {
    val id = ES.index_text(text, None)

    def try_loop(try_count: Int = max_try_count): Option[Double] = {
      if (try_count == 0) None
      else {
	TermVectorParser.parse(ES.getTermVector(id)) match {
	  case Some(m) => Some(classifier.classify(m))
	  case None => try_loop(try_count - 1)
	}
      }
    }
    try_loop()
  }

}
