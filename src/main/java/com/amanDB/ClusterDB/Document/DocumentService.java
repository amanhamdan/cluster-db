package com.amanDB.ClusterDB.Document;


import com.amanDB.ClusterDB.Broadcast.Broadcast;
import com.amanDB.ClusterDB.Broadcast.DocumentDeleteObserver;
import com.amanDB.ClusterDB.Broadcast.DocumentSaveObserver;
import com.amanDB.ClusterDB.Broadcast.DocumentUpdateObserver;
import com.amanDB.ClusterDB.FileManagment.DocumentReader;
import com.amanDB.ClusterDB.FileManagment.DocumentRemover;
import com.amanDB.ClusterDB.FileManagment.DocumentWriter;
import com.amanDB.ClusterDB.FileManagment.OptimisticReader;
import com.amanDB.ClusterDB.NodeManagment.Node;
import com.amanDB.ClusterDB.NodeManagment.NodesService;
import com.amanDB.ClusterDB.schemaValidators.SchemaValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.amanDB.ClusterDB.Broadcast.*;
import com.amanDB.ClusterDB.FileManagment.*;
import org.bson.json.JsonObject;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.math.BigInteger;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.List;

@Service
public class DocumentService {
    private final DocumentWriter writer = DocumentWriter.getDocumentWriter();
    private final DocumentRemover remover = DocumentRemover.getDocumentRemover();
    private final DocumentReader reader = DocumentReader.getDocumentReader();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SchemaValidator documentValidator = new SchemaValidator(SchemaValidator.JsonObjectType.Document);

    private final NodesService nodesService = new NodesService();
    private static int readingLoadBalancer;

    @Value("${nodeID}")
    private int nodeID;

    private List<Node> nodes;
    private final String user = "aman";
    private final String password = "12345";

    private void validateDocument(Document document) throws Exception {
        String schemaPath = "Databases/" + document.getDatabase() + "/" + document.getCollection() + "/" + "schema.json";
        try {
            reader.read(schemaPath);//to check if there is a schema
            documentValidator.validateJson((String) document.getJsonString(),schemaPath);

        } catch (Exception e) {
            if(!e.getClass().equals(NoSuchFileException.class)){
                if(e.getClass().equals(RuntimeException.class)){
                    throw new RuntimeException(e);
                }else {
                    throw e;
                }
            }
        }
    }

    protected void save(Document document) throws Exception {
        if (nodes == null) {
            nodes = nodesService.getNodesList();
        }

        if (document.get_Id() == null) {
            document.set_Id(new ObjectId().toHexString());
        }

        validateDocument(document);

        document.setNodeAffinity(calculateAffinity(document.get_Id()));
        if (document.getNodeAffinity() != nodeID) {
            /* FOR TESTING : document.setNodeAffinity(nodeID); //for debug on the same node
            String NodeURL = "http://localhost:8080/api/v1";
            */

            String nodeURL = nodes.get(document.getNodeAffinity()).getNodeURL();
            nodeURL += "/documents";
            sendSaveToNode(nodeURL, document);
        } else {
            String path = "Databases/" + document.getDatabase() + "/" + document.getCollection() + "/" + document.get_Id() + ".json";
            String jsonString = objectMapper.writeValueAsString(document.getJsonString());
            writer.write(path,jsonString);
            broadCastSave(document);
        }

    }
    protected void delete(Document document) throws Exception {
        String path = "Databases/" + document.getDatabase() + "/" + document.getCollection() + "/" + document.get_Id() + ".json";
        remover.remove(path);
        broadCastDelete(document);
    }

    protected Document read(Document document) throws Exception {
        if (nodes == null) {
            nodes = nodesService.getNodesList();
        }
        String path = "Databases/" + document.getDatabase() + "/" + document.getCollection() + "/" + document.get_Id() + ".json";
        loadBalanceReading();
        if (readingLoadBalancer != nodeID) {
            return sendReadToNode(document);
        } else {
            String stringDocument = reader.read(path);
            Object jsonObject =  objectMapper.readValue(stringDocument,Object.class);
            document.setJsonString(jsonObject);
            return document;
        }
    }
    protected List<Document> getDocuments(Document document) throws Exception {
        String path = "Databases/" + document.getDatabase() + "/" + document.getCollection();
        File collectionFile = new File(path);
        File[] files = collectionFile.listFiles();
        List<Document> documents = new ArrayList<>();
        if (files == null) {
            return documents;
        }
        for (File file : files) {
            String documentID = file.getName().replaceAll(".json", "");
            String stringDocument = reader.read(path + "/" + file.getName());
            Object jsonObject =  objectMapper.readValue(stringDocument,Object.class);
            new JsonObject(stringDocument); // to check if the string is json if not exception will be thrown
            documents.add(new Document(document.getDatabase(), document.getCollection(), documentID, jsonObject));
        }
        return documents;
    }

