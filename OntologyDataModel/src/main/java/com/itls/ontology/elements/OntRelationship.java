package com.itls.ontology.elements;

public abstract class OntRelationship {

    private OntData fromD;
    private OntData toD;

    public OntRelationship(OntData fromD, OntData toD, boolean bidirectional) {
        this.fromD = fromD;
        this.toD = toD;
    }

    public OntData getFromD() {
        return fromD;
    }

    public void setFromD(OntData fromD) {
        this.fromD = fromD;
    }

    public OntData getToD() {
        return toD;
    }

    public void setToD(OntData toD) {
        this.toD = toD;
    }


}
