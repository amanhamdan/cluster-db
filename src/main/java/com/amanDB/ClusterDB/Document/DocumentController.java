package com.amanDB.ClusterDB.Document;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/documents")
public class DocumentController {

    private final DocumentService documentService;

    @Autowired
    public DocumentController(DocumentService documentService){
        this.documentService = documentService;
    }


    @GetMapping
    public Document get(@RequestParam(value = "databaseName" )  String databaseName ,
                        @RequestParam(value = "collectionName" ) String collectionName ,
                        @RequestParam(value = "documentID" ) String documentID) throws Exception {

          Document document = new Document();
          document.set_Id(documentID);
          document.setDatabase(databaseName);
          document.setCollection(collectionName);
          return documentService.read(document);
    }

    @RequestMapping(method = RequestMethod.GET , value="/{databaseName}/{collectionName}")
    public List<Document> getAll( @PathVariable String databaseName ,@PathVariable  String collectionName) throws Exception {

        Document document = new Document();
        document.setDatabase(databaseName);
        document.setCollection(collectionName);
        return documentService.getDocuments(document);
    }

    @PostMapping
    public Document save(@RequestBody Document document) {
        try{
            documentService.save(document);
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
        return document;
    }

    @DeleteMapping
    public void delete(@RequestBody Document document) {
        try{
            documentService.delete(document);
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    @PutMapping
    public void update(@RequestBody DocumentRequest documentRequest) throws Exception {
        documentService.update(documentRequest);
    }

}
