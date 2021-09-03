/*
 * Copyright 2021 HM Revenue & Customs
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

package uk.gov.hmrc.nrs.retrieval.controllers

import org.mockito.Matchers._
import org.mockito.Mockito.when
import play.api.http.Status
import play.api.libs.ws.WSResponse
import play.api.test.Helpers.defaultAwaitTimeout
import play.api.test.{FakeRequest, Helpers, StubControllerComponentsFactory}
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.nrs.retrieval.UnitSpec
import uk.gov.hmrc.nrs.retrieval.connectors.NonrepRetrievalConnector

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class NonrepRetrievalControllerControllerSpec extends UnitSpec with StubControllerComponentsFactory {
  private val fakeRequest = FakeRequest("GET", "/")
  private val httpResponseBody = "someResponse"

  private val mockWSResponse = mock[WSResponse]
  private val mocKConnector = mock[NonrepRetrievalConnector]
  private val mockHttpResponse = mock[HttpResponse]

  when(mockHttpResponse.body).thenReturn(httpResponseBody)
  when(mockHttpResponse.status).thenReturn(Status.OK)
  when(mockHttpResponse.headers).thenReturn(Map.empty[String, Seq[String]])
  when(mocKConnector.search(any())(any())).thenReturn(Future.successful(mockHttpResponse))
  when(mocKConnector.submitRetrievalRequest(any(), any())(any())).thenReturn(Future.successful(mockHttpResponse))
  when(mocKConnector.getSubmissionBundle(any(), any(), any())(any())).thenReturn(Future.successful(mockWSResponse))
  when(mocKConnector.statusSubmissionBundle(any(), any())(any())).thenReturn(Future.successful(mockHttpResponse))

  private val controller = new NonrepRetrievalController(mocKConnector, stubControllerComponents())

  "search" should {
    "pass-through the search response" in {
      val result = controller.search()(fakeRequest)
      Helpers.status(result) shouldBe OK
    }
  }

  "submitRetrievalRequest" should {
    "pass-through the search response" in {
      val result = controller.submitRetrievalRequest("1", "2")(fakeRequest)
      Helpers.status(result) shouldBe OK
    }
  }

  "statusSubmissionBundle" should {
    "pass-through the search response" in {
      val result = controller.statusSubmissionBundle("1", "2")(fakeRequest)
      Helpers.status(result) shouldBe OK
    }
  }

  "headerCarrier" should {
    val header = "X-API-Key" -> "aValidKey"

    "create a header carrier with an X-API-Key header if one exists in the request" in {
      controller.headerCarrier(fakeRequest.withHeaders(header)).extraHeaders should contain(header)
    }

    "create an empty header carrier if no X-API-Key header exists in the request" in {
      controller.headerCarrier(fakeRequest).extraHeaders.contains(header) shouldBe false
    }
  }
}
