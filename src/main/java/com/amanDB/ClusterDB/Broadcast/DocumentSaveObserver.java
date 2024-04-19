package com.amanDB.ClusterDB.Broadcast;

import com.amanDB.ClusterDB.Document.Document;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

public class DocumentSaveObserver extends Observer {
    Document document;

    public DocumentSaveObserver(Broadcast broadcast, Document document, String nodeUrl) {
        this.broadcast = broadcast;
        this.setNodeUri(nodeUrl + "/documentBroadcast");
        this.document = document;
        this.broadcast.attach(this);
    }

    @Override
    public void update() {
        HttpHeaders headers = new HttpHeaders();

        headers.setBasicAuth("aman", "12345");
        headers.setContentType(MediaType.APPLICATION_JSON);

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<Document> request = new HttpEntity<>(this.document, headers);

        restTemplate.exchange(
                this.getNodeUri(),
                HttpMethod.POST,
                request,
                String.class);
    }
}
