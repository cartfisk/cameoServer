package models

import play.api.libs.json._
import play.api.libs.functional.syntax._
import helper.IdHelper
import traits.Model
import scala.concurrent.{ Future, ExecutionContext }
import ExecutionContext.Implicits.global
import play.api.mvc.{ Results, SimpleResult }
import play.mvc.Result
import helper.ResultHelper._
import play.api.Logger
import helper.JsonHelper._
import reactivemongo.core.commands.LastError
import constants.Contacts._

/**
 * User: Björn Reimer
 * Date: 6/25/13
 * Time: 5:53 PM
 */
case class Contact(id: MongoId,
                   groups: Seq[String],
                   identityId: MongoId,
                   contactType: String,
                   docVersion: Int ) {

  def toJson: JsObject = Json.toJson(this)(Contact.outputWrites).as[JsObject]

  def toJsonWithIdentity: Future[JsObject] =
    Identity.find(this.identityId).map {
      case None => Json.obj()
      case Some(identity) =>
        Json.toJson(this)(Contact.outputWrites).as[JsObject] ++
          Json.obj("identity" -> identity.toJson)
    }

  def toJsonWithIdentityResult: Future[SimpleResult] = {
    this.toJsonWithIdentity.map(
      js => resOK(js))
  }

  def update(contactUpdate: ContactUpdate): Any = {

    // edit groups
    if (contactUpdate.groups.isDefined) {
      val query = Json.obj("contacts.id" -> this.id)
      val newGroups = maybeEmpty("contacts.id.groups", contactUpdate.groups.map(Json.toJson(_)))
      val set = Json.obj("$set" -> newGroups)
      Contact.col.update(query, set).map {
        lastError => Logger.error("Could not update Contact: " + query + ":" + set)
      }
    }

    // edit identity only for external contacts
    if (this.contactType.equals(CONTACT_TYPE_EXTERNAL)) {
      val identityUpdate = new IdentityUpdate(VerifiedString.createOpt(contactUpdate.phoneNumber), VerifiedString.createOpt(contactUpdate.email), contactUpdate.displayName)

      Identity.find(this.identityId).map {
        case Some(identity) => identity.update(identityUpdate)
      }
    }
  }

}

object Contact extends Model[Contact] {

  implicit def col = identityCollection

  def createReads(identityId: MongoId, contactType: String): Reads[Contact] = (
    Reads.pure[MongoId](IdHelper.generateContactId()) and
    ((__ \ 'groups).read[Seq[String]] or Reads.pure(Seq[String]())) and
    Reads.pure[MongoId](identityId) and
    Reads.pure[String](contactType) and
    Reads.pure[Int](docVersion)
  )(Contact.apply _)

  def outputWrites: Writes[Contact] = Writes {
    c =>
      Json.obj("id" -> c.id.toJson) ++
        Json.obj("groups" -> c.groups) ++
        Json.obj("identityId" -> c.identityId.toJson) ++
        Json.obj("contactType" -> c.contactType)
  }

  val evolutionAddContactType: Reads[JsObject] = Reads[JsObject] {
    js =>
      val addType = __.json.update((__ \ 'contactType).json.put(JsString(CONTACT_TYPE_INTERNAL)))
      val addVersion = __.json.update((__ \ 'docVersion).json.put(JsNumber(1)))
      js.transform(addType andThen addVersion)
  }

  val evolutions: Map[Int, Reads[JsObject]] = Map( 0 -> evolutionAddContactType)

  val docVersion = 1
  val mongoReads: Reads[Contact] = createMongoReadsWithEvolutions(Json.reads[Contact], evolutions, docVersion, col)
  val mongoWrites: Writes[Contact] = createMongoWrites(Json.writes[Contact])

  implicit val mongoFormat: Format[Contact] = Format(mongoReads, mongoWrites)
}

case class ContactUpdate(groups: Option[Seq[String]],
                         displayName: Option[String],
                         email: Option[String],
                         phoneNumber: Option[String])

object ContactUpdate {
  implicit val format = Json.format[ContactUpdate]
}
