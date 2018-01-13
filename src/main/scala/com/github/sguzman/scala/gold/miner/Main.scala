package com.github.sguzman.scala.gold.miner

import net.ruippeixotog.scalascraper.browser.JsoupBrowser

import scala.language.{implicitConversions, postfixOps}
import scalaj.http.{Http, HttpResponse}

object Main {
  implicit final class ArgWrap(args: Array[String]) {
    def cred = CredConfig().parse(args, Cred()).get
  }

  implicit final class CredWrap(street: Cred) {
    def logVars = (if (street.old) List(
      "ctl00%24pageContent%24PermPinLogin%24userNameText",
      "ctl00%24pageContent%24PermPinLogin%24passwordText",
      "ctl00%24pageContent%24PermPinLogin%24loginButton"
    ) else List(
      "ctl00%24pageContent%24userNameText",
      "ctl00%24pageContent%24passwordText",
      "ctl00%24pageContent%24loginButton"
    )) zip List(street.perm, street.pass, "Login")
  }

  implicit final class StrWrap(str: String) {
    def login = Http(str).asString
    def doc = JsoupBrowser().parseString(str)
  }

  def main(args: Array[String]): Unit =
    println(args.cred.logVars)
}
