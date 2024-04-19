package com.amanDB.ClusterDB.Database;


import com.amanDB.ClusterDB.Broadcast.Broadcast;
import com.amanDB.ClusterDB.Broadcast.DatabaseDeleteObserver;
import com.amanDB.ClusterDB.Broadcast.DatabaseUpdateObserver;
import com.amanDB.ClusterDB.FileManagment.DirectoryRemover;
import com.amanDB.ClusterDB.FileManagment.DirectoryWriter;
import com.amanDB.ClusterDB.NodeManagment.Node;
import com.amanDB.ClusterDB.NodeManagment.NodesService;
import org.springframework.beans.factory.annotation.Value;


import org.springframework.stereotype.Service;



import java.io.File;
import java.security.InvalidParameterException;
import java.util.ArrayList;

import java.util.List;


@Service
public class DatabaseService {

    private List<Node> nodes;
    @Value("${nodeID}")
    private int nodeID;

    private final DirectoryWriter writer = DirectoryWriter.getDirectoryWriter();
    private final DirectoryRemover remover = DirectoryRemover.getDirectoryRemover();
    private final NodesService nodesService = new NodesService();

    //private final SchemaValidator databaseValidator = new SchemaValidator(SchemaValidator.JsonObjectType.DATABASE);

    public void validateDatabase(Database database) {
        if (database.getDatabaseName().isEmpty()) {
            throw new InvalidParameterException("Missing required parameter: database name");
        }
        //return databaseValidator.validateJson(dbJsonObject);
    }

    public void save(Database database) throws Exception {
        validateDatabase(database);
        if (new File("Databases/" + database.getDatabaseName()).exists()){
            throw new InvalidParameterException("Database with this name already exists");
        }
        writer.write("Databases/" + database.getDatabaseName());
        System.out.println("Node #" + nodeID + " : Creating New database ==>" + database.getDatabaseName());// node Identification
        if (database.isBroadcast()) {
            database.setBroadcast(false);
            broadCastChanges(database);
        }
    }

    public void delete(Database database) throws Exception {
        validateDatabase(database);
        if (!new File("Databases/" + database.getDatabaseName()).exists()){
            throw new InvalidParameterException(database.getDatabaseName() + "do not exist.");
        }
        System.out.println("Node #" + nodeID + " : Deleting database ==>" + database.getDatabaseName());
        remover.remove("Databases/" + database.getDatabaseName());
        if (database.isBroadcast()) {
            database.setBroadcast(false);
            broadCastDelete(database);
        }
    }

    public List<Database> getDatabases() {
        File directory = new File("Databases/");
        File[] files = directory.listFiles();
        List<Database> databases = new ArrayList<>();
        assert files != null;
        for (File file : files) {
            if (file.isDirectory()) {
                databases.add(new Database(file.getName()));
            }
        }
        return databases;
    }

    public void broadCastChanges(Database database) throws Exception {
        if (nodes == null) {
            nodes = nodesService.getNodesList();
        }
        Broadcast broadcast = new Broadcast();
        for (Node node :
                nodes) {
            if(!nodes.get(nodeID).equals(node)){
            new DatabaseUpdateObserver(broadcast, database, node.getNodeURL());
            }
        }
        broadcast.notifyAllObservers();

    }

    public void broadCastDelete(Database database) throws Exception {
        if (nodes == null) {
            nodes = nodesService.getNodesList();
        }
        Broadcast broadcast = new Broadcast();
        for (Node node :
                nodes) {
            if(!nodes.get(nodeID).equals(node)){
                new DatabaseDeleteObserver(broadcast, database, node.getNodeURL());
            }
        }
        broadcast.notifyAllObservers();
    }


}
