package com.seanshubin.classconflict.console

import com.seanshubin.classconflict.domain._

trait ConfigurationWiring {
  def configuration: Configuration

  val runner: Runnable = new AfterConfigurationRunner()
}
