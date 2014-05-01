package com.lensa.scoring

import wabisabi.Client
import java.security.MessageDigest
import scala.concurrent.Await
import scala.concurrent.duration.{Duration, MILLISECONDS}
import scala.util.{Success, Failure}
import dispatch.{url, Req, Http, Future}
import dispatch.Defaults._
import com.ning.http.client.Response
import grizzled.slf4j.Logger


class BetterClient(esURL: String) extends Client(esURL) {

  def tv(index: String, `type`: String, id: String): Future[Response] = {
    val req = url(esURL) / index / `type` / id / "_termvector"
    doRequest(req.GET)
  }

  private def doRequest(req: Req) = {
    val breq = req.toRequest
    Http(req.setHeader("Content-type", "application/json; charset=utf-8"))
  }

}


object ES {

  val client = new BetterClient("http://localhost:9200")
  val logger = Logger(classOf[ScoringActor])

  def index_text(text: String, score: Option[Int]) = {
    val doc_id = getId(text)
    val f = client.index(
      index="documents", `type`="document", id=Some(doc_id),
      data=getIndexData(text, score)
    )
    val resp = Await.result(f, Duration(10000, MILLISECONDS)).getResponseBody
    logger.error(resp)
    doc_id
  }

  def getIndexData(text: String, score: Option[Int]) = score match {
    case Some(score) => s"""{\"text\": \"$text\", \"score\": \"$score\"}"""
    case None => s"""{\"text\": \"$text\"}"""
  }

  def getId(text: String) = {
    val md5sum = getMD5sum(text)
    s"$md5sum-${text.length}"
  }

  def getMD5sum(text: String) = {
    val digest = MessageDigest.getInstance("MD5")
    digest.digest(text.getBytes).map("%02x".format(_)).mkString
  }

  def getTermVector(doc_id: String): String = {
    val f = client.tv("documents", "document", doc_id)
    Await.result(f, Duration(10000, MILLISECONDS)).getResponseBody
  }

}
