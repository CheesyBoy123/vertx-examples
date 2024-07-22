package io.vertx.example.jsonschema;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.json.schema.Draft;
import io.vertx.json.schema.JsonSchema;
import io.vertx.json.schema.JsonSchemaOptions;
import io.vertx.json.schema.OutputFormat;
import io.vertx.json.schema.OutputUnit;
import io.vertx.json.schema.SchemaRepository;

public class CreateJsonSchema {

  public static String BASE_DIRECTORY = "io/vertx/example/jsonschema/";
  public static SchemaRepository createSchemaRepository(JsonSchema schema) {
    return SchemaRepository.create(new JsonSchemaOptions()
      .setBaseUri("https://vertx.io")
      .setDraft(Draft.DRAFT202012)
      .setOutputFormat(OutputFormat.Basic))
      .dereference(schema);
  }

  public static Future<JsonSchema> loadSchema(Vertx vertx, String fileName) {
    return loadJsonFromFile(vertx, BASE_DIRECTORY + fileName)
      .compose(json -> Future.succeededFuture(JsonSchema.of(json)));
  }

  public static Future<JsonObject> loadJsonFromFile(Vertx vertx, String filePath) {
    return vertx.fileSystem().readFile(filePath)
      .compose(buffer -> Future.succeededFuture(buffer.toJsonObject()));
  }


  public static OutputUnit validateJson(SchemaRepository repository, JsonSchema schema, Object json) {
    return repository.validator(schema).validate(json);
  }

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();

    loadSchema(vertx, "basic_json_schema.json")
      .onSuccess(jsonSchema -> {
        System.out.println("Successfully loaded json schema.");
        SchemaRepository repository = createSchemaRepository(jsonSchema);
        System.out.println("Successfully loaded and dereferenced the json schema repository.");
        loadJsonFromFile(vertx, BASE_DIRECTORY + "/basic_json.json")
          .onSuccess(jsonObject -> {
            OutputUnit outputUnit = validateJson(repository, jsonSchema, jsonObject);
            System.out.println("Json validity: " + outputUnit.getValid() + " errors: " + outputUnit.getErrors());
            System.exit(0);
          })
          .onFailure(err -> {
            err.printStackTrace();
            System.exit(1);
          });
      })
      .onFailure(err -> {
        err.printStackTrace();
        System.exit(1);
      });

  }

}
