package com.ben.logicflow.states.quiz;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.ben.logicflow.Application;
import com.ben.logicflow.Assets;
import com.ben.logicflow.DialogAction;
import com.ben.logicflow.MongoConnection;
import com.ben.logicflow.flowchart.FlowchartController;
import com.ben.logicflow.flowchart.TryQuestionController;
import com.ben.logicflow.flowchart.view.VertexView;
import com.ben.logicflow.states.FlowchartState;
import com.mongodb.MongoTimeoutException;

import java.util.ArrayList;

public final class TryQuestionState extends FlowchartState {
	private final TryQuestionController tryQuestionController;
	private final Table selectQuestionTable = new Table(Assets.getSkin());
	/*
	 * Questions are stored in memory to keep the number of database queries to a minimum. This is to reduce the quiz's dependency on a
	 * stable internet connection.
	 */
	private final ArrayList<Question> questions = new ArrayList<>();
	private Question chosenQuestion;
	private float questionStopwatch;
	private String currentTip;
	public TryQuestionState(Application application) {
		super(application, false);
		tryQuestionController = new TryQuestionController(this, application.getFlowchartStage(), application.getFlowchartShapeRenderer());
		setController(tryQuestionController);
		initialiseUserInterface();
		hide();
	}
	private void initialiseUserInterface() {
		initialiseQuestionSelectBox();
		initialiseViewSelectBox();
		getUserInterfaceTable().add(getInformationLabel()).expandX().pad(0, 0, 0, 10).right();
		selectQuestionTable.setFillParent(true);
	}
	private void initialiseQuestionSelectBox() {
		final SelectBox<String> selectBox = new SelectBox<>(Assets.getSkin(), "menu-bar");
		selectBox.setItems("Read question", "Check answer", "Clear answer", "Quit");
		selectBox.getList().addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				if (tryQuestionController.getState() == FlowchartController.ControllerState.AVAILABLE) {
					switch (selectBox.getSelected()) {
						case "Read question":
							showFlowchartMessage(chosenQuestion.getTitle(), chosenQuestion.getDescription());
							break;
						case "Check answer":
							tryQuestionController.execute();
							break;
						case "Clear answer":
							newFlowchart();
							break;
						case "Quit":
							close();
							break;
					}
				}
			}
			@Override
			public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
				super.touchUp(event, x, y, pointer, button);
				selectBox.getSelection().set("Question");
			}
		});
		addMenuBarSelectBox(selectBox, "Question");
	}
	@Override
	public void update(float delta) {
		super.update(delta);
		if (getFlowchartView().isVisible()) {
			questionStopwatch += delta;
			if (Math.round(questionStopwatch) == 1) {
				getInformationLabel().setText(currentTip + "\nTime taken: " + String.valueOf(Math.round(questionStopwatch)) + " second.");
			} else {
				getInformationLabel().setText(currentTip + "\nTime taken: " + String.valueOf(Math.round(questionStopwatch)) + " seconds.");
			}
		}
	}
	private void showSelectQuestionSection() {
		selectQuestionTable.clear();
		selectQuestionTable.add(new Label("Questions", Assets.getSkin(), "sub-title")).colspan(5).space(10).row();
		final ButtonGroup<TextButton> questionListButtonGroup = new ButtonGroup<>();
		questionListButtonGroup.setMinCheckCount(1);
		questionListButtonGroup.setMaxCheckCount(1);
		final ArrayList<CheckBox> questionListCheckBoxes = new ArrayList<>();
		final Table questionListTable = new Table(Assets.getSkin());
		showQuestions("All", "", questionListButtonGroup, questionListCheckBoxes, questionListTable);
		final ScrollPane questionListScrollPane = new ScrollPane(questionListTable, Assets.getSkin());
		questionListScrollPane.setFadeScrollBars(false);
		questionListScrollPane.setFlickScroll(false);
		selectQuestionTable.add(questionListScrollPane).width(560).height(350).colspan(5).space(20).row();
		selectQuestionTable.add("Difficulty: ").space(20).left();
		final SelectBox<String> difficultySelectBox = new SelectBox<>(Assets.getSkin());
		difficultySelectBox.setItems("All", "Easy", "Medium", "Hard", "Very hard");
		final TextField searchTextField = new TextField("", Assets.getSkin());
		difficultySelectBox.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				showQuestions(difficultySelectBox.getSelected(), searchTextField.getText(), questionListButtonGroup, questionListCheckBoxes, questionListTable);
			}
		});
		selectQuestionTable.add(difficultySelectBox).space(20).left();
		selectQuestionTable.add().width(50);
		selectQuestionTable.add("Search term: ").space(20).left();
		searchTextField.setTextFieldListener(new TextField.TextFieldListener() {
			@Override
			public void keyTyped(TextField textField, char c) {
				showQuestions(difficultySelectBox.getSelected(), searchTextField.getText(), questionListButtonGroup, questionListCheckBoxes, questionListTable);
			}
		});
		selectQuestionTable.add(searchTextField).fillX().space(20).left().row();
		final Table buttonTable = new Table(Assets.getSkin());
		final TextButton cancelButton = new TextButton("Cancel", Assets.getSkin());
		cancelButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				getApplication().changeState(Application.ApplicationState.MENU);
			}
		});
		buttonTable.add(cancelButton).width(80).height(40).space(120);
		final TextButton confirmButton = new TextButton("OK", Assets.getSkin());
		confirmButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				if (questionListCheckBoxes.get(questionListButtonGroup.getCheckedIndex()) != null) {
					selectQuestionTable.clear();
					selectQuestionTable.remove();
					chosenQuestion = questions.get(questionListButtonGroup.getCheckedIndex());
					questionStopwatch = 0;
					showAnswerQuestionSection();
				} else {
					getApplication().showApplicationDialog("Error", "A question must be selected.", false, null);
				}
			}
		});
		buttonTable.add(confirmButton).width(80).height(40).space(120);
		selectQuestionTable.add(buttonTable).colspan(5);
		getApplication().getMainStage().addActor(selectQuestionTable);
		selectQuestionTable.setVisible(true);
	}
	private void showQuestions(String difficulty, String searchTerm, ButtonGroup<TextButton> questionListButtonGroup, ArrayList<CheckBox> questionListCheckBoxes, Table questionListTable) {
		questionListButtonGroup.clear();
		questionListCheckBoxes.clear();
		questionListTable.clear();
		//Create space for the checkboxes.
		questionListTable.add().width(15).space(10);
		questionListTable.add(new Label("Title", Assets.getSkin(), "small-sub-title")).width(350).space(10);
		questionListTable.add(new Label("Difficulty", Assets.getSkin(), "small-sub-title")).width(150).space(10).row();
		boolean questionsFound = false;
		for (Question question : questions) {
			if ((difficulty.equals("All") && searchTerm.isEmpty()) || (question.getDifficulty().equals(difficulty) && searchTerm.isEmpty()) || (difficulty.equals("All") && question.getTitle().toLowerCase().contains(searchTerm.toLowerCase())) || (question.getDifficulty().equals(difficulty) && question.getTitle().toLowerCase().contains(searchTerm.toLowerCase()))) {
				final CheckBox questionCheckBox = new CheckBox("", Assets.getSkin());
				questionListButtonGroup.add(questionCheckBox);
				questionListCheckBoxes.add(questionCheckBox);
				questionListTable.add(questionCheckBox).width(15).space(10);
				questionListTable.add(question.getTitle()).width(350).space(10).left();
				questionListTable.add(question.getDifficulty()).width(150).space(10).left().row();
				questionsFound = true;
			}
		}
		if (!questionsFound) {
			questionListTable.clear();
			questionListTable.add(new Label("No questions found.", Assets.getSkin(), "small-sub-title"));
		}
	}
	//TODO fix startVertex MINOR bug
	private void showAnswerQuestionSection() {
		super.show();
		showFlowchartMessage(chosenQuestion.getTitle(), chosenQuestion.getDescription());
	}
	public void showExecutionErrorMessage(String body, final VertexView nextVertex) {
		getApplication().showApplicationDialog("Error", body, false, new DialogAction() {
			@Override
			public void confirm() {
				tryQuestionController.execute(nextVertex, false);
			}
		});
	}
	public void displayScore() {
		super.hide();
		newFlowchart();
		tryQuestionController.setState(FlowchartController.ControllerState.AVAILABLE);
		final DialogAction dialogAction = new DialogAction() {
			@Override
			public void confirm() {
				try {
					chosenQuestion.updateStats(MongoConnection.getDatabase().getCollection(Application.getProperty("QuestionCollection")), Math.round(questionStopwatch));
				} catch (MongoTimeoutException ignored) {
				}
				getApplication().changeState(Application.ApplicationState.MENU);
			}
		};
		//A reasonable amount of data is required to calculate an accurate score.
		if (chosenQuestion.getAttempts() > 2) {
			/*
			 * Find the ratio of average time to user's time and store it as a percentage. If the user took longer than average, she/he
			 * would get a lower score. Users whose time is the same as the calculated average will score 100 points.
			 */
			long score = Math.round(((chosenQuestion.getTotalTime() / chosenQuestion.getAttempts()) / questionStopwatch) * 100);
			if (score < 100) {
				getApplication().showApplicationDialog("Answer", "Correct answer, you scored " + score + " points. That's " + (100 - score) + " points below average.", false, dialogAction);
			} else {
				getApplication().showApplicationDialog("Answer", "Correct answer, you scored " + score + " points. That's " + (score - 100) + " points above average.", false, dialogAction);
			}
		} else {
			getApplication().showApplicationDialog("Answer", "Correct answer, unfortunately there's not enough data to calculate your score.", false, dialogAction);
		}
	}
	@Override
	public void keyPressed() {
		super.keyPressed();
		if (getFlowchartView().isVisible() && noFocus() && tryQuestionController.getState() == FlowchartController.ControllerState.AVAILABLE) {
			if (getApplication().isKeysDown(true, Input.Keys.F1)) {
				showFlowchartMessage(chosenQuestion.getTitle(), chosenQuestion.getDescription());
			} else if (getApplication().isKeysDown(true, Input.Keys.F10)) {
				tryQuestionController.execute();
			}
		}
	}
	@Override
	public void show() {
		try {
			questions.clear();
			questions.addAll(Question.getQuestions(MongoConnection.getDatabase().getCollection(Application.getProperty("QuestionCollection"))));
			if (questions.size() > 1) {
				showSelectQuestionSection();
			} else {
				getApplication().changeState(Application.ApplicationState.MENU);
				getApplication().showApplicationDialog("Questions", "No existing approved questions.", false, null);
			}
		} catch (MongoTimeoutException exception) {
			getApplication().changeState(Application.ApplicationState.MENU);
			getApplication().showApplicationDialog("Error", "The database connection/query timed out.", false, null);
		}
	}
	@Override
	public void hide() {
		super.hide();
		selectQuestionTable.setVisible(false);
	}
	public Question getChosenQuestion() {
		return chosenQuestion;
	}
	//Tips are stored rather than rendered straight away so they can be added to the same label the question stopwatch is displayed on.
	@Override
	public void setInformationText(String text) {
		currentTip = text;
	}
}