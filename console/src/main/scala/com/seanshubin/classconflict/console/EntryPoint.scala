package com.seanshubin.classconflict.console

object EntryPoint extends App {
  new EntryPointWiring {
    override def commandLineArguments: Seq[String] = args
  }.runner.run()
}
