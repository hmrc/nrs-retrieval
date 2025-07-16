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

import com.google.inject.{AbstractModule, Guice, Injector}
import org.apache.pekko.util.ByteString
import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito.when
import play.api.Environment
import play.api.libs.ws.WSClient
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.nrs.retrieval.UnitSpec
import uk.gov.hmrc.nrs.retrieval.config.AppConfig

import scala.concurrent.ExecutionContext.Implicits.global
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.client.RequestBuilder
import uk.gov.hmrc.http.HttpResponse

import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.ws.DefaultBodyWritables.writeableOf_String
import play.api.libs.ws.DefaultBodyWritables.writeableOf_Bytes

class NonrepRetrievalConnectorSpec extends UnitSpec:
  private val mockWsHttp                     = mock[HttpClientV2]
  private val mockEnvironment                = mock[Environment]
  private val mockAppConfig                  = mock[AppConfig]
  private val mockWSClient                   = mock[WSClient]
  private val mockHttpResponse               = mock[HttpResponse]
  private val requestBuilder: RequestBuilder = mock[RequestBuilder]

  implicit val mockHeaderCarrier: HeaderCarrier = mock[HeaderCarrier]

  private val testModule = new AbstractModule:
    override def configure(): Unit =
      bind(classOf[HttpClientV2]).toInstance(mockWsHttp)
      bind(classOf[Environment]).toInstance(mockEnvironment)
      bind(classOf[AppConfig]).toInstance(mockAppConfig)
      bind(classOf[WSClient]).toInstance(mockWSClient)
      bind(classOf[ExecutionContext]).toInstance(scala.concurrent.ExecutionContext.Implicits.global)

  private val testVaultId   = "1"
  private val testArchiveId = "2"

  private val injector: Injector                  = Guice.createInjector(testModule)
  private val connector: NonrepRetrievalConnector = injector.getInstance(classOf[NonrepRetrievalConnector])

  when(mockAppConfig.nonrepRetrievalUrl).thenReturn("http://localhost")

  "search" should {
    "make a call to /submission-metadata" in {
      val httpResponseBody: String = "someResponse"
      when(mockHttpResponse.body).thenReturn(httpResponseBody)
      when(mockHttpResponse.status).thenReturn(OK)
      when(mockHttpResponse.headers).thenReturn(Map("headerOne" -> Seq("valOne", "valTwo")))

      when(mockWsHttp.get(any())(any())).thenReturn(requestBuilder)
      when(requestBuilder.transform(any())).thenReturn(requestBuilder)
      when(requestBuilder.execute(any(), any())).thenReturn(Future.successful(HttpResponse(OK, "")))

      connector.search(Seq(("someParameter", "someValue"))).map { response =>
        response.body shouldBe httpResponseBody
      }
    }
  }

  "submitRetrievalRequest" should {
    "make a call to /submission-bundles/:vaultId/:archiveId/retrieval-requests" in {
      val httpResponseBody: String = "someResponse"
      when(mockHttpResponse.body).thenReturn(httpResponseBody)
      when(mockHttpResponse.status).thenReturn(ACCEPTED)
      when(mockHttpResponse.headers).thenReturn(Map("headerOne" -> Seq("valOne", "valTwo")))
      when(mockWsHttp.post(any())(any())).thenReturn(requestBuilder)
      when(requestBuilder.withBody("")).thenReturn(requestBuilder)
      when(requestBuilder.execute(any(), any())).thenReturn(Future.successful(mockHttpResponse))

      connector.submitRetrievalRequest(testVaultId, testArchiveId).map { response =>
        response.status shouldBe 202
      }
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
      when(requestBuilder.setHeader(any())).thenReturn(requestBuilder)
      when(mockWsHttp.head(any())(any())).thenReturn(requestBuilder)
      when(requestBuilder.execute(any(), any())).thenReturn(Future.successful(HttpResponse(OK, "")))

      connector.statusSubmissionBundle(testVaultId, testArchiveId).map { response =>
        response.status shouldBe 200
      }
    }
  }

  "multiMetadataSearch" should:
    "make a call to /retrieval/metadata/searches" in {
      val httpRequestBody          = ByteString("someResponse")
      val httpResponseBody: String = "someResponse"
      when(mockHttpResponse.body).thenReturn(httpResponseBody)
      when(mockHttpResponse.status).thenReturn(ACCEPTED)
      when(mockHttpResponse.headers).thenReturn(Map("headerOne" -> Seq("valOne", "valTwo")))
      when(mockHeaderCarrier.headers(any())).thenReturn(Seq.empty)
      when(mockHeaderCarrier.extraHeaders).thenReturn(Seq.empty)
      when(mockHeaderCarrier.otherHeaders).thenReturn(Seq.empty)

      when(mockWsHttp.post(any())(any())).thenReturn(requestBuilder)
      when(requestBuilder.withBody(httpRequestBody)).thenReturn(requestBuilder)
      when(requestBuilder.setHeader(any())).thenReturn(requestBuilder)
      when(requestBuilder.execute(any(), any())).thenReturn(Future.successful(mockHttpResponse))

      connector.multiMetadataSearch(httpRequestBody)(using mockHeaderCarrier).map { response =>
        response.status shouldBe 202
      }
    }
