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

package uk.gov.hmrc.nrs.retrieval.config

import java.net.URLEncoder

import play.api.Logger
import play.api.libs.json.JsArray
import uk.gov.hmrc.http.HttpVerbs.{HEAD => HEAD_VERB}
import uk.gov.hmrc.http._
import uk.gov.hmrc.http.hooks.HttpHooks
import uk.gov.hmrc.http.logging.ConnectionTracing
import uk.gov.hmrc.play.http.ws.{WSHttpResponse, WSRequest}

import scala.concurrent.{ExecutionContext, Future}

trait HeadHttpTransport {
  def doHead(url: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse]
}

trait CoreHttpReads[+O] {
  def read(method: String, url: String, response: HttpResponse): O
}

object CoreHttpReads extends HttpErrorFunctions {

  private val logger = Logger(this.getClass)

  def responseHandler(method: String, url: String, response: HttpResponse): HttpResponse = {
    response.status match {
      case status if (status == 404) => {
        logger.info(s"Submission bundle not found $status for query $method $url")
        if(method == HEAD_VERB) {
          response
        } else {
          HttpResponse(404, JsArray.empty, Map[String,Seq[String]]())
        }
      }
      case _ => handleResponse(method, url)(response)
    }
  }

  implicit val readRaw: CoreHttpReads[HttpResponse] = (method, url, response) => responseHandler(method, url, response)
}

trait CoreHead {
  def HEAD[A](url: String)(implicit rds: CoreHttpReads[A], hc: HeaderCarrier, ec: ExecutionContext): Future[A]

  def HEAD[A](url: String, queryParams: Seq[(String, String)])(implicit rds: CoreHttpReads[A], hc: HeaderCarrier, ec: ExecutionContext): Future[A]
}

trait WSHead extends WSRequest with CoreHead with HeadHttpTransport {

  override def doHead(url: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = {
    buildRequest(url).head().map(WSHttpResponse(_))
  }

}

trait HttpHead extends CoreHead with HeadHttpTransport with HttpVerb with ConnectionTracing with HttpHooks {

  override def HEAD[A](url: String)(implicit rds: CoreHttpReads[A], hc: HeaderCarrier, ec: ExecutionContext): Future[A] =
    withTracing(HEAD_VERB, url) {

      val httpResponse = doHead(url)
      executeHooks(url, HEAD_VERB, None, httpResponse)
      mapErrors(HEAD_VERB, url, httpResponse).map(response => rds.read(HEAD_VERB, url, response))
    }

  override def HEAD[A](url: String, queryParams: Seq[(String, String)])(
    implicit rds: CoreHttpReads[A],
    hc: HeaderCarrier,
    ec: ExecutionContext): Future[A] = {
    val queryString = makeQueryString(queryParams)
    if (url.contains("?")) {
      throw new UrlValidationException(
        url,
        s"${this.getClass}.HEAD(url, queryParams)",
        "Query parameters must be provided as a Seq of tuples to this method")
    }
    HEAD(url + queryString)
  }

  private def makeQueryString(queryParams: Seq[(String, String)]) = {
    val paramPairs = queryParams.map(Function.tupled((k, v) => s"$k=${URLEncoder.encode(v, "utf-8")}"))
    val params = paramPairs.mkString("&")

    if (params.isEmpty) "" else s"?$params"
  }
}

