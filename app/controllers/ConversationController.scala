package controllers

import traits.ExtendedController
import models._
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._
import helper.OutputLimits
import helper.CmActions.AuthAction
import play.api.libs.json._
import helper.ResultHelper._
import play.api.mvc.Result

/**
 * User: Björn Reimer
 * Date: 6/26/13
 * Time: 7:51 PM
 */
object ConversationController extends ExtendedController {

  def createConversation = AuthAction().async(parse.tolerantJson) {
    request =>
      {
        validateFuture[ConversationUpdate](request.body, ConversationUpdate.format) {
          c =>
            val conversation = Conversation.create(c.subject, Seq(Recipient.create(request.identity.id)), c.passCaptcha, c.aePassphraseList, c.sePassphrase)

            // check if there are recipients
            val withRecipients = (request.body \ "recipients").asOpt[Seq[String]] match {
              case None => conversation
              case Some(recipientIds) =>
                checkRecipients(recipientIds, conversation, request.identity) match {
                  case None => conversation
                  case Some(recipients) => conversation.copy(recipients = recipients ++ conversation.recipients)
                }
            }

            Conversation.col.insert(withRecipients).map {
              le => resOK(withRecipients.toJson)
            }
        }
      }
  }

  def getConversation(id: String, offset: Int, limit: Int, keyId: Option[String]) = AuthAction(allowExternal = true).async {
    request =>
      Conversation.find(id, limit, offset).map {
        case None => resNotFound("conversation")
        case Some(c) => c.hasMemberResult(request.identity.id) {
          resOK(c.toJson)
        }
      }
  }

  def checkRecipients(recipientIds: Seq[String], conversation: Conversation, identity: Identity): Option[Seq[Recipient]] = {
    // remove all recipients that are already a member of this conversation
    val filtered = recipientIds.filterNot(id => conversation.recipients.exists(_.identityId.equals(id)))

    // check if all recipients are in the users address book
    filtered.forall(recipient => identity.contacts.exists(_.identityId.id.equals(recipient))) match {
      case false => None
      case true  => Some(filtered.map(Recipient.create))
    }
  }

  def addRecipients(id: String) = AuthAction().async(parse.tolerantJson) {
    request =>
      Conversation.find(new MongoId(id), -1, 0).flatMap {
        case None => Future.successful(resNotFound("conversation"))
        case Some(conversation) =>
          conversation.hasMemberFutureResult(request.identity.id) {
            validateFuture[Seq[String]](request.body \ "recipients", Reads.seq[String]) {
              recipientIds =>
                checkRecipients(recipientIds, conversation, request.identity) match {
                  case Some(recipients) =>
                    conversation.addRecipients(recipients).map {
                      case true  => resOk("updated")
                      case false => resServerError("update failed")
                    }
                  case None => Future(resKo("invalid recipient list"))
                }
            }
          }
      }
  }

  def deleteRecipient(id: String, rid: String) = AuthAction().async {
    request =>
      Conversation.find(id, -1, 0).flatMap {
        case None => Future(resNotFound("conversation"))
        case Some(c) => c.hasMemberFutureResult(request.identity.id) {
          c.deleteRecipient(new MongoId(rid)).map {
            case false => resNotFound("recipient")
            case true  => resOK()
          }
        }
      }
  }

  def getConversationSummary(id: String) = AuthAction(allowExternal = true).async {
    request =>
      Conversation.find(id, -1, 0).flatMap {
        case None => Future(resNotFound("conversation"))
        case Some(c) => c.hasMemberFutureResult(request.identity.id) {
          c.toSummaryJson.map(resOK(_))
        }
      }
  }

  def getConversations(offset: Int, limit: Int) = AuthAction().async {
    request =>
      Conversation.findByIdentityId(request.identity.id).flatMap {
        list =>
          // TODO: this can be done more efficiently with the aggregation framework in mongo
          val sorted = list.sortBy(_.lastUpdated).reverse
          val limited = OutputLimits.applyLimits(sorted, offset, limit)
          val futureJson = Future.sequence(limited.map(_.toSummaryJson))
          futureJson.map {
            json =>
              val res = Json.obj("conversations" -> json, "numberOfConversations" -> list.length)
              resOK(res)
          }
      }
  }

  def updateConversation(id: String) = AuthAction().async(parse.tolerantJson) {
    request =>
      validateFuture(request.body, ConversationUpdate.createReads) {
        cu =>
          Conversation.find(id, -1, 0).flatMap {
            case None => Future(resNotFound("conversation"))
            case Some(c) => c.hasMemberFutureResult(request.identity.id) {
              c.update(cu).map {
                case false => resServerError("could not update")
                case true  => resOk("updated")
              }
            }
          }
      }
  }
}