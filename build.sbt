import play.core.PlayVersion.current
import uk.gov.hmrc.DefaultBuildSettings.{defaultSettings, integrationTestSettings}
import uk.gov.hmrc.SbtAutoBuildPlugin
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin._

lazy val scoverageSettings = {
  import scoverage.ScoverageKeys
  Seq(
    ScoverageKeys.coverageExcludedPackages := "<empty>;Reverse.*;.*AuthService.*;modgiels/.data/..*;" +
      "uk.gov.hmrc.taxhistory.auditable;uk.gov.hmrc.taxhistory.metrics;view.*;controllers.auth.*;filters.*;forms.*;config.*;" +
      ".*BuildInfo.*;prod.Routes;app.Routes;testOnlyDoNotUseInAppConf.Routes;controllers.ExampleController;controllers.testonly.TestOnlyController",
    ScoverageKeys.coverageMinimum := 70.00,
    ScoverageKeys.coverageHighlighting := true,
    parallelExecution in Test := false
  )
}

lazy val compile = Seq(
  ws,
  "uk.gov.hmrc" %% "bootstrap-backend-play-28" % "7.11.0"
)

val it = "it"

lazy val test = Seq(
  "uk.gov.hmrc"            %% "bootstrap-test-play-28" % "7.11.0"  % Test,
  "org.scalatest"          %% "scalatest"              % "3.2.9"   % Test,
  "com.typesafe.play"      %% "play-test"              % current   % Test,
  "org.scalatestplus.play" %% "scalatestplus-play"     % "5.1.0"   % Test,
  "org.scalatestplus"      %% "mockito-1-10"           % "3.1.0.0" % Test,
  "com.vladsch.flexmark"    % "flexmark-all"           % "0.35.10" % Test
)

lazy val itTest = Seq(
  "uk.gov.hmrc"             %% "bootstrap-test-play-28" % "5.24.0"  % it,
  "org.scalatest"           %% "scalatest"              % "3.2.9"   % it,
  "com.typesafe.play"       %% "play-test"              % current   % it,
  "org.scalatestplus.play"  %% "scalatestplus-play"     % "5.1.0"   % it,
  "com.github.tomakehurst"  %  "wiremock-standalone"    % "2.27.1"  % it,
  "com.vladsch.flexmark"    % "flexmark-all"            % "0.35.10" % it
)

lazy val appName: String = "nrs-retrieval"

val silencerVersion = "1.7.1"

lazy val root = (project in file("."))
  .settings(
    name := appName,
    organization := "uk.gov.hmrc",
    PlayKeys.playDefaultPort := 9391,
    majorVersion := 0,
    scalaVersion := "2.12.12",
    resolvers ++= Seq(
      Resolver.typesafeRepo("releases")
    ),
    libraryDependencies ++=  compile ++ test ++ itTest,
    libraryDependencies ++= Seq(
      compilerPlugin("com.github.ghik" % "silencer-plugin" % silencerVersion cross CrossVersion.full),
      "com.github.ghik" % "silencer-lib" % silencerVersion % Provided cross CrossVersion.full
    ),
    scalacOptions += "-P:silencer:pathFilters=target/.*",
    publishingSettings,
    scoverageSettings)
  .settings(defaultSettings(): _*)
  .settings(integrationTestSettings())
  .configs(IntegrationTest)
  .settings(
    Keys.fork in IntegrationTest := false,
    Defaults.itSettings,
    unmanagedSourceDirectories in IntegrationTest += baseDirectory(_ / "it").value,
    parallelExecution in IntegrationTest := false
  )
  .enablePlugins(PlayScala, SbtAutoBuildPlugin, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin)
