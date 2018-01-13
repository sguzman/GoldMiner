package com.github.sguzman.scala.gold.miner

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

import net.ruippeixotog.scalascraper.browser.{Browser, JsoupBrowser}
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.model.Element

import scala.language.{implicitConversions, postfixOps}
import scalaj.http.{Http, HttpResponse}

object Main {
  implicit class ArgWrap(args: Array[String]) {
    implicit class HttpWrap(http: HttpResponse[String]) {

    }

    def cred = CredConfig().parse(args, Cred()).get
  }

  implicit class CredWrap(street: Cred) {
    def oldVars = List(
      "ctl00%24pageContent%24PermPinLogin%24userNameText",
      "ctl00%24pageContent%24PermPinLogin%24passwordText",
      "ctl00%24pageContent%24PermPinLogin%24loginButton"
    )
    def newVars = List(
      "ctl00%24pageContent%24userNameText",
      "ctl00%24pageContent%24passwordText",
      "ctl00%24pageContent%24loginButton"
    )
    def vars = if (street.old) oldVars else newVars
    def credsVar = List(street.perm, street.pass, "Login")

    implicit class TupWrap(t: (String, String)) {
      def mkString = s"${t._1}=${t._2}"
    }

    def logVars = vars zip credsVar
    def payloadRaw = "https://my.sa.ucsb.edu/gold/Login.aspx".getLogin.body.doc.hidden ++ logVars
    def payload = payloadRaw map (_.mkString) mkString "&"
  }

  implicit class StrWrap(str: String) {
    def getLogin = Http(str).asString
    def postLogin = Http("https://my.sa.ucsb.edu/gold/Login.aspx").postData(str).asString
    def doc = JsoupBrowser().parseString(str)
    def enc = URLEncoder.encode(str, StandardCharsets.UTF_8.toString)
    def attr(e: Element) = e.attr(str)
  }

  implicit class DocWrap(doc: Browser#DocumentType) {
    implicit class ListElementWrap(list: List[Element]) {
      def name = list map "name".attr
      def value = list map "value".attr map (_.enc)
      def parsed = name zip value
    }

    def hiddenRaw = doc >> elementList("""input[type="hidden"]""")
    def hidden = hiddenRaw.parsed
  }

  def main(args: Array[String]): Unit =
    println(args.cred.payload.postLogin.body)
}
