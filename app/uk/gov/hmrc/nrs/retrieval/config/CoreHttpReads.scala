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

package uk.gov.hmrc.nrs.retrieval.config

import play.api.Logger
import play.api.http.Status.NOT_FOUND
import play.api.libs.json.JsArray
import uk.gov.hmrc.http.*
import uk.gov.hmrc.http.HttpVerbs.HEAD as HEAD_VERB

trait CoreHttpReads[+O] {
  def read(method: String, url: String, response: HttpResponse): O
}

object CoreHttpReads extends HttpErrorFunctions {

  private val logger = Logger(this.getClass)

  def responseHandler(method: String, url: String, response: HttpResponse): HttpResponse = {
    response.status match {
      case status if status == NOT_FOUND =>
        logger.info(s"Submission bundle not found $status for query $method $url")
        if(method == HEAD_VERB) {
          response
        } else {
          HttpResponse(NOT_FOUND, JsArray.empty, Map[String,Seq[String]]())
        }
      case _ => handleResponseEither(method, url)(response) match {
        case Right(response) => response
        case Left(err) => throw err
      }
    }
  }

  given readRaw: CoreHttpReads[HttpResponse] = (method, url, response) => responseHandler(method, url, response)
}


