package com.amanDB.ClusterDB.Broadcast;

import java.util.ArrayList;
import java.util.List;

public class Broadcast {
    private final List<Observer> observers = new ArrayList<>();

    public void attach(Observer observer){
        observers.add(observer);
    }

    public void notifyAllObservers(){
        for (Observer observer : observers) {
            observer.update();
        }
    }
}
