package com.github.sguzman.scala.gold.miner.args

case class Cred(
  perm: String = "",
  pass: String = "",
  old: Boolean = false,
  dev: Boolean = false,
  help: Boolean = false
)