import com.typesafe.sbt.pgp.PgpKeys.{publishSigned, publishLocalSigned}

import scalariform.formatter.preferences._

// Settings for entire build

ThisBuild/version := "2.5.0-SNAPSHOT"

ThisBuild/organization := "org.bitbucket.inkytonik.kiama"

ThisBuild/scalaVersion := "2.13.4"
ThisBuild/crossScalaVersions := Seq("3.0.0-M2", "2.13.4", "2.12.13", "2.11.12")

ThisBuild/scalacOptions := {
    // Turn on all lint warnings, except:
    //  - stars-align: incorrectly reports problems if pattern matching of
    //    unapplySeq extractor doesn't match sequence directly
    //  - non-local-return: sometimes we just prefer to do this
    val lintOption =
        if (scalaVersion.value.startsWith("2.13"))
            "-Xlint:-stars-align,-nonlocal-return,_"
        else
            "-Xlint:-stars-align,_"
    if (scalaVersion.value.startsWith("3"))
        Seq(
            "-deprecation",
            "-feature",
            "-language:higherKinds",
            "-sourcepath", baseDirectory.value.getAbsolutePath,
            "-unchecked",
            "-Xfatal-warnings",
            "-Xmigration"
            // FIXME: should we have a -Xlint replacement?
        )
    else
        Seq(
            "-deprecation",
            "-feature",
            "-language:higherKinds",
            "-sourcepath", baseDirectory.value.getAbsolutePath,
            "-unchecked",
            "-Xcheckinit",
            "-Xfatal-warnings",
            "-Xsource:3",
            lintOption
        )
}

ThisBuild/resolvers ++=
    Seq(
        Resolver.sonatypeRepo("releases"),
        Resolver.sonatypeRepo("snapshots")
    )

ThisBuild/logLevel := Level.Info

ThisBuild/shellPrompt := {
    state =>
        Project.extract(state).currentRef.project + " " + version.value+
            " " + scalaVersion.value + "> "
}

ThisBuild/mainClass := None

// Common project settings

val commonSettings =
    Seq(
        unmanagedSourceDirectories in Compile ++= {
            val sourceDir = (sourceDirectory in Compile).value
            CrossVersion.partialVersion(scalaVersion.value) match {
                case Some((2, 11)) =>
                    Seq(sourceDir / "scala-2.11", sourceDir / "scala-2.11+", sourceDir / "scala-2.12-")
                case Some((2, 12)) =>
                    Seq(sourceDir / "scala-2.not11", sourceDir / "scala-2.11+", sourceDir / "scala-2.12-")
                case Some((2, 13) | (3, 0)) =>
                    Seq(sourceDir / "scala-2.not11", sourceDir / "scala-2.11+", sourceDir / "scala-2.13")
                case version =>
                    sys.error(s"unexpected Scala version $version")
            }
        },

        libraryDependencies ++=
            Seq(
                "org.scalacheck" %% "scalacheck" % "1.15.2" % "test",
                "org.scalatest" %% "scalatest-funsuite" % "3.2.3" % "test",
                "org.scalatest" %% "scalatest-shouldmatchers" % "3.2.3" % "test",
                "org.scalatestplus" %% "scalacheck-1-15" % "3.2.3.0" % "test"
            ),

        // Formatting
        scalariformPreferences := scalariformPreferences.value
            .setPreference(AlignSingleLineCaseStatements, true)
            .setPreference(DanglingCloseParenthesis, Force)
            .setPreference(IndentSpaces, 4)
            .setPreference(SpaceBeforeColon, true)
            .setPreference(SpacesAroundMultiImports, false),

        // Publishing
        publishTo := {
            val nexus = "https://oss.sonatype.org/"
            if (version.value.trim.endsWith("SNAPSHOT"))
                Some("snapshots" at nexus + "content/repositories/snapshots")
            else
                Some("releases" at nexus + "service/local/staging/deploy/maven2")
        },
        publishMavenStyle := true,
        Test/publishArtifact := true,
        pomIncludeRepository := { _ => false },
        pomExtra := (
            <url>https://github.com/inkytonik/kiama</url>
            <licenses>
                <license>
                    <name>Mozilla Public License, v. 2.0</name>
                    <url>http://mozilla.org/MPL/2.0/</url>
                    <distribution>repo</distribution>
                </license>
            </licenses>
            <scm>
                <url>https://github.com/inkytonik/kiama</url>
                <connection>scm:hg:https://github.com/inkytonik/kiama</connection>
            </scm>
            <developers>
                <developer>
                   <id>inkytonik</id>
                   <name>Tony Sloane</name>
                   <url>https://github.com/inkytonik</url>
                </developer>
            </developers>
        )
    )

