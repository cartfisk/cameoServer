package testHelper

import play.api.test.FakeApplication

/**
 * User: Björn Reimer
 * Date: 3/3/14
 * Time: 4:41 PM
 */

object TestConfig {

  val basePath = "/a/v1"
  val basePathV2 = "/a/v2"
  val baseCockpitPath = "/a/cockpit/v1"
  val dbName = "cameo_test"

  // valid users in the inital Data: login;password;identityId;token
  // 2VqTftqh;password;g9PWZY7xKNbeCO6LPNnx;hUbODA2qkVo2JF7YdEYVXe4NaHd82x6rvxxBxXbo
  // BMeSfHXQ;password;N2HKgBdxxnWBGxlYY7Dn;viRlhZZ1VDAhqcgrljvfzEXCwKj0B2dyAKw5suFZ

  // test user on dev.cameo.io
  // r1Zhpq8e;password;NyGAvBnLeR3mLEYdofgf;lFFkssj7gE4uTGSZlPlolp82Ozp3fWnOkQEFYO6k

  // Use the same FakeApplication for all tests, so the mongoConnection does not break
  val eventTimeout = 3
  val additionalConfig = Map("mongodb.db" -> dbName, "events.subscription.expire.period" -> eventTimeout, "accounts.properties.default.file.quota" -> 9)
  val additionalConfigWithLoggingDisabled = Map("mongodb.db" -> dbName, "logger.application" -> "ERROR", "logger.play" -> "ERROR")

  lazy val app = FakeApplication(additionalConfiguration = additionalConfig)

  val testUserPrefix = "testUser23"

  val domain = "cameonet.de"

  val cidExisting = "rQHQZHv4ARDXRmnEzJ92"
  val cidExistingNumberOfMessages = 100
  val cidExisting2 = "dLBDYFdfj9ymiTblElmN"
  val cidExisting3 = "OM9QeJ4RfJcdscyo52g4"
  val cidExisting4 = "IXhJ0BwoLjRtrRn41zIw"
  val cidExistingNonMember = "2GOdNSfdPMavyl95KUah"

  val loginExisting = "2VqTftqh"
  val loginExisting2 = "BMeSfHXQ"

  val accountExisting2Tel = "560277123"
  val accountExisting2Mail = "devnull@cameo.io"
  val accountExisting3Tel = "5602772323"
  val accountExisting4Mail = "devnull2@cameo.io"

  val password = "XohImNooBHFR0OVvjcYpJ3NgPQ1qq73WKhHvch0VQtg="

  val identityExisting = "g9PWZY7xKNbeCO6LPNnx"
  val identityExisting2 = "N2HKgBdxxnWBGxlYY7Dn"
  val identityExisting3 = "xiDoJmuua853krSS5SeZ"
  val identityExisting4 = "tfy8ZdsGVWlUI98igv2S"

  val cameoIdExisting = "KG5mSGTY8l3"
  val cameoIdExisting2 = "bwyVeVnCvuO"
  val cameoIdExisting3 = "m534n69eHW92DfDenrQo"
  val cameoIdExisting4 = "4EKozMkDyz7fvvTU3TI0"

  val displayNameExisting = "Moeper"
  val displayNameExisting2 = "Moeper2"

  val tokenExisting = "hUbODA2qkVo2JF7YdEYVXe4NaHd82x6rvxxBxXbo"
  val tokenExisting2 = "viRlhZZ1VDAhqcgrljvfzEXCwKj0B2dyAKw5suFZ"
  val tokenExisting3 = "PszpnGzJonsFCYNmeddqif0JGsQH2jI33ZNoRRxY"
  val tokenExisting4 = "2PnueJm3g6zpsIQib4YF7HNadad6zEN8vCcySELi"

  val telExisting = "+49123456789"
  val emailExisting = "test@cameo.io"

  val invalidPhoneNumbers = Seq("abcd", "+4912345123451234512345", "+!\"§$%&/()=", "asdfasdf1231233")
  val invalidEmails = Seq("a@a.d", "a@a", "a@a aa.de", "a.de", "123@345.43", "ajlk", "@asdf.de", "asasdf.ddsf@", "+!\"§$@%&/().de=")

  val purlExtern = "MSaKlj4hJP"
  val purlExtern2 = "V3Ml6hzqX9"
  val purlExtern3 = "V3Ml6hzqX6"
  val purlExternInvalid = "V3Ml6hzqX7"
  val purlExternIdentitityId = "GhEWGfy3Jqx8BRP1pITO"
  val purlExtern2IdentitityId = "B0F6gKOnoDVTI57dVyBU"
  val purlExtern3IdentitityId = "vRPeJOGSXFbQnP9qIK1W"
  val purlIntern = "V3Ml6hzqX8"
  val purlIntern2 = "u02iLiIeQu"
  val purlConversationId = "OM9QeJ4RfJcdscyo52g4"

  val internalContactId = "RJaMVpSkdhMRXc0DqnfT"
  val internalContactIdentityId = "l4ytByiHOw0iJ0LA2hpz"
  val internalContactToken = "b6sCo31N4iLvzIHg8IfYIzsm8uX46pEpegkeCxLv"
  val internalContactCameoId = "9kxZWFFbOCh0K9DZoHrv"
  val internalContact2CameoId = "RliVyZSsiG4e7pSVRuz2"
  val internalContact2IdentityId = "Q9nauLdsCOMhcmXmlL4p"
  val internalContact2Token = "RV6RmULN2APbVeCvOLNYxKe3AtP1HJRgNbNPhxyR"
  val internalContact3Id = "sfl4augFPWA1VbwiaTBy"

  val externalContactIdentityId = "XjizpBmduHdbh1XnKdgM"
  val externalContact2 = "EVFrPIr2oPpyVaUZGQjV"
  val externalContact2IdentityId = "DOZaWDTSnQE0ItFYgqEM"

  val fileIdImage = "cqlBuWN2lzYffbx6U8d0"
  val imageHeight = 1800
  val imageWidth = 2880
  val fileIdAudio = "XlDQEoyUJtKg9xhyl7uI"

  val validPhoneNumbers: Seq[(String, String)] =
    Seq(
      (" 0173-12  345678", "+4917312345678"),
      ("491234512345", "+491234512345"),
      ("(0049)1234512345", "+491234512345"),
      ("0123/4512345", "+491234512345"),
      ("0123-4512345", "+491234512345"),
      (" +17234512345         ", "+17234512345")
    )
  val validEmails: Seq[String] = Seq("a-b.c_d@a-b.c_d.co", "123@345.fo", "123@3-4-5.fo")

  val invalidLogins = Seq("as", "_._", "._." ,"asdfasdfasdfasdfasdfaasdfasdfasdfasdfasdf", ",asdf", "/asdf", "asdf#asdf", "asd£asdf", "<>", "\\", "asd@df")

  val validLogins = Seq("jalskdnals", "_asdf.", "ssd", "asdf_asdf", "asd.asdf", "_1_")
}
