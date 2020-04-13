package com.itls.ontology.model;

import ch.qos.logback.classic.Logger;
import com.itls.json.CustomJSONBuilderFactory;
import com.itls.json.serializers.OntologyModelCirclePackingSerializer;
import com.itls.json.serializers.OntologyModelFDGSerializer;
import com.itls.json.serializers.OntologyModelHEBSerializer;
import com.itls.json.serializers.OntologyModelStepFDGSerializer;
import com.itls.ontology.elements.OntData;
import com.itls.ontology.elements.OntMetaData;
import com.itls.ontology.elements.OntRelationship;
import com.itls.ontology.elements.Visibility;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class OntologyModel {

    private static final Logger logger = (Logger) LoggerFactory.getLogger(OntologyModel.class);

    private OntMetaData keyMD;
    private ArrayList<OntData> dataSet = new ArrayList<>();
    private ArrayList<OntData> keyList;
    private Map<String, List<OntData>> dataMap = new HashMap<>();
    private ArrayList<OntRelationship> ontRels = new ArrayList<>();


    public OntologyModel() {
    }

    //used in induced model creation with set of induced data points
    public OntologyModel(ArrayList<OntData> ontList) {
        setDataSet(ontList);
        refreshOntDataSet(getDataSet());
        refreshDataToMetaDataMap();
    }

    public void refreshModel() {
        refreshDataToMetaDataMap();
        refreshOntDataSet(getDataSet());
    }

    public OntMetaData getKeyMD() {
        return this.keyMD;
    }

    public void setKeyMD(OntMetaData keyMD) {
        this.keyMD = keyMD;
    }

    public ArrayList<OntData> getKeys() {
        keyList = dataSet.stream()
                .filter(OntData::isKey)
                .collect(Collectors.toCollection(ArrayList::new));
        return keyList;
    }

    public ArrayList<OntData> filterByKey(String key) {
        keyList = dataSet.stream()
                .filter(ontData -> ontData.getKeyValue()
                        .equals(key))
                .collect(Collectors.toCollection(ArrayList::new));
        return keyList;
    }

    public void refreshKeyMDs() {
        getDataSet()
                .forEach(ontData -> ontData.getOntMetaData()
                        .setKey(getKeyMD()));
    }


    public Map<String, List<OntData>> getDataMap() {
        return dataMap;
    }

    public void attachOntRels(List<OntRelationship> rels) {
        this.ontRels.addAll(rels);
    }

    public void detachAllOntRels() {
        this.ontRels.clear();
    }

    public ArrayList<OntRelationship> getOntRels() {
        return ontRels;
    }


    /**
     * @param md schema name
     *           find similar OntData under same schema using dataMap
     */
    public OntologyModel findSimilar(String md) {

        List<OntData> ontDataList = this.dataMap.get(md);

        ArrayList<OntData> a = (ArrayList<OntData>) ontDataList.stream()
                .filter(i -> Collections.frequency(ontDataList, i) > 1)
                .collect(Collectors.toList());
        //construct new model
        return createDerivedModel(a);
//                .forEach(t-> System.out.format("%s : %s",t.getOntMetaData().getName(),t.lookupKeyValue(t.getKey())));
    }

    private OntologyModel createDerivedModel(ArrayList<OntData> a) {
        OntologyModel newModel = new OntologyModel(a);
        newModel.setKeyMD(getKeyMD());
        newModel.refreshOntDataSet(a);
        newModel.deduplicateKeysInDataset();
        //  newModel.getDataSet().addAll((Collection<? extends OntData>) a.stream().flatMap(b->((OntData)b).getRelatesToObjs().stream()).collect(Collectors.toList()));
        newModel.refreshDataToMetaDataMap();
        return newModel;
    }


    public void hideAllMMD() {
        getDataSet()
                .forEach(ontData -> ontData.setVisibility(Visibility.OFF));
    }

    public void hideMMD(List<String> typeList) {
        getDataSet().stream()
                .filter(ontData -> typeList.stream().anyMatch(type -> ontData.getOntMetaData().getName().equals(type)))
                .forEach(ontData -> ontData.setVisibility(Visibility.OFF));
    }

    public void showMMD(List<String> typeList) {
        hideAllMMD();
        getDataSet().stream()
                .filter(ontData -> typeList.stream().anyMatch(type -> ontData.getOntMetaData().getName().equals(type)))
                .forEach(ontData -> ontData.setVisibility(Visibility.ON));
    }

    /**
     * Before deduplication, repopulate relate all key relatesTO data
     */
    public void refreshKeysNValues() {
        List<OntData> keys = getKeys();
        logger.info("After Key refreshment: keys are " + keys);
        //build keys, relatesTo objects for combined model
        //find matching key entries and populate both ways, then deduplicate the entire dataset
        HashMap<String, ArrayList<OntData>> keysMap = keys.stream().collect(
                Collectors.groupingBy(OntData::getContents, HashMap::new, Collectors.toCollection(ArrayList::new)));
        keysMap.forEach((String a, ArrayList<OntData> b) -> relatesData(b));

    }

    /**
     * used in derived data model when ont data elements are cut
     * populate OntData relatesTO data objects into dataset
     *
     * @param a
     */
    private void refreshOntDataSet(ArrayList<OntData> a) {
        getDataSet().addAll(a.stream().
                flatMap(b -> b.getRelatesToObjs().stream()).collect(Collectors.toList()));

    }

    /**
     * will only dedup key ont data
     * this dedup will not refresh relatesTo Data, assuming each of the ont data will still hold each other's data
     * use key to to create FDG instead of other elements
     */
    public void deduplicateKeysInDataset() {

        logger.info("Deduplication begins");
        if (getDataSet() != null) {
            //    getDataSet().stream().distinct().forEach(x-> logger.info(x.getContents() + " " + x.getKeyValue()));
            List<OntData> re = getDataSet().stream()
                    //only dudup based on key contents
                    .filter(distinctByKey(OntData::getContents))
                    .collect(Collectors.toList());
            setDataSet((ArrayList<OntData>) re);
            //getDataSet().stream().distinct().forEach(x-> logger.info(x.getContents() + " " + x.getKeyValue()));
        }
    }

    public void deduplicateDataset() {
        logger.info("Deduplication begins");
        // deduplication deals with multiple key & values, in some cases, after union ALl, relatesTo data has to be re-associated by key
        //A->B,C,D
        //A->C,D,E
        //return A -> B,C,D,E
        //  refreshKeysNValues();
        if (getDataSet() != null) {
            List<OntData> re = getDataSet().stream()
                    //dudup based on key + metadata + value
                    .filter(distinctByKeyAll(k -> new StringBuffer()
                            .append(k.getKeyValue())
                            .append(".")
                            .append(k.getOntMetaData().getName())
                            .append(".")
                            .append(k.getContents()).toString()))
                    .collect(Collectors.toList());
            setDataSet((ArrayList<OntData>) re);

            //getDataSet().stream().distinct().forEach(x-> logger.info(x.getContents() + " " + x.getKeyValue()));
        }
    }

    private static <T> Predicate<T> distinctByKey(Function<? super T, Object> keyExtractor) {
        Map<Object, Boolean> map = new ConcurrentHashMap<>();
        // distinct applies to key elements only, leaving all non-key elements
        return t -> !((OntData) t).isKey()
                || map.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }


    private static <T> Predicate<T> distinctByKeyAll(Function<? super T, Object> keyExtractor) {
        Map<Object, Boolean> map = new ConcurrentHashMap<>();
        // distinct applies to key elements only, leaving all non-key elements
        return t -> map.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

    /**
     * @param d add OntData to the Ontology model
     */
    public void addOntData(OntData d) {
        dataSet.add(d);
    }

    public void addAllOntData(ArrayList<OntData> ds) {
        dataSet.addAll(ds);
    }

    public void removeOntData(OntData d) {
        dataSet.remove(d);
    }

    public void removeAllOntData(List<OntData> l) {
        dataSet.removeAll(l);
    }

    /**
     * create relateTo list for all OntData in aList which should be row data list
     *
     * @param aList
     */
    public void relatesData(ArrayList<OntData> aList) {
        //should not flatALl, as we are not re-associating the original relatesTo data again
        //[577-48-3829, 577-48-3829, 577-48-3829, 577-48-3829]
        // List<OntData> flattenList = aList.stream().flatMap(ontData -> ontData.getRelatesToObjs().stream()).collect(Collectors.toList());
        for (int i = 0; i < aList.size(); i++) {
            for (int j = i + 1; j < aList.size(); j++) {
                ArrayList<OntData> a = new ArrayList<>();
                a.add(aList.get(i));
                ArrayList<OntData> b = new ArrayList<>(aList.get(i).getRelatesToObjs());
                ArrayList<OntData> c = new ArrayList<>();
                c.add(aList.get(j));
                ArrayList<OntData> d = new ArrayList<>(aList.get(j).getRelatesToObjs());
                relatesRelatesToDataFromPair(a, d);
                relatesRelatesToDataFromPair(c, b);
                relatesRelatesToDataFromPair(b, d);
            }
        }
        System.out.println(aList);
//         aList.stream().forEach(ontData -> aList.stream()
//                                .filter(innered -> !(innered == ontData))
//                                .forEach(innerd -> {ArrayList<OntData> a = new ArrayList<>();
//                                                                        a.add(ontData);
//                                                                        a.addAll(ontData.getRelatesToObjs());
//                                                                        relatesRelatesToDataFromPair(a, ontData.getRelatesToObjs());}));
        //no need to add key back to each key relatesTo objects
        //        flattenList.addAll(aList);

        //    relatesRelatesToData((ArrayList<OntData>) flattenList);
//        flattenList.stream().forEach(ontData -> aList.stream()
//                .filter(innerd -> !innerd.equals(ontData))
//                .forEach(innerd -> innerd.relatesTo(ontData)));
    }


    /**
     * @param relatesToList pairwise relatesTo data assignment in the list
     */
    public void relatesRelatesToDataFromPair(ArrayList<OntData> relatesToList, ArrayList<OntData> relatesToList_2) {
        relatesToList.forEach(ontData -> relatesToList_2
                .forEach(innerd -> {
                    innerd.relatesTo(ontData);
                    ontData.relatesTo(innerd);
                }));
    }

    /**
     * @param relatesToList pairwise relatesTo data assignment in the list
     */
    public void relatesRelatesToData(ArrayList<OntData> relatesToList) {
        relatesToList.forEach(ontData -> relatesToList.stream()
                .filter(innerd -> !innerd.equals(ontData))
                .forEach(innerd -> innerd.relatesTo(ontData)));
    }

    public ArrayList<OntData> getDataSet() {
        return dataSet;
    }

    public void setDataSet(ArrayList<OntData> dataSet) {
        this.dataSet = dataSet;
    }

    public boolean isEmpty() {
        return getDataSet() == null || getDataSet().isEmpty();
    }

    /**
     * Performs during model init
     * create md -> data mapping for all elements
     */
    public void refreshDataToMetaDataMap() {
        if (getDataSet() != null) {
            this.dataMap = dataSet.stream()
                    .collect(Collectors.groupingBy(ontdata -> ontdata.getOntMetaData().getName()));

        }
    }


    public String toHEBJSON() {
        return CustomJSONBuilderFactory.createJSONString(new OntologyModelHEBSerializer(), this);
    }

    public String toFDGJSON(boolean showKey) {
        return CustomJSONBuilderFactory.createJSONString(new OntologyModelFDGSerializer(showKey), this);
    }

    public String toStepFDGJSON() {
        return CustomJSONBuilderFactory.createJSONString(new OntologyModelStepFDGSerializer(), this);
    }

    public String toCirPakJSON() {
        return CustomJSONBuilderFactory.createJSONString(new OntologyModelCirclePackingSerializer(), this);
    }
}
