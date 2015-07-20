package com.ben.logicflow.states.quiz;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.ben.logicflow.Application;
import com.ben.logicflow.Application.ApplicationState;
import com.ben.logicflow.Assets;
import com.ben.logicflow.DialogAction;
import com.ben.logicflow.MongoConnection;
import com.ben.logicflow.states.State;
import com.mongodb.BasicDBList;
import com.mongodb.MongoTimeoutException;
import org.bson.Document;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AddQuestionState extends State {
	private final Table table = new Table(Assets.getSkin());
	//TODO fix title MINOR bug
	private final TextField titleTextField = new TextField("", Assets.getSkin());
	private final TextArea descriptionTextArea = new TextArea("", Assets.getSkin());
	private final SelectBox<String> difficultySelectBox = new SelectBox<>(Assets.getSkin());
	private final TextField[] inputNameTextFields = new TextField[20];
	private final TextField[] outputNameTextFields = new TextField[20];
	private final ArrayList<String> inputNames = new ArrayList<>();
	private final ArrayList<String> outputNames = new ArrayList<>();
	private final ArrayList<String> lastKnownInputNames = new ArrayList<>();
	private final ArrayList<String> lastKnownOutputNames = new ArrayList<>();
	private final ArrayList<ArrayList<ArrayList<TextField>>> testInputTextFieldGroupsTestCases = new ArrayList<>();
	private final ArrayList<ArrayList<ArrayList<TextField>>> expectedOutputTextFieldGroupsTestCases = new ArrayList<>();
	private boolean[] clearTestCaseValues = new boolean[3];
	private final TestCase[] testCases = new TestCase[3];
	private int currentTestCase;
	public AddQuestionState(Application application) {
		super(application);
		table.setFillParent(true);
		titleTextField.setMaxLength(100);
		titleTextField.setMessageText("e.g. 'Sum N Numbers'");
		descriptionTextArea.setMessageText("e.g. 'The variable [amount] holds the size of the array [numbers]. Add all of the values in [numbers] and...'");
		difficultySelectBox.setItems("Easy", "Medium", "Hard", "Very hard");
		application.getMainStage().addActor(table);
		showQuestionInformation();
		hide();
	}
	private void showQuestionInformation() {
		table.clear();
		table.add(new Label("Question Information", Assets.getSkin(), "small-sub-title")).colspan(2).space(10).row();
		final Label label = new Label("Make sure all of the variables that will be provided to the algorithm (e.g. 'mass') and checked when the algorithm is finished (e.g. 'weight') are mentioned in the question's description. By convention, variable names are enclosed in square brackets.", Assets.getSkin());
		label.setWrap(true);
		table.add(label).width(850).colspan(2).space(20).left().row();
		table.add("Question title:").space(20).left();
		table.add(titleTextField).width(350).space(20).left().row();
		table.add("Question description:").space(20).left();
		table.add(descriptionTextArea).width(700).height(200).space(20).left().row();
		table.add("Question difficulty:").space(20).left();
		table.add(difficultySelectBox).space(20).left().row();
		Table buttonTable = new Table(Assets.getSkin());
		final TextButton backButton = new TextButton("Back", Assets.getSkin());
		backButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				getApplication().changeState(ApplicationState.MENU);
			}
		});
		buttonTable.add(backButton).width(80).height(40).space(120);
		final TextButton nextButton = new TextButton("Next", Assets.getSkin());
		nextButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				if (titleTextField.getText().isEmpty() || descriptionTextArea.getText().isEmpty()) {
					getApplication().showApplicationDialog("Error", "Blank mandatory field(s).", false, null);
				} else if (titleTextField.getText().length() < 3) {
					getApplication().showApplicationDialog("Error", "The question's title is too short.", false, null);
				} else if (descriptionTextArea.getText().length() < 7) {
					getApplication().showApplicationDialog("Error", "The question's description is too short.", false, null);
				} else {
					showVariableNames();
				}
			}
		});
		buttonTable.add(nextButton).width(80).height(40).space(120);
		table.add(buttonTable).colspan(2).space(20);
	}
	private void showVariableNames() {
		table.clear();
		table.add(new Label("Variable Names", Assets.getSkin(), "sub-title")).space(10).row();
		final Label label = new Label("These variables have the same names that were chosen in the previous section but they're split up into two categories, input and output names. Input variables store data that's given to the algorithm (e.g. 'numbers') while output variables store the outcome of the algorithm (e.g. 'sum'). At least one input name and one output name is required. Note that an input and output variable can have the same name. For example, an algorithm could take an array of numbers as an input and output the same array but with sorted numbers. This time don't enclose the names of variables in square brackets.", Assets.getSkin());
		label.setWrap(true);
		table.add(label).width(850).space(20).left().row();
		final Table variableListTable = new Table(Assets.getSkin());
		variableListTable.add(new Label("Input Names", Assets.getSkin(), "small-sub-title")).expandX().space(10);
		variableListTable.add(new Label("Output Names", Assets.getSkin(), "small-sub-title")).expandX().space(10).row();
		for (int i = 0; i < inputNameTextFields.length; i++) {
			inputNameTextFields[i].setMaxLength(15);
			outputNameTextFields[i].setMaxLength(15);
			variableListTable.add(inputNameTextFields[i]).space(10);
			variableListTable.add(outputNameTextFields[i]).space(10).row();
		}
		final ScrollPane questionListScrollPane = new ScrollPane(variableListTable, Assets.getSkin());
		questionListScrollPane.setFadeScrollBars(false);
		questionListScrollPane.setFlickScroll(false);
		table.add(questionListScrollPane).width(500).height(235).space(20).row();
		Table buttonTable = new Table(Assets.getSkin());
		final TextButton backButton = new TextButton("Back", Assets.getSkin());
		backButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				showQuestionInformation();
			}
		});
		buttonTable.add(backButton).width(80).height(40).space(120);
		final TextButton nextButton = new TextButton("Next", Assets.getSkin());
		nextButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				if (storeVariableNames()) {
					if (inputNames.size() < 1) {
						getApplication().showApplicationDialog("Error", "Questions must have at least one input name.", false, null);
					} else if (outputNames.size() < 1) {
						getApplication().showApplicationDialog("Error", "Questions must have at least one output name.", false, null);
					} else {
						showTestCase();
					}
				}
			}
		});
		buttonTable.add(nextButton).width(80).height(40).space(120);
		table.add(buttonTable).space(20);
	}
	private boolean storeVariableNames() {
		inputNames.clear();
		for (TextField textField : inputNameTextFields) {
			if (!textField.getText().isEmpty()) {
				Pattern pattern = Pattern.compile("([a-z]|[A-Z])([a-z]|[A-Z]|\\d)*");
				Matcher matcher = pattern.matcher(textField.getText());
				if (matcher.matches()) {
					if (inputNames.contains(textField.getText())) {
						getApplication().showApplicationDialog("Error", "One or more duplicate input names.", false, null);
						return false;
					}
					inputNames.add(textField.getText());
				} else {
					getApplication().showApplicationDialog("Error", "One or more input names have an invalid format.", false, null);
					return false;
				}
			}
		}
		outputNames.clear();
		for (TextField textField : outputNameTextFields) {
			if (!textField.getText().isEmpty()) {
				Pattern pattern = Pattern.compile("([a-z]|[A-Z])([a-z]|[A-Z]|\\d)*");
				Matcher matcher = pattern.matcher(textField.getText());
				if (matcher.matches()) {
					if (outputNames.contains(textField.getText())) {
						getApplication().showApplicationDialog("Error", "One or more duplicate output names.", false, null);
						return false;
					}
					outputNames.add(textField.getText());
				} else {
					getApplication().showApplicationDialog("Error", "One or more output names have an invalid format.", false, null);
					return false;
				}
			}
		}
		return true;
	}
	private void showTestCase() {
		table.clear();
		table.add(new Label("Test Case " + currentTestCase, Assets.getSkin(), "sub-title")).space(10).row();
		final Label label = new Label("A test case checks that the user's answer is correct. For test inputs the program assigns the values you specified to each input variable. The program then checks if the algorithm's output variables have the same values as the expected output values. To ensure the user's answer is actually correct, there are three test cases. Each text field represents an element in the variable's array. If the variable is not an array then only enter data into the first text field.", Assets.getSkin());
		label.setWrap(true);
		table.add(label).width(850).space(20).left().row();
		showTestCaseValues();
		Table buttonTable = new Table(Assets.getSkin());
		final TextButton backButton = new TextButton("Back", Assets.getSkin());
		backButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				if (currentTestCase > 1) {
					currentTestCase--;
					showTestCase();
				} else {
					showVariableNames();
				}
			}
		});
		buttonTable.add(backButton).width(80).height(40).space(120);
		final TextButton nextButton = new TextButton("Next", Assets.getSkin());
		nextButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				final ArrayList<ArrayList<Double>> testInputs = new ArrayList<>();
				boolean testInputsExist = false;
				for (ArrayList<TextField> testInputTextFieldGroup : testInputTextFieldGroupsTestCases.get(currentTestCase - 1)) {
					final ArrayList<Double> testInputElements = new ArrayList<>();
					for (TextField testInputTextField : testInputTextFieldGroup) {
						try {
							testInputElements.add(Double.parseDouble(testInputTextField.getText()));
							testInputsExist = true;
						} catch (NumberFormatException exception) {
							if (!testInputTextField.getText().isEmpty()) {
								getApplication().showApplicationDialog("Error", "One or more test inputs aren't numerical.", false, null);
								return;
							}
						}
					}
					testInputs.add(testInputElements);
				}
				if (!testInputsExist) {
					getApplication().showApplicationDialog("Error", "One or more variables have no test inputs.", false, null);
					return;
				}
				boolean expectedOutputsExist = false;
				final ArrayList<ArrayList<Double>> expectedOutputs = new ArrayList<>();
				for (ArrayList<TextField> expectedOutputTextFieldGroup : expectedOutputTextFieldGroupsTestCases.get(currentTestCase - 1)) {
					final ArrayList<Double> expectedOutputElements = new ArrayList<>();
					for (TextField expectedOutputTextField : expectedOutputTextFieldGroup) {
						try {
							expectedOutputElements.add(Double.parseDouble(expectedOutputTextField.getText()));
							expectedOutputsExist = true;
						} catch (NumberFormatException exception) {
							if (!expectedOutputTextField.getText().isEmpty()) {
								getApplication().showApplicationDialog("Error", "One or more expected outputs aren't numerical.", false, null);
								return;
							}
						}
					}
					expectedOutputs.add(expectedOutputElements);
				}
				if (!expectedOutputsExist) {
					getApplication().showApplicationDialog("Error", "One or more variables have no expected outputs.", false, null);
					return;
				}
				testCases[currentTestCase - 1] = new TestCase(testInputs, expectedOutputs);
				if (currentTestCase < 3) {
					currentTestCase++;
					showTestCase();
				} else {
					addQuestion();
				}
			}
		});
		buttonTable.add(nextButton).width(80).height(40).space(120);
		table.add(buttonTable).space(20);
	}
	private void showTestCaseValues() {
		if (!lastKnownInputNames.containsAll(inputNames) || !lastKnownOutputNames.containsAll(outputNames)) {
			clearTestCaseValues = new boolean[]{true, true, true};
			lastKnownInputNames.clear();
			lastKnownOutputNames.clear();
			lastKnownInputNames.addAll(inputNames);
			lastKnownOutputNames.addAll(outputNames);
		}
		final Table variableListTable = new Table(Assets.getSkin());
		variableListTable.add(new Label("Test Inputs", Assets.getSkin(), "small-sub-title")).expandX().colspan(5).space(10);
		variableListTable.add();
		variableListTable.add(new Label("Expected Outputs", Assets.getSkin(), "small-sub-title")).expandX().colspan(5).space(10).row();
		for (int i = 0; i < Math.max(inputNames.size(), outputNames.size()); i++) {
			if (i < inputNames.size()) {
				variableListTable.add(inputNames.get(i)).colspan(5).space(10);
			} else {
				variableListTable.add().colspan(5).space(10);
			}
			variableListTable.add().width(50);
			if (i < outputNames.size()) {
				variableListTable.add(outputNames.get(i)).colspan(5).space(10).row();
			} else {
				variableListTable.add().colspan(5).space(10).row();
			}
			if (i < inputNames.size()) {
				if (clearTestCaseValues[currentTestCase - 1]) {
					if (testInputTextFieldGroupsTestCases.get(currentTestCase - 1).size() > i) {
						testInputTextFieldGroupsTestCases.get(currentTestCase - 1).get(i).clear();
					}
					final ArrayList<TextField> testInputTextFields = new ArrayList<>();
					for (int j = 0; j < 5; j++) {
						final TextField testInputTextField = new TextField("", Assets.getSkin());
						testInputTextField.setMaxLength(15);
						variableListTable.add(testInputTextField).width(65).space(10);
						testInputTextFields.add(testInputTextField);
					}
					testInputTextFieldGroupsTestCases.get(currentTestCase - 1).add(testInputTextFields);
				} else {
					for (int j = 0; j < 5; j++) {
						variableListTable.add(testInputTextFieldGroupsTestCases.get(currentTestCase - 1).get(i).get(j)).width(65).space(10);
					}
				}
				variableListTable.add().width(50);
			} else {
				variableListTable.add().colspan(5).space(10);
				variableListTable.add().width(50);
			}
			if (i < outputNames.size()) {
				if (clearTestCaseValues[currentTestCase - 1]) {
					if (expectedOutputTextFieldGroupsTestCases.get(currentTestCase - 1).size() > i) {
						expectedOutputTextFieldGroupsTestCases.get(currentTestCase - 1).get(i).clear();
					}
					final ArrayList<TextField> expectedOutputTextFields = new ArrayList<>();
					for (int j = 0; j < 5; j++) {
						final TextField expectedOutputTextField = new TextField("", Assets.getSkin());
						expectedOutputTextField.setMaxLength(15);
						variableListTable.add(expectedOutputTextField).width(65).space(10);
						expectedOutputTextFields.add(expectedOutputTextField);
					}
					expectedOutputTextFieldGroupsTestCases.get(currentTestCase - 1).add(expectedOutputTextFields);
				} else {
					for (int j = 0; j < 5; j++) {
						variableListTable.add(expectedOutputTextFieldGroupsTestCases.get(currentTestCase - 1).get(i).get(j)).width(65).space(10);
					}
				}
			} else {
				variableListTable.add().colspan(5).space(10);
			}
			variableListTable.row();
		}
		final ScrollPane questionListScrollPane = new ScrollPane(variableListTable, Assets.getSkin());
		questionListScrollPane.setFadeScrollBars(false);
		questionListScrollPane.setFlickScroll(false);
		table.add(questionListScrollPane).fillX().height(235).space(20).row();
		clearTestCaseValues[currentTestCase - 1] = false;
	}
	//TODO fix incorrect test case data COMMON MAJOR bug
	private void addQuestion() {
		final BasicDBList basicDBList = new BasicDBList();
		for (int i = 0; i < testCases.length; i++) {
			basicDBList.add(new Document("test_inputs", testCases[i].getTestInputs()).append("expected_outputs", testCases[i].getExpectedOutputs()));
		}
		final Document question = new Document("title", titleTextField.getText()).append("description", descriptionTextArea.getText()).append("difficulty", difficultySelectBox.getSelected()).append("input_names", inputNames).append("output_names", outputNames).append("test_cases", basicDBList).append("total_time", 0).append("attempts", 0).append("approved", false);
		try {
			MongoConnection.getDatabase().getCollection(Application.getProperty("QuestionCollection")).insertOne(question);
			getApplication().changeState(ApplicationState.MENU);
		} catch (MongoTimeoutException exception) {
			getApplication().showApplicationDialog("Error", "The database connection/query timed out.", false, new DialogAction() {
				@Override
				public void confirm() {
					getApplication().changeState(ApplicationState.MENU);
				}
			});
		}
	}
	@Override
	public void hide() {
		table.setVisible(false);
	}
	@Override
	public void show() {
		titleTextField.setText("");
		descriptionTextArea.setText("");
		difficultySelectBox.setSelected("Easy");
		for (int i = 0; i < inputNameTextFields.length; i++) {
			inputNameTextFields[i] = new TextField("", Assets.getSkin());
			outputNameTextFields[i] = new TextField("", Assets.getSkin());
		}
		testInputTextFieldGroupsTestCases.clear();
		expectedOutputTextFieldGroupsTestCases.clear();
		for (int i = 0; i < testCases.length; i++) {
			testInputTextFieldGroupsTestCases.add(new ArrayList<ArrayList<TextField>>());
			expectedOutputTextFieldGroupsTestCases.add(new ArrayList<ArrayList<TextField>>());
		}
		currentTestCase = 1;
		showQuestionInformation();
		table.setVisible(true);
	}
}