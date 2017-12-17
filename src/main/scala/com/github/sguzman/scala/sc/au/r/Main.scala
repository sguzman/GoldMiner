package com.github.sguzman.scala.sc.au.r

import java.net.URLEncoder.encode
import java.nio.charset.StandardCharsets.UTF_8

import com.github.sguzman.scala.sc.au.r.args.{Creds, CredsConfig}
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL._

import scala.util.{Failure, Success}
import scalaj.http.{Http, HttpResponse}

object Main {
  def main(args: Array[String]): Unit = util.Try({
    val config = arg(args)

    val postResponse = login(config)
    println(postResponse)
  }) match {
    case Success(_) => println("done")
    case Failure(e) => Console.err.println(e)
  }

  def arg(args: Array[String]): Creds = {
    val configOpt = new CredsConfig().parse(args, Creds())
    if (configOpt.isEmpty) throw new Exception("Invalid cmd args")
    configOpt.get
  }

  def login(config: Creds): HttpResponse[String] = {
    val response = Http("https://my.sa.ucsb.edu/gold/Login.aspx").asString

    val doc = JsoupBrowser().parseString(response.body)
    val hidden = doc >> elementList("""input[type="hidden"]""")
    val hiddenPairs = hidden map (_.attr("name")) zip (hidden map (_.attr("value")) map (encode(_, UTF_8.toString)))

    val logins = (if (config.old) List(
      "ctl00%24pageContent%24PermPinLogin%24userNameText",
      "ctl00%24pageContent%24PermPinLogin%24passwordText",
      "ctl00%24pageContent%24PermPinLogin%24loginButton"
    ) else List(
      "ctl00%24pageContent%24userNameText",
      "ctl00%24pageContent%24passwordText",
      "ctl00%24pageContent%24loginButton"
    )) zip List(config.perm, config.pass, "Login")

    val bodyPairs = hiddenPairs ++ logins
    val bodyStr = bodyPairs.map(t => s"${t._1}=${t._2}").mkString("&")

    val postResponse = Http("https://my.sa.ucsb.edu/gold/Login.aspx").postData(bodyStr).asString
    postResponse
  }
}
