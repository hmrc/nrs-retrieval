/*
 * Copyright 2023 HM Revenue & Customs
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

import play.api.mvc.Results.Ok
import play.api.mvc.{Request, Result}
import play.api.test.Helpers.defaultAwaitTimeout
import play.api.test.{FakeRequest, Helpers}
import uk.gov.hmrc.nrs.retrieval.UnitSpec

import scala.concurrent.Future

class StrideAuthActionSpec extends UnitSpec with StrideAuthHelpers {
  private val request = FakeRequest("GET", "/")
  private val testBlock: Request[_] => Future[Result] = _ => Future successful Ok

  "invokeBlock" should {
    "perform the action" when {
      nrsRoles.foreach{ role =>
        s"the request is authenticated and authorised with role $role" in {
          givenTheRequestIsAuthenticatedAndAuthorised(role)
          Helpers.status(strideAuthAction.invokeBlock(request, testBlock)) shouldBe OK
        }
      }
    }

    "return Unauthorized" when {
      "the request is not authenticated" in {
        givenTheRequestIsUnauthenticated()
        Helpers.status(strideAuthAction.invokeBlock(request, testBlock)) shouldBe UNAUTHORIZED
      }
    }

    "return Forbidden" when {
      "the request is authenticated but not authorised" in {
        givenTheRequestIsAuthenticatedButUnauthorised()
        Helpers.status(strideAuthAction.invokeBlock(request, testBlock)) shouldBe FORBIDDEN
      }
    }
  }
}
