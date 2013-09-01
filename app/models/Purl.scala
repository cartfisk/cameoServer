package models

import traits.Model
import play.api.libs.json._
import traits.OutputLimits
import reactivemongo.api.indexes.{IndexType, Index}
import helper.IdHelper
import java.util.Date
import scala.concurrent.{Future, ExecutionContext}
import ExecutionContext.Implicits.global

/**
 * User: Björn Reimer
 * Date: 9/1/13
 * Time: 5:23 PM
 */

case class Purl(
                 purl: String,
                 conversationId: String,
                 userType: String,
                 username: Option[String],
                 name: Option[String],
                 token: Option[String]
                 )

object Purl extends Model[Purl] {

  purlCollection.indexesManager.ensure(Index(List("purl" -> IndexType.Ascending), unique = true, sparse = true))

  implicit val collection = purlCollection
  implicit val mongoFormat: Format[Purl] = createMongoFormat(Json.reads[Purl], Json.writes[Purl])

  // Input/output format for the API
  def inputReads = Json.reads[Purl]

  def outputWrites(implicit ol: OutputLimits = OutputLimits(0, 0)): Writes[Purl] = Writes {
    purl =>
      Json.obj("conversationId" -> purl.conversationId) ++
        Json.obj("userType" -> purl.userType) ++
        toJsonOrEmpty("username", purl.username) ++
        toJsonOrEmpty("name", purl.name) ++
        toJsonOrEmpty("token", purl.token)
  }

  def find(purl: String): Future[Option[Purl]] = {
    val query = Json.obj("purl" -> purl)
    collection.find(query).one[Purl]
  }

  /*
   * Helper
   */
  def createPurl(conversationId: String, recipient: Recipient): String =
  {
    // check if the recipient is another user or not
    val purl: Purl = if(recipient.messageType.toLowerCase().equals("otherUser"))
    {
      // we are sending to another user => only need username
      new Purl(IdHelper.generatePurl(), conversationId, "registered", Some(recipient.sendTo), None, None)
    }
    else
    {
      // we are sending to another user => create new (temporary) token, save Display name
      val token = new Token(IdHelper.generateAccessToken(), None, Some(IdHelper.generatePurl()), false, new Date)
      tokenCollection.insert(token)
      new Purl(token.purl.get, conversationId, "unregistered", None, Some(recipient.name), Some(token.token))
    }

    // write to db and return purl
    purlCollection.insert(purl)
    purl.purl
  }


}