    protected void update(DocumentRequest documentRequest) throws Exception {
        if (nodes == null) {
            nodes = nodesService.getNodesList();
        }
        String path = "Databases/" + documentRequest.getDatabase() + "/" + documentRequest.getCollection() + "/" + documentRequest.get_Id() + ".json";
        documentRequest.setOldValue(reader.read(path));
        documentRequest.setNodeAffinity(calculateAffinity(documentRequest.get_Id()));

        if (documentRequest.getNodeAffinity() != nodeID) {
            String NodeURL = nodes.get(documentRequest.getNodeAffinity()).getNodeURL();
            NodeURL += "/documents";
            sendUpdateToNode(NodeURL,documentRequest);
        } else {
            OptimisticReader optimisticReader = new OptimisticReader(writer.getLock());
            String oldValue = objectMapper.writeValueAsString(documentRequest.getOldValue());
            if (optimisticReader.isMatch(path, oldValue)) {
                remover.remove(path);
                String newValue = objectMapper.writeValueAsString(documentRequest.getNewValue());
                writer.write(path, newValue);
                broadCastUpdate(documentRequest);
            } else {
                throw new Exception("write failed due to data inconsistency: trying to update document data");
            }
        }
    }
    private int calculateAffinity(String _Id){
        int numberOfNodes = nodes.size() + 1;
        ObjectId id = new ObjectId(_Id);
        String idString = id.toHexString();
        BigInteger bi = new BigInteger(idString, 16);
        return bi.mod(BigInteger.valueOf(numberOfNodes)).intValue();
    }

    private void broadCastSave(Document document) throws Exception {
        if (nodes == null) {
            nodes = nodesService.getNodesList();
        }
        Broadcast broadcast = new Broadcast();
        for (Node node :
                nodes) {
            if (!nodes.get(nodeID).equals(node)) {
                new DocumentSaveObserver(broadcast, document, node.getNodeURL());
            }
        }
        broadcast.notifyAllObservers();

    }

    private void broadCastDelete(Document document) throws Exception {
        if (nodes == null) {
            nodes = nodesService.getNodesList();
        }
        Broadcast broadcast = new Broadcast();
        for (Node node :
                nodes) {
            if (!nodes.get(nodeID).equals(node)) {
                new DocumentDeleteObserver(broadcast, document, node.getNodeURL());
            }
        }
        broadcast.notifyAllObservers();
    }

    private void broadCastUpdate(DocumentRequest documentRequest) throws Exception {
        if (nodes == null) {
            nodes = nodesService.getNodesList();
        }
        Broadcast broadcast = new Broadcast();
        for (Node node :
                nodes) {
            if (!nodes.get(nodeID).equals(node)) {
                new DocumentUpdateObserver(broadcast, documentRequest, node.getNodeURL());
            }
        }
        broadcast.notifyAllObservers();
    }

    private void sendSaveToNode(String nodeURL , Document document){
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(user, password);
        headers.setContentType(MediaType.APPLICATION_JSON);
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<Document> request = new HttpEntity<>(document, headers);
        ResponseEntity<String> productCreateResponse = restTemplate.exchange(
                nodeURL,
                HttpMethod.POST,
                request,
                String.class);
        productCreateResponse.getStatusCode();
    }

    private void sendUpdateToNode(String nodeURL , DocumentRequest documentRequest){
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(user, password);
        headers.setContentType(MediaType.APPLICATION_JSON);
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<DocumentRequest> request = new HttpEntity<>(documentRequest, headers);
        ResponseEntity<String> productCreateResponse = restTemplate.exchange(
                nodeURL,
                HttpMethod.PUT,
                request,
                String.class);
        productCreateResponse.getStatusCode();
    }

    private Document sendReadToNode(Document document){
        String NodeURL = nodes.get(readingLoadBalancer).getNodeURL();
        NodeURL += "/documents?databaseName=" + document.getDatabase() + "&collectionName=" + document.getCollection() + "&documentID=" + document.get_Id();
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(user, password);
        headers.setContentType(MediaType.APPLICATION_JSON);
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<String> request = new HttpEntity<>(null, headers);
        ResponseEntity<Document> productCreateResponse = restTemplate.exchange(
                NodeURL,
                HttpMethod.GET,
                request,
                Document.class);
        return productCreateResponse.getBody();
    }
    private void loadBalanceReading(){
        if (readingLoadBalancer == nodes.size() - 1) {
            readingLoadBalancer = 0;
        } else {
            readingLoadBalancer++;
        }
    }

}
