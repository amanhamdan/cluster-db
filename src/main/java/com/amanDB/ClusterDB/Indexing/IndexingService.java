package com.amanDB.ClusterDB.Indexing;

import com.amanDB.ClusterDB.Collection.Collection;
import com.amanDB.ClusterDB.FileManagment.DocumentReader;
import com.amanDB.ClusterDB.FileManagment.DocumentWriter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.amanDB.ClusterDB.Document.Document;
import com.amanDB.ClusterDB.LRUCash.LRUCache;
import org.springframework.stereotype.Service;


import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

@Service
public class IndexingService {


    private final DocumentReader reader = DocumentReader.getDocumentReader();
    private final DocumentWriter writer = DocumentWriter.getDocumentWriter();
    private final ObjectMapper objectMapper = new ObjectMapper();

    LRUCache cache =  LRUCache.getCache();

    public void createNewIndex(String propertyName, com.amanDB.ClusterDB.Collection.Collection collection) throws Exception {
        HashMap<String, String> indexHashMap = new HashMap<>();
        String collectionPath = "Databases/" + collection.getDatabase() + "/" + collection.getCollectionName();
        String indexPath = collectionPath + "/indexes/"+ propertyName + "_Index.json";
        File collectionFile = new File(collectionPath);
        List<File> documentsList = Arrays.asList(Objects.requireNonNull(collectionFile.listFiles()));
        if (!documentsList.isEmpty()) {
            for (File file : documentsList) {
                if (!file.isDirectory()){
                    String propertyValue = getPropertyValue(file, propertyName);
                    if (!propertyValue.isEmpty()) {
                        indexHashMap.put(propertyValue, file.getAbsolutePath());
                    }
                }

            }
            String jsonMap = writeMapAsJson(indexHashMap);
            writer.write(indexPath,jsonMap);
        }
    }


    public String getPropertyValue(File file, String propertyName) throws Exception {
        String jsonString = reader.read(file.getAbsolutePath());
        JsonNode node = objectMapper.readTree(jsonString).get(propertyName);
        if (node != null)
            return node.asText();
        else
            return "";
    }

    public String writeMapAsJson(HashMap<String,String> map) throws JsonProcessingException {
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(map);
    }

    public BTree<String,String> loadIndexFromDisk(String propertyName, com.amanDB.ClusterDB.Collection.Collection collection) throws Exception {
        String indexPath = "Databases/"+collection.getDatabase()+"/"+collection.getCollectionName()+"/indexes/" + propertyName +"_Index.json";
        File indexFile = new File(indexPath);
        if(indexFile.exists()){
            HashMap<String,String> indexMap=  loadMapFromJson(reader.read(indexPath));
            return loadBTreeFromIndexMap(indexMap);
        }else{
            throw new FileNotFoundException();
        }

    }

    public HashMap<String,String> loadMapFromJson (String mapJsonString) throws JsonProcessingException {
        TypeReference<HashMap<String,String>> mapTypeReference = new TypeReference<HashMap<String,String>>() {};
        return objectMapper.readValue(mapJsonString , mapTypeReference);
    }

    public BTree<String,String> loadBTreeFromIndexMap(HashMap<String,String> indexMap){
        BTree<String,String> indexTree = new BTree<>();
        indexMap.forEach(indexTree::put);
        return indexTree;
    }

    public Document searchByPropertyValue(PropertiesFilter filter) throws Exception {
        String generatedSearchKey = filter.getDatabaseName()+"_"+ filter.getCollectionName()+ "_" + filter.getPropertyName();
        BTree<String,String> bTree;
        if(cache.get(generatedSearchKey)!= null)
        {
            bTree = cache.get(generatedSearchKey);

        }else {
            com.amanDB.ClusterDB.Collection.Collection collection = new Collection();
            collection.setCollectionName(filter.getCollectionName());
            collection.setDatabase(filter.getDatabaseName());
            bTree =  loadIndexFromDisk(filter.getPropertyName(),collection);
            cache.set(generatedSearchKey,bTree);
        }
        Object value = filter.getValue();
        String linkToDocument =  bTree.get(String.valueOf(value));
        if(linkToDocument == null){
            throw new RuntimeException("Failed to retrieve document from index.");
        }
        Document document = new Document();
        document.setDatabase(filter.getDatabaseName());
        document.setCollection(filter.getCollectionName());
        document.setJsonString( objectMapper.readValue(reader.read(linkToDocument) , Object.class));
        File doc = new File(linkToDocument);
        document.set_Id(doc.getName().replace(".json",""));
        return document;

    }


}
