package aws.json.schema.induction;

import com.google.gson.stream.JsonToken;

import java.io.PrintWriter;
import java.util.stream.Collectors;

public class RedshiftDDLGenerator {

    public void writeDDL(SchemaNode root, String tableName, String s3location, boolean isNDJSON, PrintWriter pw) {
        String ddl = generate(root, tableName, s3location, isNDJSON);
        pw.write(ddl);
    }

    public String generate(SchemaNode root, String tableName, String s3location, boolean isNDJSON) {

        StringBuffer ddlBuffer = new StringBuffer();

        ddlBuffer.append("create external table ");
        ddlBuffer.append(tableName);
        ddlBuffer.append("(\n");
        ddlBuffer.append(getColumns(root));
        ddlBuffer.append("\n)\n");
        ddlBuffer.append("row format serde 'org.openx.data.jsonserde.JsonSerDe'\n");
        ddlBuffer.append("with serdeproperties (");
        ddlBuffer.append("'dots.in.keys' = 'true'");
        ddlBuffer.append(",'mapping.requesttime' = 'requesttimestamp'");
        if (isNDJSON)
            ddlBuffer.append(",'strip.outer.array' = 'true'");
        ddlBuffer.append(")\nlocation '");
        ddlBuffer.append(s3location);
        ddlBuffer.append("'\n;\n");
        return ddlBuffer.toString();
    }

    static String quotes(String s) {
        return "\"" + s + "\"";
    }

    private String getColumns(SchemaNode root) {
        String cols = root.getChildren().stream().map(c -> String.format("\t%s %s",  quotes(c.getLocalName()), getNodeDDLType(c))).collect(Collectors.joining(",\n"));
        return cols;
    }

    private String getNodeDDLType(SchemaNode node) {
        StringBuffer typeBuffer = new StringBuffer();
        if (node.isArray()) {
            typeBuffer.append("array<");
            String type;
            if (node.getChildren().isEmpty()) {
                writeDDLForBasicType(node, typeBuffer);
            } else if (node.getChildren().size() == 1) {
                typeBuffer.append(getNodeDDLType(node.getChildren().get(0)));
            } else {
                writeStructDDLType(node, typeBuffer);
            }

            typeBuffer.append(">");
        } else if (node.getType() == JsonToken.BEGIN_OBJECT)  {
            writeStructDDLType(node, typeBuffer);
        } else {
            writeDDLForBasicType(node,typeBuffer);
        }

        return typeBuffer.toString();
    }

    private void writeStructDDLType(SchemaNode node, StringBuffer typeBuffer) {
        typeBuffer.append("struct<");
        typeBuffer.append(node.getChildren().stream().map(c -> String.format("%s: %s",  quotes(c.getLocalName()), getNodeDDLType(c))).collect(Collectors.joining(",")));
        typeBuffer.append(">");
    }

    private void writeDDLForBasicType(SchemaNode node, StringBuffer typeBuffer) {
        JsonToken type = node.getType();
        if (type == JsonToken.STRING) {
            int len = node.getStringLength();
            // double the size to be safe
            len = Math.min(65535, (int) (len * 1.2));
            typeBuffer.append("varchar(" + len + ")");
        } else if (type == JsonToken.BOOLEAN) {
            typeBuffer.append("boolean");
        } else if (type == JsonToken.NUMBER) {
            typeBuffer.append("double precision");
        } else {
            throw new RuntimeException("Unfamiliar datatype " + type.toString());
        }
    }
}
