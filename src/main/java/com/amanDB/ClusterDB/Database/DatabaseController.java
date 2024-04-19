package com.amanDB.ClusterDB.Database;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;


import java.util.*;

@RestController
@RequestMapping("api/v1/databases")
public class DatabaseController {
    private final DatabaseService dbService;

    @Autowired
    public DatabaseController(DatabaseService databaseService){
        this.dbService = databaseService;
    }
    @Value("${nodeID}")
    private  int nodeID;


    @GetMapping
    public List<Database> get(){
        return dbService.getDatabases();
    }

    @PostMapping
    public void save(@RequestBody Database database) {

        try{
            dbService.save(database);

        }catch (Exception ex){
            throw new RuntimeException(ex);
        }

    }

    @DeleteMapping
    public void delete(@RequestBody Database database) {
        try{
            dbService.delete(database);
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

}
