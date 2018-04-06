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

package uk.gov.hmrc.nrs.retrieval.controllers

import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import play.api.http.Status
import play.api.test.FakeRequest
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.nrs.retrieval.connectors.NonrepRetrievalConnector
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

class NonrepRetrievalControllerControllerSpec extends UnitSpec with WithFakeApplication with MockitoSugar {

  val fakeRequest = FakeRequest("GET", "/")

  val httpResponseBody: String = "someResponse"
  val mockHttpResponse = mock[HttpResponse]
  when(mockHttpResponse.body).thenReturn(httpResponseBody)
  when(mockHttpResponse.status).thenReturn(Status.OK)
  when(mockHttpResponse.allHeaders).thenReturn(Map.empty[String,Seq[String]])

  val mocKConnector = mock[NonrepRetrievalConnector]
  when(mocKConnector.search(any())(any())).thenReturn(Future.successful(mockHttpResponse))
  when(mocKConnector.submitRetrievalRequest(any(), any())(any())).thenReturn(Future.successful(mockHttpResponse))
  when(mocKConnector.getSubmissionBundle(any(), any())(any())).thenReturn(Future.successful(mockHttpResponse))
  when(mocKConnector.statusSubmissionBundle(any(), any())(any())).thenReturn(Future.successful(mockHttpResponse))

  val controller = new NonrepRetrievalController(mocKConnector)

  "search" should {
    "pass-through the search response" in {
      val result = controller.search()(fakeRequest)
      status(result) shouldBe Status.OK
    }
  }

  "submitRetrievalRequest" should {
    "pass-through the search response" in {
      val result = controller.submitRetrievalRequest("1", "2")(fakeRequest)
      status(result) shouldBe Status.OK
    }
  }

  "getSubmissionBundle" should {
    "pass-through the search response" in {
      val result = controller.getSubmissionBundle("1", "2")(fakeRequest)
      status(result) shouldBe Status.OK
    }
  }

  "statusSubmissionBundle" should {
    "pass-through the search response" in {
      val result = controller.statusSubmissionBundle("1", "2")(fakeRequest)
      status(result) shouldBe Status.OK
    }
  }

  "headerCarrier" should {
    "create a header carrier with an X-API-Key header in one exists in the request" in {
      val request = fakeRequest.withHeaders("X-API-Key" -> "aValidKey")
      controller.headerCarrier(request).headers should contain ("X-API-Key" -> "aValidKey")
    }
    "create an empty header carrier if no X-API-Key header exists in the request" in {
      controller.headerCarrier(fakeRequest).headers. find(_ == ("X-API-Key" -> "aValidKey")) shouldBe empty
    }
  }

}
