package com.amanDB.ClusterDB.DocumentBroadcast;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.amanDB.ClusterDB.Document.Document;
import com.amanDB.ClusterDB.Document.DocumentRequest;
import com.amanDB.ClusterDB.FileManagment.DocumentReader;
import com.amanDB.ClusterDB.FileManagment.DocumentRemover;
import com.amanDB.ClusterDB.FileManagment.DocumentWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.NoSuchFileException;


@Service
public class DocumentBroadcastService {


    @Value("${nodeID}")
    private int nodeID;
    private final DocumentWriter writer = DocumentWriter.getDocumentWriter();
    private final DocumentRemover remover = DocumentRemover.getDocumentRemover();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final DocumentReader reader = DocumentReader.getDocumentReader();



    public void broadcastSave(Document document) throws Exception {
        String path = "Databases/" + document.getDatabase() + "/" + document.getCollection() + "/" + document.get_Id() + ".json";
        try {
            reader.read(path);
            reader.join();
            remover.remove(path);
            remover.join();
            System.out.println("Node #" + nodeID + " : saving document with Id " + document.get_Id());// node Identification
            writer.write(path, objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(document.getJsonString()) );
        } catch (Exception ex) {
            if (ex instanceof NoSuchFileException) {
                System.out.println("Document dose not exist continue saving...");
                writer.write(path, objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(document.getJsonString()));
            } else {
                throw ex;
            }
        }
    }

    public void broadcastUpdate(DocumentRequest documentRequest) throws Exception {
        String path = "Databases/" + documentRequest.getDatabase() + "/" + documentRequest.getCollection() + "/" + documentRequest.get_Id() + ".json";
        reader.read(path);
        reader.join();
        remover.remove(path);
        remover.join();
        System.out.println("Node #" + nodeID + " : updating document with Id " + documentRequest.get_Id());// node Identification
        String newValue = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(documentRequest.getNewValue());
        writer.write(path,newValue);
    }

    public void broadcastDelete(Document document)  {
        String path = "Databases/" + document.getDatabase() + "/" + document.getCollection() + "/" + document.get_Id() + ".json";
        System.out.println("Node #" + nodeID + " : deleting document with Id " + document.get_Id());// node Identification
        remover.remove(path);
    }
}
