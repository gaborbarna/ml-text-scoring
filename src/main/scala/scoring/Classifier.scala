package scoring

import org.apache.mahout.math.SequentialAccessSparseVector;
import org.apache.mahout.math.Vector;
import org.apache.mahout.vectorizer.encoders.StaticWordValueEncoder;
import org.apache.mahout.classifier.sgd.L1;
import org.apache.mahout.classifier.sgd.OnlineLogisticRegression;
import grizzled.slf4j.Logger


trait ClassifierTrait {
  def train(termVector: Map[String, Int], score: Int): Unit
  def classify(termVector: Map[String, Int]): Double
}

class Classifier(cardinality: Int) extends ClassifierTrait {

  val logger = Logger(classOf[Classifier])

  val model = new OnlineLogisticRegression(
    cardinality, cardinality, new L1());

  def train(termVector: Map[String, Int], score: Int) = {
    val vector = encode(termVector, cardinality)
    model.train(score, vector);
  }

  def classify(termVector: Map[String, Int]) = {
    val vector = encode(termVector, cardinality)
    val probs = model.classifyFull(vector);
    (0 until probs.size).foldLeft(0.0)((acc, i) => (i + 1) * probs.get(i) + acc)
  }
  
  def encode(termVector: Map[String, Int], cardinality: Int) = {
    val vector = new SequentialAccessSparseVector(cardinality)
    val encoder = new StaticWordValueEncoder("name")
    for (term <- termVector.keys) {
      for (_ <- 0 to termVector(term)) encoder.addToVector(term, vector)
    }
    vector
  }
}
