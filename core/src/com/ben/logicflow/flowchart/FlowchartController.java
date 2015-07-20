package com.ben.logicflow.flowchart;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.ben.logicflow.flowchart.model.ExecutableModel;
import com.ben.logicflow.flowchart.model.FlowchartModel;
import com.ben.logicflow.flowchart.model.VertexModel;
import com.ben.logicflow.flowchart.view.DecisionView;
import com.ben.logicflow.flowchart.view.ExecutableView;
import com.ben.logicflow.flowchart.view.FlowchartView;
import com.ben.logicflow.flowchart.view.VertexView;
import com.ben.logicflow.states.FlowchartState;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public abstract class FlowchartController {
	private final FlowchartModel model = new FlowchartModel();
	private final FlowchartView view;
	private final FlowchartState state;
	//HashMaps make it easy to get a vertex's view from its corresponding model and vice versa.
	private final HashMap<VertexModel, VertexView> vertexModelToView = new HashMap<>();
	private final HashMap<VertexView, VertexModel> vertexViewToModel = new HashMap<>();
	private ControllerState subState = ControllerState.AVAILABLE;
	private final float runDelay = 0.05f;
	/*
	 * waitStopwatch holds the how long the program has been paused during automatic execution between symbols. This is to facilitate the
	 * use of a short delay between symbols so execution doesn't happen instantly.
	 */
	private float waitStopwatch = runDelay;
	/*
	 * nextVertexView points to the vertex that will be executed after waitStopwatch reaches a certain value or when the user steps to the
	 * next symbol.
	 */
	private VertexView nextVertexView;
	FlowchartController(FlowchartState state, Stage stage, ShapeRenderer shapeRenderer) {
		this.state = state;
		view = new FlowchartView(stage, shapeRenderer);
	}
	public void addStartVertex() {
		addVertex(VertexType.START, null, true, Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() - 100);
	}
	public void update(float delta) {
		if (waitStopwatch < runDelay) {
			waitStopwatch += delta;
			if (waitStopwatch >= runDelay) {
				postWait();
			}
		}
	}
	abstract void postWait();
	VertexModel toModel(VertexView vertexView) {
		return vertexViewToModel.get(vertexView);
	}
	VertexView toView(VertexModel vertexModel) {
		return vertexModelToView.get(vertexModel);
	}
	public void drawFlowchart(VertexView selectedVertexView, boolean blackEdges) {
		final ArrayList<VertexView> visitedVertexViews = new ArrayList<>();
		view.beginEdges();
		if (blackEdges) {
			recursivelyDrawEdges(visitedVertexViews, selectedVertexView, toView(model.getStartVertex()), toView(model.getStartVertex().getNextVertex()), null, new Color(Color.BLACK), true);
		} else {
			recursivelyDrawEdges(visitedVertexViews, selectedVertexView, toView(model.getStartVertex()), toView(model.getStartVertex().getNextVertex()), null, new Color(Color.WHITE), false);
		}
		view.drawVertices();
	}
	//A variation of depth-first search that always explores an entire tree rather than look for a specific node.
	private void recursivelyDrawEdges(ArrayList<VertexView> visitedVertexViews, VertexView selectedVertexView, VertexView startVertexView, VertexView endVertexView, Vector2 startPoint, Color color, boolean blackEdges) {
		if (startPoint == null) {
			startPoint = startVertexView.getOutPoint();
		}
		//If edge editing is enabled, the current vertex is the one with its edge being edited and its edited branch is the one being drawn.
		if (subState == ControllerState.CONNECT && selectedVertexView == startVertexView && ((state.isSetTrueBranch() && !color.equals(Color.RED)) || (!state.isSetTrueBranch() && color.equals(Color.RED)))) {
			//Draw the current edge to the mouse taking into account the zoom level, how much the camera has been panned and other factors.
			if (view.zoomedOut()) {
				view.drawEdge(startPoint, new Vector2((Gdx.input.getX() * 2) - (Gdx.graphics.getWidth() / 2) + view.getViewOffsetX(), (Gdx.graphics.getHeight() * 1.5f) - (Gdx.input.getY() * 2) + view.getViewOffsetY()), startVertexView.getMidPoint(), new Vector2((Gdx.input.getX() * 2) - (Gdx.graphics.getWidth() / 2) + view.getViewOffsetX(), (Gdx.graphics.getHeight() * 1.5f) - (Gdx.input.getY() * 2) + view.getViewOffsetY()), color);
			} else {
				view.drawEdge(startPoint, new Vector2(Gdx.input.getX() + view.getViewOffsetX(), Gdx.graphics.getHeight() - Gdx.input.getY() + view.getViewOffsetY()), startVertexView.getMidPoint(), new Vector2(Gdx.input.getX() + view.getViewOffsetX(), Gdx.graphics.getHeight() - Gdx.input.getY() + view.getViewOffsetY()), color);
			}
		}
		/*
		 * As flowcharts will always contain one or more loops, a list of visited vertices has to be maintained to prevent a stack overflow
		 * error from occurring.
		 */
		visitedVertexViews.add(startVertexView);
		//Prevent edges from being drawn to the start vertex otherwise the main loop will be visible.
		if (endVertexView.getVertexType() != VertexType.START) {
			//If the edge isn't being edited.
			if (subState != ControllerState.CONNECT || selectedVertexView != startVertexView || !((state.isSetTrueBranch() && !color.equals(Color.RED)) || (!state.isSetTrueBranch() && color.equals(Color.RED)))) {
				view.drawEdge(startPoint, endVertexView.getInPoint(), startVertexView.getMidPoint(), endVertexView.getMidPoint(), color);
			}
			/*
			 * When the condition returns true the branch stops being explored. This happens when the remaining section of the branch has
			 * already been visited via other branches.
			 */
			if (!visitedVertexViews.contains(endVertexView)) {
				if (endVertexView.getVertexType() == VertexType.DECISION) {
					//Render a decision symbol's entire true branch before rendering its false branch.
					recursivelyDrawEdges(visitedVertexViews, selectedVertexView, endVertexView, toView(toModel(endVertexView).getNextVertex()), null, new Color(Color.GREEN), blackEdges);
					recursivelyDrawEdges(visitedVertexViews, selectedVertexView, endVertexView, toView(toModel(endVertexView).getNextFalseVertex()), ((DecisionView) endVertexView).getFalseOutPoint(), new Color(Color.RED), blackEdges);
				} else if (blackEdges) {
					recursivelyDrawEdges(visitedVertexViews, selectedVertexView, endVertexView, toView(toModel(endVertexView).getNextVertex()), null, new Color(Color.BLACK), true);
				} else {
					recursivelyDrawEdges(visitedVertexViews, selectedVertexView, endVertexView, toView(toModel(endVertexView).getNextVertex()), null, new Color(Color.WHITE), false);
				}
			}
		}
	}
	/*
	 * Methods split up as a new vertex might be added during deserialization so it doesn't need new data. The first method is for cases
	 * where an entirely new vertex is created and is more commonly used.
	 */
	public void addVertex(VertexType vertexType, VertexView previousVertexView, boolean branch, float x, float y) {
		VertexModel newVertexModel = model.addVertex(vertexType, toModel(previousVertexView), branch);
		newVertexModel.setPosition(x + view.getViewOffsetX(), y + view.getViewOffsetY());
		addVertex(newVertexModel, false);
		/*
		 * Ensure all vertices at the end of every branch point to the start vertex, creating one massive loop which I refer to as the main
		 * loop. This is used in Tarjan validation.
		 */
		model.updateMainLoop(new ArrayList<>(getVertexModels()));
	}
	void addVertex(VertexModel newVertexModel, boolean loaded) {
		VertexView newVertexView = view.addVertex(newVertexModel.getVertexType(), newVertexModel.getPosition(), loaded);
		state.addVertexListeners(newVertexView);
		vertexViewToModel.put(newVertexView, newVertexModel);
		vertexModelToView.put(newVertexModel, newVertexView);
	}
	public void vertexDragged(VertexView vertexView) {
		toModel(vertexView).setPosition(vertexView.getX(), vertexView.getY());
	}
	public void keyTyped(ExecutableView vertexView, int id) {
		((ExecutableModel) toModel(vertexView)).setData(id, vertexView.getTextField(id).getText());
	}
	public void removeVertex(VertexView removedVertexView, boolean branch) {
		/*
		 * model.removeVertex() returns a list of vertices that need to be disposed. These are most of the outcast/hanging vertices that
		 * are removed from the model.
		 */
		disposeRemovedVertices(model.removeVertex(toModel(removedVertexView), branch));
		//Then dispose the vertex that was removed in the first place.
		disposeVertex(removedVertexView, true);
		//Find any remaining outcast/hanging vertices and remove them from the model.
		disposeRemovedVertices(model.findOutcasts(new ArrayList<>(getVertexModels())));
		model.updateMainLoop(new ArrayList<>(getVertexModels()));
		subState = ControllerState.AVAILABLE;
	}
	private void disposeVertex(VertexView vertexView, boolean clearHashMaps) {
		view.removeVertex(vertexView);
		if (clearHashMaps) {
			vertexModelToView.remove(toModel(vertexView));
			vertexViewToModel.remove(vertexView);
		}
		vertexView.dispose();
	}
	private void disposeRemovedVertices(ArrayList<VertexModel> removedVertexModels) {
		for (VertexModel vertexModel : removedVertexModels) {
			disposeVertex(toView(vertexModel), true);
		}
	}
	public void disposeAllVertices() {
		for (VertexView currentVertexView : vertexModelToView.values()) {
			//Don't modify the HashMaps until they've been completely read to prevent ConcurrentModificationException from being thrown.
			disposeVertex(currentVertexView, false);
		}
		vertexViewToModel.clear();
		vertexModelToView.clear();
		getModel().removeStartVertex();
	}
	//If true only the start vertex exists.
	public boolean isFlowchartEmpty() {
		return vertexViewToModel.size() == 1;
	}
	public void connectVertices(VertexView firstVertexView, VertexView secondVertexView, boolean branch) {
		//If the second vertex isn't a start vertex or it is a start vertex but it's being connected to itself.
		if (secondVertexView.getVertexType() != VertexType.START || secondVertexView == firstVertexView) {
			/*
			 * Connecting a vertex to itself removes the connection between the first vertex and the second vertex. So second vertex is set
			 * to null to connect the first vertex to nothing (it will later connect to the start vertex for the main loop).
			 */
			if (secondVertexView == firstVertexView) {
				secondVertexView = null;
			}
			//Dispose all outcast vertices as a result of the connection.
			if (secondVertexView != null) {
				disposeRemovedVertices(model.connectVertices(toModel(firstVertexView), toModel(secondVertexView), branch));
			} else {
				disposeRemovedVertices(model.connectVertices(toModel(firstVertexView), null, branch));
			}
			disposeRemovedVertices(model.findOutcasts(new ArrayList<>(getVertexModels())));
			model.updateMainLoop(new ArrayList<>(getVertexModels()));
			subState = ControllerState.AVAILABLE;
		} else {
			state.showFlowchartMessage("Error", "Symbols can't be connected to the start symbol.");
		}
		//Allow the user to edit symbols again.
		view.enableVertexUI();
	}
	public void stopExecution() {
		setWaiting(false);
		//Position the camera back to where it was before execution.
		getView().positionCamera();
		getView().enableVertexUI();
		state.showMenuBarBackgroundImage();
		state.showMenuBarSelectBoxes();
		state.setInformationText("");
		setState(ControllerState.AVAILABLE);
	}
	public ControllerState getState() {
		return subState;
	}
	public void setState(ControllerState subState) {
		this.subState = subState;
	}
	FlowchartModel getModel() {
		return model;
	}
	public FlowchartView getView() {
		return view;
	}
	public double getVariable(String variable) throws FlowchartModel.VariableNameException, FlowchartModel.VariableValueException {
		return model.getVariable(variable, false);
	}
	Collection<VertexModel> getVertexModels() {
		return vertexViewToModel.values();
	}
	VertexView getNextVertexView() {
		return nextVertexView;
	}
	void setNextVertexView(VertexView nextVertexView) {
		this.nextVertexView = nextVertexView;
	}
	public void setVariable(String variable, double value) throws FlowchartModel.VariableNameException, FlowchartModel.VariableValueException {
		model.setVariable(variable, value);
	}
	void setWaiting(boolean waiting) {
		if (waiting) {
			waitStopwatch = 0;
		} else  {
			waitStopwatch = runDelay;
		}
	}
	public void setVisible(boolean visible) {
		view.setVisible(visible);
	}
	public enum ControllerState {
		AVAILABLE, DIALOG, DRAG, CONNECT, EXECUTE
	}
}