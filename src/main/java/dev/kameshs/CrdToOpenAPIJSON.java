package dev.kameshs;

import picocli.CommandLine;
import picocli.CommandLine.Spec;
import picocli.CommandLine.Model.CommandSpec;

@CommandLine.Command(name = "generate", mixinStandardHelpOptions = true, subcommands = {
    OpenApiSpecGeneratorCommand.class }, requiredOptionMarker = '*')
public class CrdToOpenAPIJSON {

  @Spec
  CommandSpec spec;

  public static void main(String[] args) {
    int exitCode = new CommandLine(new CrdToOpenAPIJSON()).execute(args);
    System.exit(exitCode);
  }

}
