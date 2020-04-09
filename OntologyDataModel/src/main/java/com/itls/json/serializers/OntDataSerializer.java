package com.itls.json.serializers;

import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.itls.ontology.elements.OntData;

import java.lang.reflect.Type;

public class OntDataSerializer implements JsonSerializer<OntData> {


    @Override
    public JsonElement serialize(OntData ontData, Type type, JsonSerializationContext jsonSerializationContext) {

        return null;
    }
}