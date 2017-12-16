package com.github.sguzman.scala.sc.au.r

import scalaj.http.Http

object Main {
  def main(args: Array[String]): Unit = {
    val url = "https://my.sa.ucsb.edu/gold/Login.aspx"
    val request = Http(url)
    val response = request.asString
    println(response.body)
  }
}
