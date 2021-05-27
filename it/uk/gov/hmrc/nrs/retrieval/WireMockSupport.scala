package uk.gov.hmrc.nrs.retrieval

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, Suite}
import play.api.http.Status

trait WireMockSupport extends BeforeAndAfterAll with BeforeAndAfter with Status { self: Suite =>
  val wireMockHost = "localhost"
  val wireMockPort = 11111
  val wireMockBaseUrl = s"http://$wireMockHost:$wireMockPort"

  lazy val wireMockServer = new WireMockServer(wireMockConfig().port(wireMockPort))

  configureFor(wireMockHost, wireMockPort)

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    wireMockServer.start()
  }

  override protected def afterAll(): Unit = {
    super.afterAll()
    wireMockServer.stop()
  }
}

