package com.itls.ontology.model;

import com.itls.ontology.elements.*;
import com.itls.ontology.elements.implementations.CommonData;
import com.itls.ontology.elements.implementations.OntMetaDataImpl;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class OntologyModelFactory implements ModelFactory {
    private static CSVParser csvParser;
    private static OntologyModel model;
    private static String[] header;

    /**
     * @param one primary table containing primary key ontology data
     * @param two child table containing multiple foreign key ontology data
     * @return dataset based on primary key one and two combined
     */
    public OntologyModel combineModels(OntologyModel one, OntologyModel two, OntMetaData keyM, String joinD) throws IllegalArgumentException {
        OntologyModel combined = new OntologyModel();
        // 1. consolidate key metadata to combined model
        combined.setKeyMD(keyM);
        // 2. In the case of primary key and foreign key have different name
        if (!one.getKeyMD().equals(keyM))
            updateKeyMetadata(one, combined);
        if (!two.getKeyMD().equals(keyM))
            updateKeyMetadata(two, combined);
        // 3 things to keep track
        // 1.1 . build OntData list, combine ontdata from both models
        // 1.2. refreshMap
        //3. refreshKey, dedup keys
        //get key data from model 1 which will have dups in foreign
        if (one.isEmpty()) {
            combined.addAllOntData(two.getDataSet());
        } else if (two.isEmpty()) {
            combined.addAllOntData(one.getDataSet());
        } else if (!one.isEmpty() && !two.isEmpty()) {
            if (joinD == null) {
                combined.addAllOntData(one.getDataSet());
                combined.addAllOntData(two.getDataSet());
            } else if (joinD.equalsIgnoreCase("L")) {
                combined.addAllOntData(one.getDataSet());
                combined.addAllOntData(two.getDataSet().stream()
                        .filter(ontDataFromLeft -> one.getKeys().stream().anyMatch(keyFromRight -> keyFromRight.getContents().equals(ontDataFromLeft.getKeyValue())))
                        .collect(Collectors.toCollection(ArrayList::new)));
            } else if (joinD.equalsIgnoreCase("R")) {
                combined.addAllOntData(two.getDataSet());
                combined.addAllOntData(one.getDataSet().stream()
                        .filter(ontDataFromLeft -> two.getKeys().stream().anyMatch(keyFromRight -> keyFromRight.getContents().equals(ontDataFromLeft.getKeyValue())))
                        .collect(Collectors.toCollection(ArrayList::new)));
            } else
                throw new IllegalArgumentException("Can only specify 'L' for left join or 'R' for right join");

            combined.refreshKeysNValues();
            //remove foreign keys to prevent key meta-data difference
            //combined.removeAllOntData(two.getKeys());
            combined.deduplicateDataset();
            combined.refreshDataToMetaDataMap();
            //  combined.deduplicateDataset();
        }
        return combined;
    }

    private void updateKeyMetadata(OntologyModel modelToUpdate, OntologyModel fromModel) {
        modelToUpdate.setKeyMD(fromModel.getKeyMD());
        modelToUpdate.getKeys().forEach(ontData -> ontData.setOntMetaData(fromModel.getKeyMD()));
        modelToUpdate.refreshKeyMDs();
    }

    public OntologyModel replicateModel(OntologyModel model) {
        OntologyModel o = new OntologyModel();
        o.addAllOntData(model.getDataSet());
        o.setKeyMD(model.getKeyMD());
        return o;
    }

    public List<OntRelationship> createRelationships(Path fromCsv, int row_count, String type) throws IOException {
//        if (model == null || model.getDataSet() == null || model.getDataSet().isEmpty()){
//            throw new Exception("data is not valid");
//        }
        Reader reader = Files.newBufferedReader(fromCsv);
        CSVFormat format = CSVFormat.EXCEL.withFirstRecordAsHeader();
        csvParser = format.parse(reader);
        List<OntRelationship> onR = new ArrayList<>();
        switch (type) {
            case "H":

            case "B":

        }
        //  OntRelationshipFactory relFac = new OntologyRelationshipFactory();
        int counter = 1;
        for (CSVRecord record : csvParser) {
            if (row_count <= counter)
                break;
            String fromSSN = record.get("SSN_HH").trim();
            String toSSN = record.get("SSN_HM").trim();
            float confidence = Float.parseFloat(record.get("Score").trim());
            //          OntRelationship o = relFac.createOntRel(type);
            CommonData fromOnt = new CommonData(fromSSN, null);
            CommonData toOnt = new CommonData(toSSN, null);

            HouseholdRelationship h = new HouseholdRelationship(fromOnt, toOnt, confidence);

            onR.add(h);
            counter++;
        }
        return onR;

    }

    /**
     * @param file
     * @param line
     * @param key  the primary key value
     * @return
     * @throws IOException
     */
    public OntologyModel createModel(Path file, int line, String key) throws IOException {
        Reader reader = Files.newBufferedReader(file, Charset.forName("Cp1252"));
        CSVFormat format = CSVFormat.EXCEL.withFirstRecordAsHeader();
        csvParser = format.parse(reader);
        header = csvParser.getHeaderNames().stream().map(String::trim).toArray(String[]::new);
        int keyi = Arrays.asList(header).indexOf(key);
        return createModel(file, line, keyi);

    }

    /**
     * @param file
     * @param line
     * @param keyi the primary key index
     * @return
     * @throws IOException
     */
    public OntologyModel createModel(Path file, int line, int keyi) throws IOException {
        Reader reader = Files.newBufferedReader(file, Charset.forName("Cp1252"));
        CSVFormat format = CSVFormat.EXCEL.withFirstRecordAsHeader();
        csvParser = format.parse(reader);
        header = csvParser.getHeaderNames().stream().map(String::trim).toArray(String[]::new);

        //<editor-fold desc="create init model">
        model = new OntologyModel();
        int counter = 0;
        // meta-data list to hold meta-data
        List<OntMetaData> mdList = new ArrayList<>();
        ArrayList<OntData> dList = new ArrayList<>();
        OntMetaData keyMD = new OntMetaDataImpl(MMDType.Vertical, header[keyi]);
        keyMD.setKey(keyMD);
        // set key to model
        model.setKeyMD(keyMD);

        for (int i = 0; i < header.length; i++) {
            // if not key index
            if (i != keyi) {
                OntMetaData md = new OntMetaDataImpl(MMDType.Vertical, header[i]);
                md.setKey(keyMD);
                mdList.add(i, md);
            } else {
                //add keyMD at correct position
                mdList.add(keyMD);
            }

        }
        for (CSVRecord record : csvParser) {
            //setup md info
            // row 1 +
            if (counter > line - 1) {
                break;
            }
            // columns reset
            dList.clear();
            for (int i = 0; i < record.size(); i++) {
                if (record.get(i) == null || record.get(i).equals("-") || record.get(i).isEmpty())
                    continue;
                String entity = record.get(i).trim();

                CommonData common = new CommonData(entity, mdList.get(i));
                dList.add(common);
                model.addOntData(common);
            }
            counter++;
            model.relatesRelatesToData(dList);
        }
        model.refreshKeyMDs();
        model.refreshDataToMetaDataMap();

        return model;
    }
}
