package aws.json.schema.induction;

import com.google.gson.stream.JsonToken;

public interface PathListener {
    void notifyPath(String path, JsonToken type);
    void notifyPath(String path, JsonToken type, int len);
}
