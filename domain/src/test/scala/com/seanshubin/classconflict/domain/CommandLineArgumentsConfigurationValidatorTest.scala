package com.seanshubin.classconflict.domain

import org.scalatest.FunSuite

class CommandLineArgumentsConfigurationValidatorTest extends FunSuite {
  test("valid configuration") {
    //given
    val commandLineArguments = Seq("some-file")
    val validator = createValidator(commandLineArguments)

    //when
    val configuration = validator.validate()

    //then
    assert(configuration.foo === "bar")
  }

  def createValidator(commandLineArguments: Seq[String]): CommandLineArgumentsConfigurationValidator = {
    new CommandLineArgumentsConfigurationValidator(commandLineArguments)
  }
}
