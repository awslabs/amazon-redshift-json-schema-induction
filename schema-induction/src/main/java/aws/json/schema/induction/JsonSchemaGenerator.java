package aws.json.schema.induction;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;

import java.io.InputStream;

public class JsonSchemaGenerator {
    private JsonSchema schema;
    public JsonSchemaGenerator() {
        try {
            schema = getJsonSchemaFromClasspath("fhir.schema.json");

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public ObjectNode generateSchemaTree(SchemaNode root, String definitionName) {
        String key = "#/definitions/" + definitionName;
        JsonNode schemaNodeDefinition = schema.getRefSchemaNode(key);
        if (schemaNodeDefinition == null)
            throw new RuntimeException("Cannot find root level definition in schema : " + definitionName);
        ObjectNode rootSchema = generateCurrent(root, schemaNodeDefinition);
        rootSchema.put("name", definitionName);
        JsonNode description = schemaNodeDefinition.get("description");
        // Enriching the induced schema with description from the Json Schema
        if (description != null) {
            rootSchema.set("description", description);
            root.setDescription(description.asText());
        }

        return rootSchema;
    }

    private ObjectNode generateCurrent(SchemaNode current, JsonNode typeChild) {
        ObjectNode newSchemaNode = generateBasicAttributes(current);
        ObjectNode childNodes = JsonNodeFactory.instance.objectNode();
        newSchemaNode.set("class", typeChild);

        if (current.getChildren().isEmpty())
            return newSchemaNode;

        newSchemaNode.set("children", childNodes);
        for (SchemaNode child : current.getChildren()) {
            JsonNode properties = typeChild.get("properties");
            if (properties != null) {
                String localName = child.getLocalName();
                JsonNode property = properties.get(localName);
                if (property == null) {
                    ObjectNode childJsonNode = generateCurrentWithoutSchema(child);
                    childNodes.set(localName,childJsonNode);
                    continue;
                }
                JsonNode items = property.get("items");
                JsonNode ref = property.get("$ref");
                if (ref != null) {
                    String childKey = ref.asText();
                    JsonNode schemaNodeDefinition = schema.getRefSchemaNode(childKey);
                    ObjectNode childJsonNode = generateCurrent(child, schemaNodeDefinition);
                    childJsonNode.set("description",property.get("description"));
                    childNodes.set(localName,childJsonNode);
                } else if (items != null) {
                    ref = items.get("$ref");
                    String childKey = ref.asText();
                    JsonNode schemaNodeDefinition = schema.getRefSchemaNode(childKey);
                    ObjectNode childJsonNode = generateCurrent(child, schemaNodeDefinition);
                    childJsonNode.set("description",property.get("description"));
                    childNodes.set(localName,childJsonNode);
                } else {
                    // skip we already added all details to a leaf node
                }
            } else  {
                // skip we already added all details to a leaf node
            }
        }

        return newSchemaNode;
    }

    private ObjectNode generateBasicAttributes(SchemaNode current) {
        ObjectNode newSchemaNode = JsonNodeFactory.instance.objectNode();
        newSchemaNode.put("name", current.getLocalName());
        newSchemaNode.put("path", current.getPath());
        newSchemaNode.put("count", current.getOccurrences());
        newSchemaNode.put("level", current.getLevel());
        return newSchemaNode;
    }

    private ObjectNode generateCurrentWithoutSchema(SchemaNode current) {
        ObjectNode newSchemaNode = generateBasicAttributes(current);
        if (current.getChildren().isEmpty())
            return newSchemaNode;
        ArrayNode childNodes = JsonNodeFactory.instance.arrayNode();
        newSchemaNode.set("children", childNodes);
        for (SchemaNode child : current.getChildren()) {
            ObjectNode childJsonNode = generateCurrentWithoutSchema(child);
            childNodes.add(childJsonNode);
        }
        return newSchemaNode;
    }
    protected JsonSchema getJsonSchemaFromClasspath(String name) throws Exception {
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V6);
        InputStream is = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(name);
        JsonSchema schema = factory.getSchema(is);
        return schema;
    }

    public String prettyPrintJsonString(JsonNode jsonNode) {
        try {
            ObjectMapper mapper = new ObjectMapper();

            Object json = mapper.readValue(jsonNode.toString(), Object.class);
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
        } catch (Exception e) {
            return "Sorry, pretty print didn't work";
        }
    }
}
