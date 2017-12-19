package com.github.sguzman.scala.sc.au.r

import java.net.URLEncoder.encode
import java.nio.charset.StandardCharsets.UTF_8

import com.github.sguzman.scala.sc.au.r.args.{Creds, CredsConfig}
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL._
import org.feijoas.mango.common.base.Preconditions

import scala.util.{Failure, Success}
import scalaj.http.{Http, HttpResponse}

object Main {
  def main(args: Array[String]): Unit = util.Try({
    val config = arg(args)
    val response = login(config)
    val basicResponse = HttpL.get("https://my.sa.ucsb.edu/gold/BasicFindCourses.aspx", response)

    val quarter = quarters(basicResponse)
    val department = departments(basicResponse)

    println(department)
  }) match {
    case Success(_) => println("done")
    case Failure(e) =>
      Console.err.println(e)
      e.printStackTrace()
  }

  def arg(args: Array[String]): Creds = {
    val configOpt = new CredsConfig().parse(args, Creds())
    AssertL.assertArgs(configOpt)
    configOpt.get
  }

  def departments(response: HttpResponse[String]): List[String] = {
    val doc = JsoupBrowser().parseString(response.body)
    val departDrop = doc >> elementList("#pageContent_subjectAreaDropDown > option")
    val values = departDrop.map(_.attr("value"))

    values.tail
  }

  def quarters(response: HttpResponse[String]): List[String] = {
    val doc = JsoupBrowser().parseString(response.body)
    val quarterDrop = doc >> elementList("#pageContent_quarterDropDown > option")
    val values = quarterDrop.map(_.attr("value"))

    values
  }

  def login(config: Creds): HttpResponse[String] = {
    val response = HttpL.get("https://my.sa.ucsb.edu/gold/Login.aspx")
    AssertL.assertGET(response)

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
    AssertL.assertLoginResponse(postResponse)

    postResponse
  }
}

object AssertL{
  def assertArgs(configOpt: Option[Creds]): Unit = {
    Preconditions.checkArgument(configOpt.isDefined, "Bad cmd args")
  }

  def assertLoginResponse(response: HttpResponse[String]): Unit = {
    Preconditions.checkNotNull(response, "Response is null")
    Preconditions.checkArgument(response.code == 302, "Code is not 302")

    val statusLine = "HTTP/1.1 302 Found"
    Preconditions.checkArgument(response.statusLine == statusLine, s"Statusline is not $statusLine")

    val body = "<html><head><title>Object moved</title></head><body>\r\n<h2>Object moved to <a href=\"/gold/Home.aspx\">here</a>.</h2>\r\n</body></html>\r\n"
    Preconditions.checkArgument(response.body == body, s"Body does not match $body")
  }

  def assertGET(response: HttpResponse[String]): Unit = {
    Preconditions.checkArgument(response.is2xx && response.isSuccess, s"Response is not success $response")
  }
}

object HttpL {
  def get(url: String, cookiesResponse: HttpResponse[String]): HttpResponse[String] = {
    val response = Http(url)
      .header("Cookie", cookiesResponse.cookies.mkString("; "))
      .asString

    AssertL.assertGET(response)
    response
  }

  def get(url: String): HttpResponse[String] = {
    val response = Http(url).asString
    AssertL.assertGET(response)

    response
  }
}
