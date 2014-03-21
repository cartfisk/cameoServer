package helper

import models.MongoId

/**
 * User: Björn Reimer
 * Date: 5/22/13
 * Time: 3:08 PM
 */
object IdHelper {

  // Random generator
  val random = new scala.util.Random

  def randomString(n: Int): String = {
    def alphabet: String = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
    //TODO check whether random.setSeed is needed
    Stream.continually(random.nextInt(alphabet.size)).map(alphabet).take(n).mkString
  }

  def randomUpperString(n: Int): String = {
    def alphabet: String = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
    //TODO check whether random.setSeed is needed
    Stream.continually(random.nextInt(alphabet.size)).map(alphabet).take(n).mkString
  }

  val defaultLength = 20

  def generatePurl(): MongoId = {
    new MongoId(randomString(10))
  }

  def generateTwoFactorSmsKey(): MongoId = {
    new MongoId(randomUpperString(8))
  }

  def generateAccessToken(): MongoId = {
    new MongoId(randomString(50))
  }

  def generateUserKey(): String = {
    randomString(40)
  }

  def generateCameoId: String = {
    randomString(15)
  }

  def generateVerificationSecret(): MongoId = {
    new MongoId(randomString(8))
  }

  def generateMongoId(): MongoId = {
    new MongoId(randomString(defaultLength))
  }

  def generateReservationSecret(): MongoId = {
    new MongoId(randomString(defaultLength))
  }

  def generateMessageId(): MongoId = {
    new MongoId(randomString(defaultLength))
  }

  def generateConversationId(): MongoId = {
    new MongoId(randomString(defaultLength))
  }

  def generateRecipientId(): MongoId = {
    new MongoId(randomString(defaultLength))
  }

  def generateContactId(): MongoId = {
    new MongoId(randomString(defaultLength))
  }

  def generateFileId(): MongoId = {
    new MongoId(randomString(defaultLength))
  }

  def generateIdentityId(): MongoId = {
    new MongoId(randomString(defaultLength))
  }

  def generateAccountId(): MongoId = {
    new MongoId(randomString(defaultLength))
  }

  def generateChunkId: MongoId = {
    new MongoId(randomString(defaultLength))
  }

  def generatePublicKeyId: MongoId = {
    new MongoId(randomString(defaultLength))
  }

}
