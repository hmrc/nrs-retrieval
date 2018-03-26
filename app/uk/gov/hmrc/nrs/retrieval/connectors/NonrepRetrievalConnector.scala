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

import javax.inject.{Inject, Singleton}

import play.api.{Environment, Logger}
import play.api.Mode.Mode
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.nrs.retrieval.config.{AppConfig, WSHttpT}
import uk.gov.hmrc.nrs.retrieval.config.WSHttpT
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._

import scala.concurrent.Future

@Singleton
class NonrepRetrievalConnector @Inject()(val environment: Environment,
                                         val httpGet: WSHttpT,
                                         implicit val appConfig: AppConfig) {

  protected def mode: Mode = environment.mode

  val searchUrl = s"${appConfig.nonrepRetrievalUrl}/submission-metadata"

  def search(queryParams: Seq[(String, String)])(implicit hc: HeaderCarrier): Future[HttpResponse] =
    httpGet.GET[HttpResponse](searchUrl, queryParams)

  def submissionPing()(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    val path = s"${appConfig.nonrepSubmissionUrl}/submission/ping"
    Logger.info(s"Sending ping request to submission API, path=$path")
    httpGet.GET[HttpResponse](path)
  }

  def retrievalPing()(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    val path = s"${appConfig.nonrepRetrievalUrl}/retrieval/ping"
    Logger.info(s"Sending ping request to retrieval API, path=$path")
    httpGet.GET[HttpResponse](path)
  }

}
