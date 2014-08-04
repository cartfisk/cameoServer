package controllers

import actors.{NewAePassphrases, UpdatedIdentity}
import helper.CmActions._
import helper.ResultHelper._
import models._
import play.api.Logger
import play.api.libs.json._
import traits.ExtendedController

import scala.concurrent.ExecutionContext.Implicits.global

/**
 * User: Björn Reimer
 * Date: 22.07.14
 * Time: 17:08
 */
object CryptoController extends ExtendedController {

  def addPublicKey() = AuthAction().async(parse.tolerantJson) {
    request =>
      validateFuture(request.body, PublicKey.createReads) {
        publicKey =>
          request.identity.addPublicKey(publicKey).map {
            case false => resServerError("unable to add")
            case true =>
              // send event to all people in address book
              request.identity.contacts.foreach {
                contact =>
                  actors.eventRouter ! UpdatedIdentity(contact.identityId, request.identity.id, Json.obj("publicKeys" -> Seq(publicKey.toJson)))
              }
              // send event to ourselves
              actors.eventRouter ! UpdatedIdentity(request.identity.id, request.identity.id, Json.obj("publicKeys" -> Seq(publicKey.toJson)))
              resOk(publicKey.toJson)
          }
      }
  }

  def editPublicKey(id: String) = AuthAction().async(parse.tolerantJson) {
    request =>
      validateFuture(request.body, PublicKeyUpdate.format) {
        pku =>
          request.identity.editPublicKey(new MongoId(id), pku).map {
            case false => resServerError("not updated")
            case true  => resOk("updated")
          }
      }
  }

  def deletePublicKey(id: String) = AuthAction().async {
    request =>
      request.identity.deletePublicKey(new MongoId(id)).map {
        case false => resServerError("unable to delete")
        case true  =>
          actors.eventRouter ! UpdatedIdentity(request.identity.id, request.identity.id, Json.obj("publicKeys" -> Seq(Json.obj("id" -> id, "deleted" -> true))))
          resOk("deleted")
      }
  }

  def addSignature(id: String) = AuthAction().async(parse.tolerantJson) {
    request =>
      validateFuture[Signature](request.body, Signature.format) {
        signature =>
          request.identity.addSignatureToPublicKey(new MongoId(id), signature).map {
            case false => resBadRequest("could not update")
            case true  => resOk(signature.toJson)
          }
      }
  }

  def deleteSignature(id: String, keyId: String) = AuthAction().async {
    request =>
      request.identity.deleteSignature(new MongoId(id), keyId).map {
        case false => resServerError("could not delete")
        case true  => resOk("deleted")
      }
  }

  case class AePassphrase(conversationId: String, aePassphrase: String)
  object AePassphrase { implicit val format = Json.format[AePassphrase] }

  def getAePassphrases(id: String, newKeyId: String, limit: Int) = AuthAction().async {
    request =>

      val maybeLimit = limit match {
        case 0 => None
        case x => Some(x)
      }

      Conversation.getAePassphrases(request.identity.id, new MongoId(id), new MongoId(newKeyId), maybeLimit).map {
        list =>
          resOk(list.map(Json.toJson(_)))
      }
  }

  def addAePassphrases(id: String) = AuthAction()(parse.tolerantJson) {
    request =>
      validate[Seq[AePassphrase]](request.body, Reads.seq(AePassphrase.format)) {
        list =>
          list.foreach {
            aePassphrase =>
              Conversation.addAePassphrases(
                Seq(EncryptedPassphrase.create(id, aePassphrase.aePassphrase)),
                new MongoId(aePassphrase.conversationId)).map {
                  case false => Logger.error("error while adding aePassphrase to conversation " + aePassphrase.conversationId)
                  case true  => Logger.debug("updated")
                }
          }
          val event =  NewAePassphrases(request.identity.id, id, list.map(_.conversationId))
          Logger.debug(event.toString)
          actors.eventRouter ! NewAePassphrases(request.identity.id, id, list.map(_.conversationId))
          resOk("updated")
      }
  }

}