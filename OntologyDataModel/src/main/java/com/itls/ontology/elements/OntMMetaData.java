package com.itls.ontology.elements;

public abstract class OntMMetaData {
    private String horizontal;
    protected String vertical;
    private String key;

    protected OntMMetaData(String horizontal, String key) {
        this.horizontal = horizontal;
        this.key = key;
    }
}
