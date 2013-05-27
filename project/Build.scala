import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "TrackMe"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    // Add your project dependencies here,
    jdbc,
    anorm,
    "com.typesafe" %% "play-plugins-mailer" % "2.1.0"
  )

  val main = play.Project(appName, appVersion, appDependencies).settings(
    scalacOptions += "-feature",
    resolvers += Resolver.url("Objectify Play Repository", url("http://schaloner.github.com/releases/"))(Resolver.ivyStylePatterns),
    playAssetsDirectories <+= baseDirectory(_ / "modules" / "bootstrap"),
    templatesImport ++= Seq("controllers.RequestHelper._")
  )

}
