import org.scalajs.jsenv.jsdomnodejs.JSDOMNodeJSEnv
import sbtcrossproject.CrossPlugin.autoImport.crossProject
import sbtcrossproject.CrossType

name := "youi"
organization in ThisBuild := "io.youi"
version in ThisBuild := "0.12.14-SNAPSHOT"
scalaVersion in ThisBuild := "2.13.1"
crossScalaVersions in ThisBuild := List("2.13.1", "2.12.10")
resolvers in ThisBuild ++= Seq(
  Resolver.sonatypeRepo("releases"),
  Resolver.sonatypeRepo("snapshots")
)
scalacOptions in ThisBuild ++= Seq("-unchecked", "-deprecation", "-feature")

publishTo in ThisBuild := sonatypePublishToBundle.value
sonatypeProfileName in ThisBuild := "io.youi"
publishMavenStyle in ThisBuild := true
licenses in ThisBuild := Seq("MIT" -> url("https://github.com/outr/youi/blob/master/LICENSE"))
sonatypeProjectHosting in ThisBuild := Some(xerial.sbt.Sonatype.GitHubHosting("outr", "youi", "matt@outr.com"))
homepage in ThisBuild := Some(url("https://github.com/outr/youi"))
scmInfo in ThisBuild := Some(
  ScmInfo(
    url("https://github.com/outr/youi"),
    "scm:git@github.com:outr/youi.git"
  )
)
developers in ThisBuild := List(
  Developer(id="darkfrog", name="Matt Hicks", email="matt@matthicks.com", url=url("http://matthicks.com"))
)

val profigVersion = "2.3.8-SNAPSHOT"
val scribeVersion = "2.7.10"
val reactifyVersion = "3.0.5"
val hasherVersion = "1.2.2"

val canvgVersion = "1.4.0_2"
val openTypeVersion = "0.7.3_1"
val picaVersion = "3.0.5_1"
val webFontLoaderVersion = "1.6.28_1"

val scalaJSDOM = "0.9.8"
val okHttpVersion = "4.3.1"
val uaDetectorVersion = "2014.10"
val undertowVersion = "2.0.29.Final"
val closureCompilerVersion = "v20200112"
val jSoupVersion = "1.12.1"
val scalaXMLVersion = "1.2.0"
val collectionCompat = "2.1.3"
val scalaTestVersion = "3.1.0-SNAP13"
val scalaCheckVersion = "1.14.0"

lazy val root = project.in(file("."))
  .aggregate(
    macrosJS, macrosJVM, coreJS, coreJVM, spatialJS, spatialJVM, stream, dom, clientJS, clientJVM, communicationJS,
    communicationJVM, server, serverUndertow, uiJS, uiJVM, optimizer, appJS, appJVM, exampleJS, exampleJVM
  )
  .settings(
    publish := {},
    publishLocal := {}
  )

lazy val macros = crossProject(JSPlatform, JVMPlatform).in(file("macros"))
  .settings(
    name := "youi-macros",
    description := "Dependency for internal Macro functionality",
    libraryDependencies ++= Seq(
      "org.scala-lang" % "scala-reflect" % scalaVersion.value,
      "org.scala-lang.modules" %% "scala-collection-compat" % collectionCompat,
      "org.scalatest" %%% "scalatest" % scalaTestVersion % "test"
    )
  )
  .jsSettings(
    jsEnv := new org.scalajs.jsenv.jsdomnodejs.JSDOMNodeJSEnv,
    scalacOptions += "-P:scalajs:sjsDefinedByDefault"
  )

lazy val macrosJS = macros.js
lazy val macrosJVM = macros.jvm

lazy val core = crossProject(JSPlatform, JVMPlatform).in(file("core"))
  .settings(
    name := "youi-core",
    description := "Core functionality leveraged and shared by most other sub-projects of YouI.",
    libraryDependencies ++= Seq(
      "org.scala-lang" % "scala-reflect" % scalaVersion.value,
      "com.outr" %%% "profig" % profigVersion,
      "com.outr" %%% "scribe" % scribeVersion,
      "com.outr" %%% "reactify" % reactifyVersion,
      "org.scalatest" %%% "scalatest" % scalaTestVersion % "test"
    )
  )
  .jsSettings(
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % scalaJSDOM
    ),
    scalacOptions += "-P:scalajs:sjsDefinedByDefault"
  )
  .dependsOn(macros)

lazy val coreJS = core.js
lazy val coreJVM = core.jvm

lazy val client = crossProject(JSPlatform, JVMPlatform).in(file("client"))
  .settings(
    name := "youi-client",
    libraryDependencies ++= Seq(
      "org.scalatest" %%% "scalatest" % scalaTestVersion % "test"
    )
  )
  .jsSettings(
    scalacOptions += "-P:scalajs:sjsDefinedByDefault"
  )
  .jvmSettings(
    libraryDependencies ++= Seq(
      "com.squareup.okhttp3" % "okhttp" % okHttpVersion
    )
  )
  .dependsOn(core)

lazy val clientJS = client.js
lazy val clientJVM = client.jvm

lazy val spatial = crossProject(JSPlatform, JVMPlatform).in(file("spatial"))
  .settings(
    name := "youi-spatial",
    libraryDependencies ++= Seq(
      "org.scalatest" %%% "scalatest" % scalaTestVersion % "test",
      "org.scalacheck" %%% "scalacheck" % scalaCheckVersion % "test"
    )
  )
  .jsSettings(
    jsEnv := new JSDOMNodeJSEnv,
    scalacOptions += "-P:scalajs:sjsDefinedByDefault"
  )
  .dependsOn(core)

