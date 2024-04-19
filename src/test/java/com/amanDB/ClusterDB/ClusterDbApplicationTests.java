package com.amanDB.ClusterDB;

import com.amanDB.ClusterDB.Collection.Collection;
import com.amanDB.ClusterDB.Document.Document;
import com.amanDB.ClusterDB.Indexing.BTree;
import com.amanDB.ClusterDB.Indexing.IndexingService;
import com.amanDB.ClusterDB.Indexing.PropertiesFilter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ClusterDbApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	void saveNewIndex () throws Exception {
		Collection collection = new Collection();
		collection.setCollectionName("MarksCollection");
		collection.setDatabase("StudentsDB");
		IndexingService indexingService = new IndexingService();
		indexingService.createNewIndex("markId", collection);
	}

	@Test
	void loadIndexFromDisk () throws Exception {
		Collection collection = new Collection();
		collection.setCollectionName("MarksCollection");
		collection.setDatabase("StudentsDB");
		IndexingService indexingService = new IndexingService();
		BTree<String,String> indexTree=  indexingService.loadIndexFromDisk("markId", collection);
		System.out.println(indexTree);
	}

	@Test
	void SearchByProperty () throws Exception {
		PropertiesFilter<Integer> filter = new PropertiesFilter<>();
		filter.setCollectionName("MarksCollection");
		filter.setDatabaseName("StudentsDB");
		filter.setPropertyName("markId");
		filter.setValue(2);
		IndexingService indexingService = new IndexingService();
		Document document =  indexingService.searchByPropertyValue(filter);

		System.out.println(document.getJsonString());
	}
}
