package com.amanDB.ClusterDB;

import com.amanDB.ClusterDB.FileManagment.InitialWrite;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class ClusterDbApplication {

	static InitialWrite init;

	@Autowired
	public ClusterDbApplication(InitialWrite initialWrite){
		init = initialWrite;
	}

	public static void main(String[] args) {

		SpringApplication.run(ClusterDbApplication.class, args);
		init.start();



	}

}
