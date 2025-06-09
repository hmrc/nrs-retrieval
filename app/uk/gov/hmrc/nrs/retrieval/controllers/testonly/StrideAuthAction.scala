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

import com.google.inject.Inject
import play.api.mvc.*
import play.api.mvc.Results.*
import uk.gov.hmrc.auth.core.AuthProvider.PrivilegedApplication
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.allEnrolments
import uk.gov.hmrc.auth.core.{AuthConnector, AuthProviders, AuthorisedFunctions}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.Singleton
import scala.concurrent.{ExecutionContext, Future}

@Singleton()
class StrideAuthAction @Inject() (override val authConnector: AuthConnector, mcc: MessagesControllerComponents)(implicit
  ec: ExecutionContext
) extends ActionBuilder[Request, AnyContent] with AuthorisedFunctions:

  override val parser: BodyParser[AnyContent]               = mcc.parsers.defaultBodyParser
  override protected val executionContext: ExecutionContext = ec

  // to do - consider whether we really support multiple stride roles, and whether to hard code or configure them.
  private val nrsRoles = Set("nrs_digital_investigator", "nrs digital investigator")

  override def invokeBlock[A](request: Request[A], block: Request[A] => Future[Result]): Future[Result] =
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequest(request)

    authorised(AuthProviders(PrivilegedApplication))
      .retrieve(allEnrolments) { enrolments =>
        if (enrolments.enrolments.map(_.key).intersect(nrsRoles).nonEmpty) {
          block(request)
        } else
          Future successful Forbidden
      }
      .recover { case _ =>
        Unauthorized
      }
