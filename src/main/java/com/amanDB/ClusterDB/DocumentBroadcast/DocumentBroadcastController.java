package com.amanDB.ClusterDB.DocumentBroadcast;

import com.amanDB.ClusterDB.Document.Document;
import com.amanDB.ClusterDB.Document.DocumentRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("api/v1/documentBroadcast")
public class DocumentBroadcastController {
    private final DocumentBroadcastService documentBroadcastService;

    @Autowired
    public DocumentBroadcastController(DocumentBroadcastService documentBroadcastService){
        this.documentBroadcastService = documentBroadcastService;
    }


    @PostMapping
    public void save(@RequestBody Document document) {

        try{
            documentBroadcastService.broadcastSave(document);
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    @DeleteMapping
    public void delete(@RequestBody Document document) {
        try{
            documentBroadcastService.broadcastDelete(document);
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    @PutMapping
    public void update(@RequestBody DocumentRequest documentRequest) throws Exception {
        documentBroadcastService.broadcastUpdate(documentRequest);


    }

}
