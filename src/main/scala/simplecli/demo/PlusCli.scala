package simplecli.demo

import simplecli.CliSupport

object PlusCli extends CliSupport {
  def main(a: Int, b: Int): Unit = println(a + b)
}
