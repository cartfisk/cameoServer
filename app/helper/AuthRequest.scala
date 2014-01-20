package helper

import play.api.mvc._
import scala.concurrent.{ExecutionContext, Future}
import ExecutionContext.Implicits.global
import models.{MongoId, Identity, Token}
import traits.ResultHelper
import services.Authentication.UserClass
import services.Authentication

/**
 * User: Björn Reimer
 * Date: 11/5/13
 * Time: 5:57 PM
 */
class AuthRequest[A](request: Request[A], identity: Identity) extends WrappedRequest[A](request)

object AuthAction extends ActionBuilder[AuthRequest] with ResultHelper {

  val REQUEST_TOKEN = "token"
  val REQUEST_TOKEN_MISSING = "no token"
  val REQUEST_ACCESS_DENIED = "not allowed"
  val EMPTY_USER = ""

  def invokeBlock[A](request: Request[A], block: (AuthRequest[A]) => Future[SimpleResult]) = {
    // check if a token is passed
    request.getQueryString(REQUEST_TOKEN) match {
      case None => Future.successful(Results.Unauthorized(REQUEST_TOKEN_MISSING))
      case Some(tokenId) => {
        Token.find(new MongoId(tokenId)).flatMap {
          case None => Future.successful(Results.Unauthorized(REQUEST_ACCESS_DENIED))
          case Some(token) => {
            Identity.find(token.identityId).flatMap {
              case Some(identity) => block(new AuthRequest[A](request,  identity))
            }
          }
        }
      }
    }
  }
}
