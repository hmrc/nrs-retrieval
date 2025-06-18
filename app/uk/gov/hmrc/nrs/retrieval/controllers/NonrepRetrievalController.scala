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

package uk.gov.hmrc.nrs.retrieval.controllers

import com.google.inject.Inject
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.util.ByteString
import play.api.Logger
import play.api.mvc.*
import uk.gov.hmrc.http.{BadGatewayException, HeaderCarrier, HttpResponse, InternalServerException, NotFoundException}
import uk.gov.hmrc.nrs.retrieval.connectors.NonrepRetrievalConnector
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.Singleton
import scala.concurrent.ExecutionContext

@Singleton()
class NonrepRetrievalController @Inject() (
  val nonrepRetrievalConnector: NonrepRetrievalConnector,
  override val controllerComponents: ControllerComponents
)(using ExecutionContext)
    extends BackendController(controllerComponents):
  private val logger = Logger(this.getClass)

  def search(): Action[AnyContent] = Action.async { request =>
    given Request[AnyContent] = request
    nonrepRetrievalConnector.search(mapToSeq(request.queryString)).map(response => Ok(response.body))
  }

  def submitRetrievalRequest(vaultId: String, archiveId: String): Action[AnyContent] = Action.async { request =>
    given Request[AnyContent] = request
    nonrepRetrievalConnector.submitRetrievalRequest(vaultId, archiveId).map(response => rewriteResponse(response))
  }

  def statusSubmissionBundle(vaultId: String, archiveId: String): Action[AnyContent] = Action.async { request =>
    given Request[AnyContent] = request
    val messagePrefix         = s"head submission bundle for vaultId: [$vaultId] archiveId: [$archiveId]"

    logger.info(messagePrefix)
    debugRequest(messagePrefix, request)

    nonrepRetrievalConnector.statusSubmissionBundle(vaultId, archiveId).map { response =>
      logger.info(s"$messagePrefix received status: [$response.status] with headers [${response.headers}] from upstream.")
      rewriteResponse(response)
    }
  }
  def getSubmissionBundle(vaultId: String, archiveId: String): Action[AnyContent]    = Action.async { request =>
    given Request[AnyContent] = request
    val messagePrefix         = s"get submission bundle for vaultId: [$vaultId] archiveId: [$archiveId]"

    given ActorSystem = ActorSystem()

    logger.info(messagePrefix)
    debugRequest(messagePrefix, request)

    nonrepRetrievalConnector.getSubmissionBundle(vaultId, archiveId).flatMap { response =>
      val message = s"$messagePrefix received response status: ${response.status.toString}"

      logger.info(message)

      response.status match
        case NOT_FOUND                                 => throw new NotFoundException(message)
        case status if status >= INTERNAL_SERVER_ERROR => throw new BadGatewayException(message)
        case status if status >= MULTIPLE_CHOICES      => throw new InternalServerException(message)
        case status                                    =>
          // log response size rather than the content as this might contain sensitive information
          response.bodyAsSource.runFold(ByteString.emptyByteString)(_ ++ _).map { bytes =>
            logger.info(
              s"$messagePrefix received status: [$status] with headers [${response.headers}] and ${bytes.size} bytes from upstream."
            )
            Ok(bytes).withHeaders(mapToSeq(response.headers)*)
          }
    }
  }

  private def debugRequest(messagePrefix: String, request: Request[?]): Unit =
    logger.debug(
      s"$messagePrefix received request with " +
        s"attrs: [${request.attrs}] " +
        s"acceptedTypes: [${request.acceptedTypes}] " +
        s"acceptLanguages: [${request.acceptLanguages}] " +
        s"body: [${request.body}] " +
        s"charset: [${request.charset}] " +
        s"clientCertificateChain: [${request.clientCertificateChain}] " +
        s"contentType: [${request.contentType}] " +
        s"cookies: [${request.cookies}] " +
        s"domain: [${request.domain}] " +
        s"headers: [${request.headers}] " +
        s"host: [${request.host}] " +
        s"mediaType: [${request.mediaType}] " +
        s"method: [${request.method}] " +
        s"path: [${request.path}] " +
        s"queryString: [${request.queryString}] " +
        s"remoteAddress: [${request.remoteAddress}] " +
        s"secure: [${request.secure}] " +
        s"session: [${request.session}] " +
        s"uri: [${request.uri}] " +
        s"version: [${request.version}]"
    )

  private def rewriteResponse(response: HttpResponse) =
    val headers: Seq[(String, String)] = mapToSeq(response.headers)
    response.status match
      case OK        => Ok(response.body).withHeaders(headers*)
      case ACCEPTED  => Accepted(response.body).withHeaders(headers*)
      case NOT_FOUND => NotFound(response.body)
      case _         => Ok(response.body)

  val submissionPing: Action[AnyContent] = Action.async { request =>
    given Request[AnyContent] = request
    nonrepRetrievalConnector.submissionPing().map(response => Ok(response.body))
  }

  val retrievalPing: Action[AnyContent] = Action.async { request =>
    given Request[AnyContent] = request
    nonrepRetrievalConnector.retrievalPing().map(response => Ok(response.body))
  }

  private def mapToSeq(sourceMap: Map[String, scala.collection.Seq[String]]): Seq[(String, String)] =
    sourceMap.keys.flatMap(k => sourceMap(k).map(v => (k, v))).toSeq

  implicit def headerCarrier(implicit request: Request[AnyContent]): HeaderCarrier =
    val xApiKey = "X-API-Key"
    request.headers.get(xApiKey) match
      case Some(xApiKeyValue) => HeaderCarrier().withExtraHeaders(xApiKey -> xApiKeyValue)
      case _                  => HeaderCarrier()
