package com.ben.logicflow.flowchart.model;

import com.ben.logicflow.flowchart.VertexType;

public final class ProcessModel extends VertexModel implements ExecutableModel {
	private transient FlowchartModel flowchart;
	private String variable;
	private String operator;
	private String value;
	public ProcessModel(FlowchartModel flowchart) {
		this.flowchart = flowchart;
	}
	@Override
	public ExecutionData execute() throws FlowchartModel.VariableNameException, FlowchartModel.VariableValueException, FlowchartModel.ValueException {
		if (variable != null && value != null && operator != null) {
			double previousValue = 0;
			boolean previousValueExists = true;
			try {
				previousValue = flowchart.getVariable(variable, false);
			} catch (FlowchartModel.VariableValueException ignored) {
				previousValueExists = false;
			}
			boolean literal = true;
			try {
				double value = Double.parseDouble(this.value);
				switch (operator) {
					case "<-":
						flowchart.setVariable(variable, value);
						break;
					case "+=":
						flowchart.setVariable(variable, flowchart.getVariable(variable, false) + value);
						break;
					case "-=":
						flowchart.setVariable(variable, flowchart.getVariable(variable, false) - value);
						break;
					case "*=":
						flowchart.setVariable(variable, flowchart.getVariable(variable, false) * value);
						break;
					case "/=":
						flowchart.setVariable(variable, flowchart.getVariable(variable, false) / value);
						break;
				}
			} catch (NumberFormatException e) {
				try {
					flowchart.validVariableName(value, true);
					switch (operator) {
						case "<-":
							flowchart.setVariable(variable, flowchart.getVariable(value, false));
							break;
						case "+=":
							flowchart.setVariable(variable, flowchart.getVariable(variable, false) + flowchart.getVariable(value, false));
							break;
						case "-=":
							flowchart.setVariable(variable, flowchart.getVariable(variable, false) - flowchart.getVariable(value, false));
							break;
						case "*=":
							flowchart.setVariable(variable, flowchart.getVariable(variable, false) * flowchart.getVariable(value, false));
							break;
						case "/=":
							flowchart.setVariable(variable, flowchart.getVariable(variable, false) / flowchart.getVariable(value, false));
							break;
					}
					literal = false;
				} catch (FlowchartModel.VariableNameException exception) {
					throw new FlowchartModel.ValueException();
				}
			}
			if (operator.equals("<-") || operator.equals("+=") || operator.equals("-=") || operator.equals("*=") || operator.equals("/=")) {
				String assignedValue;
				if (literal) {
					assignedValue = "[literal] value: " + value;
				} else {
					assignedValue = "<" + value + "> value: " + flowchart.getVariable(value, true);
				}
				if (previousValueExists) {
					return new ExecutionData(getNextVertex(), "<" + variable + "> previous value: " + previousValue + " | " + assignedValue + " | <" + variable + "> new value: " + flowchart.getVariable(variable, false));
				} else {
					return new ExecutionData(getNextVertex(), assignedValue + " | <" + variable + "> new value: " + flowchart.getVariable(variable, false));
				}
			} else {
				return new ExecutionData(getNextVertex(), "OPERATOR");
			}
		}
		return new ExecutionData(getNextVertex(), "IGNORED");
	}
	@Override
	public VertexType getVertexType() {
		return VertexType.PROCESS;
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