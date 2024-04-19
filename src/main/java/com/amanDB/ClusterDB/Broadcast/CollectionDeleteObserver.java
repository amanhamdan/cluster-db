package com.amanDB.ClusterDB.Broadcast;

import com.amanDB.ClusterDB.Collection.Collection;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

public class CollectionDeleteObserver extends Observer{
    Collection collection;
    public CollectionDeleteObserver(Broadcast broadcast , Collection collection , String nodeUrl){
        this.broadcast = broadcast;
        this.setNodeUri(nodeUrl + "/collections");
        this.collection = collection;
        this.broadcast.attach(this);
    }
    @Override
    public void update() {
        HttpHeaders headers = new HttpHeaders();

        headers.setBasicAuth("aman", "12345");
        headers.setContentType(MediaType.APPLICATION_JSON);

        RestTemplate restTemplate  = new RestTemplate();
        HttpEntity<Collection> request =  new HttpEntity<>(this.collection,headers);

        restTemplate.exchange(
                this.getNodeUri(),
                HttpMethod.DELETE,
                request,
                String.class);
    }
}
