package helper

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

  def generateMongoId(): String ={
    randomString(30)
  }

  def generateMessageId(): String = {
    randomString(8)
  }

  def generateConversationId(): String = {
    randomString(8)
  }

  def generateAccessToken(): String = {
    randomString(40)
  }

  def generateRecipientId(): String = {
    randomString(8)
  }

  def generateContactId(): String = {
    randomString(8)
  }

  def generateAssetId(): String = {
    randomString(8)
  }

  def generatePurl(): String = {
    randomString(10)
  }

  def generateUserKey(): String = {
    randomString(96)
  }
}
