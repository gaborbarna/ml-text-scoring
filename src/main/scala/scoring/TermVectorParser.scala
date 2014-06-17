package scoring

import scala.util.parsing.json._


class CC[T] {
  def unapply(a: Any): Option[T] = Some(a.asInstanceOf[T]) 
}


object TermVectorParser {

  object M extends CC[Map[String, Any]]
  object I extends CC[Int]
  object D extends CC[Double]
  object T extends CC[Tuple2[String, Any]]
  
  def parse(tv: String): Option[Map[String, Int]] = {
    JSON.parseFull(tv) match {
      case Some(a) => {
	val m = a.asInstanceOf[Map[String, Any]]
	if (m contains "term_vectors") Some(extractTuples(m)) else None
      }
      case None => None
    }
  }

  def extractTuples(map: Map[String, Any]) = {
    val tupleList = for {
      M(termVectors) <- List(map("term_vectors"))
      M(text) = termVectors("text")
      M(terms) = text("terms")
      T(term) <- terms
      (termName, M(termMap)) = term
      D(termFreqDouble) = termMap("term_freq")
      I(termFreq) = termFreqDouble.toInt
    } yield (termName, termFreq)
    tupleList.toMap
  }

}
