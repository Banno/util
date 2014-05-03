import sbt._
import Keys._

object Util extends Build {
  val libVersion = "6.13.2"
  val zkVersion = "3.3.4"
  val zkDependency = "org.apache.zookeeper" % "zookeeper" % zkVersion excludeAll(
    ExclusionRule("com.sun.jdmk", "jmxtools"),
    ExclusionRule("com.sun.jmx", "jmxri"),
    ExclusionRule("javax.jms", "jms")
  )
  val scalatest = scalaVersion(sv => sv match {
    case "2.9.2" => "org.scalatest" %% "scalatest" % "1.9.2"
    case _       => "org.scalatest" %% "scalatest" % "2.1.3"
  })

  val sharedSettings = Seq(
    version := libVersion,
    organization := "com.twitter",
    scalaVersion := "2.10.4",
    crossScalaVersions := Seq("2.9.2", "2.10.4", "2.11.0"),
    incOptions := incOptions.value.withNameHashing(true),
    // Workaround for a scaladoc bug which causes it to choke on
    // empty classpaths.
    unmanagedClasspath in Compile += Attributed.blank(new java.io.File("doesnotexist")),
    libraryDependencies ++= Seq(
      "junit"         %  "junit"       % "4.8.1"  % "test",
      "org.mockito"   %  "mockito-all" % "1.9.5"  % "test"
    ),
    libraryDependencies <+= scalatest(_ % "test"),

    libraryDependencies := {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, scalaMajor)) if scalaMajor >= 11 =>
          libraryDependencies.value ++ Seq(
            "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.1"
          )
        case _ => libraryDependencies.value
      }
    },

    resolvers += "twitter repo" at "http://maven.twttr.com",

    scalacOptions ++= Seq("-encoding", "utf8"),
    scalacOptions += "-deprecation",

    javacOptions ++= Seq("-source", "1.6", "-target", "1.6"),
    javacOptions in doc := Seq("-source", "1.6"),

    // This is bad news for things like com.twitter.util.Time
    parallelExecution in Test := false,

    // Sonatype publishing
    publishArtifact in Test := false,
    pomIncludeRepository := { _ => false },
    publishMavenStyle := true,
    pomExtra := (
      <url>https://github.com/twitter/util</url>
      <licenses>
        <license>
          <name>Apache License, Version 2.0</name>
          <url>http://www.apache.org/licenses/LICENSE-2.0</url>
        </license>
      </licenses>
      <scm>
        <url>git@github.com:twitter/util.git</url>
        <connection>scm:git:git@github.com:twitter/util.git</connection>
      </scm>
      <developers>
        <developer>
          <id>twitter</id>
          <name>Twitter Inc.</name>
          <url>https://www.twitter.com/</url>
        </developer>
      </developers>),
    publishTo <<= version { (v: String) =>
      val nexus = "https://oss.sonatype.org/"
      if (v.trim.endsWith("SNAPSHOT"))
        Some("snapshots" at nexus + "content/repositories/snapshots")
      else
        Some("releases"  at nexus + "service/local/staging/deploy/maven2")
    }
  )

  lazy val util = Project(
    id = "util",
    base = file("."),
    settings = Project.defaultSettings ++
      sharedSettings ++
      Unidoc.settings
  ) aggregate(
    utilCore, utilEval, utilCodec, utilCollection, utilReflect,
    utilLogging, utilThrift, utilHashing, utilJvm, utilZk,
    utilZkCommon, utilClassPreloader, utilBenchmark, utilApp
  )

  lazy val utilCore = Project(
    id = "util-core",
    base = file("util-core"),
    settings = Project.defaultSettings ++
      sharedSettings
  ).settings(
    name := "util-core",
    libraryDependencies ++= Seq(
      "com.twitter.common" % "objectsize" % "0.0.7" % "test"
    ),
    resourceGenerators in Compile <+=
      (resourceManaged in Compile, name, version) map { (dir, name, ver) =>
        val file = dir / "com" / "twitter" / name / "build.properties"
        val buildRev = Process("git" :: "rev-parse" :: "HEAD" :: Nil).!!.trim
        val buildName = new java.text.SimpleDateFormat("yyyyMMdd-HHmmss").format(new java.util.Date)
        val contents = (
          "name=%s\nversion=%s\nbuild_revision=%s\nbuild_name=%s"
        ).format(name, ver, buildRev, buildName)
        IO.write(file, contents)
        Seq(file)
      }
  )

  lazy val utilEval = Project(
    id = "util-eval",
    base = file("util-eval"),
    settings = Project.defaultSettings ++
      sharedSettings
  ).settings(
    name := "util-eval",
    libraryDependencies <+= scalaVersion { "org.scala-lang" % "scala-compiler" % _ % "compile" }
  ).dependsOn(utilCore)

  lazy val utilCodec = Project(
    id = "util-codec",
    base = file("util-codec"),
    settings = Project.defaultSettings ++
      sharedSettings
  ).settings(
    name := "util-codec",
    libraryDependencies ++= Seq(
      "commons-codec" % "commons-codec" % "1.5"
    )
  ).dependsOn(utilCore)

  lazy val utilCollection = Project(
    id = "util-collection",
    base = file("util-collection"),
    settings = Project.defaultSettings ++
      sharedSettings
  ).settings(
    name := "util-collection",
    // Because of different imports for wrappers in 2.9 and 2.11
    unmanagedSourceDirectories in Compile <++= (scalaBinaryVersion, baseDirectory) { (sv, bd) =>
      Seq(bd / "src" / "main" / ("scala-" + sv))
    },
    libraryDependencies ++= Seq(
      // NB: guava has a `provided` dep on jsr/javax packages, so we include them manually
      "com.google.code.findbugs" % "jsr305"              % "1.3.9",
      "javax.inject"             % "javax.inject"        % "1",
      "com.google.guava"         % "guava"               % "16.0",
      "commons-collections"      % "commons-collections" % "3.2.1"
    )
  ).dependsOn(utilCore)

  lazy val utilReflect = Project(
    id = "util-reflect",
    base = file("util-reflect"),
    settings = Project.defaultSettings ++
      sharedSettings
  ).settings(
    name := "util-reflect",
    libraryDependencies ++= Seq(
      "asm"   % "asm"         % "3.3.1",
      "asm"   % "asm-util"    % "3.3.1",
      "asm"   % "asm-commons" % "3.3.1",
      "cglib" % "cglib"       % "2.2"
    )
  ).dependsOn(utilCore)

  lazy val utilLogging = Project(
    id = "util-logging",
    base = file("util-logging"),
    settings = Project.defaultSettings ++
      sharedSettings
  ).settings(
    name := "util-logging",
    libraryDependencies <+= scalatest
  ).dependsOn(utilCore, utilApp)

  lazy val utilThrift = Project(
    id = "util-thrift",
    base = file("util-thrift"),
    settings = Project.defaultSettings ++
      sharedSettings
  ).settings(
    name := "util-thrift",
    libraryDependencies ++= Seq(
      "thrift"                     % "libthrift"        % "0.5.0",
      "org.slf4j"                  % "slf4j-nop"        % "1.5.8" % "provided",
      "com.fasterxml.jackson.core" % "jackson-core"     % "2.1.3",
      "com.fasterxml.jackson.core" % "jackson-databind" % "2.1.3"
    )
  ).dependsOn(utilCodec)

  lazy val utilHashing = Project(
    id = "util-hashing",
    base = file("util-hashing"),
    settings = Project.defaultSettings ++
      sharedSettings
  ).settings(
    name := "util-hashing",
    libraryDependencies ++= Seq(
      "commons-codec" % "commons-codec" % "1.5" % "test"
    )
  ).dependsOn(utilCore % "test")

  lazy val utilJvm = Project(
    id = "util-jvm",
    base = file("util-jvm"),
    settings = Project.defaultSettings ++
      sharedSettings
  ).settings(
    name := "util-jvm"
  ).dependsOn(utilApp, utilCore, utilLogging % "test")

  lazy val utilZk = Project(
    id = "util-zk",
    base = file("util-zk"),
    settings = Project.defaultSettings ++
      sharedSettings
  ).settings(
    name := "util-zk",
    libraryDependencies ++= Seq(
      zkDependency
    )
  ).dependsOn(utilCore, utilCollection, utilLogging)

  lazy val utilZkCommon = Project(
    id = "util-zk-common",
    base = file("util-zk-common"),
    settings = Project.defaultSettings ++
      sharedSettings
  ).settings(
    name := "util-zk-common",
    libraryDependencies ++= Seq(
      "com.twitter.common.zookeeper" % "client"     % "0.0.51",
      "com.twitter.common.zookeeper" % "group"      % "0.0.64",
      "com.twitter.common.zookeeper" % "server-set" % "1.0.69",
      zkDependency
    )
  ).dependsOn(utilCore, utilLogging, utilZk,
    // These are dependended on to provide transitive dependencies
    // that would otherwise cause incompatibilities. See above comment.
    utilCollection, utilHashing
  )

  lazy val utilClassPreloader = Project(
    id = "util-class-preloader",
    base = file("util-class-preloader"),
    settings = Project.defaultSettings ++
      sharedSettings
  ).settings(
    name := "util-class-preloader"
  ).dependsOn(utilCore)

  lazy val utilBenchmark = Project(
    id = "util-benchmark",
    base = file("util-benchmark"),
    settings = Project.defaultSettings ++
      sharedSettings
  ).settings(
    name := "util-benchmark",
    libraryDependencies ++= Seq(
      "com.google.caliper" % "caliper" % "0.5-rc1"
    )
  ).dependsOn(utilCore, utilJvm)

  lazy val utilApp = Project(
    id = "util-app",
    base = file("util-app"),
    settings = Project.defaultSettings ++
      sharedSettings
  ).settings(
    name := "util-app"
  ).dependsOn(utilCore)
}
