package aws.json.schema.induction;

import com.google.gson.stream.JsonToken;

import java.util.ArrayList;
import java.util.List;

public class SchemaNode {
    private final String localName;
    private List<SchemaNode> children = new ArrayList<>();
    private String path;
    private int occurrences;
    private int level;
    private JsonToken type;
    private String description;
    private int stringLength = -1;

    public SchemaNode(String path,JsonToken type) {
        this.path = path;
        this.occurrences = 1;
        this.level = 1;
        this.type = type;
        String parts[] = path.split("\\.");
        localName = isArray() ? parts[parts.length -1].substring(0,parts[parts.length -1].length()-2) : parts[parts.length -1];
    }

    public void addChild(SchemaNode child) {
        if (this == child)
            throw new RuntimeException("Recursive tree");
        children.add(child);
        child.level = this.level + 1;
    }

    public boolean isArray() {
        return path.endsWith("[]");
    }

    public String getLocalName() {
        return localName;
    }

    public String toString() {
        return path;
    }

    public List<SchemaNode> getChildren() {
        return children;
    }

    public String getPath() {
        return path;
    }

    public int getOccurrences() {
        return occurrences;
    }

    public int getLevel() {
        return level;
    }

    public JsonToken getType() {
        return type;
    }

    public void increaseOccurrences() {
        occurrences++;
    }

    public void generalizeTypes(JsonToken first, JsonToken second) {
        type  = JsonToken.STRING;
    }

    public String getDescription() {
        return description;
    }

    public int getStringLength() {
        return stringLength;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStringLength(int stringLength) {
        this.stringLength = stringLength;
    }
}
