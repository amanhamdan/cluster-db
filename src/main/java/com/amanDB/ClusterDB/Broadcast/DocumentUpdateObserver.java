package com.amanDB.ClusterDB.Broadcast;


import com.amanDB.ClusterDB.Document.DocumentRequest;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

public class DocumentUpdateObserver extends Observer{
    DocumentRequest documentRequest;
    public DocumentUpdateObserver(Broadcast broadcast , DocumentRequest documentRequest , String nodeUrl){
        this.broadcast = broadcast;
        this.setNodeUri(nodeUrl + "/documentBroadcast");
        this.documentRequest = documentRequest;
        this.broadcast.attach(this);
    }
    @Override
    public void update() {
        HttpHeaders headers = new HttpHeaders();

        headers.setBasicAuth("aman", "12345");
        headers.setContentType(MediaType.APPLICATION_JSON);

        RestTemplate restTemplate  = new RestTemplate();
        HttpEntity<DocumentRequest> request =  new HttpEntity<>(this.documentRequest,headers);

        restTemplate.exchange(
                this.getNodeUri(),
                HttpMethod.POST,
                request,
                String.class);
    }
}
