package com.github.sguzman.scala.gold.miner

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

import net.ruippeixotog.scalascraper.browser.{Browser, JsoupBrowser}
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.model.Document
import net.ruippeixotog.scalascraper.model.Element

import scala.language.{implicitConversions, postfixOps}
import org.scalatest.Matchers._

import scalaj.http.{Http, HttpRequest, HttpResponse}

object Main {
  implicit def argsToParse(args: Array[String]): ParseCreds = ParseCreds(args)
  final case class ParseCreds(args: Array[String]) {
    def parse: Creds = {
      val creds = CredsConfig().parse(args, Creds())
      creds should not be None

      creds.get
    }

    def req: HttpRequest = "https://my.sa.ucsb.edu/gold/Login.aspx" login args.parse

    def login: HttpResponse[String] = this.req.asString

    def courses: HttpResponse[String] = this.req.asString.courses

    def quarters: List[String] = this.login.courses.quarters
    def quarters(state: HttpResponse[String]): List[String] = state.quarters

    def departments: List[String] = this.login.courses.departments
    def departments(state: HttpResponse[String]): List[String] = state.departments

    private def argumentsDev(state: HttpResponse[String], quarter: String): List[(String, String)] =
      this.departments(state).map(b => (quarter, b))

    private def argumentsRaw(state: HttpResponse[String]): List[(String, String)] = if (args.parse.dev)
      this.argumentsDev(state, this.quarters(state).head)
    else
      this.quarters(state) flatMap (a => this.departments(state).map(b => (a,b)))

    def arguments: List[(String, String)] = this.argumentsRaw(this.courses)
  }

  implicit def strToGeneral(url: String): GeneralStr = GeneralStr(url)
  final case class GeneralStr(str: String) {
    def get: HttpResponse[String] = {
      val response = Http(str).asString
      response.is2xx should be (true)
      response.isSuccess should be (true)

      response
    }

    def log(url: String): HttpRequest = Http(url).postData(str)

    def doc: Browser#DocumentType = JsoupBrowser().parseString(str)

    def elems(doc: Document): List[Element] = doc >> elementList(str)

    def enc: String = URLEncoder.encode(str, StandardCharsets.UTF_8.toString)

    def attr(ele: Element): String = ele.attr(str)

    private def hiddenRaw(es: List[Element]) = es map "name".attr zip (es map "value".attr map (_.enc))
    def hidden: List[(String, String)] =
      this.hiddenRaw("""input[type="hidden"]""".elems("https://my.sa.ucsb.edu/gold/Login.aspx".get.body.doc))

    def login(cred: Creds): HttpRequest = (str.hidden ++ cred.hid).map(t => s"${t._1}=${t._2}").mkString("&").log(str)
  }

  implicit def HttpResponse(http: HttpResponse[String]): HttpResponseAddendum = HttpResponseAddendum(http)
  final case class HttpResponseAddendum(http: HttpResponse[String]) {
    def nextReq(url: String): HttpRequest = Http(url).header("Cookie", this.cookie)
    def next(url: String): HttpResponse[String] = this.nextReq(url).asString

    def cookie: String = http.cookies.mkString("; ")

    def courses: HttpResponse[String] = this.next("https://my.sa.ucsb.edu/gold/BasicFindCourses.aspx")
    def quarters: List[String] = "#pageContent_quarterDropDown > option" elems http.body.doc map "value".attr

    private def departmentsRaw = "#pageContent_subjectAreaDropDown > option" elems http.body.doc map "value".attr
    def departments: List[String] = this.departmentsRaw.tail
  }

  def main(args: Array[String]): Unit = {
    println(args.arguments)
  }
}
