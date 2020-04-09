package com.itls.json.serializers;

import com.google.gson.*;
import com.itls.ontology.elements.HouseholdRelationship;
import com.itls.ontology.elements.OntData;
import com.itls.ontology.elements.OntRelationship;
import com.itls.ontology.model.OntologyModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.ArrayList;

import static com.itls.ontology.elements.Visibility.OFF;

public class OntologyModelFDGSerializer implements JsonSerializer<OntologyModel> {

    private static final Logger logger = LoggerFactory.getLogger(OntologyModelFDGSerializer.class);

    private boolean showKey = false;

    public OntologyModelFDGSerializer(boolean showKey) {
        this.showKey = showKey;
    }

    public OntologyModelFDGSerializer() {
    }

    @Override
    public JsonElement serialize(OntologyModel model, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject rootObject = new JsonObject();
        JsonArray jsonOntDataArray = new JsonArray();
        JsonArray jsonOntRelDataArray = new JsonArray();
        rootObject.add("nodes", jsonOntDataArray);
        rootObject.add("links", jsonOntRelDataArray);
        ArrayList<OntData> src = model.getDataSet();
        //building nodes json list
        for (OntData d : src) {
            if (d.getVisibility() == OFF) {
                continue;
            }
            JsonObject o = new JsonObject();
            String source_id = (showKey) ? d.getKeyValue() + "." + d.getContents() : d.getContents();
            o.addProperty("id", source_id);
//            if (null == ObjCodeEnum.get(d.getClass())) {
////                System.out.println(d);
////            }

            // o.addProperty("group", ObjCodeEnum.get(d.getClass()).getType());
            if (model.getKeyMD().equals(d.getOntMetaData()))
                // key group
                o.addProperty("group", 2);
            else
                o.addProperty("group", 1);
            jsonOntDataArray.add(o);

            //building links json list
            ArrayList<OntData> relatedData = d.getRelatesToObjs();
            for (OntData e : relatedData) {
                if (e.getVisibility() == OFF) {
                    continue;
                }
                JsonObject or = new JsonObject();
                or.addProperty("source", source_id);
                String target_id = createValue(e, e.getKeyValue());
                or.addProperty("target", target_id);
                or.addProperty("type", 1);
                or.addProperty("key", e.getKeyValue());
                jsonOntRelDataArray.add(or);
            }
        }

        // process relationship data
        processRelData(jsonOntRelDataArray, model.getOntRels());

        return rootObject;
    }

    private void processRelData(JsonArray jsonOntRelDataArray, ArrayList<OntRelationship> oRels) {
        for (OntRelationship rel : oRels) {
            JsonObject or = new JsonObject();
            or.addProperty("source", createValue(rel.getFromD(), rel.getFromD().getContents()));
            or.addProperty("target", createValue(rel.getToD(), rel.getToD().getContents()));
            if (rel instanceof HouseholdRelationship) {
                or.addProperty("type", 2);
                or.addProperty("score", ((HouseholdRelationship) rel).getConfidence());
            }
            jsonOntRelDataArray.add(or);

        }
    }

    private String createValue(OntData data, String key) {
        return (showKey) ? new StringBuffer().append(key).append(".").append(data.getContents()).toString() : data.getContents();
    }


}