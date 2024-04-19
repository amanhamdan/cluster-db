package com.amanDB.ClusterDB.Indexing;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class PropertiesFilter<T> {
    private String databaseName;
    private String collectionName;
    private String propertyName;
    private T value;

    public PropertiesFilter(String databaseName, String collectionName, String propertyName) {
        this.databaseName = databaseName;
        this.collectionName = collectionName;
        this.propertyName = propertyName;
    }
}


