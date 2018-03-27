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

import javax.inject.Singleton

import com.google.inject.Inject
import uk.gov.hmrc.play.bootstrap.controller.BaseController
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._
import play.api.mvc._
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.nrs.retrieval.connectors.NonrepRetrievalConnector

import scala.concurrent.Future

@Singleton()
class NonrepRetrievalController @Inject()(val nonrepRetrievalConnector: NonrepRetrievalConnector) extends BaseController {

  // responds with a 200 and whatever response we got from the connector call
  def search() = Action.async { implicit request =>
    nonrepRetrievalConnector.search(mapToSeq(request.queryString)).map(response => Ok(response.body))
  }

  def submitRetrievalRequest(vaultId: String, archiveId: String) = Action.async { implicit request =>
    nonrepRetrievalConnector.submitRetrievalRequest(vaultId.toLong, archiveId.toLong).map(response => rewriteResponse(response))
  }

  def statusSubmissionBundle(vaultId: String, archiveId: String) = Action.async { implicit request =>
    nonrepRetrievalConnector.statusSubmissionBundle(vaultId.toLong, archiveId.toLong).map(response => rewriteResponse(response))
  }

  def getSubmissionBundle(vaultId: String, archiveId: String) = Action.async { implicit request =>
    nonrepRetrievalConnector.getSubmissionBundle(vaultId.toLong, archiveId.toLong).map(response => rewriteResponse(response))
  }

  private def rewriteResponse (response: HttpResponse) = {
    val headers: Seq[(String, String)] = mapToSeq(response.allHeaders)

    response.status match {
      case 200 => Ok(response.body).withHeaders(headers:_*)
      case 404 => NotFound(response.body)
      case _ => Ok(response.body)
    }

  }

  private def mapToSeq(sourceMap: Map[String, Seq[String]]): Seq[(String, String)] =
    sourceMap.keys.flatMap(k => sourceMap(k).map(v => (k, v))).toSeq

}