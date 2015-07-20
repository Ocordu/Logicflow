package com.ben.logicflow.flowchart.model;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.ben.logicflow.Application;
import com.ben.logicflow.flowchart.VertexType;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class FlowchartModel {
	private static final XStream X_STREAM = new XStream(new StaxDriver());
	static {
		X_STREAM.alias("start", VertexModel.class);
		X_STREAM.alias("process", ProcessModel.class);
		X_STREAM.alias("decision", DecisionModel.class);
		X_STREAM.alias("io", InputOutputModel.class);
		X_STREAM.setMode(XStream.ID_REFERENCES);
	}
	private VertexModel startVertex;
	private final HashMap<String, Double> variables = new HashMap<>();
	private int currentTarjanIndex;
	private int stronglyConnectedComponents;
	private final Random random = new Random();
	private int lastRandomNumber;
	public VertexModel addVertex(VertexType vertexType, VertexModel previousVertex, boolean branch) {
		VertexModel newVertex = null;
		switch (vertexType) {
			case START:
				newVertex = new VertexModel();
				this.startVertex = newVertex;
				break;
			case PROCESS:
				newVertex = new ProcessModel(this);
				break;
			case DECISION:
				newVertex = new DecisionModel(this);
				break;
			case IO:
				newVertex = new InputOutputModel();
				break;
		}
		if (vertexType != VertexType.START) {
			newVertex.addPreviousVertex(previousVertex);
			if (branch) {
				newVertex.setNextVertex(previousVertex.getNextVertex());
				previousVertex.setNextVertex(newVertex);
			} else {
				newVertex.setNextVertex(previousVertex.getNextFalseVertex());
				previousVertex.setNextFalseVertex(newVertex);
			}
			if (newVertex.getNextVertex() != null) {
				newVertex.getNextVertex().addPreviousVertex(newVertex);
				if (previousVertex.getVertexType() != VertexType.DECISION || (branch && previousVertex.getNextFalseVertex() != newVertex.getNextVertex()) || (!branch && previousVertex.getNextVertex() != newVertex.getNextVertex())) {
					newVertex.getNextVertex().removePreviousVertex(previousVertex);
				}
			}
		}
		return newVertex;
	}
	public void updateMainLoop(ArrayList<VertexModel> vertices) {
		for (VertexModel vertex : vertices) {
			if (vertex.getNextVertex() == null) {
				vertex.setNextVertex(startVertex);
			}
			if (vertex.getVertexType() == VertexType.DECISION && vertex.getNextFalseVertex() == null) {
				vertex.setNextFalseVertex(startVertex);
			}
		}
	}
	private void reconnect(VertexModel removedVertex, boolean branch) {
		ArrayList<VertexModel> previousVertices = removedVertex.getPreviousVertices();
		for (VertexModel previousVertex : previousVertices) {
			if (previousVertex.getVertexType() == VertexType.DECISION && previousVertex.getNextFalseVertex() == removedVertex) {
				if (branch) {
					previousVertex.setNextFalseVertex(removedVertex.getNextVertex());
					removedVertex.getNextVertex().addPreviousVertex(previousVertex);
				} else {
					previousVertex.setNextFalseVertex(removedVertex.getNextFalseVertex());
					removedVertex.getNextFalseVertex().addPreviousVertex(previousVertex);
				}
			} else if (branch) {
				previousVertex.setNextVertex(removedVertex.getNextVertex());
				removedVertex.getNextVertex().addPreviousVertex(previousVertex);
			} else {
				previousVertex.setNextVertex(removedVertex.getNextFalseVertex());
				removedVertex.getNextFalseVertex().addPreviousVertex(previousVertex);
			}
			if (previousVertex.getVertexType() == VertexType.DECISION && previousVertex.getNextVertex() == removedVertex) {
				if (branch) {
					previousVertex.setNextVertex(removedVertex.getNextVertex());
					removedVertex.getNextVertex().addPreviousVertex(previousVertex);
				} else {
					previousVertex.setNextVertex(removedVertex.getNextFalseVertex());
					removedVertex.getNextFalseVertex().addPreviousVertex(previousVertex);
				}
			}
		}
		removedVertex.getNextVertex().removePreviousVertex(removedVertex);
		if (removedVertex.getVertexType() == VertexType.DECISION) {
			removedVertex.getNextFalseVertex().removePreviousVertex(removedVertex);
		}
		if (branch) {
			removeSelfReference(removedVertex.getNextVertex());
		} else {
			removeSelfReference(removedVertex.getNextFalseVertex());
		}
	}
	private void removeSelfReference(VertexModel vertex) {
		if (vertex.getNextVertex() == vertex) {
			vertex.setNextVertex(startVertex);
			startVertex.addPreviousVertex(vertex);
		}
		if (vertex.getNextFalseVertex() == vertex) {
			vertex.setNextFalseVertex(startVertex);
			startVertex.addPreviousVertex(vertex);
		}
	}
	private void validateStructure(ArrayList<VertexModel> endVertices) {
		for (VertexModel vertex : endVertices) {
			vertex.getNextVertex().removePreviousVertex(vertex);
			if (vertex.getVertexType() == VertexType.DECISION) {
				vertex.getNextFalseVertex().removePreviousVertex(vertex);
			}
		}
	}
	public ArrayList<VertexModel> removeVertex(VertexModel removedVertex, boolean branch) {
		final ArrayList<VertexModel> protectedVertices = new ArrayList<>();
		final ArrayList<VertexModel> visitedVertices = new ArrayList<>();
		protectedVertices.add(removedVertex);
		if (removedVertex.getVertexType() == VertexType.DECISION) {
			reconnect(removedVertex, !branch);
			protectBranch(startVertex, protectedVertices);
			final ArrayList<VertexModel> endVertices = new ArrayList<>();
			if (branch) {
				recursivelyRemoveVertices(visitedVertices, removedVertex.getNextVertex(), protectedVertices, endVertices);
			} else {
				recursivelyRemoveVertices(visitedVertices, removedVertex.getNextFalseVertex(), protectedVertices, endVertices);
			}
			validateStructure(endVertices);
		} else {
			reconnect(removedVertex, true);
		}
		return visitedVertices;
	}
	private void protectBranch(VertexModel vertex, ArrayList<VertexModel> protectedVertices) {
		if (!protectedVertices.contains(vertex)) {
			protectedVertices.add(vertex);
			protectBranch(vertex.getNextVertex(), protectedVertices);
			if (vertex.getVertexType() == VertexType.DECISION) {
				protectBranch(vertex.getNextFalseVertex(), protectedVertices);
			}
		}
	}
	private void recursivelyRemoveVertices(ArrayList<VertexModel> visitedVertices, VertexModel startVertex, ArrayList<VertexModel> protectedVerticies, ArrayList<VertexModel> endVertices) {
		if (!visitedVertices.contains(startVertex) && startVertex != this.startVertex && !protectedVerticies.contains(startVertex) && startVertex.getPreviousVertices().size() < 2) {
			visitedVertices.add(startVertex);
			reconnect(startVertex, true);
			if (startVertex.getNextVertex() == this.startVertex || (startVertex.getVertexType() == VertexType.DECISION && startVertex.getNextFalseVertex() == this.startVertex) || startVertex.getNextVertex().getPreviousVertices().size() > 1 || (startVertex.getVertexType() == VertexType.DECISION && startVertex.getNextFalseVertex().getPreviousVertices().size() > 1)) {
				endVertices.add(startVertex);
			}
			recursivelyRemoveVertices(visitedVertices, startVertex.getNextVertex(), protectedVerticies, endVertices);
			if (startVertex.getVertexType() == VertexType.DECISION) {
				recursivelyRemoveVertices(visitedVertices, startVertex.getNextFalseVertex(), protectedVerticies, endVertices);
			}
		}
	}
	public ArrayList<VertexModel> findOutcasts(ArrayList<VertexModel> vertices) {
		@SuppressWarnings("UnnecessaryLocalVariable") final ArrayList<VertexModel> outcastVertices = vertices;
		filterOutcasts(startVertex, outcastVertices, new ArrayList<VertexModel>());
		for (VertexModel vertexModel : vertices) {
			for (VertexModel outcastVertexModel : outcastVertices) {
				if (vertexModel.getPreviousVertices().contains(outcastVertexModel)) {
					vertexModel.getPreviousVertices().remove(outcastVertexModel);
				}
			}
		}
		return outcastVertices;
	}
	private void filterOutcasts(VertexModel vertex, ArrayList<VertexModel> vertices, ArrayList<VertexModel> visitedVertices) {
		if (!visitedVertices.contains(vertex)) {
			visitedVertices.add(vertex);
			vertices.remove(vertex);
			filterOutcasts(vertex.getNextVertex(), vertices, visitedVertices);
			if (vertex.getVertexType() == VertexType.DECISION) {
				filterOutcasts(vertex.getNextFalseVertex(), vertices, visitedVertices);
			}
		}
	}
	public ArrayList<VertexModel> connectVertices(VertexModel firstVertex, VertexModel secondVertex, boolean branch) {
		final ArrayList<VertexModel> protectedVertices = new ArrayList<>();
		final ArrayList<VertexModel> visitedVertices = new ArrayList<>();
		final ArrayList<VertexModel> endVertices = new ArrayList<>();
		protectedVertices.add(firstVertex);
		if (secondVertex != null) {
			protectedVertices.add(secondVertex);
		}
		if (branch) {
			if (firstVertex.getVertexType() != VertexType.DECISION || firstVertex.getNextFalseVertex() != firstVertex.getNextVertex()) {
				recursivelyRemoveVertices(visitedVertices, firstVertex.getNextVertex(), protectedVertices, endVertices);
				if (!(firstVertex.getNextVertex() == startVertex && firstVertex == startVertex)) {
					firstVertex.getNextVertex().removePreviousVertex(firstVertex);
				}
			}
			if (secondVertex == null) {
				secondVertex = startVertex;
			}
			firstVertex.setNextVertex(secondVertex);
		} else {
			if (firstVertex.getVertexType() != VertexType.DECISION || firstVertex.getNextVertex() != firstVertex.getNextFalseVertex()) {
				recursivelyRemoveVertices(visitedVertices, firstVertex.getNextFalseVertex(), protectedVertices, endVertices);
				firstVertex.getNextFalseVertex().removePreviousVertex(firstVertex);
			}
			if (secondVertex == null) {
				secondVertex = startVertex;
			}
			firstVertex.setNextFalseVertex(secondVertex);
		}
		if (secondVertex != null) {
			secondVertex.addPreviousVertex(firstVertex);
		}
		validateStructure(endVertices);
		return visitedVertices;
	}
	public boolean isTarjanValid(ArrayList<VertexModel> vertices) {
		currentTarjanIndex = 0;
		stronglyConnectedComponents = 0;
		final Stack<VertexModel> stack = new Stack<>();
		for (VertexModel vertex : vertices) {
			vertex.setIndex(-1);
			vertex.setLowlink(-1);
		}
		for (VertexModel vertex : vertices) {
			if (vertex.getIndex() == -1) {
				strongConnect(vertex, stack);
			}
			if (stronglyConnectedComponents > 1) {
				return false;
			}
		}
		return true;
	}
	private void strongConnect(VertexModel vertex, Stack<VertexModel> stack) {
		vertex.setIndex(currentTarjanIndex);
		vertex.setLowlink(currentTarjanIndex);
		currentTarjanIndex++;
		stack.push(vertex);
		final ArrayList<VertexModel> nextVertices = new ArrayList<>();
		if (vertex.getNextVertex() != null) {
			nextVertices.add(vertex.getNextVertex());
		}
		if (vertex.getVertexType() == VertexType.DECISION && vertex.getNextFalseVertex() != null) {
			nextVertices.add(vertex.getNextFalseVertex());
		}
		for (VertexModel nextVertex : nextVertices) {
			if (nextVertex.getIndex() == -1) {
				strongConnect(nextVertex, stack);
				vertex.setLowlink(Math.min(vertex.getLowlink(), nextVertex.getLowlink()));
			} else if (stack.contains(nextVertex)) {
				vertex.setLowlink(Math.min(vertex.getLowlink(), nextVertex.getIndex()));
			}
		}
		if (vertex.getLowlink() == vertex.getIndex()) {
			VertexModel currentVertex;
			do {
				currentVertex = stack.pop();
			} while (currentVertex != vertex);
			stronglyConnectedComponents++;
		}
	}
	public void resetVariables() {
		variables.clear();
	}
	public void save(String fileName) {
		final FileHandle file = new FileHandle(Application.getProperty("FlowchartDirectory") + Application.getFileSeparator() + fileName + ".xml");
		file.writeString(X_STREAM.toXML(startVertex), false);
	}
	public boolean load(String fileName, VertexModel previousStartVertex) {
		try {
			final FileHandle file = new FileHandle(Application.getProperty("FlowchartDirectory") + Application.getFileSeparator() + fileName + ".xml");
			startVertex = (VertexModel) X_STREAM.fromXML(file.readString());
		} catch (GdxRuntimeException exception) {
			startVertex = previousStartVertex;
			return false;
		}
		return true;
	}
	public void removeStartVertex() {
		this.startVertex = null;
	}
	public VertexModel getStartVertex() {
		return startVertex;
	}
	public double getVariable(String name, boolean stepThrough) throws VariableNameException, VariableValueException {
		Pattern pattern = Pattern.compile("([a-z]|[A-Z])([a-z]|[A-Z]|\\d)*(\\[.+\\])*");
		Matcher matcher = pattern.matcher(name);
		if (!matcher.matches() && !name.equals("#RAND")) {
			throw new VariableNameException();
		}
		if (name.equals("#RAND")) {
			if (stepThrough) {
				return lastRandomNumber;
			}
			lastRandomNumber = random.nextInt(100) + 1;
			return lastRandomNumber;
		}
		pattern = Pattern.compile("\\[.+\\]");
		matcher = pattern.matcher(name);
		final String squareBracketsRegex = "\\[|\\]";
		final String arrayIdentifier = "\\[.+\\]";
		int matches = 0;
		while (matcher.find()) {
			if (Pattern.matches("([a-z]|[A-Z])([a-z]|[A-Z]|\\d)*", matcher.group().replaceAll(squareBracketsRegex, ""))) {
				name = name.replace(matcher.group().replaceAll(squareBracketsRegex, ""), String.valueOf((int) getVariable(matcher.group().replaceAll(squareBracketsRegex, ""), false)));
			} else if (Pattern.matches("\\d+", matcher.group().replaceAll(squareBracketsRegex, ""))) {
				name = name.replace(matcher.group().replaceAll(squareBracketsRegex, ""), String.valueOf(Integer.parseInt(matcher.group().replaceAll(squareBracketsRegex, ""))));
			} else {
				throw new VariableNameException();
			}
			matches++;
		}
		pattern = Pattern.compile("\\[0\\]");
		matcher = pattern.matcher(name);
		if (matches == 1 && matcher.find()) {
			name = name.replaceAll(arrayIdentifier, "");
		}
		if (!variables.containsKey(name)) {
			throw new VariableValueException();
		}
		return variables.get(name);
	}
	public int arrayLength(String name) {
		Pattern pattern = Pattern.compile(name + "\\[.+?\\]");
		int matches = 1;
		for (String variableName : variables.keySet()) {
			Matcher matcher = pattern.matcher(variableName);
			if (matcher.find()) {
				matches++;
			}
		}
		return matches;
	}
	public void storeArray(String name, ArrayList<Double> values) {
		int i = 0;
		for (double value : values) {
			try {
				setVariable(name + "[" + i + "]", value);
			} catch (VariableNameException | VariableValueException ignored) {
			}
			i++;
		}
	}
	public void setVariable(String name, double value) throws VariableNameException, VariableValueException {
		Pattern pattern = Pattern.compile("([a-z]|[A-Z])([a-z]|[A-Z]|\\d)*(\\[.+\\])*");
		Matcher matcher = pattern.matcher(name);
		if (!matcher.matches()) {
			throw new VariableNameException();
		}
		pattern = Pattern.compile("\\[.+\\]");
		matcher = pattern.matcher(name);
		final String squareBracketsRegex = "\\[|\\]";
		final String arrayIdentifier = "\\[.+\\]";
		int matches = 0;
		while (matcher.find()) {
			if (Pattern.matches("([a-z]|[A-Z])([a-z]|[A-Z]|\\d)*", matcher.group().replaceAll(squareBracketsRegex, ""))) {
				name = name.replace(matcher.group().replaceAll(squareBracketsRegex, ""), String.valueOf((int) getVariable(matcher.group().replaceAll(squareBracketsRegex, ""), false)));
			} else if (Pattern.matches("\\d+", matcher.group().replaceAll(squareBracketsRegex, ""))) {
				name = name.replace(matcher.group().replaceAll(squareBracketsRegex, ""), String.valueOf(Integer.parseInt(matcher.group().replaceAll(squareBracketsRegex, ""))));
			} else {
				throw new VariableNameException();
			}
			matches++;
		}
		pattern = Pattern.compile("\\[0\\]");
		matcher = pattern.matcher(name);
		if (matches == 1 && matcher.find()) {
			name = name.replaceAll(arrayIdentifier, "");
		}
		variables.put(name, value);
	}
	void validVariableName(String name, boolean readValue) throws VariableNameException, VariableValueException {
		if (!(readValue && name.equals("#RAND"))) {
			Pattern pattern = Pattern.compile("([a-z]|[A-Z])([a-z]|[A-Z]|\\d)*(\\[.+\\])*");
			Matcher matcher = pattern.matcher(name);
			if (!matcher.matches()) {
				throw new VariableNameException();
			}
			pattern = Pattern.compile("\\[.+\\]");
			matcher = pattern.matcher(name);
			final String squareBracketsRegex = "\\[|\\]";
			while (matcher.find()) {
				if (Pattern.matches("([a-z]|[A-Z])([a-z]|[A-Z]|\\d)*", matcher.group().replaceAll(squareBracketsRegex, ""))) {
					name = name.replace(matcher.group().replaceAll(squareBracketsRegex, ""), String.valueOf((int) getVariable(matcher.group().replaceAll(squareBracketsRegex, ""), false)));
				} else if (Pattern.matches("\\d+", matcher.group().replaceAll(squareBracketsRegex, ""))) {
					name = name.replace(matcher.group().replaceAll(squareBracketsRegex, ""), String.valueOf(Integer.parseInt(matcher.group().replaceAll(squareBracketsRegex, ""))));
				} else {
					throw new VariableNameException();
				}
			}
		}
	}
	public final static class VariableNameException extends Exception {
	}
	public final static class VariableValueException extends Exception {
	}
	public final static class ValueException extends Exception {
	}
}