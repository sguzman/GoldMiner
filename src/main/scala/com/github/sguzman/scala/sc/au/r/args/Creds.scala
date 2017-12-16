package com.github.sguzman.scala.sc.au.r.args

case class Creds(
                perm: String = "",
                pass: String = "",
                old: Boolean = false,
                verbose: Boolean = false,
                dev: Boolean = false,
                help: Boolean = false
                )