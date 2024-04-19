package com.amanDB.ClusterDB.FileManagment;

import java.io.File;
import java.nio.file.*;

public class DirectoryWriter extends Thread{

    private String path;
    private final Object lock = new Object();

    private final static DirectoryWriter directoryWriter = new DirectoryWriter();

    private DirectoryWriter() {
    }

    public static DirectoryWriter getDirectoryWriter(){
        return directoryWriter;
    }

    public void write(String path) throws Exception {
        Path directoryPath = Paths.get(path);
        System.out.println(path);
        if(Files.exists(directoryPath)){
            throw new FileAlreadyExistsException("Directory with the same name already exists.");
        }else{
            this.path = path;
            this.start();
        }
    }

    @Override
    public void start() {
        synchronized (lock) {
            File directory = new File(path);
            if (!directory.mkdir()) {
                throw new RuntimeException("Could not create Directory please try again");
            }
        }
    }
}
