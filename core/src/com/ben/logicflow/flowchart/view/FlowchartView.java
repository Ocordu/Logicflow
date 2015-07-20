package com.ben.logicflow.flowchart.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.ben.logicflow.flowchart.VertexType;

import java.util.ArrayList;

public final class FlowchartView {
	private final Stage stage;
	private final ArrayList<VertexView> vertices = new ArrayList<>();
	private final ShapeRenderer shapeRenderer;
	/*
	 * originalCameraPosition stores the last position the camera was in when the user moved it. The program moves the camera during
	 * execution, the value of originalCameraPosition is used to position it back to where it was pre-execution.
	 */
	private final Vector2 originalCameraPosition = new Vector2();
	//These two variables store how far the camera is from its initial position when the flowchart was first shown.
	private int viewOffsetX;
	private int viewOffsetY;
	private boolean visible;
	public FlowchartView(final Stage stage, ShapeRenderer shapeRenderer) {
		this.stage = stage;
		this.shapeRenderer = shapeRenderer;
		originalCameraPosition.set(new Vector2(stage.getCamera().position.x, stage.getCamera().position.y));
	}
	public VertexView addVertex(VertexType vertexType, Vector2 position, boolean loading) {
		VertexView newVertex = null;
		switch (vertexType) {
			case START:
				newVertex = new StartView(stage);
				break;
			case PROCESS:
				newVertex = new ProcessView(stage);
				break;
			case DECISION:
				newVertex = new DecisionView(stage);
				break;
			case IO:
				newVertex = new InputOutputView(stage);
		}
		if (loading && vertexType != VertexType.START) {
			newVertex.setPosition(position.x + (newVertex.getWidth() / 2), position.y + (newVertex.getHeight() / 2));
		} else {
			newVertex.setPosition(position.x, position.y);
		}
		vertices.add(newVertex);
		return newVertex;
	}
	public void beginEdges() {
		shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
	}
	public void drawEdge(Vector2 startPoint, Vector2 endPoint, Vector2 startMidPoint, Vector2 endMidPoint, Color color) {
		//The minimum distance the edge must travel perpendicularly to/from the vertex it's connected to.
		final int minimumDistance = 20;
		//The points that satisfy the minimum distance requirement.
		final Vector2 newStartPoint = new Vector2();
		final Vector2 newEndPoint = new Vector2();
		/*
		 * Determine where the edge is drawn from relative to the vertex. If the first condition is satisfied, the edge must originate from
		 * the bottom of the vertex. This is because edges are only drawn from the horizontal center of a vertex when they're drawn from
		 * the bottom of the vertex. The other two condition apply to decision symbols.
		 */
		if (startMidPoint.x == startPoint.x) {
			newStartPoint.set(startPoint.x, startPoint.y - minimumDistance);
		} else if (startMidPoint.x - startPoint.x > 0) {
			newStartPoint.set(startPoint.x - minimumDistance, startPoint.y);
		} else {
			newStartPoint.set(startPoint.x + minimumDistance, startPoint.y);
		}
		//Same concept as before.
		if (endMidPoint.x == endPoint.x) {
			newEndPoint.set(endPoint.x, endPoint.y + minimumDistance);
		} else if (endMidPoint.x - endPoint.x > 0) {
			newStartPoint.set(endPoint.x - minimumDistance, endPoint.y);
		} else {
			newStartPoint.set(endPoint.x + minimumDistance, endPoint.y);
		}
		shapeRenderer.setColor(color);
		shapeRenderer.line(startPoint, newStartPoint);
		if (startPoint.x == startMidPoint.x || endPoint.y - startPoint.y + minimumDistance <= 0 && ((startPoint.x - startMidPoint.x > 0 && endPoint.x - newStartPoint.x >= 0) || (startPoint.x - startMidPoint.x < 0 && endPoint.x - newStartPoint.x <= 0))) {
			if (startPoint.x - startMidPoint.x == 0 && ((startPoint.y - endPoint.y < minimumDistance * 2 && startPoint.x - startMidPoint.x == 0) || (startPoint.y - endPoint.y < minimumDistance && startPoint.x - startMidPoint.x != 0))) {
				shapeRenderer.line(newStartPoint.x, newStartPoint.y, (startPoint.x + endPoint.x) / 2, newStartPoint.y);
				shapeRenderer.line((startPoint.x + endPoint.x) / 2, newStartPoint.y, (startPoint.x + endPoint.x) / 2, newEndPoint.y);
				shapeRenderer.line((startPoint.x + endPoint.x) / 2, newEndPoint.y, endPoint.x, newEndPoint.y);
				shapeRenderer.line(endPoint.x, newEndPoint.y, endPoint.x, endPoint.y + 8);
			} else {
				shapeRenderer.line(newStartPoint.x, newStartPoint.y, newEndPoint.x, newStartPoint.y);
				shapeRenderer.line(newEndPoint.x, newStartPoint.y, endPoint.x, endPoint.y + 8);
			}
		} else {
			shapeRenderer.line(newStartPoint.x, newStartPoint.y, newStartPoint.x, newEndPoint.y);
			shapeRenderer.line(newStartPoint.x, newEndPoint.y, newEndPoint.x, newEndPoint.y);
			shapeRenderer.line(newEndPoint.x, newEndPoint.y, endPoint.x, endPoint.y + 8);
		}
		shapeRenderer.triangle(endPoint.x, endPoint.y, endPoint.x - 4, endPoint.y + 8, endPoint.x + 4, endPoint.y + 8);
	}
	public void drawVertices() {
		for (VertexView vertex : vertices) {
			vertex.draw(shapeRenderer);
		}
		shapeRenderer.end();
	}
	public void removeVertex(VertexView removedVertex) {
		vertices.remove(removedVertex);
	}
	public void moveCamera(Array<Direction> directions) {
		int amount = 20;
		if (zoomedOut()) {
			amount *= 2;
		}
		for (Direction direction : directions) {
			switch (direction) {
				case UP:
					stage.getCamera().translate(0, amount, 0);
					viewOffsetY += amount;
					break;
				case DOWN:
					stage.getCamera().translate(0, -amount, 0);
					viewOffsetY -= amount;
					break;
				case LEFT:
					stage.getCamera().translate(-amount, 0, 0);
					viewOffsetX -= amount;
					break;
				case RIGHT:
					stage.getCamera().translate(amount, 0, 0);
					viewOffsetX += amount;
					break;
			}
		}
		stage.getCamera().update();
		originalCameraPosition.set(stage.getCamera().position.x, stage.getCamera().position.y);
	}
	public void moveCamera(Direction direction) {
		int amount = 120;
		if (zoomedOut()) {
			amount *= 2;
		}
		switch (direction) {
			case UP:
				stage.getCamera().translate(0, amount, 0);
				viewOffsetY += amount;
				break;
			case DOWN:
				stage.getCamera().translate(0, -amount, 0);
				viewOffsetY -= amount;
				break;
			case LEFT:
				stage.getCamera().translate(-amount, 0, 0);
				viewOffsetX -= amount;
				break;
			case RIGHT:
				stage.getCamera().translate(amount, 0, 0);
				viewOffsetX += amount;
		}
		stage.getCamera().update();
		originalCameraPosition.set(stage.getCamera().position.x, stage.getCamera().position.y);
	}
	public void positionCamera(float x, float y) {
		stage.getCamera().position.set(x, y, stage.getCamera().position.z);
		stage.getCamera().update();
	}
	public void positionCamera() {
		stage.getCamera().position.set(originalCameraPosition.x, originalCameraPosition.y, stage.getCamera().position.z);
		stage.getCamera().update();
	}
	public void zoom(boolean out) {
		if (out) {
			stage.getCamera().viewportWidth = Gdx.graphics.getWidth() * 2;
			stage.getCamera().viewportHeight = Gdx.graphics.getHeight() * 2;
		} else {
			stage.getCamera().viewportWidth = Gdx.graphics.getWidth();
			stage.getCamera().viewportHeight = Gdx.graphics.getHeight();
		}
		stage.getCamera().update();
	}
	public boolean zoomedOut() {
		return stage.getCamera().viewportWidth > Gdx.graphics.getWidth();
	}
	public void reset() {
		stage.getCamera().viewportWidth = Gdx.graphics.getWidth();
		stage.getCamera().viewportHeight = Gdx.graphics.getHeight();
		stage.getCamera().position.set(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2, stage.getCamera().position.z);
		originalCameraPosition.set(stage.getCamera().position.x, stage.getCamera().position.y);
		viewOffsetX = 0;
		viewOffsetY = 0;
		stage.getCamera().update();
	}
	public void disableVertexUI(boolean disableHighlighting) {
		for (VertexView vertex : vertices) {
			vertex.disableUI(disableHighlighting);
		}
		stage.setKeyboardFocus(null);
	}
	public void enableVertexUI() {
		for (VertexView vertex : vertices) {
			vertex.enableUI();
		}
	}
	public float getViewOffsetX() {
		return viewOffsetX;
	}
	public float getViewOffsetY() {
		return viewOffsetY;
	}
	public boolean isVisible() {
		return visible;
	}
	public void setVisible(boolean visible) {
		for (VertexView vertex : vertices) {
			vertex.setVisible(visible);
		}
		this.visible = visible;
	}
	public enum Direction {
		UP, DOWN, LEFT, RIGHT
	}
}