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

import play.api.Logger
import play.api.libs.ws.DefaultBodyWritables.writeableOf_String
import uk.gov.hmrc.http.HeaderNames.explicitlyIncludedHeaders
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, HttpResponse, StringContextOps}
import uk.gov.hmrc.nrs.retrieval.config.AppConfig
import uk.gov.hmrc.nrs.retrieval.config.CoreHttpReads.responseHandler

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class NonrepRetrievalConnector @Inject() (val httpClientV2: HttpClientV2)(using
  val appConfig: AppConfig,
  ec: ExecutionContext
):

  private val logger = Logger(this.getClass)

  /** import uk.gov.hmrc.http.HttpReads.Implicits._ could be used instead, but here we have an additional log entry for 404 status
    */
  implicit val readRaw: HttpReads[HttpResponse] = responseHandler(_, _, _)

  def search(queryParams: Seq[(String, String)])(using hc: HeaderCarrier): Future[HttpResponse] =
    val path = s"${appConfig.nonrepRetrievalUrl}/retrieval/submission-metadata"
    logger.info(s"Get $path with $queryParams")
    //   http.GET[HttpResponse](path, queryParams)

    httpClientV2
      .get(url"$path")
      .transform(_.withQueryStringParameters(queryParams: _*))
      .execute[HttpResponse]

  def submitRetrievalRequest(vaultId: String, archiveId: String)(using hc: HeaderCarrier): Future[HttpResponse] =
    val path = s"${appConfig.nonrepRetrievalUrl}/retrieval/submission-bundles/$vaultId/$archiveId/retrieval-requests"
    logger.info(s"Post $path")
    // http.POST(path, "", Seq.empty)

    httpClientV2
      .post(url"$path")
      .withBody("")
      .execute[HttpResponse]

  def statusSubmissionBundle(vaultId: String, archiveId: String)(using hc: HeaderCarrier): Future[HttpResponse] =
    val path = s"${appConfig.nonrepRetrievalUrl}/retrieval/submission-bundles/$vaultId/$archiveId"
    logger.info(s"Head $path")
    //   http.HEAD(path, allHeaders)

    httpClientV2
      .head(url"$path")
      .setHeader(allHeaders: _*)
      .execute[HttpResponse]

  def getSubmissionBundle(vaultId: String, archiveId: String)(using hc: HeaderCarrier): Future[HttpResponse] =
    val path = s"${appConfig.nonrepRetrievalUrl}/retrieval/submission-bundles/$vaultId/$archiveId"
    logger.info(s"Get $path")
    //  ws.url(path).withHttpHeaders(allHeaders: _*).get()

    httpClientV2
      .get(url"$path")
      .setHeader(allHeaders: _*)
      .execute[HttpResponse]

  def submissionPing()(using hc: HeaderCarrier): Future[HttpResponse] =
    val path = s"${appConfig.nonrepSubmissionPingUrl}/submission/ping"
    logger.info(s"Sending ping request to submission API, path=$path")
    // http.GET[HttpResponse](path)

    httpClientV2
      .get(url"$path")
      .execute[HttpResponse]

  def retrievalPing()(using hc: HeaderCarrier): Future[HttpResponse] =
    val path = s"${appConfig.nonrepRetrievalPingUrl}/retrieval/ping"
    logger.info(s"Sending ping request to retrieval API, path=$path")
    //  http.GET[HttpResponse](path)

    httpClientV2
      .get(url"$path")
      .execute[HttpResponse]

  private def allHeaders(using hc: HeaderCarrier) =
    hc.headers(explicitlyIncludedHeaders) ++ hc.extraHeaders ++ hc.otherHeaders
