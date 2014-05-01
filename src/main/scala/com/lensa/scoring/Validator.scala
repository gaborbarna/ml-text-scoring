package com.lensa.scoring


class ScoreRangeError(message: String = null) extends RuntimeException(message, null)
case class TrainInstance(text: String, score: Int)
case class IndexInstance(id: String)
case class ClassifyInstance(text: String)


object Validator {

  def validateTrainInstance(params: Map[String, String]): Either[String, TrainInstance] = {
    try {
      val score = params("score").toInt
      if (score < 1 || score > 10) throw new ScoreRangeError("1 <= score <= 10")
      Right(TrainInstance(params("text"), score - 1))
    } catch {
      case e: Throwable => Left(e.getMessage)
    }
  }

  def validateClassifyInstance(params: Map[String, String]): Either[String, ClassifyInstance] = {
    try {
      Right(ClassifyInstance(params("text")))
    } catch {
      case e: Throwable => Left(e.getMessage)
    }
  }
}
