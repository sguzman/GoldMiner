package com.github.sguzman.scala.gold.miner

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

import com.github.sguzman.scala.gold.miner.args.{Cred, CredConfig}
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.scraper.ContentExtractors.elementList
import net.ruippeixotog.scalascraper.scraper.ContentExtractors.element

import scalaj.http.{Http, HttpResponse}

object Main {
  object Search {
    def getCourses(http: HttpResponse[String]) =
      Http("https://my.sa.ucsb.edu/gold/BasicFindCourses.aspx")
      .header("Cookie", http.cookies.mkString("; "))
      .asString

    def quarters(http: HttpResponse[String]) =
      JsoupBrowser().parseString(http.body)
        .>>(elementList("#pageContent_quarterDropDown > option"))
      .map(_.attr("value"))

    def departments(http: HttpResponse[String]) =
      JsoupBrowser().parseString(http.body)
        .>>(elementList("#pageContent_subjectAreaDropDown > option"))
        .map(_.attr("value"))

    def doc(http: HttpResponse[String]) =
      JsoupBrowser().parseString(getCourses(http).body)
        .>>(elementList("""input[type="hidden"]"""))
        .map(t => (t.attr("name"),
          URLEncoder.encode(t.attr("value"),
            StandardCharsets.UTF_8.toString)))

    def hiddenVars(qt: String, dp: String) =
      List(
      "ctl00%24pageContent%24quarterDropDown",
      "ctl00%24pageContent%24subjectAreaDropDown",
      "ctl00%24pageContent%24searchButton"
    ) zip List(qt, URLEncoder.encode(dp, StandardCharsets.UTF_8.toString), "Search")

    def payload(http: HttpResponse[String], qt: String, dp: String) =
      (doc(http) ++ hiddenVars(qt, dp)).map(t => s"${t._1}=${t._2}").mkString("&")

    def post(http: HttpResponse[String], qt: String, dp: String) =
      Http("https://my.sa.ucsb.edu/gold/BasicFindCourses.aspx")
      .header("Cookie", http.cookies.mkString("; "))
      .postData(payload(http, qt, dp))
      .asString

    def results(http: HttpResponse[String]) =
      Http("https://my.sa.ucsb.edu/gold/ResultsFindCourses.aspx")
      .header("Cookie", http.cookies.mkString("; "))
      .asString

    def postResults(http: HttpResponse[String], qt: String, dp: String) = {
      post(http, qt, dp)
      results(http)
    }
  }

  def main(args: Array[String]): Unit = {
    val cred = CredConfig().parse(args, Cred()).get
    val resp = Login(cred)
    val qt = Search.quarters(Search.getCourses(resp))
    val dp = Search.departments(Search.getCourses(resp)).tail

    println(
      JsoupBrowser().parseString(Search.postResults(resp, qt.head, dp.head).body)
        .>>(element("body")).text
    )
  }
}
