package com.mvpjava.tutorial;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Optional;
import java.util.Random;

import org.apache.commons.lang3.RandomStringUtils;
import org.bson.Document;
import org.slf4j.LoggerFactory;

import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;

public class App {
    private final static  java.util.logging.Logger LOGGER =  java.util.logging.Logger.getLogger(App.class.getName());

	public static void main(String[] args) {
		App app = new App();
		app.mongoDemo();
		
	}

	private void mongoDemo() {		
		Logger rootLogger = (Logger)LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME); 
		rootLogger.setLevel(Level.ERROR);
		
		MongoCredential credential = MongoCredential.createCredential
				(
				  System.getenv("MONGO_INITDB_ROOT_USERNAME"), "admin",
				  System.getenv("MONGO_INITDB_ROOT_PASSWORD").toCharArray()
				);
		
    	LOGGER.info("Attempting to connect to mongo ...");
		
		MongoClient mongoClient = MongoClients.create(
		        MongoClientSettings.builder()
		                .applyToClusterSettings(builder -> builder.hosts(Arrays.asList(new ServerAddress("mongo", 27017))))
		                .credential(credential)
		                .build());
		
	    MongoDatabase database = mongoClient.getDatabase("demo");
	    MongoCollection<Document> collection = database.getCollection("metrics");
 
	    LOGGER.info("Connected to mongo");
	    
	    //Main Loop which inserts a new Document into Collection every 5 seconds
	    Random randomHits = new Random();

	    while (true)
	    {	
	    	LOGGER.info("Inserting Document into metrics Collection");
			collection.insertOne(new Document()
						    .append("website", (RandomStringUtils.randomAlphabetic(10))+ ".com")
	    					.append("hitcount", randomHits.nextInt(1000))
					    );	
			
			try {
				//Slow this down on purpose
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				LOGGER.warning("Sleep was interuppted by another Thread. Exiting");
				System.exit(-1);
			}
			
	    }    	    
	   
	}
	
	/*
	 * Dumps contents of Collection when user enters any key and hits ENTER from console.
	 * To stop program just press ENTER key 
	 */
	public void viewCollectionViaUserInput(MongoCollection<Document> collection) {
	    MongoCursor<Document> cursor ;

	    while (isUserInputAvailable())
	    {
			cursor = collection.find().iterator();
			try {
				while (cursor.hasNext()) {
					System.out.println(cursor.next().toJson());
				}
			} finally {
				cursor.close();
			}	
	    }
	}
	
	
	private boolean isUserInputAvailable() {
		return getUserInput(getSTDIN()).isPresent();
	}
	
	private BufferedReader getSTDIN() {
		return (new BufferedReader(new InputStreamReader(System.in)));
	}
	
	private Optional<String> getUserInput(BufferedReader reader) {
		String userInput = null;
		try {
		    System.out.println ("Press Any Key and press ENTER to view data");
			 userInput = reader.readLine();
			 if (userInput == null || userInput.length() == 0) {
				 return Optional.empty();
			 }
		} catch (IOException e) {
		    System.out.println ("Unable to read user input");
			e.printStackTrace();
		}
		return Optional.of(userInput);
	}

}
