package com.amanDB.ClusterDB.FileManagment;


import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class OptimisticReader extends Thread {
    private String path;
    private String document;
    private String comparableDocument;
    private final Object lock;

    private boolean match = false;

    public OptimisticReader(Object lock) {
        this.lock = lock;
    }

    public void setComparableDocument(String comparableDocument) {
        this.comparableDocument = comparableDocument;
    }

    public boolean isMatch(String path,String comparableDocument) throws Exception {
        this.comparableDocument = comparableDocument;
        Path filePath = Paths.get(path);
        System.out.println(path);
        if (!Files.exists(filePath)) {
            throw new NoSuchFileException("Document Cannot be found.");
        } else {
            this.path = path;
            this.start();
            this.join();
        }
        return this.match;
    }


    @Override
    public void start() {
        try {
            while(DocumentWriter.holdsLock(lock)){
                lock.wait();
            }
                File file = new File(path);
                Scanner myReader = new Scanner(file);
                document = "";
                while (myReader.hasNextLine()) {
                    document += myReader.nextLine();
                    System.out.println(document);
                }
                myReader.close();

                if(comparableDocument!= null){
                    if(comparableDocument.equals(document)){
                        match = true;
                    }
                }

        } catch (FileNotFoundException e) {
            System.out.println("Cannot Read file.");
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }
}
