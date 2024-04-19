package com.amanDB.ClusterDB.FileManagment;


import java.io.File;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DirectoryRemover extends  Thread{
    private String path;
    private final Object lock = new Object();

    private final static DirectoryRemover directoryRemover = new DirectoryRemover();

    private DirectoryRemover() {
    }

    public static DirectoryRemover getDirectoryRemover(){
        return directoryRemover;
    }

    public void remove(String path) {
        this.path = path;
        this.start();

    }

    @Override
    public void start() {
        Path directoryPath = Paths.get(path);
        System.out.println(path);
        if(!Files.exists(directoryPath)){
            try {
                throw new NoSuchFileException("Directory doesn't exists.");
            } catch (NoSuchFileException e) {
                throw new RuntimeException(e);
            }
        }else{
            // Delete all files in the directory if not empty or delete directly.
            synchronized (lock) {
                File directory = new File(path);
                try {
                    System.out.println(deleteDirectory(directory));

                } catch (Exception e) {
                    e.getStackTrace();
                }
            }

        }
    }

    private String deleteDirectory(File directory){

        File[] files = directory.listFiles();

        if(files != null){
            for(File file : files) {
                if(file.isDirectory()){
                    return deleteDirectory(file);
                }
                if(file.delete())
                    System.out.println(file + " deleted.");
                else
                    System.out.println(file + "could not be deleted");
            }
        }


        if(directory.delete()) {
           return "Directory Deleted";
        }
        else {
            return "Directory not Deleted";
        }
    }
}
