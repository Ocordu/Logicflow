package com.ben.logicflow.states.quiz;

import com.mongodb.Block;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

public final class Question {
	private final String id;
	private final String title;
	private final String description;
	private final String difficulty;
	/*
	 * The names of the variables that will have sets of values assigned to them when the user executes her/his algorithm. These values can
	 * be thought of as arguments.
	 */
	private final ArrayList<String> inputNames;
	/*
	 * The names of the variables that will be compared to sets of values once execution has finished. The values of these variables can be
	 * thought of as values returned by a function. The sets of values they're compared against will determine the correctness of the
	 * algorithm.
	 */
	private final ArrayList<String> outputNames;
	/*
	 * Each test case will check if an algorithm returns an expected outcome for a set of values. The more test cases an algorithm is
	 * checked against, the more likely it's correct.
	 */
	private final ArrayList<TestCase> testCases;
	//These statistics represent all students who attempted the question.
	private int totalTime;
	private int attempts;
	private Question(String questionID, String title, String description, String difficulty, List<String> inputNames, List<String> outputNames, ArrayList<TestCase> testCases, int totalTime, int attempts) {
		this.id = questionID;
		this.title = title;
		this.description = description;
		this.difficulty = difficulty;
		this.inputNames = (ArrayList<String>) inputNames;
		this.outputNames = (ArrayList<String>) outputNames;
		this.testCases = testCases;
		this.totalTime = totalTime;
		this.attempts = attempts;
	}
	//Class utility method.
	public static ArrayList<Question> getQuestions(MongoCollection<Document> questionCollection) {
		final ArrayList<Question> questions = new ArrayList<>();
		final FindIterable<Document> iterable = questionCollection.find();
		iterable.forEach(new Block<Document>() {
			@Override
			public void apply(Document document) {
				if ((boolean) document.get("approved")) {
					questions.add(new Question(((ObjectId) document.get("_id")).toString(), (String) document.get("title"), (String) document.get("description"), (String) document.get("difficulty"), (List<String>) document.get("input_names"), (List<String>) document.get("output_names"), deserializeTestCases(document.get("test_cases")), (int) document.get("total_time"), (int) document.get("attempts")));
				}
			}
		});
		return questions;
	}
	private static ArrayList<TestCase> deserializeTestCases(Object serializedTestCases) {
		//A document can be thought of as a row in a relational database.
		final List<Document> documents = (List<Document>) serializedTestCases;
		final ArrayList<TestCase> testCases = new ArrayList<>();
		ArrayList<ArrayList<Double>> testInputs = new ArrayList<>();
		ArrayList<ArrayList<Double>> expectedOutputs = new ArrayList<>();
		for (Document document : documents) {
			final ArrayList<ArrayList<Double>> storedTestInputs = (ArrayList<ArrayList<Double>>) document.get("test_inputs");
			for (ArrayList<Double> setOfValues : storedTestInputs) {
				testInputs.add(setOfValues);
			}
			final ArrayList<ArrayList<Double>> storedExpectedOutputs = (ArrayList<ArrayList<Double>>) document.get("expected_outputs");
			for (ArrayList<Double> setOfValues : storedExpectedOutputs) {
				expectedOutputs.add(setOfValues);
			}
			testCases.add(new TestCase(testInputs, expectedOutputs));
			testInputs = new ArrayList<>();
			expectedOutputs = new ArrayList<>();
		}
		return testCases;
	}
	void updateStats(MongoCollection<Document> questionCollection, int time) {
		totalTime += time;
		attempts++;
		questionCollection.updateOne(new Document("_id", new ObjectId(id)), new Document("$set", new Document("total_time", totalTime)));
		questionCollection.updateOne(new Document("_id", new ObjectId(id)), new Document("$set", new Document("attempts", attempts)));
	}
	public String getTitle() {
		return title;
	}
	public String getDescription() {
		return description;
	}
	public String getDifficulty() {
		return difficulty;
	}
	public ArrayList<String> getInputNames() {
		return inputNames;
	}
	public ArrayList<String> getOutputNames() {
		return outputNames;
	}
	public TestCase getTestCase(int index) {
		return testCases.get(index);
	}
	public int getTotalTime() {
		return totalTime;
	}
	public int getAttempts() {
		return attempts;
	}
}