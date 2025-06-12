/*
 * Copyright 2024 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.nrs.retrieval

import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.client.{MappingBuilder, WireMock}
import com.github.tomakehurst.wiremock.matching.EqualToPattern
import play.api.libs.ws.writeableOf_String

class NrsRetrievalIntegrationSpec extends IntegrationSpec:
  private val xApiKeyHeader = "X-API-Key"
  private val xApiKey       = "xApiKey"
  private val equalsXApiKey = new EqualToPattern(xApiKey)

  override def beforeEach(): Unit = WireMock.reset()

  private def wsClientWithXApiKeyHeader(url: String) =
    wsClient.url(url).withHttpHeaders((xApiKeyHeader, xApiKey))

  private def mappingBuilder(headMapping: MappingBuilder, withHeaders: Boolean) =
    if withHeaders then headMapping.withHeader(xApiKeyHeader, equalsXApiKey)
    else headMapping

  private def givenRequestRedirects(builder: MappingBuilder, withHeaders: Boolean): Unit =
    val redirectPath = "/redirect"
    val redirectUrl  = s"$wireMockBaseUrl$redirectPath"

    wireMockServer.addStubMapping(
      mappingBuilder(get(urlPathMatching(redirectPath)), withHeaders)
        .willReturn(aResponse().withStatus(OK))
        .build()
    )

    wireMockServer.addStubMapping(
      mappingBuilder(builder, withHeaders).willReturn(aResponse().withStatus(SEE_OTHER).withHeader("Location", redirectUrl)).build()
    )

  "GET /ping/ping" should:
    "succeed" in:
      wsClient.url(s"$local/ping/ping").get().futureValue.status shouldBe OK

  "GET /nrs-retrieval/submission/ping" should:
    val url                   = s"$serviceRoot/submission/ping"
    val requestWithoutHeaders = wsClient.url(url)
    val requestWithHeader     = wsClientWithXApiKeyHeader(url)
    val retrievalPath         = "/submission/ping"

    def givenSubmissionPingReturns(status: Int, withHeaders: Boolean): Unit =
      wireMockServer.addStubMapping(
        mappingBuilder(get(urlPathMatching(retrievalPath)), withHeaders)
          .willReturn(aResponse().withStatus(status))
          .build()
      )

    Seq(
      (requestWithHeader, true, "the X-API-Key header"),
      (requestWithoutHeaders, false, "no headers")
    ).foreach { case (request, withHeaders, headersLabel) =>
      "pass through the X-API-Key header if set and return OK" when:
        Seq(OK, CREATED, ACCEPTED, NOT_FOUND).foreach { status =>
          s"the request has $headersLabel and the submission bundles request returns the status $status" in:
            givenSubmissionPingReturns(status, withHeaders)
            request.get().futureValue.status shouldBe OK
        }

        s"the request has $headersLabel and the submit request returns a 3xx status" in:
          givenRequestRedirects(get(urlPathMatching(retrievalPath)), withHeaders)
          request.get().futureValue.status shouldBe OK

      "pass through the X-API-Key header if set and return INTERNAL_SERVER_ERROR" when:
        Seq(BAD_REQUEST, UNAUTHORIZED, FORBIDDEN).foreach { status =>
          s"the request has $headersLabel and the submit request returns a 4xx status $status" in:
            givenSubmissionPingReturns(status, withHeaders)
            request.get().futureValue.status shouldBe INTERNAL_SERVER_ERROR
        }

      "pass through the X-API-Key header if set and return BAD_GATEWAY" when:
        s"the request has $headersLabel and the submit request returns a 5xx status" in:
          givenSubmissionPingReturns(INTERNAL_SERVER_ERROR, withHeaders)
          request.get().futureValue.status shouldBe BAD_GATEWAY
    }

  "GET /nrs-retrieval/retrieval/ping" should:
    val url                   = s"$serviceRoot/retrieval/ping"
    val requestWithoutHeaders = wsClient.url(url)
    val requestWithHeader     = wsClientWithXApiKeyHeader(url)
    val retrievalPath         = "/retrieval/ping"

    def givenRetrievalPingReturns(status: Int, withHeaders: Boolean): Unit =
      wireMockServer.addStubMapping(
        mappingBuilder(get(urlPathMatching(retrievalPath)), withHeaders)
          .willReturn(aResponse().withStatus(status))
          .build()
      )

    Seq(
      (requestWithHeader, true, "the X-API-Key header"),
      (requestWithoutHeaders, false, "no headers")
    ).foreach { case (request, withHeaders, headersLabel) =>
      "pass through the X-API-Key header if set and return OK" when:
        Seq(OK, CREATED, ACCEPTED, NOT_FOUND).foreach { status =>
          s"the request has $headersLabel and the submission bundles request returns the status $status" in:
            givenRetrievalPingReturns(status, withHeaders)
            request.get().futureValue.status shouldBe OK
        }

        s"the request has $headersLabel and the submit request returns a 3xx status" in:
          givenRequestRedirects(get(urlPathMatching(retrievalPath)), withHeaders)
          request.get().futureValue.status shouldBe OK

      "pass through the X-API-Key header if set and return INTERNAL_SERVER_ERROR" when:
        Seq(BAD_REQUEST, UNAUTHORIZED, FORBIDDEN).foreach { status =>
          s"the request has $headersLabel and the submit request returns a 4xx status $status" in:
            givenRetrievalPingReturns(status, withHeaders)
            request.get().futureValue.status shouldBe INTERNAL_SERVER_ERROR
        }

      "pass through the X-API-Key header if set and return BAD_GATEWAY" when:
        s"the request has $headersLabel and the submit request returns a 5xx status" in:
          givenRetrievalPingReturns(INTERNAL_SERVER_ERROR, withHeaders)
          request.get().futureValue.status shouldBe BAD_GATEWAY
    }

  "GET /nrs-retrieval/submission-metadata" should:
    val url                   = s"$serviceRoot/submission-metadata"
    val requestWithoutHeaders = wsClient.url(url)
    val requestWithHeader     = wsClientWithXApiKeyHeader(url)
    val retrievalPath         = "/retrieval/submission-metadata"

    def givenSubmissionMetaDataReturns(status: Int, withHeaders: Boolean): Unit =
      wireMockServer.addStubMapping(
        mappingBuilder(get(urlPathMatching(retrievalPath)), withHeaders)
          .willReturn(aResponse().withStatus(status))
          .build()
      )

    Seq(
      (requestWithHeader, true, "the X-API-Key header"),
      (requestWithoutHeaders, false, "no headers")
    ).foreach { case (request, withHeaders, headersLabel) =>
      "pass through the X-API-Key header if set and return OK" when:
        Seq(OK, CREATED, ACCEPTED, NOT_FOUND).foreach { status =>
          s"the request has $headersLabel and the submission bundles request returns the status $status" in:
            givenSubmissionMetaDataReturns(status, withHeaders)
            request.get().futureValue.status shouldBe OK
        }

        s"the request has $headersLabel and the submit request returns a 3xx status" in:
          givenRequestRedirects(get(urlPathMatching(retrievalPath)), withHeaders)
          request.get().futureValue.status shouldBe OK

      "pass through the X-API-Key header if set and return INTERNAL_SERVER_ERROR" when:
        Seq(BAD_REQUEST, UNAUTHORIZED, FORBIDDEN).foreach { status =>
          s"the request has $headersLabel and the submit request returns a 4xx status $status" in:
            givenSubmissionMetaDataReturns(status, withHeaders)
            request.get().futureValue.status shouldBe INTERNAL_SERVER_ERROR
        }

      "pass through the X-API-Key header if set and return BAD_GATEWAY" when:
        s"the request has $headersLabel and the submit request returns a 5xx status" in:
          givenSubmissionMetaDataReturns(INTERNAL_SERVER_ERROR, withHeaders)
          request.get().futureValue.status shouldBe BAD_GATEWAY
    }

  "GET /nrs-retrieval/submission-bundles/:vaultId/:archiveId" should:
    val url                   = s"$serviceRoot/submission-bundles/vaultId/archiveId"
    val requestWithoutHeaders = wsClient.url(url)
    val requestWithHeader     = wsClientWithXApiKeyHeader(url)
    val retrievalPath         = "/retrieval/submission-bundles/vaultId/archiveId"

    def givenSubmissionBundlesReturns(status: Int, withHeaders: Boolean, body: String = "some bytes"): Unit =
      wireMockServer.addStubMapping(
        mappingBuilder(get(urlPathMatching(retrievalPath)), withHeaders)
          .willReturn(aResponse().withStatus(status).withBody(body))
          .build()
      )

    Seq(
      (requestWithHeader, true, "the X-API-Key header"),
      (requestWithoutHeaders, false, "no headers")
    ).foreach { case (request, withHeaders, headersLabel) =>
      "pass through the X-API-Key header if set and return OK" when:
        Seq(OK, CREATED, ACCEPTED).foreach { status =>
          s"the request has $headersLabel and the submission bundles request returns the status $status" in:
            givenSubmissionBundlesReturns(status, withHeaders)
            request.get().futureValue.status shouldBe OK
            verify(1, getRequestedFor(urlEqualTo(retrievalPath)))
        }

        s"the request has $headersLabel and the submit request returns a 3xx status" in:
          givenRequestRedirects(get(urlPathMatching(retrievalPath)), withHeaders)
          request.get().futureValue.status shouldBe OK
          verify(1, getRequestedFor(urlEqualTo(retrievalPath)))

      "pass through the X-API-Key header if set and return NOT_FOUND" when:
        s"the request has $headersLabel and the submission bundles request returns NOT_FOUND" in:
          givenSubmissionBundlesReturns(NOT_FOUND, withHeaders)
          request.get().futureValue.status shouldBe NOT_FOUND
          verify(1, getRequestedFor(urlEqualTo(retrievalPath)))

      "pass through the X-API-Key header if set and return INTERNAL_SERVER_ERROR" when:
        Seq(BAD_REQUEST, UNAUTHORIZED, FORBIDDEN).foreach { status =>
          s"the request has $headersLabel and the submission bundles request returns the status $status" in:
            givenSubmissionBundlesReturns(status, withHeaders)
            request.get().futureValue.status shouldBe INTERNAL_SERVER_ERROR
            verify(1, getRequestedFor(urlEqualTo(retrievalPath)))
        }

      "pass through the X-API-Key header if set and return BAD_GATEWAY" when:
        Seq(INTERNAL_SERVER_ERROR, BAD_GATEWAY).foreach { status =>
          s"the request has $headersLabel and the submission bundles request returns the status $status" in:
            givenSubmissionBundlesReturns(status, withHeaders)
            request.get().futureValue.status shouldBe BAD_GATEWAY
            verify(1, getRequestedFor(urlEqualTo(retrievalPath)))
        }
    }

  "HEAD /nrs-retrieval/submission-bundles/:vaultId/:archiveId" should:
    val url                   = s"$serviceRoot/submission-bundles/vaultId/archiveId"
    val requestWithoutHeaders = wsClient.url(url)
    val requestWithHeader     = wsClientWithXApiKeyHeader(url)
    val retrievalPath         = "/retrieval/submission-bundles/vaultId/archiveId"

    def givenSubmissionBundlesReturns(status: Int, withHeaders: Boolean): Unit =
      wireMockServer.addStubMapping(
        mappingBuilder(head(urlPathMatching(retrievalPath)), withHeaders)
          .willReturn(aResponse().withStatus(status))
          .build()
      )

    Seq(
      (requestWithHeader, true, "the X-API-Key header"),
      (requestWithoutHeaders, false, "no headers")
    ).foreach { case (request, withHeaders, headersLabel) =>
      "pass through the X-API-Key header if set and return OK" when:
        s"the request has $headersLabel and the submission bundles request returns OK" in:
          givenSubmissionBundlesReturns(OK, withHeaders)
          request.head().futureValue.status shouldBe OK

        s"the request has $headersLabel and the submission bundles request returns a 2xx status" in:
          givenSubmissionBundlesReturns(CREATED, withHeaders)
          request.head().futureValue.status shouldBe OK

        s"the request has $headersLabel and the submit request returns a 3xx status" in:
          givenRequestRedirects(get(urlPathMatching(retrievalPath)), withHeaders)
          request.get().futureValue.status shouldBe OK

      "pass through the X-API-Key header if set and return ACCEPTED" when:
        s"the request has $headersLabel and the submission bundles request returns ACCEPTED" in:
          givenSubmissionBundlesReturns(ACCEPTED, withHeaders)
          request.head().futureValue.status shouldBe ACCEPTED

      "pass through the X-API-Key header if set and return NOT_FOUND" when:
        s"the request has $headersLabel and the submit request returns NOT_FOUND" in:
          givenSubmissionBundlesReturns(NOT_FOUND, withHeaders)
          request.head().futureValue.status shouldBe NOT_FOUND

      "pass through the X-API-Key header if set and return INTERNAL_SERVER_ERROR" when:
        Seq(BAD_REQUEST, UNAUTHORIZED, FORBIDDEN).foreach { status =>
          s"the request has $headersLabel and the submit request returns a 4xx status $status" in:
            givenSubmissionBundlesReturns(status, withHeaders)
            request.head().futureValue.status shouldBe INTERNAL_SERVER_ERROR
        }

      "pass through the X-API-Key header if set and return BAD_GATEWAY" when:
        s"the request has $headersLabel and the submit request returns a 5xx status" in:
          givenSubmissionBundlesReturns(INTERNAL_SERVER_ERROR, withHeaders)
          request.head().futureValue.status shouldBe BAD_GATEWAY
    }

  "POST /nrs-retrieval/submission-bundles/:vaultId/:archiveId/retrieval-requests" should:
    val requestBody           = ""
    val url                   = s"$serviceRoot/submission-bundles/vaultId/archiveId/retrieval-requests"
    val requestWithoutHeaders = wsClient.url(url)
    val requestWithHeader     = wsClientWithXApiKeyHeader(url)
    val retrievalPath         = "/retrieval/submission-bundles/vaultId/archiveId/retrieval-requests"

    def givenSubmitRetrievalRequestReturns(status: Int, withHeaders: Boolean): Unit =
      wireMockServer.addStubMapping(
        mappingBuilder(post(urlPathMatching(retrievalPath)), withHeaders)
          .willReturn(aResponse().withStatus(status))
          .build()
      )

    Seq(
      (requestWithHeader, true, "the X-API-Key header"),
      (requestWithoutHeaders, false, "no headers")
    ).foreach { case (request, withHeaders, headersLabel) =>
      "pass through the X-API-Key header if set and return OK " when:
        s"the request has $headersLabel and the submit request returns OK" in:
          givenSubmitRetrievalRequestReturns(OK, withHeaders)
          request.post(requestBody).futureValue.status shouldBe OK

        s"the request has $headersLabel and the submit request returns a 2xx status" in:
          givenSubmitRetrievalRequestReturns(CREATED, withHeaders)
          request.post(requestBody).futureValue.status shouldBe OK

        s"the request has $headersLabel and the submit request returns a 3xx status" in:
          givenRequestRedirects(post(urlPathMatching(retrievalPath)), withHeaders)
          request.post(requestBody).futureValue.status shouldBe OK

      "pass through the X-API-Key header if set and return ACCEPTED" when:
        s"the request has $headersLabel and the submit request returns ACCEPTED" in:
          givenSubmitRetrievalRequestReturns(ACCEPTED, withHeaders)
          request.post(requestBody).futureValue.status shouldBe ACCEPTED

      "pass through the X-API-Key header if set and return NOT_FOUND" when:
        s"the request has $headersLabel and the submit request returns NOT_FOUND" in:
          givenSubmitRetrievalRequestReturns(NOT_FOUND, withHeaders)
          request.post(requestBody).futureValue.status shouldBe NOT_FOUND

      "pass through the X-API-Key header if set and return INTERNAL_SERVER_ERROR" when:
        Seq(BAD_REQUEST, UNAUTHORIZED, FORBIDDEN).foreach { status =>
          s"the request has $headersLabel and the submit request returns a 4xx status $status" in:
            givenSubmitRetrievalRequestReturns(status, withHeaders)
            request.post(requestBody).futureValue.status shouldBe INTERNAL_SERVER_ERROR
        }

      "pass through the X-API-Key header if set and return BAD_GATEWAY" when:
        s"the request has $headersLabel and the submit request returns a 5xx status" in:
          givenSubmitRetrievalRequestReturns(INTERNAL_SERVER_ERROR, withHeaders)
          request.post(requestBody).futureValue.status shouldBe BAD_GATEWAY
    }

  "GET /nrs-retrieval/test-only/check-authorisation" should:
    "return NOT_FOUND" when:
      "test-only endpoints are disabled" in:
        wsClient.url(s"$serviceRoot/test-only/check-authorisation").get().futureValue.status shouldBe NOT_FOUND
