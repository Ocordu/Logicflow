package com.ben.logicflow.flowchart.view;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.ben.logicflow.flowchart.VertexType;

public final class InputOutputView extends ExecutableView {
	public InputOutputView(Stage stage) {
		super(stage);
		getTextField(0).setMessageText("Op");
		getTextField(0).setMaxLength(3);
		getTable().add(getTextField(0)).width(40).space(5);
		getTextField(1).setMessageText("Variable");
		getTextField(1).setMaxLength(15);
		getTable().add(getTextField(1)).width(70).space(5);
		getTextField(2).setMessageText("Message");
		getTextField(2).setMaxLength(50);
		getTable().add(getTextField(2)).width(100).space(5);
		getInnerRectangle().setSize((int) (getTable().getPrefWidth() / 10) * 10 + 10, (int) (getTable().getPrefHeight() / 10) * 10 + 20);
		getContainer().setSize(getInnerRectangle().getWidth() + 80, getInnerRectangle().getHeight());
	}
	@Override
	void draw(ShapeRenderer shapeRenderer) {
		setColor(shapeRenderer);
		shapeRenderer.line(getX(), getY(), getInnerRectangle().getX() + getInnerRectangle().getWidth(), getY());
		shapeRenderer.line(getInnerRectangle().getX(), getY() + getHeight(), getX() + getWidth(), getY() + getHeight());
		shapeRenderer.line(getX(), getY(), getInnerRectangle().getX(), getY() + getHeight());
		shapeRenderer.line(getX() + getWidth(), getY() + getHeight(), getInnerRectangle().getX() + getInnerRectangle().getWidth(), getY());
	}
	@Override
	public VertexType getVertexType() {
		return VertexType.IO;
	}
}