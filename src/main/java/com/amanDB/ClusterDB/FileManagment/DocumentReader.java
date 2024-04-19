package com.amanDB.ClusterDB.FileManagment;


import java.io.File;
import java.io.FileNotFoundException;

import java.nio.file.*;
import java.util.Scanner;


public class DocumentReader extends Thread{

    private String path;
    private String document;

    private final static DocumentReader documentReader = new DocumentReader();

    private DocumentReader() {
    }

    public static DocumentReader getDocumentReader() {
        return documentReader;
    }

    public String read(String path) throws Exception {
        Path filePath = Paths.get(path);
        System.out.println(path);
        if (!Files.exists(filePath)) {
            throw new NoSuchFileException("Document Cannot be found.");
        } else {
            this.path = path;
            this.start();
            this.join();
        }
        return this.document;
    }

    @Override
    public void start() {
        try {
            File file = new File(path);
            Scanner myReader = new Scanner(file);
            document = "";
            while (myReader.hasNextLine()) {
                document += myReader.nextLine();

            }
            System.out.println(document);
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("Cannot Read file.");
            e.printStackTrace();
        }

    }
}
