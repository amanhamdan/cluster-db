package com.amanDB.ClusterDB.NodeManagment;

import com.amanDB.ClusterDB.FileManagment.DocumentReader;
import com.amanDB.ClusterDB.FileManagment.DocumentRemover;
import com.amanDB.ClusterDB.FileManagment.DocumentWriter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Service
public class NodesService {

    private final DocumentReader reader = DocumentReader.getDocumentReader();
    private final DocumentWriter writer = DocumentWriter.getDocumentWriter();
    private final DocumentRemover remover = DocumentRemover.getDocumentRemover();

    private final ObjectMapper objectMapper = new ObjectMapper();
    public NodesService() {
    }
    public  List<Node> getNodesList() throws Exception {
        String nodesJsonArray = reader.read("ClusterNodes.json");
        TypeReference<List<Node>> mapType = new TypeReference<List<Node>>() {};
        return new  ObjectMapper().readValue(nodesJsonArray, mapType);
    }

    public void writeNodesFromBootstrapper() throws Exception {
        List<Node> nodesList = getNodesFromBootstrapper();

        String listToJson = objectMapper.writeValueAsString(nodesList);
        System.out.println(listToJson);
        File nodesFile = new File("ClusterNodes.json");
        if(nodesFile.exists()){
            remover.remove("ClusterNodes.json");
        }
        writer.write("ClusterNodes.json" ,listToJson);
    }

    private List<Node> getNodesFromBootstrapper() throws JsonProcessingException {
        HttpHeaders headers = new HttpHeaders();
        String nodeURL = "http://host.docker.internal:9090/api/v1/nodes";
        headers.setBasicAuth("aman", "password");
        headers.setContentType(MediaType.APPLICATION_JSON);

        RestTemplate restTemplate  = new RestTemplate();
        HttpEntity<String> request =  new HttpEntity<>(null,headers);

        ResponseEntity<String> response = restTemplate.exchange(
                nodeURL,
                HttpMethod.GET,
                request,
                String.class);

        if(response.getStatusCodeValue() == 200){
            TypeReference<ArrayList<Node>> mapType = new TypeReference<ArrayList<Node>>() {};
            return objectMapper.readValue(response.getBody(), mapType);
        }else{
            throw new RuntimeException("Failed to fetch Nodes from bootstrapper please restart");
        }


    }

}
