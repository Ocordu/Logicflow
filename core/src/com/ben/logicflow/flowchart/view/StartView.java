package com.ben.logicflow.flowchart.view;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.ben.logicflow.Assets;
import com.ben.logicflow.flowchart.VertexType;

public final class StartView extends VertexView {
	private final Label startLabel = new Label("Start", Assets.getSkin());
	public StartView(Stage stage) {
		super(stage);
		stage.addActor(startLabel);
		//Ensure the vertex's size is a multiple of 10 so it correctly snaps to the grid.
		getInnerRectangle().setSize((int) (startLabel.getPrefWidth() / 10) * 10 + 110, (int) (startLabel.getPrefHeight() / 10) * 10 + 30);
		getContainer().setSize(getInnerRectangle().getWidth() + getInnerRectangle().getHeight(), getInnerRectangle().getHeight());
	}
	@Override
	protected void draw(ShapeRenderer shapeRenderer) {
		setColor(shapeRenderer);
		//Draw the bottom horizontal line of the start symbol.
		shapeRenderer.line(getInnerRectangle().getX(), getInnerRectangle().getY(), getInnerRectangle().getX() + getInnerRectangle().getWidth(), getInnerRectangle().getY());
		//Draw the top horizontal line of the start symbol.
		shapeRenderer.line(getInnerRectangle().getX(), getInnerRectangle().getY() + getHeight(), getInnerRectangle().getX() + getInnerRectangle().getWidth(), getInnerRectangle().getY() + getHeight());
		//Draw a bezier curve that looks like a semicircle at the left side of the start symbol connected to the horizontal lines.
		shapeRenderer.curve(getInnerRectangle().getX(), getY() + getHeight(), getX() - 8, getY() + getHeight(), getX() - 8, getY(), getInnerRectangle().getX(), getY(), 100);
		//Draw a bezier curve that looks like a semicircle at the right side of the start symbol connected to the horizontal lines.
		shapeRenderer.curve(getInnerRectangle().getX() + getInnerRectangle().getWidth(), getY() + getHeight(), getX() + getWidth() + 8, getY() + getHeight(), getX() + getWidth() + 8, getY(), getInnerRectangle().getX() + getInnerRectangle().getWidth(), getY(), 100);
	}
	@Override
	public void addListener(EventListener listener) {
		super.addListener(listener);
		startLabel.addListener(listener);
	}
	public void invertLabelColour() {
		if (startLabel.getColor().toFloatBits() == Color.toFloatBits(1, 1, 1, 1)) {
			startLabel.setColor(0, 0, 0, 1);
		} else {
			startLabel.setColor(1, 1, 1, 1);
		}
	}
	@Override
	public void dispose() {
		super.dispose();
		startLabel.clear();
		startLabel.remove();
	}
	@Override
	public VertexType getVertexType() {
		return VertexType.START;
	}
	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		startLabel.setVisible(visible);
	}
	@Override
	public void setPosition(float x, float y) {
		super.setPosition(x, y);
		startLabel.setPosition(getX() + (getWidth() / 2) - (startLabel.getWidth() / 2), getY() + (getHeight() / 2) - (startLabel.getHeight() / 2));
	}
}