lazy val spatialJS = spatial.js
lazy val spatialJVM = spatial.jvm

lazy val stream = project.in(file("stream"))
  .settings(
    name := "youi-stream",
    libraryDependencies ++= Seq(
      "org.scalatest" %%% "scalatest" % scalaTestVersion % "test",
      "org.scalacheck" %%% "scalacheck" % scalaCheckVersion % "test"
    )
  )
  .dependsOn(coreJVM)

lazy val dom = project.in(file("dom"))
  .enablePlugins(ScalaJSPlugin)
  .settings(
    name := "youi-dom",
    libraryDependencies ++= Seq(
      "com.outr" %% "profig" % profigVersion,
      "org.scalatest" %%% "scalatest" % scalaTestVersion % "test"
    ),
    jsEnv := new org.scalajs.jsenv.jsdomnodejs.JSDOMNodeJSEnv,
    scalacOptions += "-P:scalajs:sjsDefinedByDefault"
  )
  .dependsOn(coreJS)
  .dependsOn(stream % "compile")

lazy val communication = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("communication"))
  .settings(
    name := "youi-communication",
    libraryDependencies ++= Seq(
      "org.scala-lang" % "scala-reflect" % scalaVersion.value,
      "org.scalatest" %%% "scalatest" % scalaTestVersion % "test"
    )
  )
  .jsSettings(
    test := {},
    scalacOptions += "-P:scalajs:sjsDefinedByDefault"
  )
  .dependsOn(core)

lazy val communicationJS = communication.js
lazy val communicationJVM = communication.jvm

lazy val server = project.in(file("server"))
  .settings(
    name := "youi-server",
    libraryDependencies ++= Seq(
      "net.sf.uadetector" % "uadetector-resources" % uaDetectorVersion,
      "org.scalatest" %% "scalatest" % scalaTestVersion % "test"
    )
  )
  .dependsOn(communicationJVM, stream)

lazy val serverUndertow = project.in(file("serverUndertow"))
  .settings(
    name := "youi-server-undertow",
    fork := true,
    libraryDependencies ++= Seq(
      "io.undertow" % "undertow-core" % undertowVersion,
      "org.scalatest" %% "scalatest" % scalaTestVersion % "test"
    )
  )
  .dependsOn(server, clientJVM % "test->test")

lazy val ui = crossProject(JSPlatform, JVMPlatform).in(file("ui"))
  .settings(
    name := "youi-ui"
  )
  .jsSettings(
    libraryDependencies ++= Seq(
      "com.outr" %%% "webfontloader-scala-js" % webFontLoaderVersion,
      "com.outr" %%% "canvg-scala-js" % canvgVersion,
      "com.outr" %%% "opentype-scala-js" % openTypeVersion,
      "com.outr" %%% "pica-scala-js" % picaVersion
    ),
    jsEnv := new org.scalajs.jsenv.jsdomnodejs.JSDOMNodeJSEnv,
    scalacOptions += "-P:scalajs:sjsDefinedByDefault"
  )
  .dependsOn(spatial)

lazy val uiJS = ui.js.dependsOn(dom)
lazy val uiJVM = ui.jvm

lazy val optimizer = project.in(file("optimizer"))
  .settings(
    name := "youi-optimizer",
    description := "Provides optimization functionality for application development.",
    fork := true,
    libraryDependencies ++= Seq(
      "com.google.javascript" % "closure-compiler" % closureCompilerVersion,
      "com.outr" %% "scribe" % scribeVersion,
      "com.outr" %% "hasher" % hasherVersion
    )
  )
  .dependsOn(stream)

lazy val app = crossProject(JSPlatform, JVMPlatform).in(file("app"))
  .settings(
    name := "youi-app",
    libraryDependencies ++= Seq(
      "org.scalatest" %%% "scalatest" % scalaTestVersion % "test"
    )
  )
  .jsSettings(
    jsEnv := new org.scalajs.jsenv.jsdomnodejs.JSDOMNodeJSEnv,
    scalacOptions += "-P:scalajs:sjsDefinedByDefault"
  )
  .dependsOn(core, communication, ui)

lazy val appJS = app.js
lazy val appJVM = app.jvm.dependsOn(server)

lazy val example = crossApplication.in(file("example"))
  .settings(
    name := "youi-example",
    youiVersion := version.value
  )
  .jsSettings(
    jsEnv := new org.scalajs.jsenv.jsdomnodejs.JSDOMNodeJSEnv,
    scalacOptions += "-P:scalajs:sjsDefinedByDefault"
  )
  .jvmSettings(
    scalaJSUseMainModuleInitializer := true,
    libraryDependencies ++= Seq(
      "org.scala-lang" % "scala-reflect" % scalaVersion.value,
      "org.scala-lang.modules" %% "scala-xml" % scalaXMLVersion
    )
  )

lazy val exampleJS = example.js.dependsOn(appJS)
lazy val exampleJVM = example.jvm.dependsOn(serverUndertow, appJVM)

lazy val utilities = project.in(file("utilities"))
  .settings(
    name := "youi-utilities",
    fork := true,
    libraryDependencies ++= Seq(
      "org.jsoup" % "jsoup" % jSoupVersion
    )
  )
