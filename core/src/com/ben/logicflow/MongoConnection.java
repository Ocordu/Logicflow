package com.ben.logicflow;

import com.mongodb.*;
import com.mongodb.client.MongoDatabase;

import java.util.Arrays;

public final class MongoConnection {
	/*
	 * Only one instance of MongoClient is needed so it's treated as a class variable.
	 */
	private static MongoClient client;
	/*
	 * The constructor is private to prevent other classes from creating an instance of MongoConnection. This is because MongoConnection
	 * has no instance methods or subclasses.
	 */
	private MongoConnection() {
	}
	public static MongoDatabase getDatabase() {
		//Lazy initialisation used for convenience. This means that variables are only assigned when their values are needed.
		if (client == null) {
			final MongoClientOptions mongoClientOptions = MongoClientOptions.builder().serverSelectionTimeout(5000).connectTimeout(5000).socketTimeout(5000).heartbeatConnectTimeout(5000).heartbeatSocketTimeout(5000).maxWaitTime(5000).build();
			//Check the lowercase value of NoServerAuthentication so that capitalisation doesn't matter.
			switch (Application.getProperty("NoServerAuthentication").toLowerCase()) {
				case "true":
					client = new MongoClient(new ServerAddress(Application.getProperty("ServerIP"), Integer.parseInt(Application.getProperty("ServerPort"))), mongoClientOptions);
					break;
				case "false":
					client = new MongoClient(new ServerAddress(Application.getProperty("ServerIP"), Integer.parseInt(Application.getProperty("ServerPort"))), Arrays.asList(MongoCredential.createScramSha1Credential(Application.getProperty("DatabaseUsername"), Application.getProperty("DatabaseName"), Application.getProperty("DatabasePassword").toCharArray())), mongoClientOptions);
					break;
			}
		}
		return client.getDatabase(Application.getProperty("DatabaseName"));
	}
}