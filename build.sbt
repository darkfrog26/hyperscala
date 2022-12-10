name := "youi"
ThisBuild / organization := "io.youi"
ThisBuild / version := "1.0.0-SNAPSHOT"
ThisBuild / scalaVersion := "2.13.10"
ThisBuild / crossScalaVersions := List("2.13.10")
ThisBuild / scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")

ThisBuild / publishTo := sonatypePublishToBundle.value
ThisBuild / sonatypeProfileName := "io.youi"
ThisBuild / licenses := Seq("MIT" -> url("https://github.com/outr/youi/blob/master/LICENSE"))
ThisBuild / sonatypeProjectHosting := Some(xerial.sbt.Sonatype.GitHubHosting("outr", "youi", "matt@outr.com"))
ThisBuild / homepage := Some(url("https://github.com/outr/youi"))
ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/outr/youi"),
    "scm:git@github.com:outr/youi.git"
  )
)
ThisBuild / developers := List(
  Developer(id="darkfrog", name="Matt Hicks", email="matt@matthicks.com", url=url("https://matthicks.com"))
)

ThisBuild / versionScheme := Some("semver-spec")

val spiceVersion: String = "0.0.7-SNAPSHOT"
val fabricVersion: String = "1.8.3"
val profigVersion: String = "3.4.6"
val scribeVersion: String = "3.10.5"
val reactifyVersion: String = "4.0.8"
val hasherVersion: String = "1.2.2"
val openTypeVersion: String = "1.1.0"
val webFontLoaderVersion: String = "1.6.28_2"
val canvgVersion: String = "1.4.0_3"
val scalaJSDOMVersion: String = "2.3.0"
val closureCompilerVersion: String = "v20220803"
val catsVersion: String = "3.3.14"
val fs2Version: String = "3.2.12"
val scalaTestVersion: String = "3.2.13"
val catsEffectTestVersion: String = "1.4.0"

ThisBuild / evictionErrorLevel := Level.Info

lazy val root = project.in(file("."))
  .aggregate(
    coreJS, coreJVM, spatialJS, spatialJVM, dom, gui, capacitor, optimizer
  )
  .settings(
    publish := {},
    publishLocal := {}
  )

lazy val core = crossProject(JSPlatform, JVMPlatform).in(file("core"))
  .settings(
    name := "youi-core",
    description := "Core functionality leveraged and shared by most other sub-projects of YouI.",
    libraryDependencies ++= Seq(
      "com.outr" %%% "spice-core" % spiceVersion,
      "org.typelevel" %%% "fabric-io" % fabricVersion,
      "com.outr" %%% "profig" % profigVersion,
      "com.outr" %%% "scribe" % scribeVersion,
      "com.outr" %%% "reactify" % reactifyVersion,
      "org.typelevel" %%% "cats-effect" % catsVersion,
      "co.fs2" %% "fs2-core" % fs2Version,
      "org.scalatest" %%% "scalatest" % scalaTestVersion % Test,
      "org.typelevel" %%% "cats-effect-testing-scalatest" % catsEffectTestVersion % Test
    )
  )
  .jsSettings(
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % scalaJSDOMVersion
    )
  )

lazy val coreJS = core.js
lazy val coreJVM = core.jvm

lazy val spatial = crossProject(JSPlatform, JVMPlatform).in(file("spatial"))
  .settings(
    name := "youi-spatial",
    libraryDependencies ++= Seq(
      "org.scalatest" %%% "scalatest" % scalaTestVersion % Test,
      "org.typelevel" %%% "cats-effect-testing-scalatest" % catsEffectTestVersion % Test
    )
  )
  .dependsOn(core)

lazy val spatialJS = spatial.js
lazy val spatialJVM = spatial.jvm

lazy val dom = project.in(file("dom"))
  .enablePlugins(ScalaJSPlugin)
  .settings(
    name := "youi-dom",
    libraryDependencies ++= Seq(
      "com.outr" %%% "profig" % profigVersion,
      "com.outr" %% "spice-delta" % spiceVersion,
      "org.scalatest" %%% "scalatest" % scalaTestVersion % Test,
      "org.typelevel" %%% "cats-effect-testing-scalatest" % catsEffectTestVersion % Test
    ),
    test := {},     // TODO: figure out why this no longer works
    jsEnv := new org.scalajs.jsenv.jsdomnodejs.JSDOMNodeJSEnv()
  )
  .dependsOn(coreJS)

lazy val gui = project.in(file("gui"))
  .enablePlugins(ScalaJSPlugin)
  .settings(
    name := "youi-gui",
    libraryDependencies ++= Seq(
      "com.outr" %%% "webfontloader-scala-js" % webFontLoaderVersion,
      "com.outr" %%% "opentype-scala-js" % openTypeVersion,
      "com.outr" %%% "canvg-scala-js" % canvgVersion
    )
  )
  .dependsOn(dom, spatialJS)

lazy val capacitor = project.in(file("capacitor"))
  .enablePlugins(ScalaJSPlugin)
  .settings(
    name := "youi-capacitor"
  )

lazy val optimizer = project.in(file("optimizer"))
  .settings(
    name := "youi-optimizer",
    description := "Provides optimization functionality for application development.",
    fork := true,
    libraryDependencies ++= Seq(
      "com.google.javascript" % "closure-compiler" % closureCompilerVersion,
      "com.outr" %% "scribe" % scribeVersion,
      "com.outr" %% "spice-delta" % spiceVersion,
      "com.outr" %% "hasher" % hasherVersion
    )
  )

/*
lazy val example = crossApplication.in(file("example"))
  .settings(
    name := "youi-example",
    youiVersion := version.value,
    libraryDependencies += "com.outr" %%% "scribe" % scribeVersion,
    jsEnv := new org.scalajs.jsenv.jsdomnodejs.JSDOMNodeJSEnv()
  )

lazy val exampleJS = example.js.dependsOn(gui)
lazy val exampleJVM = example.jvm

lazy val utilities = project.in(file("utilities"))
  .settings(
    name := "youi-utilities",
    fork := true,
    libraryDependencies ++= Seq(
      "org.jsoup" % "jsoup" % jSoupVersion
    )
  )
  .dependsOn(coreJVM)
*/
