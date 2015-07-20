package com.ben.logicflow.flowchart.model;

import com.badlogic.gdx.math.Vector2;
import com.ben.logicflow.flowchart.VertexType;

import java.util.ArrayList;

public class VertexModel {
	private final Vector2 position = new Vector2();
	private VertexModel nextVertex;
	private transient ArrayList<VertexModel> previousVertices;
	private transient int index;
	private transient int lowlink;
	public void addPreviousVertex(VertexModel previousVertex) {
		if (previousVertices == null) {
			previousVertices = new ArrayList<>();
		}
		if (!previousVertices.contains(previousVertex) && previousVertex != this) {
			previousVertices.add(previousVertex);
		}
	}
	public void removePreviousVertex(VertexModel previousVertex) {
		previousVertices.remove(previousVertex);
	}
	public VertexModel getNextVertex() {
		return nextVertex;
	}
	public void setNextVertex(VertexModel nextVertex) {
		this.nextVertex = nextVertex;
	}
	public VertexModel getNextFalseVertex() {
		return null;
	}
	public void setNextFalseVertex(VertexModel nextFalseVertex) {
	}
	public ArrayList<VertexModel> getPreviousVertices() {
		if (previousVertices == null) {
			previousVertices = new ArrayList<>();
		}
		return previousVertices;
	}
	public VertexType getVertexType() {
		return VertexType.START;
	}
	public Vector2 getPosition() {
		return position;
	}
	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}
	public int getLowlink() {
		return lowlink;
	}
	public void setLowlink(int lowlink) {
		this.lowlink = lowlink;
	}
	public void setPosition(float x, float y) {
		position.set(x, y);
	}
}