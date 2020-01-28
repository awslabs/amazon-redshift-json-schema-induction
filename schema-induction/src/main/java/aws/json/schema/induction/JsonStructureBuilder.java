package aws.json.schema.induction;

import com.google.gson.stream.JsonToken;

import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.Map;

public class JsonStructureBuilder implements PathListener {


    private Map<String, SchemaNode> paths = new LinkedHashMap<>();
    private SchemaNode root;

    public JsonStructureBuilder(boolean isArray) {
        if (isArray) {
           root = new SchemaNode("$[]",JsonToken.BEGIN_ARRAY);
        } else {
           root = new SchemaNode("$",JsonToken.BEGIN_OBJECT);
        }
        addNodeToPaths(root);
    }

    public void addNodeToPaths(SchemaNode node){
        paths.put(node.getPath(), node);
    }

    @Override
    public void notifyPath(String path, JsonToken type, int len) {
        String genericPath = generalizePath(path);

        if (paths.containsKey(genericPath)) {
            SchemaNode current = paths.get(genericPath);
            current.increaseOccurrences();
            if (current.getType() != type) {
                current.generalizeTypes(current.getType(), type);
            }
            if (len != -1 && current.getStringLength() < len)
                current.setStringLength(len);
        } else {
            SchemaNode current = new SchemaNode(genericPath,type);
            addNodeToPaths(current);
            addParentsIfNotExists(current);
            current.setStringLength(len);
        }
    }
    @Override
    public void notifyPath(String path, JsonToken type) {
        notifyPath(path,type,-1);
    }

    //$[].item[].procedureLinkId[]
    private void addParentsIfNotExists(SchemaNode current) {
        String parts[] = current.getPath().split("\\.");
        SchemaNode parent = addParentsByPath(parts);
        parent.addChild(current);
    }

    private SchemaNode addParentsByPath(String[] parts) {
        SchemaNode parent = null;
        SchemaNode child = null;
        for (String currentPart : parts) {
            String currentPath = parent == null ? currentPart : parent.getPath() + "." + currentPart;
            if (paths.containsKey(currentPath)) {
                child = paths.get(currentPath);
            } else {
                JsonToken type;
                if (currentPath.endsWith("[]"))
                    type = JsonToken.BEGIN_ARRAY;
                else
                    type = JsonToken.BEGIN_OBJECT;

                child = new SchemaNode(currentPath,type);
                addNodeToPaths(child);
                if (parent != null) {
                    parent.addChild(child);
                }
            }
            if (currentPart != parts[parts.length-1])
                parent = child;

        }
        return parent;
    }

    public static String generalizePath(String path) {
        String genericPath = path.replaceAll("\\[\\d*\\]","\\[\\]");
        return genericPath;
    }

    public void writePaths(PrintWriter pw) {
        System.out.println("Total number of paths: " + paths.size());
        paths.entrySet().stream().forEach( e -> pw.println(String.format("%s, %s, %s",e.getKey(),e.getValue().getType().toString(),e.getValue().getOccurrences())));
    }

    public void writeTree(PrintWriter printWriter) {
        printNode(root,printWriter);
    }

    private void printNode(SchemaNode current, PrintWriter pw) {
        for (int i=0;i<current.getLevel();i++)
            pw.print("  ");
        pw.println(String.format("%s, %s, %s",current.getPath(),current.getType().toString(),current.getOccurrences()));
        pw.flush();
        current.getChildren().stream().forEach(c -> printNode(c,pw));
    }



    public Map<String, SchemaNode> getPaths() {
        return paths;
    }

    public SchemaNode getRoot() {
        return root;
    }
}
