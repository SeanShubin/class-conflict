package com.seanshubin.classconflict.mavenplugin

import com.seanshubin.classconflict.console.EntryPoint
import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugins.annotations.{Mojo, Parameter}

@Mojo(name = "report")
class ReportMojo extends AbstractMojo {
  @Parameter(defaultValue = "${configurationFileName}")
  var configurationFileName: String = null

  override def execute(): Unit = {
    EntryPoint.main(Array(configurationFileName))
  }
}
