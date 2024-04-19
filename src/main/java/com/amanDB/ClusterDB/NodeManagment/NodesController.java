package com.amanDB.ClusterDB.NodeManagment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/v1/nodes")
public class NodesController {

    private NodesService nodesService;

    @Autowired
    public NodesController(NodesService nodesService){
        this.nodesService = nodesService;
    }

    @GetMapping
    public List<Node> broadCastNodes() throws Exception {

        return  nodesService.getNodesList();
    }

}
