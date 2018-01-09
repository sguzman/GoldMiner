/** Name of project */
name := "ScAUr"

/** Organization */
organization := "com.github.sguzman"

/** Project Version */
version := "1.0"

/** Scala version */
scalaVersion := "2.12.4"

/** Scalac parameters */
scalacOptions ++= Seq("-feature", "-unchecked", "-deprecation", "-encoding", "utf8")

/** Javac parameters */
javacOptions ++= Seq("-server")

/** Resolver */
resolvers ++= Seq(
  DefaultMavenRepository,
  Resolver.sonatypeRepo("public"),
  Resolver.typesafeRepo("releases"),
  Resolver.sbtPluginRepo("releases"),
  Resolver.jcenterRepo,
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
  "Artima Maven Repository" at "http://repo.artima.com/releases"
)

/** Source Dependencies */
libraryDependencies ++= Seq(
  "io.circe" %% "circe-core" % "0.9.0-M2",
  "io.circe" %% "circe-generic" % "0.9.0-M2",
  "io.circe" %% "circe-parser" % "0.9.0-M2",
  "org.feijoas" %% "mango" % "0.14",
  "org.scalaj" %% "scalaj-http" % "2.3.0",
  "com.github.scopt" %% "scopt" % "3.7.0",
  "net.ruippeixotog" %% "scala-scraper" % "2.0.0",
  "com.outr" %% "scribe" % "1.4.5"

)

/** Make sure to fork on run */
fork in run := true