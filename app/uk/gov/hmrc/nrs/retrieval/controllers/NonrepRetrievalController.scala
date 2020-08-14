/*
 * Copyright 2020 HM Revenue & Customs
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
import play.api.mvc._
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.nrs.retrieval.connectors.NonrepRetrievalConnector
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.ExecutionContext

@Singleton()
class NonrepRetrievalController @Inject()(val nonrepRetrievalConnector: NonrepRetrievalConnector,
                                          override val controllerComponents: ControllerComponents)
                                         (implicit ec: ExecutionContext)
  extends BackendController(controllerComponents) {

   def search() = Action.async { implicit request =>
    nonrepRetrievalConnector.search(mapToSeq(request.queryString)).map(response => Ok(response.body))
  }

  def submitRetrievalRequest(vaultId: String, archiveId: String) = Action.async { implicit request =>
    nonrepRetrievalConnector.submitRetrievalRequest(vaultId, archiveId).map(response => rewriteResponse(response))
  }

  def statusSubmissionBundle(vaultId: String, archiveId: String) = Action.async { implicit request =>
    nonrepRetrievalConnector.statusSubmissionBundle(vaultId, archiveId).map(response => rewriteResponse(response))
  }

  def getSubmissionBundle(vaultId: String, archiveId: String) = Action.async { implicit request =>
    nonrepRetrievalConnector.getSubmissionBundle(vaultId, archiveId)
      .map{response => Ok(response.bodyAsBytes).withHeaders(mapToSeq(response.headers):_*)}
  }

  private def rewriteResponse (response: HttpResponse) = {
    val headers: Seq[(String, String)] = mapToSeq(response.headers)
    response.status match {
      case 200 => Ok(response.body).withHeaders(headers:_*)
      case 202 => Accepted(response.body).withHeaders(headers:_*)
      case 404 => NotFound(response.body)
      case _ => Ok(response.body)
    }
  }

  def submissionPing = Action.async { implicit request =>
    nonrepRetrievalConnector.submissionPing().map(response => Ok(response.body))
  }

  def retrievalPing = Action.async { implicit request =>
    nonrepRetrievalConnector.retrievalPing().map(response => Ok(response.body))
  }

  private def mapToSeq(sourceMap: Map[String, Seq[String]]): Seq[(String, String)] =
    sourceMap.keys.flatMap(k => sourceMap(k).map(v => (k, v))).toSeq

  implicit def headerCarrier (implicit request: Request[AnyContent]): HeaderCarrier = {
    val xApiKey = "X-API-Key"
    request.headers.get(xApiKey) match {
      case Some(xApiKeyValue) => HeaderCarrier().withExtraHeaders(xApiKey -> xApiKeyValue)
      case _ => HeaderCarrier()
    }
  }
}