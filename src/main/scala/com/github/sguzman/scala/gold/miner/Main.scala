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

  implicit def tup2String(tup: (String, String)) = Tuple2StringWrapper(tup)
  final case class Tuple2StringWrapper(tuple: (String, String)) {
    def mk = s"${tuple._1}=${tuple._2}"
  }

  implicit def listTuple2String(list: List[(String, String)]) = ListTup2StringWrapper(list)
  final case class ListTup2StringWrapper(list: List[(String, String)]) {
    def body = list map (_.mk) mkString "&"
  }

  implicit def argsToCred(args: Array[String]) = ArgsWrapper(args)
  final case class ArgsWrapper(args: Array[String]) {
    def creds = CredWrapper(CredConfig().parse(args, Cred()).get)
  }

  implicit def strToPost(str: String) = StrPostWrapper(str)
  final case class StrPostWrapper(str: String) {
    def login = HttpBodyWrapper(Http("https://my.sa.ucsb.edu/gold/Login.aspx")
      .postData(str)
      .asString)
  }

  final case class StrDocWrapper(str: String) {
    def doc = DocHiddenWrapper(JsoupBrowser().parseString(str))
  }

  final case class HttpBodyWrapper(http: HttpResponse[String]) {
    def body = StrDocWrapper(http.body)
    def courses = Http("https://my.sa.ucsb.edu/gold/BasicFindCourses.aspx")
        .header("Cookie", http.cookies.mkString("; "))
        .asString
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

  implicit def listElement(hid: List[Element]) = ListElementToStr(hid)
  final case class ListElementToStr(hid: List[Element]) {
    def hidden = hid map "name".attr zip (hid map "value".attr)
  }

  final case class DocHiddenWrapper(doc: Document) {
    def hidden = (doc >> elementList("""input[type="hidden"]""")).hidden
  }

  final case class CredWrapper(cred: Cred) {
    def login = HttpBodyWrapper(Http("https://my.sa.ucsb.edu/gold/Login.aspx").asString)
    def body = StrPostWrapper((this.login.body.doc.hidden ++ ((if (cred.old)
      List(
        "ctl00%24pageContent%24PermPinLogin%24userNameText",
        "ctl00%24pageContent%24PermPinLogin%24passwordText",
        "ctl00%24pageContent%24PermPinLogin%24loginButton"
      )
    else
      List(
        "ctl00%24pageContent%24userNameText",
        "ctl00%24pageContent%24passwordText",
        "ctl00%24pageContent%24loginButton"
      )
      ) zip List(cred.perm, cred.pass, "Login"))).body)
  }

  def main(args: Array[String]): Unit = println(args.creds.body.login.courses.body)
}
