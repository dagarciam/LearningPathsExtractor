// https://mvnrepository.com/artifact/org.seleniumhq.selenium/selenium-java
libraryDependencies += "org.seleniumhq.selenium" % "selenium-java" % "4.10.0"
// https://mvnrepository.com/artifact/org.seleniumhq.selenium/selenium-chrome-driver
libraryDependencies += "org.seleniumhq.selenium" % "selenium-chrome-driver" % "4.10.0"
// https://mvnrepository.com/artifact/org.slf4j/slf4j-simple
libraryDependencies += "org.slf4j" % "slf4j-simple" % "2.0.7"

// https://mvnrepository.com/artifact/org.apache.poi/poi-ooxml
libraryDependencies += "org.apache.poi" % "poi-ooxml" % "5.2.3"

ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "minsait.data.azure"
ThisBuild / scalaVersion := "2.13.8"

lazy val root = (project in file("."))
  .settings(
    name := "ExtractorTemariosAzure"
  )

