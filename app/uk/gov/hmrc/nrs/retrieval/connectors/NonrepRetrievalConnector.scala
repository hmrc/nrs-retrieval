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

import play.api.libs.ws.{WSClient, WSResponse}
import play.api.{Environment, Logger}
import uk.gov.hmrc.http.HeaderNames.explicitlyIncludedHeaders
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, HttpResponse}
import uk.gov.hmrc.nrs.retrieval.config.CoreHttpReads.responseHandler
import uk.gov.hmrc.nrs.retrieval.config.{AppConfig, WSHttpT}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class NonrepRetrievalConnector @Inject()(val environment: Environment,
                                         val http: WSHttpT,
                                         ws: WSClient)
                                        (implicit val appConfig: AppConfig,
                                         ec: ExecutionContext) {

  private val logger = Logger(this.getClass)

  /**
   * import uk.gov.hmrc.http.HttpReads.Implicits._ could be used instead,
   * but here we have an additional log entry for 404 status
   */
  implicit val readRaw: HttpReads[HttpResponse] = responseHandler(_, _ , _)

  def search(queryParams: Seq[(String, String)])(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    val path = s"${appConfig.nonrepRetrievalUrl}/retrieval/submission-metadata"
    logger.info(s"Get $path with $queryParams")
    http.GET[HttpResponse](path, queryParams)
  }

  def submitRetrievalRequest(vaultId: String, archiveId: String)(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    val path = s"${appConfig.nonrepRetrievalUrl}/retrieval/submission-bundles/$vaultId/$archiveId/retrieval-requests"
    logger.info(s"Post $path")
    http.POST(path, "", Seq.empty)
  }

  def statusSubmissionBundle(vaultId: String, archiveId: String)(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    val path = s"${appConfig.nonrepRetrievalUrl}/retrieval/submission-bundles/$vaultId/$archiveId"
    logger.info(s"Head $path")
    http.HEAD(path, allHeaders)
  }

  def getSubmissionBundle(vaultId: String, archiveId: String)(implicit hc: HeaderCarrier): Future[WSResponse] = {
    val path = s"${appConfig.nonrepRetrievalUrl}/retrieval/submission-bundles/$vaultId/$archiveId"
    logger.info(s"Get $path")
    ws.url(path).withHttpHeaders(allHeaders: _*).get
  }

  def submissionPing()(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    val path = s"${appConfig.nonrepSubmissionPingUrl}/submission/ping"
    logger.info(s"Sending ping request to submission API, path=$path")
    http.GET[HttpResponse](path)
  }

  def retrievalPing()(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    val path = s"${appConfig.nonrepRetrievalPingUrl}/retrieval/ping"
    logger.info(s"Sending ping request to retrieval API, path=$path")
    http.GET[HttpResponse](path)
  }

  private def allHeaders(implicit hc: HeaderCarrier) =
    hc.headers(explicitlyIncludedHeaders) ++ hc.extraHeaders ++ hc.otherHeaders
}
