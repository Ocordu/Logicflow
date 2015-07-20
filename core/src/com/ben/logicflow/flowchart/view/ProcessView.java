package com.ben.logicflow.flowchart.view;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.ben.logicflow.flowchart.VertexType;

public final class ProcessView extends ExecutableView {
	public ProcessView(Stage stage) {
		super(stage);
		getTextField(0).setMessageText("Variable");
		getTextField(0).setMaxLength(15);
		getTable().add(getTextField(0)).width(70).space(5);
		getTextField(1).setMessageText("Op");
		getTextField(1).setMaxLength(2);
		getTable().add(getTextField(1)).width(40).space(5);
		getTextField(2).setMessageText("Value");
		getTextField(2).setMaxLength(15);
		getTable().add(getTextField(2)).width(70).space(5);
		getInnerRectangle().setSize((int) (getTable().getPrefWidth() / 10) * 10 + 30, (int) (getTable().getPrefHeight() / 10) * 10 + 30);
		getContainer().setSize(getInnerRectangle().getWidth(), getInnerRectangle().getHeight());
	}
	@Override
	public VertexType getVertexType() {
		return VertexType.PROCESS;
	}
}