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

import play.api.Configuration

import javax.inject.{Inject, Singleton}

@Singleton
class AppConfig @Inject() (val runModeConfiguration: Configuration):

  private def loadConfig(key: String) =
    runModeConfiguration.getOptional[String](key).getOrElse(throw new Exception(s"Missing configuration key: $key"))

  private val contactHost                  = runModeConfiguration.getOptional[String](s"contact-frontend.host").getOrElse("")
  private val contactFormServiceIdentifier = "MyService"

  lazy val assetsPrefix: String             = loadConfig(s"assets.url") + loadConfig(s"assets.version")
  lazy val analyticsToken: String           = loadConfig(s"google-analytics.token")
  lazy val analyticsHost: String            = loadConfig(s"google-analytics.host")
  lazy val reportAProblemPartialUrl: String = s"$contactHost/contact/problem_reports_ajax?service=$contactFormServiceIdentifier"
  lazy val reportAProblemNonJSUrl: String   = s"$contactHost/contact/problem_reports_nonjs?service=$contactFormServiceIdentifier"

  lazy val nonrepRetrievalUrl: String      = loadConfig(s"awsservices.nonrepRetrieval.url")
  lazy val nonrepRetrievalPingUrl: String  = loadConfig(s"awsservices.nonrepRetrievalPing.url")
  lazy val nonrepSubmissionPingUrl: String = loadConfig(s"awsservices.nonrepSubmissionPing.url")
