package com.ben.logicflow.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;
import com.ben.logicflow.flowchart.FlowchartController;
import com.ben.logicflow.flowchart.FlowchartController.ControllerState;
import com.ben.logicflow.flowchart.VertexType;
import com.ben.logicflow.flowchart.view.ExecutableView;
import com.ben.logicflow.flowchart.view.VertexView;

final class FlowchartStateListeners {
	private FlowchartStateListeners() {
	}
	final static class EditListener extends ClickListener {
		private final FlowchartState state;
		private final FlowchartController controller;
		private final VertexView listeningVertex;
		//Stores whether a vertex is being clicked on with the left or right mouse button. altEdit is true for left-clicks.
		private final boolean altEdit;
		EditListener(FlowchartState state, FlowchartController controller, VertexView listeningVertex, int button) {
			super(button);
			this.state = state;
			this.controller = controller;
			this.listeningVertex = listeningVertex;
			altEdit = button == Buttons.LEFT;
		}
		//Called when the mouse begins hovering over the listening vertex.
		@Override
		public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
			super.enter(event, x, y, pointer, fromActor);
			listeningVertex.setHighlighted(true);
			if (controller.getState() == ControllerState.AVAILABLE) {
				if (listeningVertex.getVertexType() == VertexType.START) {
					state.setInformationText("Tip: click on the start symbol to edit the flowchart.");
				} else {
					state.setInformationText("Tip: drag a symbol to move it, right-click a symbol to edit the flowchart.");
				}
			}
		}
		@Override
		public void clicked(InputEvent event, float x, float y) {
			switch (controller.getState()) {
				case AVAILABLE:
					//If any symbol is right-clicked or a start symbol is left-clicked.
					if (!altEdit || listeningVertex.getVertexType() == VertexType.START) {
						state.showEditDialog(listeningVertex);
					}
					break;
				case CONNECT:
					controller.connectVertices(state.getCurrentVertex(), listeningVertex, state.isSetTrueBranch());
					state.setInformationText("");
					state.showMenuBarSelectBoxes();
					break;
			}
		}
		//Called when the mouse stops hovering over the listening vertex.
		@Override
		public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
			super.exit(event, x, y, pointer, toActor);
			listeningVertex.setHighlighted(false);
		}
	}
	final static class MoveListener extends DragListener {
		private final FlowchartController controller;
		private final VertexView listeningVertex;
		//These variables store where the mouse begins dragging the vertex relative to its position.
		private float initialRelativeX;
		private float initialRelativeY;
		MoveListener(FlowchartController controller, VertexView listeningVertex) {
			this.controller = controller;
			this.listeningVertex = listeningVertex;
			setTapSquareSize(0);
		}
		@Override
		public void dragStart(InputEvent event, float x, float y, int pointer) {
			if (controller.getState() == ControllerState.AVAILABLE) {
				initialRelativeX = x;
				initialRelativeY = y;
				controller.setState(ControllerState.DRAG);
			}
		}
		@Override
		public void drag(InputEvent event, float x, float y, int pointer) {
			if (controller.getState() == ControllerState.DRAG) {
				if (!controller.getView().zoomedOut()) {
					/*
					 * Set the vertex's position to the mouse's position minus the point it was initially dragged from so it doesn't 'jump'
					 * as the user drags it. Take into account that positions are relative to the bottom-left corners of objects, how much
					 * the user has panned the camera when positioning the vertex and that the value Gdx.input.getY() returns is y-down.
					 * The new position is divided by five, casted to an integer and then multiplied by 5 so its value is to the nearest 5
					 * pixels, effectively snapping the vertex to a grid.
					 */
					listeningVertex.setPosition((int) ((Gdx.input.getX() - initialRelativeX + (listeningVertex.getWidth() / 2) + controller.getView().getViewOffsetX()) / 5) * 5, (int) ((-Gdx.input.getY() - initialRelativeY + (listeningVertex.getHeight() / 2) + Gdx.graphics.getHeight() + controller.getView().getViewOffsetY()) / 5) * 5);
				} else {
					//The same idea as before but with the view being zoomed out taken in account and some trial and error.
					listeningVertex.setPosition((int) (((Gdx.input.getX() * 2) - initialRelativeX + (listeningVertex.getWidth() / 2) - (Gdx.graphics.getWidth() / 2) + controller.getView().getViewOffsetX()) / 5) * 5, (int) (((-Gdx.input.getY() * 2) - initialRelativeY + (listeningVertex.getHeight() / 2) + (Gdx.graphics.getHeight() * 1.5) + controller.getView().getViewOffsetY()) / 5) * 5);
				}
				//Sync the model with the view.
				controller.vertexDragged(listeningVertex);
			}
		}
		@Override
		public void dragStop(InputEvent event, float x, float y, int pointer) {
			if (controller.getState() == ControllerState.DRAG) {
				controller.setState(ControllerState.AVAILABLE);
			}
		}
	}
	final static class TextFieldStateListener implements TextFieldListener {
		private final FlowchartController controller;
		private final ExecutableView listeningVertex;
		//Stores which text field in the vertex is being updated.
		private final int id;
		TextFieldStateListener(FlowchartController controller, ExecutableView listeningVertex, int id) {
			this.controller = controller;
			this.listeningVertex = listeningVertex;
			this.id = id;
		}
		@Override
		public void keyTyped(TextField textField, char c) {
			//Sync the model with the view.
			controller.keyTyped(listeningVertex, id);
		}
	}
	final static class TextFieldHoverListener extends InputListener {
		private final FlowchartState state;
		private final FlowchartController controller;
		private final ExecutableView listeningVertex;
		TextFieldHoverListener(FlowchartState state, FlowchartController controller, ExecutableView listeningVertex) {
			this.state = state;
			this.controller = controller;
			this.listeningVertex = listeningVertex;
		}
		@Override
		public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
			if (controller.getState() == ControllerState.AVAILABLE) {
				final String message = ((TextField) event.getTarget()).getMessageText();
				switch (message) {
					case "Variable":
						String action = "";
						switch (listeningVertex.getVertexType()) {
							case PROCESS:
								action = "assign";
								break;
							case DECISION:
								action = "compare";
								break;
							case IO:
								action = "input/output";
								break;
						}
						state.setInformationText("Tip: type in a valid variable name such as 'cash' to " + action + " its value.");
						break;
					case "Op":
						switch (listeningVertex.getVertexType()) {
							case PROCESS:
								state.setInformationText("Tip: op is short for operator, valid operators are '<-', '+=', '-=', '*=' and '/='.");
								break;
							case DECISION:
								state.setInformationText("Tip: op is short for operator, valid operators are '=', '!=', '<>', '>', '<', '>=' and '<='.");
								break;
							case IO:
								state.setInformationText("Tip: op is short for operator, valid operators are 'IN' (input) and 'OUT' (output).");
								break;
						}
						break;
					case "Value":
						String usage = "";
						switch (listeningVertex.getVertexType()) {
							case PROCESS:
								usage = "assigned";
								break;
							case DECISION:
								usage = "compared";
								break;
						}
						state.setInformationText("Tip: the value " + usage + " can be a number or data from an existing variable.");
						break;
					case "Message":
						state.setInformationText("Tip: this is the message the input/output dialog shows.");
						break;
				}
			}
		}
	}
}