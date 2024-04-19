package com.amanDB.ClusterDB.Database;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Setter
@Getter
public class Database {
    private String databaseName;
    private boolean isBroadcast = true;

    public Database(String databaseName) {
        this.databaseName = databaseName;
        this.isBroadcast = isBroadcast;
    }



}
