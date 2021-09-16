package uk.gov.hmrc.nrs.retrieval

import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.WSClient

trait IntegrationSpec
    extends AnyWordSpec with Matchers with ScalaFutures with GuiceOneServerPerSuite with WireMockSupport with IntegrationPatience
    with BeforeAndAfterEach {
  lazy val local = s"http://localhost:$port"
  lazy val serviceRoot = s"$local/nrs-retrieval"

  /*
   * Use WSClient rather than HttpClient here because:
   * 1. we have implemented our own HttpClient.HEAD (with opinionated behaviour) and we don't want to mix tests of
   *    this with tests of service endpoint behaviour.
   * 2. when we update http-verbs the tests are not subtly changed.
   */
  lazy val wsClient: WSClient = fakeApplication().injector.instanceOf[WSClient]

  val baseConfiguration: Map[String, Any] = Map[String, Any](
    "awsservices.nonrepSubmissionPing.url" -> wireMockBaseUrl,
    "awsservices.nonrepRetrievalPing.url" -> wireMockBaseUrl,
    "awsservices.nonrepRetrieval.url" -> wireMockBaseUrl,
    "auditing.enabled" -> false,
    "metrics.jvm" -> false)

  def configuration: Map[String, Any] = baseConfiguration

  override def fakeApplication(): Application = new GuiceApplicationBuilder().configure(configuration).build()
}
