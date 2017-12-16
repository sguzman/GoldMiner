package com.github.sguzman.scala.sc.au.r

import com.github.sguzman.scala.sc.au.r.args.{Creds, CredsConfig}
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL._

import scala.util.{Failure, Success}
import scalaj.http.Http

object Main {
  def main(args: Array[String]): Unit = util.Try({
    val config = new CredsConfig().parse(args, Creds())
    if (config.isEmpty) throw new Exception("Invalid cmd args")

    val url = "https://my.sa.ucsb.edu/gold/Login.aspx"
    val request = Http(url)
    val response = request.asString
    val doc = JsoupBrowser().parseString(response.body)
    val hidden = doc >> elementList("""input[type="hidden"]""")
    val hiddenPairs = hidden map (_.attr("name")) zip (hidden map (_.attr("value")))
    println(hiddenPairs)
  }) match {
    case Success(_) => println("done")
    case Failure(e) => Console.err.println(e)
  }
}
