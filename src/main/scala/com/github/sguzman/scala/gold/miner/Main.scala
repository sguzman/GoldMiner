package com.github.sguzman.scala.gold.miner

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.model.{Document, Element}
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL._

import scala.language.{implicitConversions, postfixOps}
import scalaj.http.{Http, HttpResponse}

object Main {
  implicit def toStr[A](a: A) = StrGenWrapper(a)
  final case class StrGenWrapper[A](a: A) {
    def str = a.toString
    def as[B] = a.asInstanceOf[B]
  }

  implicit def argsToCred(args: Array[String]) = ArgsWrapper(args)
  final case class ArgsWrapper(args: Array[String]) {
    def creds = CredWrapper(CredConfig().parse(args, Cred()).get)
  }

  implicit def strToPost(str: String) = StrPostWrapper(str)
  final case class StrPostWrapper(str: String) {
    def loginReq = Http("https://my.sa.ucsb.edu/gold/Login.aspx").postData(str)
    def login = this.loginReq.asString
  }

  final case class StrDocWrapper(str: String) {
    def doc = JsoupBrowser().parseString(str)
  }

  final case class HttpBodyWrapper(http: HttpResponse[String]) {
    def bod = StrDocWrapper(http.body)
  }

  implicit def strToEnc(str: String) = StrEncWrapper(str)
  final case class StrEncWrapper(str: String) {
    def enc = URLEncoder.encode(str, StandardCharsets.UTF_8.str)
  }

  implicit def attrtoStr(str: String) = AttrStrWrapper(str)
  final case class AttrStrWrapper(str: String) {
    def attr(e: Element) = str match {
      case "value" => e.attr(str).enc
      case _ => e.attr(str)
    }
  }

  implicit def docToHidden(doc: Document) = DocHiddenWrapper(doc)
  final case class DocHiddenWrapper(doc: Document) {
    def hiddenElem = doc >> elementList("""input[type="hidden"]""")

    private def hiddenRaw(hid: List[Element]) = Main.hidden(hid)
    def hidden = this.hiddenRaw(this.hiddenElem)
  }

  final case class CredWrapper(cred: Cred) {
    def login = HttpBodyWrapper(Main.login)
    def vars = (if (cred.old) List(
      "ctl00%24pageContent%24PermPinLogin%24userNameText",
      "ctl00%24pageContent%24PermPinLogin%24passwordText",
      "ctl00%24pageContent%24PermPinLogin%24loginButton"
    ) else List(
      "ctl00%24pageContent%24userNameText",
      "ctl00%24pageContent%24passwordText",
      "ctl00%24pageContent%24loginButton"
    )) zip List(cred.perm, cred.pass, "Login")
    def hidden = this.login.bod.doc.hidden
    def body = this.hidden ++ this.vars
    def loginPayload = Main.body(this.body)
  }

  def loginReq = Http("https://my.sa.ucsb.edu/gold/Login.aspx")
  def login = this.loginReq.asString
  def unpack(t: (String, String)) = s"${t._1}=${t._2}"
  def body(list: List[(String, String)]) = list map Main.unpack mkString "&"
  def hidden(hid: List[Element]) = hid map "name".attr zip (hid map "value".attr)

  def main(args: Array[String]): Unit = {
    println(args.creds.loginPayload)
  }
}
