package com.itls.json.serializers;

import com.google.gson.*;
import com.itls.ontology.elements.OntData;
import com.itls.ontology.model.OntologyModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.ArrayList;

import static com.itls.ontology.elements.Visibility.OFF;

public class OntologyModelHEBSerializer implements JsonSerializer<OntologyModel> {


    private static final Logger logger = LoggerFactory.getLogger(OntologyModelCirclePackingSerializer.class);
    private static final int DEFAULT_VALUE = 1;

    @Override
    public JsonElement serialize(OntologyModel model, Type typeOfSrc, JsonSerializationContext context) {
        JsonArray rootObject = new JsonArray();

        ArrayList<OntData> src = model.getDataSet();
        //building nodes json list
        for (OntData d : src) {
            if (d.getVisibility() == OFF) {
                continue;
            }
            JsonObject key = new JsonObject();
            key.addProperty("name", "Ontology." + d.getOntMetaData().getName() + ":" + d.getContents());
            JsonArray relatesTo = new JsonArray();
            key.add("relatesToObjs", relatesTo);
            for (OntData relOnt : d.getRelatesToObjs()) {
                if (relOnt.getVisibility() == OFF) {
                    continue;
                }
                relatesTo.add("Ontology." + relOnt.getOntMetaData().getName() + ":" + relOnt.getContents());
            }
            rootObject.add(key);

        }
        return rootObject;
    }
}
