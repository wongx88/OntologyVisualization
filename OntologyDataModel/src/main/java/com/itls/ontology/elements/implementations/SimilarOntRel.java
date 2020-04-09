package com.itls.ontology.elements.implementations;

import com.itls.ontology.elements.OntData;
import com.itls.ontology.elements.OntRelationship;

public class SimilarOntRel extends OntRelationship {

    public SimilarOntRel(OntData fromD, OntData toD, boolean bidirectional) {
        super(fromD, toD, bidirectional);
    }
}
