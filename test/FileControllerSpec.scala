import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO

import constants.ErrorCodes
import play.api.test._
import play.api.libs.json.{ JsValue, Json, JsObject }
import play.api.test.Helpers._
import play.api.test.FakeApplication
import testHelper.Helper._
import scala.concurrent.ExecutionContext
import play.api.{ Play, Logger, GlobalSettings }
import testHelper.{ StartedApp, Helper }
import org.specs2.mutable._
import testHelper.TestConfig._
import play.api.Play.current

class FileControllerSpec extends StartedApp {

  sequential

  "FileController" should {

    val fileName = "some_name.pdf"
    val fileType = "some_type"
    var fileId = ""

    val chunks: Seq[String] = {
      Seq.fill(10)(Helper.randomString(1024))
    }

    val newChunk = Helper.randomString(1024)
    val newChunkIndex = Helper.random.nextInt(chunks.size)

    "upload file meta data" in {
      val path = basePath + "/file"

      val header: Seq[(String, String)] = Seq(
        ("X-File-Name", fileName),
        ("X-Max-Chunks", chunks.size.toString),
        ("X-File-Size", chunks.map(_.size).sum.toString),
        ("X-File-Type", fileType)) :+
        tokenHeader(tokenExisting2)

      val req = FakeRequest(POST, path).withHeaders(header: _*).withJsonBody(Json.obj())
      val res = route(req).get

      if (status(res) != OK) {
        Logger.error("Response: " + contentAsString(res))
      }
      status(res) must equalTo(OK)

      val data = (contentAsJson(res) \ "data").as[JsObject]

      (data \ "id").asOpt[String] must beSome
      fileId = (data \ "id").as[String]
      (data \ "chunks")(0).asOpt[Int] must beNone
      (data \ "fileName").asOpt[String] must beSome(fileName)
      (data \ "maxChunks").asOpt[Int] must beSome(chunks.size)
      (data \ "fileSize").asOpt[Int] must beSome(chunks.map(_.size).sum)
      (data \ "fileType").asOpt[String] must beSome(fileType)
    }

    "upload chunks" in {
      val path = basePath + "/file/" + fileId

      chunks.zipWithIndex.map {
        case (chunk, i) =>

          val body: Array[Byte] = chunk.getBytes

          val header: Seq[(String, String)] = Seq(
            ("X-Index", i.toString)) :+
            tokenHeader(tokenExisting2)

          val req = FakeRequest(POST, path).withHeaders(header: _*).withRawBody(body)
          val res = route(req).get

          if (status(res) != OK) {
        Logger.error("Response: " + contentAsString(res))
      }
      status(res) must equalTo(OK)
      }
    }

    "detect missing chunk header" in {
      val path = basePath + "/file/" + fileId

      val body: Array[Byte] = chunks(1).getBytes

      val req = FakeRequest(POST, path).withHeaders(tokenHeader(tokenExisting2)).withRawBody(body)
      val res = route(req).get

      status(res) must equalTo(BAD_REQUEST)
    }

    "detect invalid chunk header" in {
      val path = basePath + "/file/" + fileId

      val body: Array[Byte] = chunks(1).getBytes

      val header: Seq[(String, String)] = Seq(
        ("X-Index", "moep")) :+
        tokenHeader(tokenExisting2)

      val req = FakeRequest(POST, path).withHeaders(header: _*).withRawBody(body)
      val res = route(req).get

      status(res) must equalTo(BAD_REQUEST)
    }

    "refuse upload to chunks to invalid fileId" in {
      val path = basePath + "/file/" + "asdfasdf"

      val body = chunks(1).getBytes

      val header: Seq[(String, String)] = Seq(
        ("X-Index", "2")) :+
        tokenHeader(tokenExisting2)

      val req = FakeRequest(POST, path).withHeaders(header: _*).withRawBody(body)
      val res = route(req).get

      status(res) must equalTo(NOT_FOUND)
    }

    "file should be marked as not Completed" in {

      val path = basePath + "/file/" + fileId

      val req = FakeRequest(GET, path).withHeaders(tokenHeader(tokenExisting2))
      val res = route(req).get

      if (status(res) != OK) {
        Logger.error("Response: " + contentAsString(res))
      }
      status(res) must equalTo(OK)

      val data = (contentAsJson(res) \ "data").as[JsObject]

      (data \ "isCompleted").asOpt[Boolean] must beSome(false)
    }

    "mark file upload as complete" in {
      val path = basePath + "/file/" + fileId + "/completed"

      val req = FakeRequest(POST, path).withHeaders(tokenHeader(tokenExisting2)).withJsonBody(Json.obj())
      val res = route(req).get

      if (status(res) != OK) {
        Logger.error("Response: " + contentAsString(res))
      }
      status(res) must equalTo(OK)
    }

    "file should now be marked as completed" in {

      val path = basePath + "/file/" + fileId

      val req = FakeRequest(GET, path).withHeaders(tokenHeader(tokenExisting2))
      val res = route(req).get

      if (status(res) != OK) {
        Logger.error("Response: " + contentAsString(res))
      }
      status(res) must equalTo(OK)

      val data = (contentAsJson(res) \ "data").as[JsObject]

      (data \ "isCompleted").asOpt[Boolean] must beSome(true)
    }

    "get all file meta information" in {

      val path = basePath + "/file/" + fileId

      val req = FakeRequest(GET, path).withHeaders(tokenHeader(tokenExisting2))
      val res = route(req).get

      if (status(res) != OK) {
        Logger.error("Response: " + contentAsString(res))
      }
      status(res) must equalTo(OK)

      val data = (contentAsJson(res) \ "data").as[JsObject]

      (data \ "id").asOpt[String] must beSome
      fileId = (data \ "id").as[String]
      (data \ "chunks").asOpt[Seq[Int]] must beSome
      val returnedChunks: Seq[Int] = (data \ "chunks").as[Seq[Int]].sorted
      returnedChunks.size must beEqualTo(10)
      returnedChunks.min must beEqualTo(0)
      returnedChunks.max must beEqualTo(9)
      (data \ "fileName").asOpt[String] must beSome(fileName)
      (data \ "maxChunks").asOpt[Int] must beSome(chunks.size)
      (data \ "fileSize").asOpt[Int] must beSome(chunks.map(_.size).sum)
      (data \ "fileType").asOpt[String] must beSome(fileType)
    }

    "get all chunks of a file" in {
      chunks.zipWithIndex.map {
        case (chunk, i) => {
          val path = basePath + "/file/" + fileId + "/" + i

          val req = FakeRequest(GET, path).withHeaders(tokenHeader(tokenExisting2))
          val res = route(req).get

          if (status(res) != OK) {
        Logger.error("Response: " + contentAsString(res))
      }
      status(res) must equalTo(OK)

          val raw = contentAsBytes(res)

          val string = new String(raw)

          string must beEqualTo(chunks(i))
        }
      }
    }

    "overwrite existing chunk" in {
      val path = basePath + "/file/" + fileId

      val body = newChunk.getBytes

      val header: Seq[(String, String)] = Seq(
        ("X-Index", newChunkIndex.toString)) :+
        tokenHeader(tokenExisting2)

      val req = FakeRequest(POST, path).withHeaders(header: _*).withRawBody(body)
      val res = route(req).get

      if (status(res) != OK) {
        Logger.error("Response: " + contentAsString(res))
      }
      status(res) must equalTo(OK)
    }

    "check if old chunk has been overwritten" in {

      val path = basePath + "/file/" + fileId + "/" + newChunkIndex

      val req = FakeRequest(GET, path).withHeaders(tokenHeader(tokenExisting2))
      val res = route(req).get

      if (status(res) != OK) {
        Logger.error("Response: " + contentAsString(res))
      }
      status(res) must equalTo(OK)

      val raw = contentAsBytes(res)

      raw must equalTo(raw)
    }

    "check that there is only one chunk for each index" in {
      val path = basePath + "/file/" + fileId

      val req = FakeRequest(GET, path).withHeaders(tokenHeader(tokenExisting2))
      val res = route(req).get

      if (status(res) != OK) {
        Logger.error("Response: " + contentAsString(res))
      }
      status(res) must equalTo(OK)

      val data = (contentAsJson(res) \ "data").as[JsObject]

      (data \ "chunks").asOpt[Seq[Int]] must beSome
      val returnedChunks: Seq[Int] = (data \ "chunks").as[Seq[Int]].sorted

      returnedChunks.distinct.size must beEqualTo(returnedChunks.size)

    }

    "refuse to return non existing chunk" in {
      chunks.zipWithIndex.map {
        case (chunk, i) =>
          val path = basePath + "/file/" + fileId + "/15"

          val req = FakeRequest(GET, path).withHeaders(tokenHeader(tokenExisting2))
          val res = route(req).get

          status(res) must equalTo(NOT_FOUND)
      }
    }

    "detect non-numerical chunk index" in {
      chunks.zipWithIndex.map {
        case (chunk, i) =>
          val path = basePath + "/file/" + fileId + "/d"

          val req = FakeRequest(GET, path).withHeaders(tokenHeader(tokenExisting2))
          val res = route(req).get

          status(res) must equalTo(BAD_REQUEST)
      }
    }

    "refuse to return non existing FileMeta" in {
      val path = basePath + "/file/0"

      val req = FakeRequest(GET, path).withHeaders(tokenHeader(tokenExisting2))
      val res = route(req).get

      status(res) must equalTo(NOT_FOUND)
    }

    "refuse to upload file that is larger than maximum filesize" in {
      val maxSize = Play.configuration.getInt("files.size.max").get

      val path = basePath + "/file"

      val header: Seq[(String, String)] = Seq(
        ("X-File-Name", fileName),
        ("X-Max-Chunks", chunks.size.toString),
        ("X-File-Size", (maxSize + 10).toString),
        ("X-File-Type", fileType)) :+
        tokenHeader(tokenExisting2)

      val req = FakeRequest(POST, path).withHeaders(header: _*).withJsonBody(Json.obj())
      val res = route(req).get

      status(res) must equalTo(BAD_REQUEST)

      (contentAsJson(res) \ "data" \ "maxFileSize").asOpt[Int] must beSome(maxSize)
      (contentAsJson(res) \ "errorCodes").asOpt[Seq[String]] must beSome(ErrorCodes.FILE_UPLOAD_FILESIZE_EXCEEDED)
    }

    "return error if actual size exceeds submitted size" in {
      val path = basePath + "/file"

      val header: Seq[(String, String)] = Seq(
        ("X-File-Name", fileName),
        ("X-Max-Chunks", chunks.size.toString),
        ("X-File-Size", (chunks.map(_.size).sum / 10).toString),
        ("X-File-Type", fileType)) :+
        tokenHeader(tokenExisting2)

      val req = FakeRequest(POST, path).withHeaders(header: _*).withJsonBody(Json.obj())
      val res = route(req).get

      if (status(res) != OK) {
        Logger.error("Response: " + contentAsString(res))
      }
      status(res) must equalTo(OK)

      val data = (contentAsJson(res) \ "data").as[JsObject]

      (data \ "id").asOpt[String] must beSome
      val fileId2 = (data \ "id").as[String]

      // upload chunks
      val path2 = basePath + "/file/" + fileId2

      var error: JsValue = Json.obj()

      chunks.zipWithIndex.seq.map {
        case (chunk, i) =>

          val json = Json.obj("chunk" -> chunk)

          val header: Seq[(String, String)] = Seq(
            ("X-Index", i.toString)) :+
            tokenHeader(tokenExisting2)

          val req2 = FakeRequest(POST, path2).withHeaders(header: _*).withJsonBody(json)
          val res2 = route(req2).get

          status(res2) match {
            case OK =>
            case BAD_REQUEST =>
              error = contentAsJson(res2)
            case _ =>
          }
      }

      (error \ "error").asOpt[String] must beSome(contain("actual fileSize is bigger than submitted value"))
    }

    "refuse upload of another file that exceeds file quota" in {
      val path = basePath + "/file"

      val header: Seq[(String, String)] = Seq(
        ("X-File-Name", fileName),
        ("X-Max-Chunks", "13"),
        ("X-File-Size", (10*1024*1024).toString),
        ("X-File-Type", fileType)) :+
        tokenHeader(tokenExisting2)

      val req = FakeRequest(POST, path).withHeaders(header: _*).withJsonBody(Json.obj())
      val res = route(req).get

      if (status(res) != BAD_REQUEST) {
        Logger.error("Response: " + contentAsString(res))
      }
      status(res) must equalTo(BAD_REQUEST)

      (contentAsJson(res) \ "errorCodes").asOpt[Seq[String]] must beSome(ErrorCodes.FILE_UPLOAD_QUOTA_EXCEEDED)
    }

    "return raw image file" in {
      val path = basePath + "/file/" + fileIdImage + "/raw?token=" + tokenExisting

      val req = FakeRequest(GET, path)
      val res = route(req).get

      if (status(res) != OK) {
        Logger.error("Response: " + contentAsString(res))
      }
      status(res) must equalTo(OK)

      val image: BufferedImage = ImageIO.read(new ByteArrayInputStream(contentAsBytes(res)))
      image.getHeight must beEqualTo(imageHeight)
      image.getWidth must beEqualTo(imageWidth)
    }

    "return blank image if file is not found" in {
      val path = basePath + "/file/moep/raw?token=" + tokenExisting

      val req = FakeRequest(GET, path)
      val res = route(req).get

      if (status(res) != OK) {
        Logger.error("Response: " + contentAsString(res))
      }
      status(res) must equalTo(OK)

      val image: BufferedImage = ImageIO.read(new ByteArrayInputStream(contentAsBytes(res)))
      image.getHeight must beEqualTo(1)
      image.getWidth must beEqualTo(1)
    }

    "scale image" in {
      val path = basePath + "/file/" + fileIdImage + "/scale/400?token=" + tokenExisting

      val req = FakeRequest(GET, path)
      val res = route(req).get

      if (status(res) != OK) {
        Logger.error("Response: " + contentAsString(res))
      }
      status(res) must equalTo(OK)

      val image: BufferedImage = ImageIO.read(new ByteArrayInputStream(contentAsBytes(res)))
      image.getHeight must beEqualTo(400)
      image.getWidth must beEqualTo(400)
    }

    "not scale image when contrains are larger than original" in {
      val path = basePath + "/file/" + fileIdImage + "/scale/4000?token=" + tokenExisting

      val req = FakeRequest(GET, path)
      val res = route(req).get

      if (status(res) != OK) {
        Logger.error("Response: " + contentAsString(res))
      }
      status(res) must equalTo(OK)

      val image: BufferedImage = ImageIO.read(new ByteArrayInputStream(contentAsBytes(res)))
      image.getHeight must beEqualTo(imageHeight)
      image.getWidth must beEqualTo(imageHeight)
    }

    "return blank image, when scaling invalid image" in {
      val path = basePath + "/file/" + fileIdAudio + "/scale/200?token=" + tokenExisting

      val req = FakeRequest(GET, path)
      val res = route(req).get

      if (status(res) != OK) {
        Logger.error("Response: " + contentAsString(res))
      }
      status(res) must equalTo(OK)

      val image: BufferedImage = ImageIO.read(new ByteArrayInputStream(contentAsBytes(res)))
      image.getHeight must beEqualTo(1)
      image.getWidth must beEqualTo(1)
    }

    "return blank image, when scaling a file that does not exist" in {
      val path = basePath + "/file/moep/scale/200?token=" + tokenExisting

      val req = FakeRequest(GET, path)
      val res = route(req).get

      if (status(res) != OK) {
        Logger.error("Response: " + contentAsString(res))
      }
      status(res) must equalTo(OK)

      val image: BufferedImage = ImageIO.read(new ByteArrayInputStream(contentAsBytes(res)))
      image.getHeight must beEqualTo(1)
      image.getWidth must beEqualTo(1)
    }

  }
}
