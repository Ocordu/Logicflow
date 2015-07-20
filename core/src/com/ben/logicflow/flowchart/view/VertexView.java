package com.ben.logicflow.flowchart.view;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.ben.logicflow.flowchart.VertexType;

public abstract class VertexView {
	//An invisible UI element that will listen for a vertex's mouse events.
	private final Widget container = new Widget();
	//Next four variables are related to drawing. The first one is for drawing a vertex and the next three are for drawing its edges.
	private final Rectangle innerRectangle = new Rectangle();
	private final Vector2 inPoint = new Vector2();
	private final Vector2 outPoint = new Vector2();
	private final Vector2 midPoint = new Vector2();
	//debug = true when a vertex is currently being executed during step through.
	private boolean debug;
	private boolean highlighted;
	private boolean highlightingDisabled;
	VertexView(Stage stage) {
		stage.addActor(container);
	}
	void draw(ShapeRenderer shapeRenderer) {
		setColor(shapeRenderer);
		shapeRenderer.rect(getX(), getY(), getWidth(), getHeight());
	}
	void setColor(ShapeRenderer shapeRenderer) {
		if (debug || (highlighted && !highlightingDisabled)) {
			shapeRenderer.setColor(0.75f, 0.75f, 0.75f, 1);
		} else {
			shapeRenderer.setColor(0.5f, 0.5f, 0.5f, 1);
		}
	}
	void disableUI(boolean disableHighlighting) {
		highlightingDisabled = disableHighlighting;
	}
	void enableUI() {
		highlightingDisabled = false;
	}
	public void addListener(EventListener listener) {
		container.addListener(listener);
	}
	public void dispose() {
		container.clear();
		container.remove();
	}
	public abstract VertexType getVertexType();
	Widget getContainer() {
		return container;
	}
	public float getWidth() {
		return container.getWidth();
	}
	public float getHeight() {
		return container.getHeight();
	}
	public float getX() {
		return container.getX();
	}
	public float getY() {
		return container.getY();
	}
	Rectangle getInnerRectangle() {
		return innerRectangle;
	}
	public Vector2 getInPoint() {
		return inPoint;
	}
	public Vector2 getOutPoint() {
		return outPoint;
	}
	public Vector2 getMidPoint() {
		return midPoint;
	}
	//Set a vertex's position relative to its center for convenience and update the rest of its data.
	public void setPosition(float x, float y) {
		container.setPosition(x - (getWidth() / 2), y - (getHeight() / 2));
		innerRectangle.setPosition(getX() + (getWidth() / 2) - (innerRectangle.getWidth() / 2), getY() + (getHeight() / 2) - (innerRectangle.getHeight() / 2));
		inPoint.set(getX() + (getWidth() / 2), getY() + getHeight());
		outPoint.set(getX() + (getWidth() / 2), getY());
		midPoint.set(getX() + (getWidth() / 2), getY() + (getHeight() / 2));
	}
	void setVisible(boolean visible) {
		container.setVisible(visible);
	}
	public void setDebug(boolean debug) {
		this.debug = debug;
	}
	public void setHighlighted(boolean highlighted) {
		this.highlighted = highlighted;
	}
}