// Project configuration:
//   - core project containing main Kiama functionality, including its tests
//   - extras project containing utilities, including their tests and examples
//   - kiama (root) project aggregates core and extras

def setupProject(project : Project, projectName : String) : Project =
    project.settings(
        name := projectName
    )

def setupSubProject(project : Project, projectName : String) : Project =
    setupProject(
        project,
        projectName
    ).enablePlugins(
        ScalaUnidocPlugin
    ).settings(
        commonSettings : _*
    )

val noPublishSettings =
    Seq(
        publish := {},
        publishLocal := {},
        publishSigned := {},
        publishLocalSigned := {}
    )

val extrasProject = ProjectRef(file("."), "extras")

lazy val core =
    setupSubProject(
        project in file("core"),
        "kiama"
    ).settings(
        libraryDependencies ++=
            Seq(
                // Caching:
                "com.google.guava" % "guava" % "27.1-jre"
            ),

        console/initialCommands := """
            import org.bitbucket.inkytonik.kiama._
            import rewriting.Rewriter._
        """.stripMargin,

        // Unidoc so we combine docs from core (but not extras)
        Compile/doc := (ScalaUnidoc/doc).value,
        Test/doc := (TestScalaUnidoc/doc).value,
        ScalaUnidoc/unidoc/target := crossTarget.value / "api",
        TestScalaUnidoc/unidoc/target := crossTarget.value / "test-api",
        ScalaUnidoc/unidoc/scalacOptions ++=
            Seq(
                "-doc-source-url",
                    "https://github.com/inkytonik/kiama/blob/master€{FILE_PATH}.scala"
            ),
        TestScalaUnidoc/unidoc/scalacOptions := (ScalaUnidoc/unidoc/scalacOptions).value,
        ScalaUnidoc/unidoc/unidocProjectFilter := inAnyProject -- inProjects(extrasProject),
        TestScalaUnidoc/unidoc/unidocProjectFilter := (ScalaUnidoc/unidoc/unidocProjectFilter).value
    )

lazy val extras =
    setupSubProject(
        project in file("extras"),
        "kiama-extras"
    ).settings(
        libraryDependencies ++=
            Seq(
                // Command-line handling:
                "org.rogach" %% "scallop" % "4.0.1",
                // Language server protocol:
                "org.eclipse.lsp4j" % "org.eclipse.lsp4j" % "0.10.0",
                "com.google.code.gson" % "gson" % "2.8.2",
                // REPLs:
                "jline" % "jline" % "2.14.6"
            ),
        javaOptions ++= Seq("-Xss8M"),
        fork := true,
        run/connectInput := true,
        run/outputStrategy := Some(StdoutOutput),
        Test/console/initialCommands :=
            (Test/console/initialCommands).value + """
                import org.bitbucket.inkytonik.kiama._
                import example.json.PrettyPrinter._
                import example.json.JSONTree._
            """.stripMargin,
        Compile/doc/scalacOptions ++=
            Seq(
                "-doc-source-url",
                    "https://github.com/inkytonik/kiama/blob/master€{FILE_PATH}.scala"
            ),
        Test/doc/scalacOptions := (Compile/doc/scalacOptions).value
    ).settings(
       inConfig(Test)(baseAssemblySettings)
    ).settings(
        // Test/assembly/test := {},
        Test/assembly/assemblyJarName := s"${name.value}-assembly-${version.value}-tests.jar"
    ).dependsOn(
        core % "compile; test->test"
    )

lazy val root =
    setupProject(
        project in file("."),
        "root"
    ).settings(
        noPublishSettings : _*
    ).aggregate(
        core, extras
    )
