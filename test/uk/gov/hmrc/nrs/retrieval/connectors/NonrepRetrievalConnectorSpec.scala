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

package uk.gov.hmrc.nrs.retrieval.connectors

import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito.when
import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.nrs.retrieval.UnitSpec

import scala.concurrent.Future

class NonrepRetrievalConnectorSpec extends UnitSpec:
  private val mockHttpClientV2                  = mock[HttpClientV2]
  private val mockHttpResponse                  = mock[HttpResponse]
  private val mockRequestBuilder                = mock[RequestBuilder]
  given mockHeaderCarrier: HeaderCarrier = mock[HeaderCarrier]

  "search" should {
    "make a call to /submission-metadata" in {
      val httpResponseBody: String = "someResponse"
      when(mockHttpResponse.body).thenReturn(httpResponseBody)
      when(mockHttpResponse.status).thenReturn(OK)
      when(mockHttpResponse.headers).thenReturn(Map("headerOne" -> Seq("valOne", "valTwo")))

      when(mockHttpClientV2.get(any())(using any[HeaderCarrier])).thenReturn(mockRequestBuilder)
      when(mockRequestBuilder.setHeader(any())).thenReturn(mockRequestBuilder)
      when(mockRequestBuilder.execute[HttpResponse](any(), any())).thenReturn(Future.successful(mockHttpResponse))
    }

    "submitRetrievalRequest" should {
      "make a call to /submission-bundles/:vaultId/:archiveId/retrieval-requests" in {
        val httpResponseBody: String = "someResponse"
        when(mockHttpResponse.body).thenReturn(httpResponseBody)
        when(mockHttpResponse.status).thenReturn(ACCEPTED)
        when(mockHttpResponse.headers).thenReturn(Map("headerOne" -> Seq("valOne", "valTwo")))

        when(mockHttpClientV2.post(any())(using any[HeaderCarrier])).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.setHeader(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(ArgumentMatchers.eq(""))(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[HttpResponse](any(), any())).thenReturn(Future.successful(mockHttpResponse))

      }
    }

    "statusSubmissionBundle" should {
      "make a call to /submission-bundles/$vaultId/$archiveId" in {
        val httpResponseBody: String = "someResponse"
        when(mockHttpResponse.body).thenReturn(httpResponseBody)
        when(mockHttpResponse.status).thenReturn(OK)
        when(mockHttpResponse.headers).thenReturn(Map("headerOne" -> Seq("valOne", "valTwo")))
        when(mockHeaderCarrier.headers(any())).thenReturn(Seq.empty)
        when(mockHeaderCarrier.extraHeaders).thenReturn(Seq.empty)
        when(mockHeaderCarrier.otherHeaders).thenReturn(Seq.empty)

        when(mockHttpClientV2.head(any())(using any[HeaderCarrier])).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.setHeader(any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.withBody(ArgumentMatchers.eq(""))(any(), any(), any())).thenReturn(mockRequestBuilder)
        when(mockRequestBuilder.execute[HttpResponse](any(), any())).thenReturn(Future.successful(mockHttpResponse))
      }
    }
  }
