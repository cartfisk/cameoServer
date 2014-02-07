package controllers

import traits.ExtendedController
import models._
import play.api.mvc.Action
import scala.concurrent.{ Future, ExecutionContext }
import ExecutionContext.Implicits.global
import helper.ResultHelper._
import helper.AuthAction
import scala.Some
import play.api.libs.json.{ Json, Format, JsError }
import scala.Some
import helper.MongoHelper._

/**
 * User: Björn Reimer
 * Date: 1/20/14
 * Time: 12:07 PM
 */
object IdentityController extends ExtendedController {

  def getIdentity(id: String) = AuthAction.async {

    val mongoId = new MongoId(id)

    Identity.find(mongoId).map {
      case None           => NotFound(resKO("Identity not found"))
      case Some(identity) => resOK(identity.toJson)
    }
  }

  case class IdentityUpdate(phoneNumber: Option[String],
                            email: Option[String],
                            displayName: Option[String])

  object IdentityUpdate {
    implicit val format: Format[IdentityUpdate] = Json.format[IdentityUpdate]
  }

  def updateIdentity() = AuthAction.async(parse.tolerantJson) {
    request =>
      request.body.validate[IdentityUpdate].map {
        update =>
          {

            val newMail = update.email.flatMap { getNewValueVerifiedString(request.identity.email, _) }
            val newPhoneNumber = update.phoneNumber.flatMap { getNewValueVerifiedString(request.identity.phoneNumber, _) }
            val newDisplayName = update.displayName.flatMap { getNewValueString(request.identity.displayName, _) }

            request.identity.update(email = newMail, phoneNumber = newPhoneNumber, displayName = newDisplayName).flatMap {
              lastError =>
                {
                  Identity.find(request.identity.id).map {
                    case None    => NotFound(resKO("no identity"))
                    case Some(i) => resOK(i.toJson)
                  }
                }

            }
          }
      }.recoverTotal(e => Future(BadRequest(resKO(JsError.toFlatJson(e)))))
  }
}