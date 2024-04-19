package com.amanDB.ClusterDB.Collection;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@NoArgsConstructor
@Getter
@Setter
public class Collection {
    private  String database;
    private String collectionName;
    private Object schema;
    private Boolean isBroadcast = true;

    public Collection(String database,String collectionName,Object schema,boolean isBroadcast ) {
        this.database =database;
        this.collectionName = collectionName;
        this.schema = schema;
        this.isBroadcast = isBroadcast;
    }
}
