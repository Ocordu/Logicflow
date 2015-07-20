package com.ben.logicflow.flowchart;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.ben.logicflow.flowchart.model.ExecutableModel;
import com.ben.logicflow.flowchart.model.ExecutionData;
import com.ben.logicflow.flowchart.model.FlowchartModel;
import com.ben.logicflow.flowchart.model.VertexModel;
import com.ben.logicflow.flowchart.view.VertexView;
import com.ben.logicflow.states.FlowchartState;
import com.ben.logicflow.states.quiz.TestCase;
import com.ben.logicflow.states.quiz.TryQuestionState;

import java.util.ArrayList;

public final class TryQuestionController extends FlowchartController {
	private final TryQuestionState state;
	//Stores the index of the current test case being executed.
	private int testCaseIndex;
	public TryQuestionController(FlowchartState state, Stage stage, ShapeRenderer shapeRenderer) {
		super(state, stage, shapeRenderer);
		this.state = (TryQuestionState) state;
	}
	@Override
	void postWait() {
		execute(getNextVertexView(), false);
	}
	public void execute() {
		if (isFlowchartEmpty()) {
			state.showFlowchartMessage("Error", "Empty flowcharts can't be executed.");
		} else if (!getModel().isTarjanValid(new ArrayList<>(getVertexModels()))) {
			state.showFlowchartMessage("Error", "Flowcharts with one or more infinite loop(s) can't be executed.");
		} else {
			setState(ControllerState.EXECUTE);
			getView().disableVertexUI(true);
			getModel().resetVariables();
			storeTestInputs();
			execute(toView(getModel().getStartVertex()), true);
		}
	}
	private void storeTestInputs() {
		final TestCase testCase = state.getChosenQuestion().getTestCase(testCaseIndex);
		final ArrayList<String> inputNames = state.getChosenQuestion().getInputNames();
		int i = 0;
		for (String inputName : inputNames) {
			getModel().storeArray(inputName, testCase.getTestInputs().get(i));
			i++;
		}
	}
	public void execute(VertexView startVertexView, boolean firstExecution) {
		if (startVertexView != toView(getModel().getStartVertex()) || firstExecution) {
			VertexModel currentVertexModel = toModel(startVertexView);
			VertexView currentVertexView = toView(currentVertexModel);
			getView().positionCamera(currentVertexView.getX() + (currentVertexView.getWidth() / 2), currentVertexView.getY() + (currentVertexView.getHeight() / 2));
			if (currentVertexModel.getVertexType() != VertexType.START) {
				try {
					ExecutionData executionData = ((ExecutableModel) currentVertexModel).execute();
					if (executionData.getInformation().equals("OPERATOR")) {
						state.showFatalExecutionErrorMessage("Invalid symbol operator.");
					} else {
						VertexModel nextVertexModel = executionData.getNextVertex();
						boolean ignored = executionData.getInformation().equals("IGNORED");
						if (nextVertexModel != null) {
							if (ignored) {
								state.showExecutionErrorMessage("Blank mandatory field(s), symbol ignored.", toView(nextVertexModel));
							} else {
								wait(toView(nextVertexModel));
							}
						} else {
							if (ignored) {
								state.showExecutionErrorMessage("Blank mandatory field(s), symbol ignored.", null);
							} else {
								wait(null);
							}
						}
					}
				} catch (FlowchartModel.VariableNameException exception) {
					state.showFatalExecutionErrorMessage("Invalid variable identifier.");
				} catch (FlowchartModel.VariableValueException exception) {
					state.showFatalExecutionErrorMessage("Variable/array element has no associated value.");
				} catch (FlowchartModel.ValueException exception) {
					state.showFatalExecutionErrorMessage("Values can only be numerical/valid variable identifiers.");
				}
			} else {
				wait(toView(currentVertexModel.getNextVertex()));
			}
		} else {
			//Execution finished so check if the algorithm output the correct values.
			testExpectedOutputs();
		}
	}
	private void wait(VertexView nextVertexView) {
		setNextVertexView(nextVertexView);
		setWaiting(true);
	}
	private void testExpectedOutputs() {
		if (correctOutputs()) {
			testCaseIndex++;
			if (testCaseIndex < 3) {
				execute();
			} else {
				//User must've passed all test cases.
				stopExecution();
				state.displayScore();
			}
		} else {
			state.showFlowchartMessage("Answer", "Incorrect answer.");
			stopExecution();
		}
	}
	private boolean correctOutputs() {
		final TestCase testCase = state.getChosenQuestion().getTestCase(testCaseIndex);
		final ArrayList<String> outputNames = state.getChosenQuestion().getOutputNames();
		int i = 0;
		for (String outputName : outputNames) {
			ArrayList<Double> expectedOutputs = testCase.getExpectedOutputs().get(i);
			//The user's algorithm filled one of the checked arrays with more elements than it should have.
			if (getModel().arrayLength(outputName) > testCase.getExpectedOutputs().get(i).size()) {
				return false;
			}
			int j = 0;
			//Compare the contents of both arrays.
			for (double expectedOutput : expectedOutputs) {
				try {
					if (getVariable(outputName + "[" + j + "]") != expectedOutput) {
						return false;
					}
				} catch (FlowchartModel.VariableValueException exception) {
					return false;
				} catch (FlowchartModel.VariableNameException ignored) {
				}
				j++;
			}
			i++;
		}
		return true;
	}
	@Override
	public void stopExecution() {
		super.stopExecution();
		testCaseIndex = 0;
	}
}