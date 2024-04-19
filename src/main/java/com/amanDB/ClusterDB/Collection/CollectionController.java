package com.amanDB.ClusterDB.Collection;


import com.amanDB.ClusterDB.Document.Document;
import com.amanDB.ClusterDB.Indexing.IndexingService;
import com.amanDB.ClusterDB.Indexing.PropertiesFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("api/v1/collections")
public class CollectionController {

    private final CollectionService collectionService;
    private final IndexingService indexingService;

    @Autowired
    public CollectionController(CollectionService collectionService, IndexingService indexingService){
        this.collectionService = collectionService;
        this.indexingService = indexingService;
    }

    @GetMapping
    public List<Collection> get(@RequestParam(value = "databaseName") String databaseName){
        return collectionService.getCollections(databaseName);
    }

    @PostMapping
    public void save(@RequestBody Collection collection) throws Exception {
            collectionService.save(collection);
    }

    @DeleteMapping
    public void delete(@RequestBody Collection collection) throws Exception {
            collectionService.delete(collection);
    }

    @RequestMapping(method = RequestMethod.POST , value="/{databaseName}/{collectionName}/{propertyName}")
    public void createNewIndex(@PathVariable String databaseName , @PathVariable  String collectionName , @PathVariable String propertyName) throws Exception {

        Collection collection = new Collection();
        collection.setDatabase(databaseName);
        collection.setCollectionName(collectionName);
         indexingService.createNewIndex(propertyName,collection);
    }

    @RequestMapping(method = RequestMethod.GET ,value = "/index")
    public Document searchByPropertyValue(@RequestBody PropertiesFilter propertiesFilter) throws Exception {
        return indexingService.searchByPropertyValue(propertiesFilter);
    }


}
