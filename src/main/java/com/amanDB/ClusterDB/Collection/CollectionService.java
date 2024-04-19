package com.amanDB.ClusterDB.Collection;

import com.amanDB.ClusterDB.Broadcast.Broadcast;
import com.amanDB.ClusterDB.Broadcast.CollectionDeleteObserver;
import com.amanDB.ClusterDB.Broadcast.CollectionUpdateObserver;
import com.amanDB.ClusterDB.FileManagment.DirectoryRemover;
import com.amanDB.ClusterDB.FileManagment.DirectoryWriter;
import com.amanDB.ClusterDB.FileManagment.DocumentWriter;
import com.amanDB.ClusterDB.NodeManagment.Node;
import com.amanDB.ClusterDB.NodeManagment.NodesService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.amanDB.ClusterDB.Broadcast.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.FileAlreadyExistsException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

@Service
public class CollectionService {

    private List<Node> nodes;
    @Value("${nodeID}")
    private int nodeID;
    private final DirectoryWriter writer = DirectoryWriter.getDirectoryWriter();
    private final NodesService nodesService = new NodesService();
    private final DirectoryRemover remover = DirectoryRemover.getDirectoryRemover();
    private final DocumentWriter documentWriter = DocumentWriter.getDocumentWriter();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /* private final SchemaValidator collectionValidator = new SchemaValidator(SchemaValidator.JsonObjectType.COLLECTION); */

    private void validateCollection(Collection collection) {
        if (collection.getDatabase().isEmpty()) {
            throw new InvalidParameterException("Missing required parameter: database");
        }
        if (collection.getCollectionName().isEmpty()) {
            throw new InvalidParameterException("Missing required parameter: collection Name");
        }
        //return collectionValidator.validateJson(collectionJsonObject);
    }

    protected void save(Collection collection) throws Exception {
        validateCollection(collection);
        String path = "Databases/" + collection.getDatabase() + "/" + collection.getCollectionName();
        System.out.println("Node #" + nodeID + " : Creating Collection ==>" + collection.getCollectionName());
        try{
            writer.write(path);
            writer.write(path + "/indexes");
        } catch (Exception exception){
            if(!exception.getClass().equals(FileAlreadyExistsException.class)){
                throw exception;
            }
        }

        if (collection.getSchema() != null) {
            documentWriter.write(path + "/schema.json",objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(collection.getSchema()));
        }
        if (collection.getIsBroadcast()) {
            collection.setIsBroadcast(false);
            broadCastChanges(collection);
        }

    }

    protected void delete(Collection collection) throws Exception {
        validateCollection(collection);
        remover.remove("Databases/" + collection.getDatabase() + "/" + collection.getCollectionName());
        System.out.println("Node #" + nodeID + " : Deleting Collection ==>" + collection.getCollectionName());
        if (collection.getIsBroadcast()) {
            collection.setIsBroadcast(false);
            broadCastDelete(collection);
        }

    }

    protected List<Collection> getCollections(String databaseName) {
        File directory = new File("Databases/" + databaseName);
        File[] files = directory.listFiles();
        List<Collection> collections = new ArrayList<>();
        if (files == null) {
            return collections;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                collections.add(new Collection(databaseName, file.getName(),"",  false));
            }
        }
        return collections;
    }

    private void broadCastChanges(Collection collection) throws Exception {
        if (nodes == null) {
            nodes = nodesService.getNodesList();
        }
        Broadcast broadcast = new Broadcast();
        for (Node node :
                nodes) {
            if (!nodes.get(nodeID).equals(node)) {
                new CollectionUpdateObserver(broadcast, collection, node.getNodeURL());
            }
        }
        broadcast.notifyAllObservers();

    }

    private void broadCastDelete(Collection collection) throws Exception {
        if (nodes == null) {
            nodes = nodesService.getNodesList();
        }
        Broadcast broadcast = new Broadcast();
        for (Node node :
                nodes) {
            if (!nodes.get(nodeID).equals(node)) {
                new CollectionDeleteObserver(broadcast, collection, node.getNodeURL());
            }
        }
        broadcast.notifyAllObservers();
    }
}
