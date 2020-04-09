package com.itls.ontology.elements;

public class HouseholdRelationship extends OntRelationship {

    private float confidence;

    public HouseholdRelationship(OntData fromD, OntData toD, float confidence) {
        super(fromD, toD, true);
        setConfidence(confidence);
    }


    public float getConfidence() {
        return confidence;
    }

    public void setConfidence(float confidence) {
        this.confidence = confidence;
    }
}
