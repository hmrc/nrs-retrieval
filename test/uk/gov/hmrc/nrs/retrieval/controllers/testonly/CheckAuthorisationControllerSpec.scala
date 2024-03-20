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

package uk.gov.hmrc.nrs.retrieval.controllers.testonly

import play.api.test.Helpers.defaultAwaitTimeout
import play.api.test.{FakeRequest, Helpers}
import uk.gov.hmrc.nrs.retrieval.UnitSpec

class CheckAuthorisationControllerSpec extends UnitSpec with StrideAuthHelpers {
  private val request = FakeRequest("GET", "/")
  private val controller = new CheckAuthorisationController(strideAuthAction, stubControllerComponents())

  "testAuthorisation" should {
    "return OK" when {
      "the request is authenticated and authorised" in {
        givenTheRequestIsAuthenticatedAndAuthorised(nrsDigitalInvestigatorRole)
        Helpers.status(controller.checkAuthorisation(request)) shouldBe OK
      }
    }

    "return Unauthorized" when {
      "return UNAUTHORIZED" in {
        givenTheRequestIsUnauthenticated()
        Helpers.status(controller.checkAuthorisation(request)) shouldBe UNAUTHORIZED
      }
    }

    "return Forbidden" when {
      "return FORBIDDEN" in {
        givenTheRequestIsAuthenticatedButUnauthorised()
        Helpers.status(controller.checkAuthorisation(request)) shouldBe FORBIDDEN
      }
    }
  }
}
