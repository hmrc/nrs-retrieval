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

import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito.when
import org.mockito.internal.stubbing.answers.Returns
import org.mockito.stubbing.OngoingStubbing
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.StubControllerComponentsFactory
import uk.gov.hmrc.auth.core.retrieve.EmptyRetrieval
import uk.gov.hmrc.auth.core.{AuthConnector, Enrolment, Enrolments}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait StrideAuthHelpers extends MockitoSugar with StubControllerComponentsFactory:
  val authConnector: AuthConnector       = mock[AuthConnector]
  val strideAuthAction: StrideAuthAction = new StrideAuthAction(authConnector, stubMessagesControllerComponents())

  val nrsDigitalInvestigatorRole = "nrs_digital_investigator"
  val nrsRoles                   = Set(nrsDigitalInvestigatorRole, "nrs digital investigator")

  private def givenTheRequestIsAuthenticatedWithRole(role: String) =
    when(authConnector.authorise(any(), any())(any(), any())).thenAnswer(
      new Returns(Future.successful(Enrolments(Set(Enrolment(role, Seq.empty, "state")))))
    )

  def givenTheRequestIsAuthenticatedAndAuthorised(role: String): OngoingStubbing[Future[Nothing]] =
    givenTheRequestIsAuthenticatedWithRole(role)

  def givenTheRequestIsAuthenticatedButUnauthorised(): OngoingStubbing[Future[Nothing]] =
    givenTheRequestIsAuthenticatedWithRole("some_other_role")

  def givenTheRequestIsUnauthenticated(): OngoingStubbing[Future[Nothing]] =
    when(authConnector.authorise(any(), any())(any(), any())).thenAnswer(new Returns(Future.successful(EmptyRetrieval)))
