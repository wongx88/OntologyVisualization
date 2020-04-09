package com.itls.json;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonSerializer;
import com.itls.ontology.model.OntologyModel;

public class CustomJSONBuilderFactory {

    public static GsonBuilder getCustomJSONBuilder(JsonSerializer<OntologyModel> type) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        // Type OntDataListType = new TypeToken<ArrayList<OntData>>() {}.getType();
        gsonBuilder.registerTypeAdapter(OntologyModel.class, type);
        return gsonBuilder;
    }

    public static String createJSONString(JsonSerializer<OntologyModel> type, Object a) {
        return getCustomJSONBuilder(type).create().toJson(a);
    }
}
