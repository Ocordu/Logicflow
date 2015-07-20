package com.ben.logicflow.flowchart.model;

public final class ExecutionData {
	private final VertexModel nextVertex;
	private final String information;
	ExecutionData(VertexModel nextVertex, String information) {
		this.nextVertex = nextVertex;
		this.information = information;
	}
	public VertexModel getNextVertex() {
		return nextVertex;
	}
	public String getInformation() {
		return information;
	}
}