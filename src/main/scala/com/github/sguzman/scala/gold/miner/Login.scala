package com.github.sguzman.scala.gold.miner

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

import com.github.sguzman.scala.gold.miner.args.Cred
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.scraper.ContentExtractors.elementList

import scalaj.http.Http

object Login {
  def apply(street: Cred) =
    Http("https://my.sa.ucsb.edu/gold/Login.aspx")
      .postData((JsoupBrowser()
        .parseString(Http("https://my.sa.ucsb.edu/gold/Login.aspx")
          .asString
          .body).>>(elementList("""input[type="hidden"]"""))
        .map(t => (
          t.attr("name"),
          URLEncoder.encode(t.attr("value"), StandardCharsets.UTF_8.toString)
        )) ++ (if (street.old) List(
        "ctl00%24pageContent%24PermPinLogin%24userNameText",
        "ctl00%24pageContent%24PermPinLogin%24passwordText",
        "ctl00%24pageContent%24PermPinLogin%24loginButton"
      ) else List(
        "ctl00%24pageContent%24userNameText",
        "ctl00%24pageContent%24passwordText",
        "ctl00%24pageContent%24loginButton"))
        .zip(List(street.perm, street.pass, "Login")))
        .map(t => s"${t._1}=${t._2}")
        .mkString("&"))
      .asString
}
