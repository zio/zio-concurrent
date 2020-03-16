import BuildHelper._

inThisBuild(
  List(
    organization := "dev.zio",
    homepage := Some(url("https://zio.github.io/zio-concurrent/")),
    licenses := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
    developers := List(
      Developer(
        "dkarlinsky",
        "Dmitry Karlinsky",
        "dkarlinsky@gmail.com",
        url("https://github.com/dkarlinsky")
      ),
      Developer(
        "natansil",
        "Natan Silnitsky",
        "natan.sil@gmail.com",
        url("https://github.com/natansil")
      )
    ),
    pgpPassphrase := sys.env.get("PGP_PASSWORD").map(_.toArray),
    pgpPublicRing := file("/tmp/public.asc"),
    pgpSecretRing := file("/tmp/secret.asc"),
    scmInfo := Some(
      ScmInfo(url("https://github.com/zio/zio-concurrent/"), "scm:git:git@github.com:zio/zio-concurrent.git")
    )
  )
)

addCommandAlias("fmt", "all scalafmtSbt scalafmt test:scalafmt")
addCommandAlias("check", "all scalafmtSbtCheck scalafmtCheck test:scalafmtCheck")

val zioVersion = "1.0.0-RC18-2"
libraryDependencies ++= Seq(
  "dev.zio" %% "zio"          % zioVersion,
  "dev.zio" %% "zio-test"     % zioVersion % "test",
  "dev.zio" %% "zio-test-sbt" % zioVersion % "test"
)

testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework"))

lazy val root =
  (project in file("."))
    .settings(
      stdSettings("zio-concurrent")
    )
    .settings(buildInfoSettings("zio.concurrent"))
    .enablePlugins(BuildInfoPlugin)

lazy val docs = project
  .in(file("zio-concurrent-docs"))
  .settings(
    skip.in(publish) := true,
    moduleName := "zio-concurrent-docs",
    scalacOptions -= "-Yno-imports",
    scalacOptions -= "-Xfatal-warnings",
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % zioVersion
    ),
    unidocProjectFilter in (ScalaUnidoc, unidoc) := inProjects(root),
    target in (ScalaUnidoc, unidoc) := (baseDirectory in LocalRootProject).value / "website" / "static" / "api",
    cleanFiles += (target in (ScalaUnidoc, unidoc)).value,
    docusaurusCreateSite := docusaurusCreateSite.dependsOn(unidoc in Compile).value,
    docusaurusPublishGhpages := docusaurusPublishGhpages.dependsOn(unidoc in Compile).value
  )
  .dependsOn(root)
  .enablePlugins(MdocPlugin, DocusaurusPlugin, ScalaUnidocPlugin)
