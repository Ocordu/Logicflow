package com.ben.logicflow.flowchart.model;

public interface ExecutableModel {
	ExecutionData execute() throws FlowchartModel.VariableNameException, FlowchartModel.VariableValueException, FlowchartModel.ValueException;
	String getData(int id);
	void setData(int id, String data);
}