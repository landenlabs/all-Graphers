package com.landenlabs.test.JsonGson1;

import com.google.gson.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class GeometryDeserializer implements JsonDeserializer<GeoJsonFeature.Geometry> {

    @Override
    public GeoJsonFeature.Geometry deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        String type = jsonObject.get("type").getAsString();

        GeoJsonFeature.Geometry geometry = new GeoJsonFeature.Geometry();
        geometry.type = type;

        if ("Polygon".equals(type)) {
            geometry.coordinates = context.deserialize(jsonObject.get("coordinates"), new com.google.gson.reflect.TypeToken<List<List<List<Double>>>>() {}.getType());
        } else if ("MultiPolygon".equals(type)) {
            geometry.coordinates = context.deserialize(jsonObject.get("coordinates"), new com.google.gson.reflect.TypeToken<List<List<List<List<Double>>>>>() {}.getType());
        }

        return geometry;
    }
}