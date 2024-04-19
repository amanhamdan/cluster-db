package com.amanDB.ClusterDB.FileManagment;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DocumentRemover extends Thread{
    private String path;
    private final Object lock = new Object();

    private final static DocumentRemover documentRemover = new DocumentRemover();

    private DocumentRemover() {
    }

    public static DocumentRemover getDocumentRemover(){
        return documentRemover;
    }

    public void remove(String path){
        this.path = path;
        this.start();
    }

    @Override
    public void start() {
        Path filePath = Paths.get(path);
        System.out.println(path);
        if(!Files.exists(filePath)){
            try {
                throw new NoSuchFileException("Document doesn't exists.");
            } catch (NoSuchFileException e) {
                throw new RuntimeException(e);
            }
        }else{
            synchronized (lock) {
                File document = new File(path);
                try {

                    if(document.delete()) {
                        System.out.println("Document Deleted!");
                    }
                    else {
                        System.out.println("Document not Found");
                    }

                } catch (Exception e) {
                    e.getStackTrace();
                }
            }

        }
    }

}
