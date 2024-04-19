package com.amanDB.ClusterDB.Document;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@NoArgsConstructor
@Setter
@Getter
public class Document {

    private String database;
    private String collection;
    private String _Id;

    private Object jsonString;

    private int NodeAffinity = -1;


    public Document(String database, String collection, String _Id,Object jsonString) {
        this.database = database;
        this.collection = collection;
        this._Id = _Id;
        this.jsonString = jsonString;
    }



}
