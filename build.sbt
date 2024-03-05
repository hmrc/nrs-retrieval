import uk.gov.hmrc.DefaultBuildSettings
import play.core.PlayVersion.current

lazy val scoverageSettings = {
  import scoverage.ScoverageKeys
  Seq(
    ScoverageKeys.coverageExcludedPackages := "<empty>;Reverse.*;.*AuthService.*;modgiels/.data/..*;" +
      "uk.gov.hmrc.taxhistory.auditable;uk.gov.hmrc.taxhistory.metrics;view.*;controllers.auth.*;filters.*;forms.*;config.*;" +
      ".*BuildInfo.*;prod.Routes;app.Routes;testOnlyDoNotUseInAppConf.Routes;controllers.ExampleController;controllers.testonly.TestOnlyController",
    ScoverageKeys.coverageMinimumStmtTotal := 70.00,
    ScoverageKeys.coverageHighlighting := true,
    Test / parallelExecution := false
  )
}

val bootstrapPlayVersion = "8.4.0"
val currentScalaVersion = "2.13.12"
lazy val appDependencies: Seq[ModuleID] = compile ++ test()
lazy val appDependenciesIt: Seq[ModuleID] = it()
lazy val appName: String = "nrs-retrieval"

lazy val compile = Seq(
  ws,
  "uk.gov.hmrc" %% "bootstrap-backend-play-30" % bootstrapPlayVersion
)
def test(scope: String = "test"): Seq[ModuleID] = Seq(
  "uk.gov.hmrc" %% "bootstrap-test-play-30" % bootstrapPlayVersion % scope,
  "org.scalatest" %% "scalatest" % "3.2.9" % scope,
  "org.playframework" %% "play-test" % current % scope,
  "org.scalatestplus.play" %% "scalatestplus-play" % "7.0.0" % scope,
  "org.scalatestplus" %% "mockito-1-10" % "3.1.0.0" % scope,
  "com.vladsch.flexmark" % "flexmark-all" % "0.35.10" % scope
)
def it(scope: String = "test"): Seq[ModuleID] = Seq(
  "uk.gov.hmrc" %% "bootstrap-test-play-30" % bootstrapPlayVersion % scope,
  "org.playframework" %% "play-test" % current % scope,
  "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % scope,
  "com.vladsch.flexmark" % "flexmark-all" % "0.35.10" % scope
)

lazy val root = (project in file("."))
  .settings(
    name := appName,
    organization := "uk.gov.hmrc",
    PlayKeys.playDefaultPort := 9391,
    majorVersion := 0,
    scalaVersion := currentScalaVersion,
    scalacOptions += "-Wconf:cat=deprecation:warning-verbose",
    scalacOptions += "-Wconf:cat=unused-imports&src=routes/.*:s",
    scalacOptions += "-Wconf:src=routes/.*:s",
    resolvers ++= Seq(
      Resolver.typesafeRepo("releases")
    ),
    libraryDependencies ++= appDependencies,
    scoverageSettings)
  .enablePlugins(PlayScala, SbtAutoBuildPlugin, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin)

lazy val it = project
  .dependsOn(root % "test->test")
  .settings(DefaultBuildSettings.itSettings())
  .enablePlugins(play.sbt.PlayScala)
  .settings(scalaVersion := currentScalaVersion)
  .settings(majorVersion := 1)
  .settings(
    libraryDependencies ++= appDependenciesIt
  )