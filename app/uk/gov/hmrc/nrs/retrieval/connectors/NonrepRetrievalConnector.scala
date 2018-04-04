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
import uk.gov.hmrc.nrs.retrieval.config.AppConfig
import uk.gov.hmrc.nrs.retrieval.config.WSHttpT
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._

import scala.concurrent.Future

@Singleton
class NonrepRetrievalConnector @Inject()(val environment: Environment,
                                         val http: WSHttpT,
                                         implicit val appConfig: AppConfig) {

  private val logger = Logger(this.getClass)

  protected def mode: Mode = environment.mode

  def search(queryParams: Seq[(String, String)])(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    val path = s"${appConfig.nonrepRetrievalUrl}/retrieval/submission-metadata"
    logger.info(s"Get $path with $queryParams")
    http.GET[HttpResponse](path, queryParams).map {response =>
      logger.info(s"$path : ${response.status} : ${response.allHeaders} : ${response.body}")
      response
    }
  }

  def submitRetrievalRequest(vaultId: String, archiveId: String)(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    val path = s"${appConfig.nonrepRetrievalUrl}/retrieval/submission-bundles/$vaultId/$archiveId/retrieval-requests"
    logger.info(s"Post $path")
    http.doPostString(path, "", Seq.empty) map {response =>
      logger.info(s"$path : ${response.status} : ${response.allHeaders} : ${response.body}")
      response
    }
  }

  def statusSubmissionBundle(vaultId: String, archiveId: String)(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    val path = s"${appConfig.nonrepRetrievalUrl}/retrieval/submission-bundles/$vaultId/$archiveId"
    logger.info(s"Head $path")
    http.doHead(path) map {response =>
      logger.info(s"$path : ${response.status} : ${response.allHeaders} : ${response.body.toString}")
      response
    }
  }

  def getSubmissionBundle(vaultId: String, archiveId: String)(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    val path = s"${appConfig.nonrepRetrievalUrl}/retrieval/submission-bundles/$vaultId/$archiveId"
    logger.info(s"Get $path")
    http.doGet(path) map {response =>
      logger.info(s"$path : ${response.status} : ${response.allHeaders} : ${response.body.toString}")
      response
    }
  }

  def submissionPing()(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    val path = s"${appConfig.nonrepSubmissionPingUrl}/submission/ping"
    logger.info(s"Sending ping request to submission API, path=$path")
    http.GET[HttpResponse](path) map {response =>
      logger.info(s"$path : ${response.status} : ${response.allHeaders} : ${response.body.toString}")
      response
    }
  }

  def retrievalPing()(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    val path = s"${appConfig.nonrepRetrievalPingUrl}/retrieval/ping"
    logger.info(s"Sending ping request to retrieval API, path=$path")
    http.GET[HttpResponse](path) map {response =>
      logger.info(s"$path : ${response.status} : ${response.allHeaders} : ${response.body.toString}")
      response
    }
  }

}
