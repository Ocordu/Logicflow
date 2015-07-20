package com.ben.logicflow.flowchart.model;

import com.ben.logicflow.flowchart.VertexType;

public final class DecisionModel extends VertexModel implements ExecutableModel {
	private transient FlowchartModel flowchart;
	private VertexModel nextFalseVertex;
	private String variable;
	private String operator;
	private String value;
	public DecisionModel(FlowchartModel flowchart) {
		this.flowchart = flowchart;
	}
	@Override
	public ExecutionData execute() throws FlowchartModel.VariableNameException, FlowchartModel.VariableValueException, FlowchartModel.ValueException {
		if (variable != null && operator != null && value != null) {
			boolean trueResult = false;
			boolean literal = true;
			try {
				int value = Integer.parseInt(this.value);
				if (operator.equals("=") && flowchart.getVariable(variable, false) == value) {
					trueResult = true;
				} else if ((operator.equals("!=") || operator.equals("<>")) && flowchart.getVariable(variable, false) != value) {
					trueResult = true;
				} else if (operator.equals(">") && flowchart.getVariable(variable, false) > value) {
					trueResult = true;
				} else if (operator.equals("<") && flowchart.getVariable(variable, false) < value) {
					trueResult = true;
				} else if (operator.equals(">=") && flowchart.getVariable(variable, false) >= value) {
					trueResult = true;
				} else if (operator.equals("<=") && flowchart.getVariable(variable, false) <= value) {
					trueResult = true;
				}
			} catch (NumberFormatException e) {
				try {
					flowchart.validVariableName(value, true);
					if (operator.equals("=") && flowchart.getVariable(variable, false) == flowchart.getVariable(value, false)) {
						trueResult = true;
					} else if ((operator.equals("!=") || operator.equals("<>")) && flowchart.getVariable(variable, false) != flowchart.getVariable(value, false)) {
						trueResult = true;
					} else if (operator.equals(">") && flowchart.getVariable(variable, false) > flowchart.getVariable(value, false)) {
						trueResult = true;
					} else if (operator.equals("<") && flowchart.getVariable(variable, false) < flowchart.getVariable(value, false)) {
						trueResult = true;
					} else if (operator.equals(">=") && flowchart.getVariable(variable, false) >= flowchart.getVariable(value, false)) {
						trueResult = true;
					} else if (operator.equals("<=") && flowchart.getVariable(variable, false) <= flowchart.getVariable(value, false)) {
						trueResult = true;
					}
					literal = false;
				} catch (FlowchartModel.VariableNameException exception) {
					throw new FlowchartModel.ValueException();
				}
			}
			if (operator.equals("=") || (operator.equals("!=") || operator.equals("<>")) || operator.equals(">") || operator.equals("<") || operator.equals(">=") || operator.equals("<=")) {
				String comparedValue;
				if (literal) {
					comparedValue = " | [literal] value: " + value;
				} else {
					comparedValue = " | <" + value + "> value: " + flowchart.getVariable(value, true);
				}
				if (trueResult) {
					return new ExecutionData(getNextVertex(), "<" + variable + "> value: " + flowchart.getVariable(variable, true) + comparedValue + " | outcome: true");
				}
				return new ExecutionData(nextFalseVertex, "<" + variable + "> value: " + flowchart.getVariable(variable, true) + comparedValue + " | outcome: false");
			} else {
				return new ExecutionData(getNextVertex(), "OPERATOR");
			}
		}
		return new ExecutionData(getNextVertex(), "IGNORED");
	}
	@Override
	public VertexModel getNextFalseVertex() {
		return nextFalseVertex;
	}
	@Override
	public void setNextFalseVertex(VertexModel nextFalseVertex) {
		this.nextFalseVertex = nextFalseVertex;
	}
	@Override
	public VertexType getVertexType() {
		return VertexType.DECISION;
	}
	@Override
	public String getData(int id) {
		switch (id) {
			case 0:
				return variable;
			case 1:
				return operator;
			case 2:
				return value;
			default:
				return null;
		}
	}
	public void setFlowchart(FlowchartModel flowchart) {
		this.flowchart = flowchart;
	}
	@Override
	public void setData(int id, String data) {
		switch (id) {
			case 0:
				variable = data;
				if (variable.isEmpty()) {
					variable = null;
				}
				break;
			case 1:
				operator = data;
				if (operator.isEmpty()) {
					operator = null;
				}
				break;
			case 2:
				value = data;
				if (value.isEmpty()) {
					value = null;
				}
		}
	}
}