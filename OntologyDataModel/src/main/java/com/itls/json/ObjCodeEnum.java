package com.itls.json;

import com.itls.ontology.elements.implementations.AddressData;
import com.itls.ontology.elements.implementations.CardData;
import com.itls.ontology.elements.implementations.CommonData;
import com.itls.ontology.elements.implementations.NameData;

public enum ObjCodeEnum {
    //AddressData.class:1
    Class1(1, AddressData.class),
    Class2(2, NameData.class),
    Class3(3, CardData.class),
    Class4(4, CommonData.class);

    private final Class myClass;
    private final int type;

    ObjCodeEnum(int type, Class myClass) {
        this.myClass = myClass;
        this.type = type;
    }

    public static ObjCodeEnum get(Class theClass) {
        for (ObjCodeEnum ft : values())
            if (ft.getDataObj().equals(theClass))
                return ft;
        return null;
    }

    public Class getDataObj() {
        return myClass;
    }

    public int getType() {
        return type;
    }
}
