package com.github.sguzman.scala.gold.miner

case class CredConfig(title: String = "GoldMiner") extends scopt.OptionParser[Cred](title) {
  head(title, "1.0")

  opt[String]('u', "perm")
    .text("Perm Number")
    .required()
    .valueName("<perm>")
    .minOccurs(1)
    .maxOccurs(1)
    .action((x, c) => c.copy(perm = x))

  opt[String]('p', "pass")
    .text("Password")
    .required()
    .valueName("<pass>")
    .minOccurs(1)
    .maxOccurs(1)
    .action((x, c) => c.copy(pass = x))

  opt[Unit]('o', "old")
    .text("Is an old account?")
    .optional()
    .action((x, c) => c.copy(old = true))

  opt[Unit]('d', "dev")
    .text("Is this development mode?")
    .optional()
    .action((x, c) => c.copy(dev = true))

  help("help")
    .text("this menu")
}