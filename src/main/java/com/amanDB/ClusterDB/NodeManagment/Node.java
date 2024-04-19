package com.amanDB.ClusterDB.NodeManagment;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class Node {
    private int nodeID;
    private String nodeURL;

    public Node(int nodeID, String nodeURL) {
        this.nodeID = nodeID;
        this.nodeURL = nodeURL;
    }
}
