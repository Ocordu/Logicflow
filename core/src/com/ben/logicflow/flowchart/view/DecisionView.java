package com.ben.logicflow.flowchart.view;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.ben.logicflow.flowchart.VertexType;

public final class DecisionView extends ExecutableView {
	private final Vector2 falseOutPoint = new Vector2();
	public DecisionView(Stage stage) {
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
		getInnerRectangle().setSize((int) (getTable().getPrefWidth() / 10) * 10 + 10, (int) (getTable().getPrefHeight() / 10) * 10 + 10);
		getContainer().setSize(getInnerRectangle().getWidth() + 100, getInnerRectangle().getHeight() + 80);
	}
	@Override
	void draw(ShapeRenderer shapeRenderer) {
		setColor(shapeRenderer);
		shapeRenderer.line(getX(), getY() + (getHeight() / 2), getX() + (getWidth() / 2), getY() + getHeight());
		shapeRenderer.line(getX() + (getWidth() / 2), getY() + getHeight(), getX() + getWidth(), getY() + (getHeight() / 2));
		shapeRenderer.line(getX() + getWidth(), getY() + (getHeight() / 2), getX() + (getWidth() / 2), getY());
		shapeRenderer.line(getX() + (getWidth() / 2), getY(), getX(), getY() + (getHeight() / 2));
	}
	@Override
	public VertexType getVertexType() {
		return VertexType.DECISION;
	}
	public Vector2 getFalseOutPoint() {
		return falseOutPoint;
	}
	@Override
	public void setPosition(float x, float y) {
		getContainer().setPosition(x - (getWidth() / 2), y - (getHeight() / 2));
		getInnerRectangle().setPosition(getX() + (getWidth() / 2) - (getInnerRectangle().getWidth() / 2), getY() + (getHeight() / 2) - (getInnerRectangle().getHeight() / 2));
		getInPoint().set(getX() + (getWidth() / 2), getY() + getHeight());
		getOutPoint().set(getX() + getWidth() - 1, getY() + (getHeight() / 2));
		getMidPoint().set(getX() + (getWidth() / 2), getY() + (getHeight() / 2));
		getTable().setPosition(getX() + (getWidth() / 2), getY() + (getHeight() / 2));
		falseOutPoint.set(getX() + 1, getY() + (getHeight() / 2));
	}
}