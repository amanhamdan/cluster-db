package com.amanDB.ClusterDB.Broadcast;


import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public abstract class Observer {

    protected Broadcast broadcast;
    private String NodeUri;
    public abstract void update();

}
