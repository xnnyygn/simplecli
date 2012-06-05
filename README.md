simplecli
=========

A simple command line support framework. The idea is from [optional](https://github.com/alexy/optional). simplecli just implement the basic usage of a command line application.

simple usage
============

your cli

    object FunnyCli extends simplecli.CliSupport {
		  def main(prefix: String, suffix: String): Unit = println(prefix + suffix)
	  }

in sbt console

   run --prefix a --suffix b

will produce

    ab
