package com.itls.json.serializers;

import com.google.gson.*;
import com.itls.ontology.elements.OntData;
import com.itls.ontology.model.OntologyModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.ArrayList;

import static com.itls.ontology.elements.Visibility.OFF;

public class OntologyModelCirclePackingSerializer implements JsonSerializer<OntologyModel> {

    private static final Logger logger = LoggerFactory.getLogger(OntologyModelCirclePackingSerializer.class);
    private static final int DEFAULT_VALUE = 1;
    public static final String NAME = "name";
    public static final String ROOT = "OntologyModel";
    public static final String CHILDREN = "children";

    @Override
    public JsonElement serialize(OntologyModel model, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject rootObject = new JsonObject();
        rootObject.addProperty(NAME, ROOT);
        JsonArray first = new JsonArray();
        JsonObject keyRoot = new JsonObject();
        first.add(keyRoot);
        rootObject.add(CHILDREN, first);


        keyRoot.addProperty(NAME, model.getKeyMD().getName());
        JsonArray keys = new JsonArray();
        keyRoot.add(CHILDREN, keys);
        //build the key metadata

        ArrayList<OntData> src = model.getDataSet();
        //building nodes json list
        for (OntData d : src) {
            if (d.getVisibility() == OFF) {
                continue;
            }

            if (d.getOntMetaData().getKey() != null && d.getOntMetaData().getName().equals(d.getOntMetaData().getKey().getName())) {

                JsonObject key = new JsonObject();
                key.addProperty(NAME, d.getContents());
                JsonArray values = new JsonArray();
                key.add(CHILDREN, values);

                for (OntData relOnt : d.getRelatesToObjs()) {
                    JsonObject value = new JsonObject();
                    value.addProperty(NAME, relOnt.getOntMetaData().getName());
                    JsonArray contentArray = new JsonArray();
                    value.add(CHILDREN, contentArray);
                    JsonObject content = new JsonObject();
                    content.addProperty(NAME, relOnt.getContents());
                    content.addProperty("value", DEFAULT_VALUE);
                    contentArray.add(content);
//                    value.addProperty("value", relOnt.getContents());
                    values.add(value);
                }
                keys.add(key);
            }

        }
        return rootObject;
    }
}