# CRD JsonSchema Generator

A Java utility project to generate JSONSchema for the Kubernetes CRDS.

__NOTE__: Still under development

## Generate OpenAPI JSON Schema

```shell
java -jar target/crd-jsonschema-generator.jar \
  --dir=<your crd yamls directory>
```

e.g

```shell
java -jar target/quarkus-app/quarkus-run.jar openapispec -d ~/git/solo-io/gloo-mesh/install/helm/gloo-mesh-crds/crds
```

The command will generate the OpenAPI Spec JSON in `$PWD/scheme` directory

## Generate JSONSchema