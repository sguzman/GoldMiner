package com.github.sguzman.scala.gold.miner

import org.scalatest.FlatSpec
import org.scalatest.Matchers._

class MainTest extends FlatSpec {

  behavior of "MainTest"

  it should "main" in {
    val perm = System.getenv("USERNAME")
    perm should not be null

    val pass = System.getenv("PASSWORD")
    pass should not be null

    Main.main(Array("--perm", perm, "--pass", pass, "--old"))
  }

}
