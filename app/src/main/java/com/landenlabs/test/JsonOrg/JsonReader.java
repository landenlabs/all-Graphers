package com.landenlabs.test.JsonOrg;

import org.json.JSONException;
import org.json.JSONObject;

public class JsonReader {

    String jsonStr;
    boolean throwError;

    public JSONObject jsonObject;

    public JsonReader(String jsonStr, boolean throwError)  {
        this.jsonStr = jsonStr;
        this.throwError = throwError;

        try {
            jsonObject = new JSONObject(jsonStr);
        } catch (JSONException e) {
            if (throwError)
                throw new RuntimeException(e);
        }
    }
}
