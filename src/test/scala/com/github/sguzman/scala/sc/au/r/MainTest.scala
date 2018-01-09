package com.github.sguzman.scala.sc.au.r

import org.scalatest.FlatSpec
import org.scalatest.Matchers._

class MainTest extends FlatSpec {
  "App" should "run without errors" in {
    val user = System.getenv("USERNAME")
    user should not be null

    val pass = System.getenv("PASSWORD")
    pass should not be null

    Main.main(Array("--perm", user,"--pass", pass, "--old", "--dev"))
  }
}
