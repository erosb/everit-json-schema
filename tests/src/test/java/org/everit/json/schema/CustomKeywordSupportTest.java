package org.everit.json.schema;

import java.util.List;

import org.everit.json.schema.loader.AbstractSchemaExtractor;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.Test;

class ProductSchemaExtractor extends AbstractSchemaExtractor {

    ProductSchemaExtractor(SchemaLoader defaultLoader) {
        super(defaultLoader);
    }

    @Override public List<Schema.Builder<?>> extract() {
        return null;
    }
}

public class CustomKeywordSupportTest {

    private JSONObject readSchemaJson() {
        return new JSONObject(new JSONTokener(CustomKeywordSupportTest.class
                .getResourceAsStream("schema-with-product-keywords.json")));
    }

    @Test
    public void test() {
        SchemaLoader.builder().schemaJson(readSchemaJson())
                .registerSchemaExtractor(ProductSchemaExtractor::new)
                .build();
    }

}
