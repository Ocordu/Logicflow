package com.ben.logicflow.flowchart;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.ben.logicflow.flowchart.model.*;
import com.ben.logicflow.flowchart.view.ExecutableView;
import com.ben.logicflow.flowchart.view.StartView;
import com.ben.logicflow.flowchart.view.VertexView;
import com.ben.logicflow.states.FlowchartState;
import com.ben.logicflow.states.PracticeState;

import java.util.ArrayList;

public final class PracticeController extends FlowchartController {
	private final PracticeState state;
	private boolean stepThrough;
	private VertexView currentVertexView;
	public PracticeController(FlowchartState state, Stage stage, ShapeRenderer shapeRenderer) {
		super(state, stage, shapeRenderer);
		this.state = (PracticeState) state;
	}
	@Override
	void postWait() {
		state.setInformationText("");
		execute(getNextVertexView(), false);
	}
	public void stepAndExecute() {
		if (stepThrough) {
			state.setInformationText("");
			//The current vertex is no longer being executed.
			currentVertexView.setDebug(false);
			execute(getNextVertexView(), false);
		}
	}
	public void execute(boolean stepThrough) {
		if (isFlowchartEmpty()) {
			state.showFlowchartMessage("Error", "Empty flowcharts can't be executed.");
		} else if (!getModel().isTarjanValid(new ArrayList<>(getVertexModels()))) {
			state.showFlowchartMessage("Error", "Flowcharts with one or more infinite loop(s) can't be executed.");
		} else {
			setState(ControllerState.EXECUTE);
			getView().disableVertexUI(true);
			//Clear the variables HashMap.
			getModel().resetVariables();
			this.stepThrough = stepThrough;
			state.hideMenuBarSelectBoxes();
			if (!stepThrough) {
				state.setInformationText("");
				state.hideMenuBarBackgroundImage();
			} else {
				state.setInformationText("Tip: press space to continue execution.");
			}
			execute(toView(getModel().getStartVertex()), true);
		}
	}
	public void execute(VertexView vertexView, boolean firstExecution) {
		//If the next vertex is the start vertex and this isn't the first execution, the end of the flowchart must've been reached.
		if (vertexView != toView(getModel().getStartVertex()) || firstExecution) {
			VertexModel currentVertexModel = toModel(vertexView);
			VertexView currentVertexView = toView(currentVertexModel);
			this.currentVertexView = currentVertexView;
			getView().positionCamera(currentVertexView.getX() + (currentVertexView.getWidth() / 2), currentVertexView.getY() + (currentVertexView.getHeight() / 2));
			if (stepThrough) {
				currentVertexView.setDebug(true);
			}
			//If the vertex is an instance of ExecutableModel.
			if (currentVertexModel.getVertexType() != VertexType.START) {
				state.hideMenuBarBackgroundImage();
				try {
					ExecutionData executionData = ((ExecutableModel) currentVertexModel).execute();
					if (executionData != null && executionData.getInformation().equals("OPERATOR")) {
						currentVertexView.setDebug(false);
						state.showFatalExecutionErrorMessage("Invalid symbol operator.");
					} else {
						VertexModel nextVertexModel = null;
						//ignored = true if the symbol has blank mandatory fields.
						boolean ignored = false;
						if (executionData != null) {
							nextVertexModel = executionData.getNextVertex();
							if (executionData.getInformation().equals("IGNORED")) {
								ignored = true;
							} else if (stepThrough) {
								state.showMenuBarBackgroundImage();
								state.setInformationText("Debug information: " + executionData.getInformation());
							}
						}
						if (nextVertexModel != null) {
							if (ignored) {
								state.showExecutionErrorMessage("Blank mandatory field(s), symbol ignored.", toView(nextVertexModel), currentVertexView);
							} else {
								wait(toView(nextVertexModel));
							}
						} else {
							//IO symbols' execution data return a null next vertex in order to pause execution when they're reached.
							final InputOutputModel inputOutputModel = (InputOutputModel) currentVertexModel;
							if (ignored) {
								//getNextVertex() still returns IO symbols' real next vertex.
								state.showExecutionErrorMessage("Blank mandatory field(s), symbol ignored.", toView(inputOutputModel.getNextVertex()), currentVertexView);
							} else {
								currentVertexView.setDebug(false);
								switch (inputOutputModel.getOperationType()) {
									case INPUT:
										state.showInputDialog(inputOutputModel.getMessage(), inputOutputModel.getVariable(), toView(inputOutputModel.getNextVertex()));
										break;
									case OUTPUT:
										state.showOutputDialog(inputOutputModel.getMessage(), inputOutputModel.getVariable(), toView(inputOutputModel.getNextVertex()));
										break;
								}
							}
						}
					}
				} catch (FlowchartModel.VariableNameException exception) {
					currentVertexView.setDebug(false);
					state.showFatalExecutionErrorMessage("Invalid variable identifier.");
				} catch (FlowchartModel.VariableValueException exception) {
					currentVertexView.setDebug(false);
					state.showFatalExecutionErrorMessage("Variable/array element has no associated value.");
				} catch (FlowchartModel.ValueException exception) {
					currentVertexView.setDebug(false);
					state.showFatalExecutionErrorMessage("Values can only be numerical/valid variable identifiers.");
				}
			} else {
				//Start vertex was visited, pause for a short delay rather than execute it.
				wait(toView(currentVertexModel.getNextVertex()));
			}
		} else {
			stopExecution();
		}
	}
	private void wait(VertexView nextVertexView) {
		setNextVertexView(nextVertexView);
		//If the person is stepping through the flowchart, automatic execution is disabled.
		if (!stepThrough) {
			setWaiting(true);
		}
	}
	public void save(String fileName) {
		getModel().save(fileName);
	}
	public boolean load(String fileName) {
		//Store the previous flowchart in case loading fails.
		VertexModel previousStartVertexModel = getModel().getStartVertex();
		disposeAllVertices();
		getView().reset();
		boolean success = getModel().load(fileName, previousStartVertexModel);
		recursivelyDeserializeVertices(getModel().getStartVertex());
		for (VertexModel vertex : getVertexModels()) {
			if (vertex.getVertexType() == VertexType.PROCESS) {
				((ProcessModel) vertex).setFlowchart(getModel());
			} else if (vertex.getVertexType() == VertexType.DECISION) {
				((DecisionModel) vertex).setFlowchart(getModel());
			}
		}
		getModel().updateMainLoop(new ArrayList<>(getVertexModels()));
		setState(ControllerState.AVAILABLE);
		return success;
	}
	private void recursivelyDeserializeVertices(VertexModel vertexModel) {
		if (vertexModel != null && !getVertexModels().contains(vertexModel)) {
			addVertex(vertexModel, true);
			deserializeVertexState(vertexModel);
			recursivelyDeserializeVertices(vertexModel.getNextVertex());
			if (vertexModel.getNextVertex() != null) {
				vertexModel.getNextVertex().addPreviousVertex(vertexModel);
			}
			if (vertexModel.getVertexType() == VertexType.DECISION) {
				recursivelyDeserializeVertices(vertexModel.getNextFalseVertex());
				if (vertexModel.getNextFalseVertex() != null) {
					vertexModel.getNextFalseVertex().addPreviousVertex(vertexModel);
				}
			}
		}
	}
	public void invertLabelColour() {
		((StartView) toView(getModel().getStartVertex())).invertLabelColour();
	}
	private void deserializeVertexState(VertexModel vertexModel) {
		final VertexView vertexView = toView(vertexModel);
		if (vertexModel.getVertexType() != VertexType.START) {
			for (int i = 0; i < 3; i++) {
				if (((ExecutableModel) vertexModel).getData(i) != null) {
					//Display the text the user typed into a flowchart's text fields before the flowchart was saved.
					((ExecutableView) vertexView).getTextField(i).setText(((ExecutableModel) vertexModel).getData(i));
				}
			}
		}
	}
	@Override
	public void stopExecution() {
		super.stopExecution();
		state.setExecutionDialog(false);
		currentVertexView.setDebug(false);
		currentVertexView = null;
	}
}