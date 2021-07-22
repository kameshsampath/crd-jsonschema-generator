package dev.kameshs;

import io.fabric8.kubernetes.api.model.apiextensions.v1beta1.CustomResourceDefinition;
import io.fabric8.kubernetes.api.model.apiextensions.v1beta1.JSONSchemaProps;
import io.fabric8.kubernetes.client.utils.Serialization;
import io.vertx.core.json.JsonObject;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;

@Command(mixinStandardHelpOptions = true, description = "Generates OpenApiSpec JSON", name = "openapispec")
public class OpenApiSpecGeneratorCommand implements Runnable {

  final static Logger LOGGER = Logger.getLogger(
      OpenApiSpecGeneratorCommand.class.getName());

  @ParentCommand
  private CrdToOpenAPIJSON parent;

  @CommandLine.Option(names = {"-d",
      "--dir"}, description = "The CRDs Directory", required = true)
  File crdDir;

  @CommandLine.Option(names = {"-o",
      "--out"}, description = "The Output Directory where OpenAPISpec JSON to be generated")
  File outDir;

  @Override
  public void run() {
    List<Path> crdFiles = Collections.emptyList();

    if (outDir == null) {
      outDir = new File(System.getProperty("user.dir"));
      if (!outDir.exists()) {
        outDir.mkdirs();
      }
    }

    try (Stream<Path> walk = Files.walk(crdDir.toPath())) {
      crdFiles = walk.filter(Files::isRegularFile)
                     .filter(p -> p.getFileName()
                                   .toString()
                                   .endsWith(".yaml"))
                     .collect(Collectors.toList());
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, "Error collecting file", e);
      System.exit(1);
    }
    crdFiles.forEach(p -> {
      try {
        LOGGER.info("Processing file " + p);
        FileInputStream fin = new FileInputStream(p.toFile());
        Object k8sResource = Serialization.unmarshal(fin,
            Collections.emptyMap());
        //Check if the collected resource is Single YAML doc or multi YAML doc
        if (k8sResource instanceof CustomResourceDefinition) {
          CustomResourceDefinition crd = (CustomResourceDefinition) k8sResource;
          try {
            createOpenAPISpecJson(crd);
          } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error processing file %s",
                new Object[]{p});
          }
        } else {
          List<CustomResourceDefinition> customResourceDefinitions =
              (List<CustomResourceDefinition>) k8sResource;
          customResourceDefinitions.forEach(crd -> {
            try {
              createOpenAPISpecJson(crd);
            } catch (IOException e) {
              LOGGER.log(Level.SEVERE, "Error processing file %s",
                  new Object[]{p});
              System.exit(1);
            }
          });
        }

      } catch (FileNotFoundException e) {
        LOGGER.log(Level.SEVERE, "Error processing file", e);
        System.exit(1);
      }
    });

  }

  private void createOpenAPISpecJson(CustomResourceDefinition crd)
      throws IOException {
    Path schemePath = Path.of(outDir.getAbsolutePath(), "scheme", crd.getSpec()
                                                                     .getGroup());
    if (!Files.exists(schemePath)) {
      Files.createDirectories(schemePath);
    }

    LOGGER.log(Level.ALL, "Processing CRD" + crd);
    JsonObject definitions = new JsonObject();
    JsonObject crdSpecJson = new JsonObject();
    JSONSchemaProps schemaProps = crd.getSpec()
                                     .getValidation()
                                     .getOpenAPIV3Schema();

    JSONSchemaProps crdSpec = schemaProps.getProperties()
                                         .get("spec");
    crdSpecJson.put("description", crdSpec.getDescription());
    crdSpecJson.put("properties", crdSpec.getProperties());

    //Api Version
    String apiVersion = crd.getSpec()
                           .getVersions()
                           .get(0)
                           .getName();
    //Group
    String apiGroup = crd.getSpec()
                         .getGroup();

    //Kind
    String apiKind = crd.getSpec()
                        .getNames()
                        .getKind();

    String crdFqn = apiGroup + "." + apiVersion + "." + apiKind;

    definitions.put(crdFqn, crdSpecJson);
    JsonObject openAPISpec = new JsonObject();
    openAPISpec.put("definitions", definitions);
    String crdJsonFileName = apiVersion + "_" + apiKind
        + ".json";
    Path crdOpenAPISpecPath = Path.of(schemePath.toString(), crdJsonFileName);
    FileWriter crdWriter = new FileWriter(crdOpenAPISpecPath.toFile());
    crdWriter.write(openAPISpec.encodePrettily());
  }
}
