package com.itls.ontology.model;

import com.itls.ontology.elements.*;
import com.itls.ontology.elements.implementations.*;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class OntologyModelTest {
    private static final Logger logger = LoggerFactory.getLogger(OntologyModelTest.class);

    private static CSVParser csvParser, csvParser2;
    private static OntologyModel cust_data_model, census_data_model;
    private static List<OntRelationship> household_rel;
    private static String[] header;
    private static OntologyModelFactory mf = new OntologyModelFactory();

    @org.junit.jupiter.api.BeforeAll
    static void setUp() throws IOException {
        Path cust = Paths.get("latest/Customer_Transaction_Data_Consolidated_with_Proensity_MultiCategory.csv");
        cust_data_model = mf.createModel(cust, 40, "SSN");
        Path sellbuy = Paths.get("latest/Transactional_Data_3-25.csv");
        census_data_model = mf.createModel(sellbuy, 3, 0);
        //</editor-fold>
        Path hh = Paths.get("latest/HouseHold_v2.csv");
        household_rel = mf.createRelationships(hh, 2, "H");
    }

    @org.junit.jupiter.api.Test
    @Disabled
    void addOntData() {
        int counter = 0;
        for (CSVRecord record : csvParser) {
            if (counter > 6) {
                break;
            }
            String name = record.get("Card Holder's Name");
            String address = record.get("Billing Address");
            String card = record.get("Card Type");
            String amount = record.get("Amount");
            NameData n = new NameData(name);
            n.setVisibility(Visibility.ON);
            CardData c = new CardData(card);
            AddressData a = new AddressData(address);
            n.relatesTo(a);
            n.relatesTo(c);
            cust_data_model.addOntData(n);
            cust_data_model.addOntData(a);
            cust_data_model.addOntData(c);
            counter++;
        }
        logger.info(cust_data_model.toFDGJSON(true));
    }

    @org.junit.jupiter.api.Test
    @Disabled
    void visibilityTest() {
        int counter = 0;
        for (CSVRecord record : csvParser) {
            if (counter > 15) {
                break;
            }
            String name = record.get("Card Holder's Name");
            String address = record.get("Billing Address");
            String card = record.get("Card Type");
            String amount = record.get("Amount");
            NameData n = new NameData(name);
            CardData c = new CardData(card);
            c.setVisibility(Visibility.OFF);
            AddressData a = new AddressData(address);
            n.relatesTo(a);
            n.relatesTo(c);
            cust_data_model.addOntData(n);
            cust_data_model.addOntData(a);
            cust_data_model.addOntData(c);
            counter++;
        }
        logger.info(cust_data_model.toFDGJSON(true));
    }


    @org.junit.jupiter.api.Test
    @Disabled
    void addMetaDataTest() {
        int counter = 0;
        OntMetaData SSNMD = new OntMetaDataImpl(MMDType.Vertical, "SSN");
        SSNMD.setKey(SSNMD);
        OntMetaData nameMD = new OntMetaDataImpl(MMDType.Vertical, "Name");
        nameMD.setKey(SSNMD);
        OntMetaData addressMD = new OntMetaDataImpl(MMDType.Vertical, "Address");
        addressMD.setKey(SSNMD);

        for (CSVRecord record : csvParser) {
            if (counter > 15) {
                break;
            }
            String name = record.get("Card Holder's Name");
            String address = record.get("Billing Address");
            String ssn = record.get("Card Number");
            CommonData common = new CommonData(name, nameMD);
            CommonData common2 = new CommonData(address, addressMD);
            CommonData common3 = new CommonData(ssn, SSNMD);
            cust_data_model.addOntData(common);
            cust_data_model.addOntData(common2);
            cust_data_model.addOntData(common3);
            cust_data_model.relatesRelatesToData(new ArrayList<>(Arrays.asList(common, common2, common3)));
            counter++;

        }
        cust_data_model.refreshDataToMetaDataMap();
        System.out.println(cust_data_model.findSimilar("Card Holder's Name"));

    }


    @org.junit.jupiter.api.Test
    void distinctFunTest() {
        //cust_data_model.deduplicateKeysInDataset();
        OntologyModel a = new OntologyModel();
        CommonData common = new CommonData("727-40-3153.Mrs", null);
        CommonData common2 = new CommonData("727-40-3153.Mrs", null);
        ArrayList<OntData> l = new ArrayList<>();
        l.add(common);
        l.add(common2);
        a.setDataSet(l);
        a.deduplicateDataset();
        logger.info(a.getDataSet().toString());
        //model.getDataSet().stream().forEach(ontData -> System.out.println(ontData.getRelatesToObjs()));
        // logger.info(model.findSimilar("Card Holder's Name").toFDGJSON());
    }


    @org.junit.jupiter.api.Test
    void modelTest() {
        //cust_data_model.deduplicateKeysInDataset();
        cust_data_model.deduplicateDataset();
        logger.info(cust_data_model.getDataSet().toString());
        logger.info(cust_data_model.toFDGJSON(true));
        //model.getDataSet().stream().forEach(ontData -> System.out.println(ontData.getRelatesToObjs()));
        // logger.info(model.findSimilar("Card Holder's Name").toFDGJSON());
    }

    @org.junit.jupiter.api.Test
    void distinctTest() {
        cust_data_model.deduplicateKeysInDataset();
        logger.info(cust_data_model.toStepFDGJSON());
        //model.getDataSet().stream().forEach(ontData -> System.out.println(ontData.getRelatesToObjs()));
        // logger.info(model.findSimilar("Card Holder's Name").toFDGJSON());
    }

    @org.junit.jupiter.api.Test
    void levelOneFilteringNameTest() {
        OntologyModel newModel = cust_data_model.findSimilar("Last Name");
        //   model.getDataSet().stream().forEach(ontData -> System.out.println(ontData.getRelatesToObjs()));
        logger.info(newModel.toFDGJSON(false));

    }

    @org.junit.jupiter.api.Test
    void levelTwoFilteringAddressTest() {
        OntologyModel newModel = cust_data_model.findSimilar("Card Holder's Name");
        OntologyModel newModel2 = newModel.findSimilar("Billing Address");
        logger.info(newModel2.toFDGJSON(false));
    }

    @org.junit.jupiter.api.Test
    void hideByTypeTest() {
        String[] hidList = {"SSN", "First Name", "Last Name", "Residential_Street Address"};
        cust_data_model.showMMD(Arrays.asList(hidList));
        cust_data_model.deduplicateDataset();
        //  cust_data_model.getDataSet().stream().filter(d -> d.getVisibility() == Visibility.ON).forEach(System.out::println);
        logger.info(cust_data_model.toFDGJSON(true));
    }

    @Test
    void attachRelTest() {
        String[] showList = {"SSN", "First Name", "Last Name", "Gender", "Age", "Marital Status", "Residential_Street Address"};
        cust_data_model.showMMD(Arrays.asList(showList));
        cust_data_model.deduplicateDataset();
        cust_data_model.attachOntRels(household_rel);
        logger.info(cust_data_model.toFDGJSON(true));
    }

    @org.junit.jupiter.api.Test
    void combineDataModelTest() {
        OntologyModel m = mf.combineModels(cust_data_model, census_data_model, cust_data_model.getKeyMD(), null);
        //  OntologyModel household =  mf.combineModels(m,household_model,household_model.getKeyMD());
        logger.info(m.toFDGJSON(true));
    }


    @org.junit.jupiter.api.Test
    void circlePackingModelTest() {
        logger.info(cust_data_model.toCirPakJSON());
    }

    @org.junit.jupiter.api.Test
    void unionListTest() {
        List<String> list = Arrays.asList("red", "blue", "blue", "green", "red");
        List<String> otherList = Arrays.asList("red", "green", "green", "yellow");
        list.stream()
                .filter(otherList::contains).forEach(System.out::println);


    }


}