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
  final case class ParseCreds(args: Array[String]) {
    def parse: Creds = {
      val creds = CredsConfig().parse(args, Creds())
      creds should not be None

      creds.get
    }

    def login: HttpRequest = {
      "https://my.sa.ucsb.edu/gold/Login.aspx".login(args.parse)
    }
  }
  implicit def argsToParse(args: Array[String]): ParseCreds = ParseCreds(args)

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

    def hidden: List[(String, String)] = {
      val hidden = """input[type="hidden"]""".elems("https://my.sa.ucsb.edu/gold/Login.aspx".get.body.doc)
      hidden map "name".attr zip (hidden map "value".attr map (_.enc))
    }

    def login(cred: Creds): HttpRequest = (str.hidden ++ cred.hid).map(t => s"${t._1}=${t._2}").mkString("&").log(str)
  }

  implicit def strToGeneral(url: String): GeneralStr = GeneralStr(url)

  def main(args: Array[String]): Unit = {
    val login = args.login.asString
    println(login)
  }
}
