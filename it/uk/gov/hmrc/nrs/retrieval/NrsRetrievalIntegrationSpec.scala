package uk.gov.hmrc.nrs.retrieval

import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, head, post, urlPathMatching}
import com.github.tomakehurst.wiremock.client.{MappingBuilder, WireMock}
import com.github.tomakehurst.wiremock.matching.EqualToPattern
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.{BeforeAndAfterEach, Matchers, WordSpecLike}
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.WSClient

class NrsRetrievalIntegrationSpec
  extends WordSpecLike
    with Matchers
    with ScalaFutures
    with GuiceOneServerPerSuite
    with WireMockSupport
    with IntegrationPatience
    with BeforeAndAfterEach {
  private val xApiKeyHeader = "X-API-Key"
  private val xApiKey = "xApiKey"
  private val equalsXApiKey = new EqualToPattern(xApiKey)

  override lazy val port: Int = 19391

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

  "GET /ping/ping" should {
    "succeed" in {
      wsClient.url(s"$local/ping/ping").get().futureValue.status shouldBe OK
    }
  }

  "GET /nrs-retrieval/submission/ping" should {
    "succeed" in {
      wsClient.url(s"$serviceRoot/submission/ping").get().futureValue.status shouldBe OK
    }
  }

  "GET /nrs-retrieval/retrieval/ping" should {
    "succeed" in {
      wsClient.url(s"$serviceRoot/retrieval/ping").get().futureValue.status shouldBe OK
    }
  }

  "GET /nrs-retrieval/submission-metadata" should {
    "succeed" in {
      wsClient.url(s"$serviceRoot/submission-metadata").get().futureValue.status shouldBe OK
    }
  }

  "GET /nrs-retrieval/submission-bundles/:vaultId/:archiveId" should {
    "succeed" in {
      wsClient.url(s"$serviceRoot/submission-bundles/vaultId/archiveId").get().futureValue.status shouldBe OK
    }
  }

  "HEAD /nrs-retrieval/submission-bundles/:vaultId/:archiveId" should {
    val url = s"$serviceRoot/submission-bundles/vaultId/archiveId"
    val requestWithoutHeaders = wsClient.url(url)
    val requestWithHeader = wsClientWithXApiKeyHeader(url)

    def givenSubmissionBundlesReturns(status: Int, withHeaders: Boolean): Unit =
      wireMockServer.addStubMapping(
        mappingBuilder(
          head(urlPathMatching("/retrieval/submission-bundles/vaultId/archiveId")), withHeaders
        ).willReturn(aResponse().withStatus(status)).build())

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
        s"the request has $headersLabel and the submit request returns a 3xx status" in {
          givenSubmissionBundlesReturns(SEE_OTHER, withHeaders)
          request.head().futureValue.status shouldBe INTERNAL_SERVER_ERROR
        }

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

    def givenSubmitRetrievalRequestReturns(status: Int, withHeaders: Boolean): Unit =
      wireMockServer.addStubMapping(
        mappingBuilder(
          post(urlPathMatching("/retrieval/submission-bundles/vaultId/archiveId/retrieval-requests")), withHeaders
        ).willReturn(aResponse().withStatus(status)).build())

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
      }

      "pass through the X-API-Key header if set and return ACCEPTED" when {
        s"the request has $headersLabel and the submit request returns ACCEPTED" in {
          givenSubmitRetrievalRequestReturns(ACCEPTED, withHeaders)
          requestWithHeader.post(requestBody).futureValue.status shouldBe ACCEPTED
        }
      }

      "pass through the X-API-Key header if set and return NOT_FOUND" when {
        s"the request has $headersLabel and the submit request returns NOT_FOUND" in {
          givenSubmitRetrievalRequestReturns(NOT_FOUND, withHeaders)
          requestWithHeader.post(requestBody).futureValue.status shouldBe NOT_FOUND
        }
      }

      "pass through the X-API-Key header if set and return INTERNAL_SERVER_ERROR" when {
        s"the request has $headersLabel and the submit request returns a 3xx status" in {
          givenSubmitRetrievalRequestReturns(SEE_OTHER, withHeaders)
          requestWithHeader.post(requestBody).futureValue.status shouldBe INTERNAL_SERVER_ERROR
        }

        Seq(BAD_REQUEST, UNAUTHORIZED, FORBIDDEN).foreach { status =>
          s"the request has $headersLabel and the submit request returns a 4xx status $status" in {
            givenSubmitRetrievalRequestReturns(status, withHeaders)
            requestWithHeader.post(requestBody).futureValue.status shouldBe INTERNAL_SERVER_ERROR
          }
        }
      }

      "pass through the X-API-Key header if set and return BAD_GATEWAY" when {
        s"the request has $headersLabel and the submit request returns a 5xx status" in {
          givenSubmitRetrievalRequestReturns(INTERNAL_SERVER_ERROR, withHeaders)
          requestWithHeader.post(requestBody).futureValue.status shouldBe BAD_GATEWAY
        }
      }
    }
  }
}
