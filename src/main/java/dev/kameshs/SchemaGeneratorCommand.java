package dev.kameshs;

import io.quarkus.picocli.runtime.annotations.TopCommand;
import picocli.CommandLine;
import picocli.CommandLine.Spec;
import picocli.CommandLine.Model.CommandSpec;

@TopCommand
@CommandLine.Command(name = "generate", mixinStandardHelpOptions = true, subcommands = {
    OpenApiSpecGeneratorCommand.class }, requiredOptionMarker = '*')
public class SchemaGeneratorCommand implements Runnable {

  @Override
  public void run() {
    //Nothing here for now
  }
}
