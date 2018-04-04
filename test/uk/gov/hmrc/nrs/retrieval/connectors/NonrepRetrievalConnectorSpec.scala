/*
 * Copyright 2018 HM Revenue & Customs
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
import org.mockito.Matchers.{any, contains}
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import play.api.Environment
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.nrs.retrieval.config.{AppConfig, WSHttpT}
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class NonrepRetrievalConnectorSpec extends UnitSpec with MockitoSugar {

  "search" should {
    "make a call to /submission-metadata" in {
      val httpResponseBody: String = "someResponse"
      when(mockHttpResponse.body).thenReturn(httpResponseBody)
      when(mockHttpResponse.status).thenReturn(200)
      when(mockHttpResponse.allHeaders).thenReturn(Map("headerOne" -> Seq("valOne", "valTwo")))
      when(mockWsHttp.GET[HttpResponse](contains("submission-metadata"), any())(any(), any(), any())).thenReturn(Future.successful(mockHttpResponse))

      connector.search(Seq(("someParameter", "someValue"))).map { response =>
        response.body shouldBe httpResponseBody
      }
    }
  }

  "submitRetrievalRequest" should {
    "make a call to /submission-bundles/:vaultId/:archiveId/retrieval-requests" in {
      val httpResponseBody: String = "someResponse"
      when(mockHttpResponse.body).thenReturn(httpResponseBody)
      when(mockHttpResponse.status).thenReturn(202)
      when(mockHttpResponse.allHeaders).thenReturn(Map("headerOne" -> Seq("valOne", "valTwo")))
      when(mockWsHttp.doPostString(contains("submission-bundles"), any(), any())(any())).thenReturn(Future.successful(mockHttpResponse))

      connector.submitRetrievalRequest(testVaultId, testArchiveId).map { response =>
        response.status shouldBe 202
      }
    }
  }

  "statusSubmissionBundle" should {
    "make a call to /submission-bundles/$vaultId/$archiveId" in {
      val httpResponseBody: String = "someResponse"
      when(mockHttpResponse.body).thenReturn(httpResponseBody)
      when(mockHttpResponse.status).thenReturn(200)
      when(mockHttpResponse.allHeaders).thenReturn(Map("headerOne" -> Seq("valOne", "valTwo")))
      when(mockWsHttp.doHead(contains("submission-bundles"))(any())).thenReturn(Future.successful(mockHttpResponse))

      connector.statusSubmissionBundle(testVaultId, testArchiveId).map { response =>
        response.status shouldBe 200
      }
    }
  }

  "getSubmissionBundle" should {
    "make a call to /submission-bundles/$vaultId/$archiveId" in {
      val httpResponseBody: String = "someResponse"
      when(mockHttpResponse.status).thenReturn(200)
      when(mockHttpResponse.body).thenReturn(httpResponseBody)
      when(mockHttpResponse.allHeaders).thenReturn(Map("headerOne" -> Seq("valOne", "valTwo")))
      when(mockWsHttp.doGet(contains("submission-bundles"))(any())).thenReturn(Future.successful(mockHttpResponse))

      connector.getSubmissionBundle(testVaultId, testArchiveId).map { response =>
        response.body shouldBe httpResponseBody
      }
    }
  }


  private val mockWsHttp = mock[WSHttpT]
  private val mockEnvironemnt = mock[Environment]
  private val mockAppConfig = mock[AppConfig]

  private val mockHttpResponse = mock[HttpResponse]

  implicit val mockHeaderCarrier: HeaderCarrier = mock[HeaderCarrier]

  private val testModule = new AbstractModule {
    override def configure(): Unit = {
      bind(classOf[WSHttpT]).toInstance(mockWsHttp)
      bind(classOf[Environment]).toInstance(mockEnvironemnt)
      bind(classOf[AppConfig]).toInstance(mockAppConfig)
    }
  }

  private val testVaultId = "1"
  private val testArchiveId = "2"

  private val injector: Injector = Guice.createInjector(testModule)
  private val connector: NonrepRetrievalConnector = injector.getInstance(classOf[NonrepRetrievalConnector])

}
