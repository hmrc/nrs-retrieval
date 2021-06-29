package uk.gov.hmrc.nrs.retrieval

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.client.{MappingBuilder, WireMock}
import com.github.tomakehurst.wiremock.matching.EqualToPattern
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.WSClient

class NrsRetrievalIntegrationSpec
  extends AnyWordSpec
    with Matchers
    with ScalaFutures
    with GuiceOneServerPerSuite
    with WireMockSupport
    with IntegrationPatience
    with BeforeAndAfterEach {
  private val xApiKeyHeader = "X-API-Key"
  private val xApiKey = "xApiKey"
  private val equalsXApiKey = new EqualToPattern(xApiKey)

  private lazy val local = s"http://localhost:$port"
  private lazy val serviceRoot = s"$local/nrs-retrieval"

  /*
   * Use WSClient rather than HttpClient here because:
   * 1. we have implemented our own HttpClient.HEAD (with opinionated behaviour) and we don't want to mix tests of
   *    this with tests of service endpoint behaviour.
   * 2. when we update http-verbs the tests are not subtly changed.
   */
  private lazy val wsClient = fakeApplication().injector.instanceOf[WSClient]

  override def fakeApplication(): Application =
    new GuiceApplicationBuilder().configure(Map[String, Any](
      "awsservices.nonrepSubmissionPing.url" -> wireMockBaseUrl,
      "awsservices.nonrepRetrievalPing.url" -> wireMockBaseUrl,
      "awsservices.nonrepRetrieval.url" -> wireMockBaseUrl,
      "auditing.enabled" -> false,
      "metrics.jvm" -> false)
    ).build()

  override def beforeEach(): Unit = WireMock.reset()

  private def wsClientWithXApiKeyHeader(url: String) =
    wsClient.url(url).withHttpHeaders((xApiKeyHeader, xApiKey))

  private def mappingBuilder(headMapping: MappingBuilder, withHeaders: Boolean) =
    if (withHeaders) {
      headMapping.withHeader(xApiKeyHeader, equalsXApiKey)
    } else {
      headMapping
    }

  private def givenRequestRedirects(builder: MappingBuilder, withHeaders: Boolean): Unit = {
    val redirectPath = "/redirect"
    val redirectUrl = s"$wireMockBaseUrl$redirectPath"

    wireMockServer.addStubMapping(
      mappingBuilder(get(urlPathMatching(redirectPath)), withHeaders).willReturn(aResponse().withStatus(OK))
        .build())

    wireMockServer.addStubMapping(
      mappingBuilder( builder, withHeaders).willReturn(aResponse().withStatus(SEE_OTHER).withHeader("Location", redirectUrl )).build())
  }


  "GET /ping/ping" should {
    "succeed" in {
      wsClient.url(s"$local/ping/ping").get().futureValue.status shouldBe OK
    }
  }

  "GET /nrs-retrieval/submission/ping" should {
    val url = s"$serviceRoot/submission/ping"
    val requestWithoutHeaders = wsClient.url(url)
    val requestWithHeader = wsClientWithXApiKeyHeader(url)
    val retrievalPath = "/submission/ping"

    def givenSubmissionPingReturns(status: Int, withHeaders: Boolean): Unit =
      wireMockServer.addStubMapping(
        mappingBuilder(get(urlPathMatching(retrievalPath)), withHeaders)
          .willReturn(aResponse().withStatus(status)).build())

    Seq(
      (requestWithHeader, true, "the X-API-Key header"),
      (requestWithoutHeaders, false, "no headers")
    ).foreach { case (request, withHeaders, headersLabel) =>
      "pass through the X-API-Key header if set and return OK" when {
        Seq(OK, CREATED, ACCEPTED, NOT_FOUND).foreach { status =>
          s"the request has $headersLabel and the submission bundles request returns the status $status" in {
            givenSubmissionPingReturns(status, withHeaders)
            request.get().futureValue.status shouldBe OK
          }
        }

        s"the request has $headersLabel and the submit request returns a 3xx status" in {
          givenRequestRedirects(get(urlPathMatching(retrievalPath)), withHeaders)
          request.get().futureValue.status shouldBe OK
        }
      }

      "pass through the X-API-Key header if set and return INTERNAL_SERVER_ERROR" when {
        Seq(BAD_REQUEST, UNAUTHORIZED, FORBIDDEN).foreach { status =>
          s"the request has $headersLabel and the submit request returns a 4xx status $status" in {
            givenSubmissionPingReturns(status, withHeaders)
            request.get().futureValue.status shouldBe INTERNAL_SERVER_ERROR
          }
        }
      }

      "pass through the X-API-Key header if set and return BAD_GATEWAY" when {
        s"the request has $headersLabel and the submit request returns a 5xx status" in {
          givenSubmissionPingReturns(INTERNAL_SERVER_ERROR, withHeaders)
          request.get().futureValue.status shouldBe BAD_GATEWAY
        }
      }
    }
  }

  "GET /nrs-retrieval/retrieval/ping" should {
    val url = s"$serviceRoot/retrieval/ping"
    val requestWithoutHeaders = wsClient.url(url)
    val requestWithHeader = wsClientWithXApiKeyHeader(url)
    val retrievalPath = "/retrieval/ping"

    def givenRetrievalPingReturns(status: Int, withHeaders: Boolean): Unit =
      wireMockServer.addStubMapping(
        mappingBuilder(get(urlPathMatching(retrievalPath)), withHeaders)
          .willReturn(aResponse().withStatus(status)).build())

    Seq(
      (requestWithHeader, true, "the X-API-Key header"),
      (requestWithoutHeaders, false, "no headers")
    ).foreach { case (request, withHeaders, headersLabel) =>
      "pass through the X-API-Key header if set and return OK" when {
        Seq(OK, CREATED, ACCEPTED, NOT_FOUND).foreach { status =>
          s"the request has $headersLabel and the submission bundles request returns the status $status" in {
            givenRetrievalPingReturns(status, withHeaders)
            request.get().futureValue.status shouldBe OK
          }
        }

        s"the request has $headersLabel and the submit request returns a 3xx status" in {
          givenRequestRedirects(get(urlPathMatching(retrievalPath)), withHeaders)
          request.get().futureValue.status shouldBe OK
        }
      }

      "pass through the X-API-Key header if set and return INTERNAL_SERVER_ERROR" when {
        Seq(BAD_REQUEST, UNAUTHORIZED, FORBIDDEN).foreach { status =>
          s"the request has $headersLabel and the submit request returns a 4xx status $status" in {
            givenRetrievalPingReturns(status, withHeaders)
            request.get().futureValue.status shouldBe INTERNAL_SERVER_ERROR
          }
        }
      }

      "pass through the X-API-Key header if set and return BAD_GATEWAY" when {
        s"the request has $headersLabel and the submit request returns a 5xx status" in {
          givenRetrievalPingReturns(INTERNAL_SERVER_ERROR, withHeaders)
          request.get().futureValue.status shouldBe BAD_GATEWAY
        }
      }
    }

  }

  "GET /nrs-retrieval/submission-metadata" should {
    val url = s"$serviceRoot/submission-metadata"
    val requestWithoutHeaders = wsClient.url(url)
    val requestWithHeader = wsClientWithXApiKeyHeader(url)
    val retrievalPath = "/retrieval/submission-metadata"

    def givenSubmissionMetaDataReturns(status: Int, withHeaders: Boolean): Unit =
      wireMockServer.addStubMapping(
        mappingBuilder(get(urlPathMatching(retrievalPath)), withHeaders)
          .willReturn(aResponse().withStatus(status)).build())

    Seq(
      (requestWithHeader, true, "the X-API-Key header"),
      (requestWithoutHeaders, false, "no headers")
    ).foreach { case (request, withHeaders, headersLabel) =>
      "pass through the X-API-Key header if set and return OK" when {
        Seq(OK, CREATED, ACCEPTED, NOT_FOUND).foreach { status =>
          s"the request has $headersLabel and the submission bundles request returns the status $status" in {
            givenSubmissionMetaDataReturns(status, withHeaders)
            request.get().futureValue.status shouldBe OK
          }
        }

        s"the request has $headersLabel and the submit request returns a 3xx status" in {
          givenRequestRedirects(get(urlPathMatching(retrievalPath)), withHeaders)
          request.get().futureValue.status shouldBe OK
        }
      }

      "pass through the X-API-Key header if set and return INTERNAL_SERVER_ERROR" when {
        Seq(BAD_REQUEST, UNAUTHORIZED, FORBIDDEN).foreach { status =>
          s"the request has $headersLabel and the submit request returns a 4xx status $status" in {
            givenSubmissionMetaDataReturns(status, withHeaders)
            request.get().futureValue.status shouldBe INTERNAL_SERVER_ERROR
          }
        }
      }

      "pass through the X-API-Key header if set and return BAD_GATEWAY" when {
        s"the request has $headersLabel and the submit request returns a 5xx status" in {
          givenSubmissionMetaDataReturns(INTERNAL_SERVER_ERROR, withHeaders)
          request.get().futureValue.status shouldBe BAD_GATEWAY
        }
      }
    }
  }

  "GET /nrs-retrieval/submission-bundles/:vaultId/:archiveId" should {
    val url = s"$serviceRoot/submission-bundles/vaultId/archiveId"
    val requestWithoutHeaders = wsClient.url(url)
    val requestWithHeader = wsClientWithXApiKeyHeader(url)
    val retrievalPath = "/retrieval/submission-bundles/vaultId/archiveId"

    def givenSubmissionBundlesReturns(status: Int, withHeaders: Boolean, body: String = "some bytes"): Unit =
      wireMockServer.addStubMapping(
        mappingBuilder(get(urlPathMatching(retrievalPath)), withHeaders)
          .willReturn(aResponse().withStatus(status).withBody(body)).build())

    Seq(
      (requestWithHeader, true, "the X-API-Key header"),
      (requestWithoutHeaders, false, "no headers")
    ).foreach { case (request, withHeaders, headersLabel) =>
      "pass through the X-API-Key header if set and return OK" when {
        Seq(OK, CREATED, ACCEPTED).foreach { status =>
          s"the request has $headersLabel and the submission bundles request returns the status $status" in {
            givenSubmissionBundlesReturns(status, withHeaders)
            request.get().futureValue.status shouldBe OK
            verify(1, getRequestedFor(urlEqualTo(retrievalPath)))
          }
        }

        s"the request has $headersLabel and the submit request returns a 3xx status" in {
          givenRequestRedirects(get(urlPathMatching(retrievalPath)), withHeaders)
          request.get().futureValue.status shouldBe OK
          verify(1, getRequestedFor(urlEqualTo(retrievalPath)))
        }
      }

      "pass through the X-API-Key header if set and return NOT_FOUND" when {
        s"the request has $headersLabel and the submission bundles request returns NOT_FOUND" in {
          givenSubmissionBundlesReturns(NOT_FOUND, withHeaders)
          request.get().futureValue.status shouldBe NOT_FOUND
          verify(1, getRequestedFor(urlEqualTo(retrievalPath)))
        }
      }

      "pass through the X-API-Key header if set and return INTERNAL_SERVER_ERROR" when {
        Seq(BAD_REQUEST, UNAUTHORIZED, FORBIDDEN).foreach { status =>
          s"the request has $headersLabel and the submission bundles request returns the status $status" in {
            givenSubmissionBundlesReturns(status, withHeaders)
            request.get().futureValue.status shouldBe INTERNAL_SERVER_ERROR
            verify(1, getRequestedFor(urlEqualTo(retrievalPath)))
          }
        }
      }

      "pass through the X-API-Key header if set and return BAD_GATEWAY" when {
        Seq(INTERNAL_SERVER_ERROR, BAD_GATEWAY).foreach { status =>
          s"the request has $headersLabel and the submission bundles request returns the status $status" in {
            givenSubmissionBundlesReturns(status, withHeaders)
            request.get().futureValue.status shouldBe BAD_GATEWAY
            verify(1, getRequestedFor(urlEqualTo(retrievalPath)))
          }
        }
      }
    }
  }

  "HEAD /nrs-retrieval/submission-bundles/:vaultId/:archiveId" should {
    val url = s"$serviceRoot/submission-bundles/vaultId/archiveId"
    val requestWithoutHeaders = wsClient.url(url)
    val requestWithHeader = wsClientWithXApiKeyHeader(url)
    val retrievalPath = "/retrieval/submission-bundles/vaultId/archiveId"

    def givenSubmissionBundlesReturns(status: Int, withHeaders: Boolean): Unit =
      wireMockServer.addStubMapping(
        mappingBuilder(head(urlPathMatching(retrievalPath)), withHeaders)
          .willReturn(aResponse().withStatus(status)).build())

    Seq(
      (requestWithHeader, true, "the X-API-Key header"),
      (requestWithoutHeaders, false, "no headers")
    ).foreach{ case (request, withHeaders, headersLabel) =>
      "pass through the X-API-Key header if set and return OK" when {
        s"the request has $headersLabel and the submission bundles request returns OK" in {
          givenSubmissionBundlesReturns(OK, withHeaders)
          request.head().futureValue.status shouldBe OK
        }

       s"the request has $headersLabel and the submission bundles request returns a 2xx status" in {
          givenSubmissionBundlesReturns(CREATED, withHeaders)
          request.head().futureValue.status shouldBe OK
        }

        s"the request has $headersLabel and the submit request returns a 3xx status" in {
          givenRequestRedirects(head(urlPathMatching(retrievalPath)), withHeaders)
          request.head().futureValue.status shouldBe OK
        }
      }

      "pass through the X-API-Key header if set and return ACCEPTED" when {
        s"the request has $headersLabel and the submission bundles request returns ACCEPTED" in {
          givenSubmissionBundlesReturns(ACCEPTED, withHeaders)
          request.head().futureValue.status shouldBe ACCEPTED
        }
      }

      "pass through the X-API-Key header if set and return NOT_FOUND" when {
        s"the request has $headersLabel and the submit request returns NOT_FOUND" in {
          givenSubmissionBundlesReturns(NOT_FOUND, withHeaders)
          request.head().futureValue.status shouldBe NOT_FOUND
        }
      }

      "pass through the X-API-Key header if set and return INTERNAL_SERVER_ERROR" when {
        Seq(BAD_REQUEST, UNAUTHORIZED, FORBIDDEN).foreach{ status =>
          s"the request has $headersLabel and the submit request returns a 4xx status $status" in {
            givenSubmissionBundlesReturns(status, withHeaders)
            request.head().futureValue.status shouldBe INTERNAL_SERVER_ERROR
          }
        }
      }

      "pass through the X-API-Key header if set and return BAD_GATEWAY" when {
        s"the request has $headersLabel and the submit request returns a 5xx status" in {
          givenSubmissionBundlesReturns(INTERNAL_SERVER_ERROR, withHeaders)
          request.head().futureValue.status shouldBe BAD_GATEWAY
        }
      }
    }
  }

  "POST /nrs-retrieval/submission-bundles/:vaultId/:archiveId/retrieval-requests" should {
    val requestBody = ""
    val url = s"$serviceRoot/submission-bundles/vaultId/archiveId/retrieval-requests"
    val requestWithoutHeaders = wsClient.url(url)
    val requestWithHeader = wsClientWithXApiKeyHeader(url)
    val retrievalPath = "/retrieval/submission-bundles/vaultId/archiveId/retrieval-requests"

    def givenSubmitRetrievalRequestReturns(status: Int, withHeaders: Boolean): Unit =
      wireMockServer.addStubMapping(
        mappingBuilder(post(urlPathMatching(retrievalPath)), withHeaders)
          .willReturn(aResponse().withStatus(status)).build())

    Seq(
      (requestWithHeader, true, "the X-API-Key header"),
      (requestWithoutHeaders, false, "no headers")
    ).foreach { case (request, withHeaders, headersLabel) =>
      "pass through the X-API-Key header if set and return OK " when {
        s"the request has $headersLabel and the submit request returns OK" in {
          givenSubmitRetrievalRequestReturns(OK, withHeaders)
          request.post(requestBody).futureValue.status shouldBe OK
        }

        s"the request has $headersLabel and the submit request returns a 2xx status" in {
          givenSubmitRetrievalRequestReturns(CREATED, withHeaders)
          request.post(requestBody).futureValue.status shouldBe OK
        }

        s"the request has $headersLabel and the submit request returns a 3xx status" in {
          givenRequestRedirects(post(urlPathMatching(retrievalPath)), withHeaders)
          request.post(requestBody).futureValue.status shouldBe OK
        }
      }

      "pass through the X-API-Key header if set and return ACCEPTED" when {
        s"the request has $headersLabel and the submit request returns ACCEPTED" in {
          givenSubmitRetrievalRequestReturns(ACCEPTED, withHeaders)
          request.post(requestBody).futureValue.status shouldBe ACCEPTED
        }
      }

      "pass through the X-API-Key header if set and return NOT_FOUND" when {
        s"the request has $headersLabel and the submit request returns NOT_FOUND" in {
          givenSubmitRetrievalRequestReturns(NOT_FOUND, withHeaders)
          request.post(requestBody).futureValue.status shouldBe NOT_FOUND
        }
      }

      "pass through the X-API-Key header if set and return INTERNAL_SERVER_ERROR" when {
        Seq(BAD_REQUEST, UNAUTHORIZED, FORBIDDEN).foreach { status =>
          s"the request has $headersLabel and the submit request returns a 4xx status $status" in {
            givenSubmitRetrievalRequestReturns(status, withHeaders)
            request.post(requestBody).futureValue.status shouldBe INTERNAL_SERVER_ERROR
          }
        }
      }

      "pass through the X-API-Key header if set and return BAD_GATEWAY" when {
        s"the request has $headersLabel and the submit request returns a 5xx status" in {
          givenSubmitRetrievalRequestReturns(INTERNAL_SERVER_ERROR, withHeaders)
          request.post(requestBody).futureValue.status shouldBe BAD_GATEWAY
        }
      }
    }
  }
}
