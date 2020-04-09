package com.itls.ontology.elements;

import com.itls.ontology.interfaces.OntDataInterface;

public abstract class OntMetaData implements OntDataInterface {
    private final Object MMDType;
    private OntMetaData key;
    private final String name;

    private String switcher = "ON";


    public OntMetaData(MMDType theMMDType, String name) {
        this.MMDType = theMMDType;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    protected boolean shareKeyWith(OntMetaData theOMData) {
        return theOMData.getName().equals(this.getKey().getName());
    }

    protected void isKeyOf(OntMetaData theOMData) {
    }

    public OntMetaData getKey() {
        return key;
    }

    public void setKey(OntMetaData key) {
        this.key = key;
    }

    public Object getMMDType() {
        return MMDType;
    }

    @Override
    public boolean equals(Object o) {
        //  logger.info("OntData Equality method invoked");
        OntMetaData omd;
        if (!(o instanceof OntMetaData)) {
            return false;
        } else {
            omd = (OntMetaData) o;
            return this.getName().equals(omd.getName()) && this.MMDType == omd.MMDType;
        }
    }
}
