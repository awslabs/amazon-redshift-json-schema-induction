package aws.json.schema.induction;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class JsonPathsReader {

    private final List<PathListener> listeners;

    public JsonPathsReader(List<PathListener> listeners) {
        this.listeners = listeners;
    }

    public JsonPathsReader(JsonStructureBuilder builder) {
        this.listeners = new ArrayList<PathListener>();
        listeners.add(builder);
    }

    private void notifyPath(String path, JsonToken type) {
        for (PathListener l : listeners) {
            l.notifyPath(path,type);
        }
    }

    private void notifyPath(String path, JsonToken type, int len) {
        for (PathListener l : listeners) {
            l.notifyPath(path,type, len);
        }
    }

    public void readStream(InputStreamReader is, boolean isArray) throws IOException {
        JsonReader reader = new JsonReader(is);
        reader.setLenient(true);

        if (isArray)
            handleArray(reader);
        else
            handleObject(reader);


    }

    private void handleObject(JsonReader reader) throws IOException
    {
        reader.beginObject();
        while (reader.hasNext()) {
            JsonToken token = reader.peek();
            String path = reader.getPath();
            if (token.equals(JsonToken.BEGIN_ARRAY))
                handleArray(reader);
            else if (token.equals(JsonToken.BEGIN_OBJECT)) {
                handleObject(reader);
                return;
            }
            else if (token.equals(JsonToken.END_OBJECT)) {
                reader.endObject();
                return;
            } else
                handleNonArrayToken(reader, token);
        }

    }

    /**
     * Handle a json array. The first token would be JsonToken.BEGIN_ARRAY.
     * Arrays may contain objects or primitives.
     *
     * @param reader
     * @throws IOException
     */
    private void handleArray(JsonReader reader) throws IOException
    {
        reader.beginArray();
        while (true) {
            JsonToken token = reader.peek();
            if (token.equals(JsonToken.END_ARRAY)) {
                reader.endArray();
                break;
            } else if (token.equals(JsonToken.BEGIN_ARRAY)) {
                handleArray(reader);
            } else if (token.equals(JsonToken.BEGIN_OBJECT)) {
                handleObject(reader);
            } else if (token.equals(JsonToken.END_OBJECT)) {
                reader.endObject();
            } else
                handleNonArrayToken(reader, token);
        }
    }

    /**
     * Handle non array non object tokens
     *
     * @param reader
     * @param token
     * @throws IOException
     */
    private void handleNonArrayToken(JsonReader reader, JsonToken token) throws IOException
    {
        if (token.equals(JsonToken.NAME)) {
            String name = reader.nextName();
            String path = reader.getPath();
            int j = 0;
        }
        else if (token.equals(JsonToken.STRING)) {
            String path = reader.getPath();
            String x = reader.nextString();
            int len = x.length();
            notifyPath(path,JsonToken.STRING,len);
        }
        else if (token.equals(JsonToken.NUMBER)) {
            String path = reader.getPath();
            notifyPath(path, JsonToken.NUMBER);
            double x = reader.nextDouble();
        }
        else if (token.equals(JsonToken.BOOLEAN)) {
            String path = reader.getPath();
            notifyPath(path, JsonToken.BOOLEAN);
            boolean x = reader.nextBoolean();
        } else if (token.equals(JsonToken.NULL)) {
            String path = reader.getPath();
            notifyPath(path, JsonToken.NULL);
            reader.skipValue();
        }
        else {
            reader.skipValue();
        }
    }

}
