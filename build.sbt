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

val bootstrapPlayVersion = "9.14.0"
val currentScalaVersion = "3.3.5"
lazy val appDependencies: Seq[ModuleID] = compile ++ test()
lazy val appDependenciesIt: Seq[ModuleID] = it()
lazy val appName: String = "nrs-retrieval"

lazy val compile = Seq(
  ws,
  "uk.gov.hmrc" %% "bootstrap-backend-play-30" % bootstrapPlayVersion
)
def test(scope: String = "test"): Seq[ModuleID] = Seq(
  "uk.gov.hmrc" %% "bootstrap-test-play-30" % bootstrapPlayVersion % scope,
  "org.scalatest" %% "scalatest" % "3.2.19" % scope,
  "org.playframework" %% "play-test" % current % scope,
  "org.scalatestplus" %% "mockito-5-12" % "3.2.19.0" % scope,
  "com.vladsch.flexmark" % "flexmark-all" % "0.64.8" % scope
)
def it(scope: String = "test"): Seq[ModuleID] = Seq(
  "uk.gov.hmrc" %% "bootstrap-test-play-30" % bootstrapPlayVersion % scope,
  "org.playframework" %% "play-test" % current % scope,
  "com.vladsch.flexmark" % "flexmark-all" % "0.64.8" % scope
)

lazy val root = (project in file("."))
  .settings(
    name := appName,
    organization := "uk.gov.hmrc",
    PlayKeys.playDefaultPort := 9391,
    majorVersion := 0,
    scalaVersion := currentScalaVersion,
    scalacOptions += "-Wconf:msg=unused import&src=html/.*:s",
    scalacOptions += "-Wconf:msg=Flag.*repeatedly:s",
    scalacOptions += "-Wconf:src=routes/.*:s",
    Test / unmanagedSourceDirectories += baseDirectory.value / "it" / "test",
    resolvers ++= Seq(
      Resolver.typesafeRepo("releases")
    ),
    libraryDependencies ++= appDependencies,
    scoverageSettings)
  .enablePlugins(PlayScala, SbtDistributablesPlugin)
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