package uk.gov.hmrc.nrs.retrieval

class TestOnyEndpointsIntegrationSpec extends IntegrationSpec {
  override def configuration: Map[String, Any] = baseConfiguration + ("application.router" -> "testOnlyDoNotUseInAppConf.Routes")

  "GET /nrs-retrieval/test-only/check-authorisation" should {
    "return UNAUTHORISED" when {
      "test-only endpoints are enabled and the request is unauthenticated" in {
        wsClient.url(s"$serviceRoot/test-only/check-authorisation").get().futureValue.status shouldBe UNAUTHORIZED
      }
    }
  }
}
