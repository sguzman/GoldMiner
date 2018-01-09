package com.github.sguzman.scala.gold.miner

case class Creds(
  perm: String = "",
  pass: String = "",
  old: Boolean = false,
  dev: Boolean = false,
  help: Boolean = false
) {
  def hid = (if (this.old) List(
    "ctl00%24pageContent%24PermPinLogin%24userNameText",
    "ctl00%24pageContent%24PermPinLogin%24passwordText",
    "ctl00%24pageContent%24PermPinLogin%24loginButton"
  ) else List(
    "ctl00%24pageContent%24userNameText",
    "ctl00%24pageContent%24passwordText",
    "ctl00%24pageContent%24loginButton"
  )) zip List(this.perm, this.pass, "Login")
}