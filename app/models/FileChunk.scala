package models

import play.modules.reactivemongo.json.collection.JSONCollection
import play.modules.reactivemongo.ReactiveMongoPlugin
import play.api.libs.json.{ Reads, Json, Format }
import play.api.Play.current
import traits.Model
import helper.JsonHelper._
import helper.IdHelper
import play.api.libs.json._
import play.api.libs.functional.syntax._
import java.util.Date
import traits.Model
import play.api.libs.json.Reads._
import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global
import helper.MongoCollections._

/**
 * User: Björn Reimer
 * Date: 1/15/14
 * Time: 11:59 AM
 */
case class FileChunk(
    id: MongoId,
    chunk: String) {
  def toJson: JsObject = Json.obj("chunk" -> chunk)
}

object FileChunk extends Model[FileChunk] {

  // mongo collection
  def col = fileChunkCollection

  implicit val mongoFormat: Format[FileChunk] = createMongoFormat(Json.reads[FileChunk], Json.writes[FileChunk])

  def docVersion = 0

  def evolutions = Map()

  val createReads: Reads[FileChunk] = (
    Reads.pure[MongoId](IdHelper.generateChunkId) and
    (__ \ 'chunk).read[String]
  )(FileChunk.apply _)

}