package com.ben.logicflow.states.quiz;

import java.util.ArrayList;

/*
 * A test case has multiple test inputs which store multiple sets of values associated with variables. Variables have sets of values
 * associated with them as they could be arrays. A non-array variable will only have one value in its set of associated values. This is
 * the same for expected outputs. If for a given set of test inputs an algorithm returns a set of values that match the expected
 * outputs, that algorithm passes the test case in question.
 */
public final class TestCase {
	private final ArrayList<ArrayList<Double>> testInputs;
	private final ArrayList<ArrayList<Double>> expectedOutputs;
	TestCase(ArrayList<ArrayList<Double>> testInputs, ArrayList<ArrayList<Double>> expectedOutputs) {
		this.testInputs = testInputs;
		this.expectedOutputs = expectedOutputs;
	}
	public ArrayList<ArrayList<Double>> getTestInputs() {
		return testInputs;
	}
	public ArrayList<ArrayList<Double>> getExpectedOutputs() {
		return expectedOutputs;
	}
}