package com.amanDB.ClusterDB.Document;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@NoArgsConstructor
@Setter
@Getter
public class DocumentRequest {

    private String database;
    private String collection;

    private String _Id;
    private Object oldValue;
    private Object newValue;
    private int nodeAffinity;

    public DocumentRequest(String database, String collection,Object oldValue,Object newValue) {
        this.database = database;
        this.collection = collection;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }
}
