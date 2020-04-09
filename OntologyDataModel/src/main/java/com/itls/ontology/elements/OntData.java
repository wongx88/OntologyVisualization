package com.itls.ontology.elements;

import com.google.gson.*;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Optional;

public abstract class OntData {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(OntData.class);

    @SerializedName(value = "name")
    private String contents;

    private ArrayList<OntData> relatesToObjs = new ArrayList<>();
    //defaulting
    private Visibility visibility = Visibility.ON;

    private String key;

    private OntMetaData ontMetaData;

    public OntData() {

    }

    public OntMetaData getOntMetaData() {
        return ontMetaData;
    }

    public void setOntMetaData(OntMetaData ontMetaData) {
        this.ontMetaData = ontMetaData;
    }


    public OntData(String contents, OntMetaData md) {
        setOntMetaData(md);
        setContents(contents);
    }

    public OntData(String contents) {
        this.contents = contents;
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public Visibility getVisibility() {
        return this.visibility;
    }

    public void setVisibility(Visibility v) {
        this.visibility = v;
    }

    public void relatesTo(OntData anotherOData) {
        this.relatesToObjs.add(anotherOData);
    }

//    public OntRelationship relatesTo(OntData anotherOData, OntRelationship theRel) {
//        this.relatesToObjs.add(anotherOData);
//        return theRel;
//    }
//
//    public OntRelationship shareClassWith(OntData anotherOData) {
//        return anotherOData;
//    }


    public boolean isKey() {
        return this.getKey().equals(this.getOntMetaData());
    }

    public OntMetaData belongsTo(OntMetaData ontMetaData) {
        this.ontMetaData = ontMetaData;
        return ontMetaData;
    }

    public ArrayList<OntData> getRelatesToObjs() {
        return relatesToObjs;
    }

    public String toString() {
        return this.contents;
    }

//    public String getCustomJSON() {
//        Gson customGson = getCustomJSONBuilder();
//        String customJSON = customGson.toJson(this);
//        return customJSON;
//    }

    public static Gson getCustomJSONBuilder() {
        JsonSerializer<ArrayList<OntData>> serializer = new JsonSerializer<ArrayList<OntData>>() {
            public JsonElement serialize(ArrayList<OntData> src, Type typeOfSrc, JsonSerializationContext context) {
                JsonArray jsonOntData = new JsonArray();
                ArrayList<String> relatedData = new ArrayList<String>(src.size());
                for (OntData d : src) {
                    jsonOntData.add("Ontology." + d.getContents());
                    //  relatedData.add("" + d.getContents());
                }
                return jsonOntData;
            }
        };
        JsonSerializer<String> strSerializer = new JsonSerializer<String>() {
            public JsonElement serialize(String src, Type typeOfSrc, JsonSerializationContext context) {

                return new JsonPrimitive("Ontology." + src);
            }
        };
        Type OntDataListType = new TypeToken<ArrayList<OntData>>() {
        }.getType();
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(OntDataListType, serializer);

        gsonBuilder.registerTypeAdapter(String.class, strSerializer);

        return gsonBuilder.create();
    }

    public OntMetaData getKey() {
        if (getOntMetaData() != null) {
            //get key from metadata getRelatesToObjs
            return getOntMetaData().getKey();
        }
        return null;
    }


    public String getKeyValue() {
        if (this.key == null) {
            this.key = lookupKeyValue();
        }
        return this.key;
    }

    /**
     * Lookup key based on key-meta
     *
     * @return key value
     */
    private String lookupKeyValue() {
        if (getOntMetaData() != null) {
            OntMetaData md = getOntMetaData();
            if (md.getName().equals(md.getKey().getName())) {
                return getContents();
            }
            Optional<OntData> result = getRelatesToObjs().stream().filter(ontData -> ontData.getOntMetaData().getName().equals(md.getKey().getName())).findAny();
            return result.get().getContents();
        } else
            return null;
    }


    @Override
    public boolean equals(Object o) {
        //  logger.info("OntData Equality method invoked");
        OntData od;
        if (!(o instanceof OntData)) {
            return false;
        } else {
            od = (OntData) o;
            if (this.getContents().equals(od.getContents())) {
                if (this.getOntMetaData() == null || od.getOntMetaData() == null)
                    return true;
                else if (this.getOntMetaData().getName().equals(od.getOntMetaData().getName())) {
                    if (this.getKey() == null || od.getKey() == null) {
                        return true;
                    } else
                        return this.getKey().equals(od.getKey());
                }
            }
        }
        return false;
    }

}
