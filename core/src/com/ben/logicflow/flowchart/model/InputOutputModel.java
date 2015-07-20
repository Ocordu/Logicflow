package com.ben.logicflow.flowchart.model;

import com.ben.logicflow.flowchart.VertexType;

public final class InputOutputModel extends VertexModel implements ExecutableModel {
	private String operator;
	private String variable;
	private String message;
	@Override
	public ExecutionData execute() {
		if (message == null || (getOperationType() == IOOperation.INPUT && variable == null)) {
			return new ExecutionData(null, "IGNORED");
		}
		if (getOperationType() == IOOperation.NONE) {
			return new ExecutionData(null, "OPERATOR");
		}
		return null;
	}
	@Override
	public VertexType getVertexType() {
		return VertexType.IO;
	}
	@Override
	public String getData(int id) {
		switch (id) {
			case 0:
				return operator;
			case 1:
				return variable;
			case 2:
				return message;
			default:
				return null;
		}
	}
	public IOOperation getOperationType() {
		if (operator != null) {
			if (operator.toUpperCase().equals("IN")) {
				return IOOperation.INPUT;
			} else if (operator.toUpperCase().equals("OUT")) {
				return IOOperation.OUTPUT;
			} else {
				return IOOperation.NONE;
			}
		} else {
			return IOOperation.NONE;
		}
	}
	public String getVariable() {
		return variable;
	}
	public String getMessage() {
		return message;
	}
	@Override
	public void setData(int id, String data) {
		switch (id) {
			case 0:
				operator = data;
				if (operator.isEmpty()) {
					operator = null;
				}
				break;
			case 1:
				variable = data;
				if (variable.isEmpty()) {
					variable = null;
				}
				break;
			case 2:
				message = data;
				if (message.isEmpty()) {
					message = null;
				}
		}
	}
	public enum IOOperation {
		INPUT, OUTPUT, NONE
	}
}