package com.amanDB.ClusterDB.Broadcast;

import com.amanDB.ClusterDB.Database.Database;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

public class DatabaseDeleteObserver extends Observer{
    Database database;
    public DatabaseDeleteObserver(Broadcast broadcast , Database database , String nodeUrl){
        this.broadcast = broadcast;
        this.setNodeUri(nodeUrl + "/databases");
        this.database = database;
        this.broadcast.attach(this);
    }
    @Override
    public void update() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth("aman", "12345");
        headers.setContentType(MediaType.APPLICATION_JSON);

        RestTemplate restTemplate  = new RestTemplate();
        HttpEntity<String> request =  new HttpEntity<>(null,headers);

        String uri = this.getNodeUri() + "?databaseName=" + database.getDatabaseName();
        ResponseEntity<String> response = restTemplate.exchange(
                uri,
                HttpMethod.DELETE,
                request,
                String.class);

        response.getStatusCode();
    }
}
