package com.amanDB.ClusterDB.FileManagment;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.amanDB.ClusterDB.NodeManagment.NodesService;
import com.amanDB.ClusterDB.Usersbuild.User;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


@NoArgsConstructor
@Configuration
public class InitialWrite extends Thread {

    @Value("${nodeID}")
    private int nodeID;
    private final NodesService nodesService = new NodesService();
    private final DocumentWriter writer = DocumentWriter.getDocumentWriter();

    @Override
    public void start() {
        try {
            nodesService.writeNodesFromBootstrapper();
            getPredefinedUsers();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        ArrayList<File> mainFiles = new ArrayList<>();

        File dbDirectory = new File("Databases");
        File usersDirectory = new File("DatabaseUsers");
        File SchemasDirectory = new File("Schemas");
        System.out.println("main Directory Creation: ===>");
        System.out.println(dbDirectory.getAbsolutePath());
        System.out.println(usersDirectory.getAbsolutePath());
        System.out.println(SchemasDirectory.getAbsolutePath());

        System.out.println(dbDirectory.getPath());
        mainFiles.add(dbDirectory);
        mainFiles.add(usersDirectory);
        mainFiles.add(SchemasDirectory);

        for (File dir :
                mainFiles) {
            if (!dir.exists()) {
                if (!dir.mkdir()) {
                    throw new RuntimeException("Could not create Directory please try again");
                } else {
                    System.out.println(dir.getAbsolutePath() + " Created Successfully");
                }
            } else {
                System.out.println(dir.getAbsolutePath() + " already Exists");
            }
        }
    }
    public void getPredefinedUsers() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        String NodeURL = "http://host.docker.internal:9090/api/v1/usersConfig?nodeID=" + nodeID;
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth("aman", "password");
        headers.setContentType(MediaType.APPLICATION_JSON);
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<String> request = new HttpEntity<>(null, headers);
        ResponseEntity<String> productCreateResponse = restTemplate.exchange(
                NodeURL,
                HttpMethod.GET,
                request,
                String.class);


        String usersJsonArray = productCreateResponse.getBody();
        TypeReference<List<User>> mapType = new TypeReference<List<User>>() {};
        List<User> users = objectMapper.readValue(usersJsonArray, mapType);
        for (User user :
                users) {
            writer.write("DatabaseUsers/" + user.getUsername() + ".json",objectMapper.writeValueAsString(user));
        }
    }

}
