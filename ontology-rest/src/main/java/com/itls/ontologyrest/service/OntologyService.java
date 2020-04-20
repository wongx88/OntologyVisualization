package com.itls.ontologyrest.service;

import com.itls.ontology.elements.OntRelationship;
import com.itls.ontology.elements.implementations.CommonData;
import com.itls.ontology.elements.implementations.NameData;
import com.itls.ontology.model.OntologyModel;
import com.itls.ontology.model.OntologyModelFactory;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import java.io.*;
import java.util.Arrays;
import java.util.List;

@Service
public class OntologyService implements IOntologyService {

    public static final String EMPTY_JSON = "{}";
    public static final String LEFT_JOIN = "L";
    private static final String RIGHT_JOIN = "R";
    private static OntologyModel ontologyModel, ontologyModel2;
    private static File cust, buysell, hh, cust_all;
    private static List<OntRelationship> household_rel;
    private static final Logger logger = LoggerFactory.getLogger(OntologyService.class);

    public OntologyService() {
        try {
            setupDemo();
        } catch (IOException e) {
            logger.error(e.getLocalizedMessage());
        }
    }

    /**
     * create models based on POC
     */
    private static void setupDemo() throws IOException {
        OntologyModelFactory mf = new OntologyModelFactory();

        cust = ResourceUtils.getFile("classpath:dataset/Customer_Transaction_Data_2020-04-06-updatedbyTera-coloredonly.csv");
        cust_all = ResourceUtils.getFile("classpath:dataset/Customer_Transaction_Data_2020-04-06.csv");
        buysell = ResourceUtils.getFile("classpath:dataset/PropensityToBuyScore_15_04_2020-coloredonly.csv");
        hh = ResourceUtils.getFile("classpath:dataset/HH_identification_Score_2020-04_12-coloredonly.csv");
        ontologyModel = mf.createModel(cust.toPath(), 15000, "SSN");
        //1. always hide metadata before deduplication
        ontologyModel.hideAllMMD();
        //2.
        ontologyModel.refreshKeysNValues();
        //3. dedup customer data rows
        ontologyModel.deduplicateDataset();
        //4. create buysell model
        ontologyModel2 = mf.createModel(buysell.toPath(), 10000, 0);
        ontologyModel2.hideAllMMD();
        //5. right join buysell with customer data

        ontologyModel = mf.combineModels(ontologyModel, ontologyModel2, ontologyModel.getKeyMD(), LEFT_JOIN);

        //5. create hh rels
        household_rel = mf.createRelationships(hh.toPath(), 15000, "H");
        // ontologyModel = getOntologyModel();
    }

//
//    public getCustomerbySSN(String SSN){
//        ontologyModel.getDataSet().forEach();
//        return ontologyModel.toFDGJSON(true);
//    }


    public String getCustomerCSV() {
        StringBuilder contentBuilder = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(cust_all))) {

            String sCurrentLine;
            while ((sCurrentLine = br.readLine()) != null) {
                contentBuilder.append(sCurrentLine)
                        .append("\n");
            }
        } catch (IOException e) {
            logger.error(e.getLocalizedMessage());
        }
        return contentBuilder.toString();
    }

    /**
     * @return HEB
     */
    @Override
    public String getHEBJSON() {
        if (null == ontologyModel) return EMPTY_JSON;
        OntologyModel model = ontologyModel;
        String[] showList = {"SSN", "First Name", "Last Name", "Gender", "Age", "Marital Status", "Residential_Street Address", "Item", "Propensity", "Product"};
        //  String[] showList = {"SSN","First Name","Last Name","Item"};
        model.showMMD(Arrays.asList(showList));
        model.deduplicateDataset();
        return model.toHEBJSON();
    }

    public String getHEBJSON(String SSN) {
        if (null == ontologyModel) return EMPTY_JSON;
        OntologyModel model = new OntologyModel();
        model.hideAllMMD();
        model.addAllOntData(ontologyModel.filterByKey(SSN));
        String[] showList = {"SSN", "First Name", "Last Name", "Gender", "Age", "Marital Status", "Residential_Street Address", "Item", "Propensity"};
        model.showMMD(Arrays.asList(showList));
        model.deduplicateDataset();
        return model.toHEBJSON();
    }

    /**
     * @return FDG without household
     */
    public String getFDGJSON() {
        if (null != ontologyModel) {
            ontologyModel.detachAllOntRels();
            String[] showList = {"SSN", "First Name", "Last Name", "Item", "Propensity", "Residential_Street Address"};
            // String[] showList = {"SSN","First Name","Last Name","Gender", "Age","Marital Status", "Residential_Street Address","Item","Propensity"};
            ontologyModel.showMMD(Arrays.asList(showList));
            return ontologyModel.toFDGJSON(true);
        } else return EMPTY_JSON;

    }

    /**
     * @return FDG with household
     */
    public String getHouseHoldFDG() {
        if (null == ontologyModel) return EMPTY_JSON;
        ontologyModel.detachAllOntRels();
        String[] showList = {"SSN", "First Name", "Last Name", "Gender", "Age", "Marital Status", "Residential_Street Address", "Item"};
        // String[] showList = {"SSN","First Name","Last Name","Gender", "Age","Marital Status", "Residential_Street Address"};
        ontologyModel.showMMD(Arrays.asList(showList));
        ontologyModel.deduplicateDataset();
        ontologyModel.attachOntRels(household_rel);
        return ontologyModel.toFDGJSON(true);

    }

    //old household service
//    public String getHouseHoldFDG() {
//        //apply last name filter
//        OntologyModel newModel = ontologyModel.findSimilar("Last Name");
//        return newModel.toFDGJSON(false);
//
//    }


    /**
     * @return CP
     */
    public String getCirclePackingJSON() {
        if (null == ontologyModel) return EMPTY_JSON;
        OntologyModel model = ontologyModel;
        return model.toCirPakJSON();

    }

    @Deprecated
    private OntologyModel getOntologyModel() throws IOException {
        if (ontologyModel == null) {
            CSVParser csvParser;

            InputStream inputStream = this.getClass().getResourceAsStream("/Txn_data.csv");
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            csvParser = CSVFormat.EXCEL.withFirstRecordAsHeader().parse(reader);

            OntologyModel model = new OntologyModel();
            int counter = 0;
            for (CSVRecord record : csvParser) {
                if (counter > 5) {
                    break;
                }
                String name = record.get("Card Holder's Name");
                NameData n = new NameData(name);
                model.addOntData(n);
                for (int i = 2; i < record.size(); i++) {
                    CommonData com = new CommonData(record.get(i));
                    n.relatesTo(com);
                    model.addOntData(com);
                }

                //     String name = record.get("Card Holder's Name");
//                String address = record.get("Billing Address");
//                String card = record.get("Card Type");
//                String amount = record.get("Amount");
//                NameData n = new NameData(name);
//                n.setVisibility(Visibility.ON);
//                CardData c = new CardData(card);
//                AddressData a = new AddressData(address);
//                a.setVisibility(Visibility.OFF);
//                n.relatesTo(a);
//                n.relatesTo(c);
//                model.addOntData(n);
//                model.addOntData(a);
//                model.addOntData(c);
                counter++;
            }
            return model;
        } else {
            return ontologyModel;
        }
    }
}
