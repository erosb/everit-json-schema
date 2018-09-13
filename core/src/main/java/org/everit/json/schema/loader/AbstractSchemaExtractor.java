package org.everit.json.schema.loader;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.everit.json.schema.ArraySchema;
import org.everit.json.schema.NumberSchema;
import org.everit.json.schema.ObjectSchema;
import org.everit.json.schema.Schema;

public abstract class AbstractSchemaExtractor implements SchemaExtractor {

    static final List<String> NUMBER_SCHEMA_PROPS = asList("minimum", "maximum",
            "exclusiveMinimum", "exclusiveMaximum", "multipleOf");

    static final List<String> STRING_SCHEMA_PROPS = asList("minLength", "maxLength",
            "pattern", "format");

    protected JsonObject schemaJson;

    private Set<String> consumedKeys;

    final SchemaLoader defaultLoader;

    private ExclusiveLimitHandler exclusiveLimitHandler;

    public AbstractSchemaExtractor(SchemaLoader defaultLoader) {
        this.defaultLoader = requireNonNull(defaultLoader, "defaultLoader cannot be null");
    }

    @Override
    public final ExtractionResult extract(JsonObject schemaJson) {
        this.schemaJson = requireNonNull(schemaJson, "schemaJson cannot be null");
        this.exclusiveLimitHandler = ExclusiveLimitHandler.ofSpecVersion(config().specVersion);
        consumedKeys = new HashSet<>(schemaJson.keySet().size());
        return new ExtractionResult(consumedKeys, extract());
    }

    void keyConsumed(String key) {
        if (schemaJson.keySet().contains(key)) {
            consumedKeys.add(key);
        }
    }

    JsonValue require(String key) {
        keyConsumed(key);
        return schemaJson.require(key);
    }

    Optional<JsonValue> maybe(String key) {
        keyConsumed(key);
        return schemaJson.maybe(key);
    }

    boolean containsKey(String key) {
        return schemaJson.containsKey(key);
    }


    boolean schemaHasAnyOf(Collection<String> propNames) {
        return propNames.stream().anyMatch(schemaJson::containsKey);
    }

    LoaderConfig config() {
        return schemaJson.ls.config;
    }

    ObjectSchema.Builder buildObjectSchema() {
        config().specVersion.objectKeywords().forEach(this::keyConsumed);
        return new ObjectSchemaLoader(schemaJson.ls, config(), defaultLoader).load();
    }

    ArraySchema.Builder buildArraySchema() {
        config().specVersion.arrayKeywords().forEach(this::keyConsumed);
        return new ArraySchemaLoader(schemaJson.ls, config(), defaultLoader).load();
    }

    NumberSchema.Builder buildNumberSchema() {
        PropertySnifferSchemaExtractor.NUMBER_SCHEMA_PROPS.forEach(this::keyConsumed);
        NumberSchema.Builder builder = NumberSchema.builder();
        maybe("minimum").map(JsonValue::requireNumber).ifPresent(builder::minimum);
        maybe("maximum").map(JsonValue::requireNumber).ifPresent(builder::maximum);
        maybe("multipleOf").map(JsonValue::requireNumber).ifPresent(builder::multipleOf);
        maybe("exclusiveMinimum").ifPresent(exclMin -> exclusiveLimitHandler.handleExclusiveMinimum(exclMin, builder));
        maybe("exclusiveMaximum").ifPresent(exclMax -> exclusiveLimitHandler.handleExclusiveMaximum(exclMax, builder));
        return builder;
    }

    public abstract List<Schema.Builder<?>> extract();
}
