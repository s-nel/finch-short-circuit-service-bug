import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.finagle.{Http, Service, http}
import com.twitter.util.{Await, Future}
import io.circe.{Encoder, Json}
import io.circe.generic.auto._
import io.finch._
import io.finch.circe._
import io.circe.syntax._
import org.scalatest.{MustMatchers, WordSpec}

import scala.util.Try

class ClientServerTest extends WordSpec with MustMatchers {
  final case class BodyObject(foo: String = "bar")

  implicit val encodeExceptionCirce: Encoder[Exception] = Encoder.instance(
    e =>
      Json.obj(
        "message" -> Option(e.getMessage).fold(Json.Null)(Json.fromString)))

  def sendPostRequest(service: Service[http.Request, http.Response]) = {
    val data = BodyObject()
    val request = http.Request(http.Method.Post, s"/foo")
    request.setContentTypeJson()
    request.setContentString(data.asJson.noSpaces)
    Await.result(service(request))
  }

  val finchService = io.finch
    .post("foo" :: jsonBody[BodyObject]) { (obj: BodyObject) =>
      println(obj)
      Ok("done".asJson)
    }
    .toService

  val finagleService = new Service[Request, Response] {
    def apply(req: Request): Future[Response] = {
      println(req.contentString)
      Future.value(Response()) // HTTP 200
    }
  }

  def withNetworkService(service: Service[Request, Response],
                         f: Service[Request, Response] => Unit) = {
    val listeningServer = Http.server.serve(":8185", service)
    val factory = Http.client.newClient("localhost:8185")
    val client = Await.result(factory())
    val result = Try(f(client))
    listeningServer.close()
    result.get
  }

  "no-network finagle client" must {
    "be able to post to finch service" in {
      val response = sendPostRequest(finchService)
      println(response.contentString)
      response.status mustBe Status.Ok
    }

    "be able to post to finagle service" in {
      val response = sendPostRequest(finagleService)
      println(response.contentString)
      response.status mustBe Status.Ok
    }
  }

  "network finagle client" must {
    "be able to post to network finch service" in {
      withNetworkService(finchService, { client =>
        val response = sendPostRequest(client)
        println(response.contentString)
        response.status mustBe Status.Ok
      })
    }

    "be able to post to network finagle service" in {
      withNetworkService(finagleService, { client =>
        val response = sendPostRequest(client)
        println(response.contentString)
        response.status mustBe Status.Ok
      })
    }
  }
}
