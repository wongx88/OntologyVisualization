package com.itls.ontology.elements.implementations;

import com.itls.ontology.elements.OntData;
import com.itls.ontology.elements.OntMetaData;

public class CommonData extends OntData {
    public CommonData(String sampleData) {
        super(sampleData);
    }

    public CommonData(String contents, OntMetaData md) {
        super(contents, md);
    }
}
