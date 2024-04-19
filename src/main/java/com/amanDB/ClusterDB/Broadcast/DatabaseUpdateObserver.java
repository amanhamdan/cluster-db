package com.amanDB.ClusterDB.Broadcast;


import com.amanDB.ClusterDB.Database.Database;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

public class DatabaseUpdateObserver extends Observer{
    private final Database database;
    public DatabaseUpdateObserver(Broadcast broadcast , Database database , String nodeUrl){
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
        HttpEntity<Database> request =  new HttpEntity<>(this.database,headers);

        restTemplate.exchange(
                this.getNodeUri(),
                HttpMethod.POST,
                request,
                String.class);

    }

}
