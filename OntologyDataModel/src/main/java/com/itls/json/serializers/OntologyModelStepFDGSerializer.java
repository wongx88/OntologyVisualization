package com.itls.json.serializers;

import com.google.gson.*;
import com.itls.ontology.elements.OntData;
import com.itls.ontology.model.OntologyModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.ArrayList;

import static com.itls.ontology.elements.Visibility.OFF;

public class OntologyModelStepFDGSerializer implements JsonSerializer<OntologyModel> {

    private static final Logger logger = LoggerFactory.getLogger(OntologyModelStepFDGSerializer.class);

    @Override
    public JsonElement serialize(OntologyModel model, Type typeOfSrc, JsonSerializationContext context) {
        JsonArray rootArray = new JsonArray();

        ArrayList<OntData> src = model.getDataSet();
        //building nodes json list
        for (OntData d : src) {
            if (d.getVisibility() == OFF) {
                continue;
            }

            if (d.getOntMetaData().getKey() != null && d.getOntMetaData().getName().equals(d.getOntMetaData().getKey().getName())) {

                JsonObject rootObject = new JsonObject();
                JsonObject expand = new JsonObject();
                rootObject.add("expand", expand);
                rootObject.addProperty("fix", d.getKeyValue());
                JsonArray linkList = new JsonArray();
                JsonArray nodeList = new JsonArray();
                nodeList.add(d.getContents());
                for (OntData relOnt : d.getRelatesToObjs()) {
                    JsonArray in = new JsonArray();
                    in.add(relOnt.getContents());
                    in.add(d.getContents());
                    nodeList.add(relOnt.getContents());
                    linkList.add(in);
                }
                expand.add("links", linkList);
                expand.add("nodes", nodeList);
                rootArray.add(rootObject);
            }

        }
        return rootArray;
    }
}