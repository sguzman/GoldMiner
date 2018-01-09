package com.github.sguzman.scala.gold.miner

import scala.language.implicitConversions
import org.scalatest.Matchers._

object Main {
  final case class ParseCreds(args: Array[String]) {
    def parse: Creds = {
      val creds = CredsConfig().parse(args, Creds())
      creds should not be None
      creds.get
    }
  }
  implicit def argsToParse(args: Array[String]): ParseCreds = ParseCreds(args)

  def main(args: Array[String]): Unit = {
    val creds = args.parse
    println(creds)
  }
}
