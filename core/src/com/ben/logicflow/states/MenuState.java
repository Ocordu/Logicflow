package com.ben.logicflow.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.ben.logicflow.Application;
import com.ben.logicflow.Application.ApplicationState;
import com.ben.logicflow.Assets;
import com.ben.logicflow.DialogAction;

public final class MenuState extends State {
	//Used to easily position UI elements.
	private final Table userInterfaceTable = new Table(Assets.getSkin());
	private final TextButton learnButton = new TextButton("Learn", Assets.getSkin());
	private final TextButton practiceButton = new TextButton("Practice", Assets.getSkin());
	private final TextButton quizButton = new TextButton("Quiz", Assets.getSkin());
	private final TextButton quitButton = new TextButton("Quit", Assets.getSkin());
	private final TextButton tryQuestionButton = new TextButton("Try Question", Assets.getSkin());
	private final TextButton addQuestionButton = new TextButton("Add Question", Assets.getSkin());
	private final TextButton quizMenuBackButton = new TextButton("Back", Assets.getSkin());
	public MenuState(Application application) {
		super(application);
		initialiseUserInterface();
	}
	private void initialiseUserInterface() {
		//Set the table to fill the entire screen (stage becomes userInterfaceTable's parent when it's added).
		userInterfaceTable.setFillParent(true);
		learnButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				getApplication().changeState(ApplicationState.LEARN);
			}
		});
		practiceButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				getApplication().changeState(ApplicationState.PRACTICE);
			}
		});
		quizButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				showQuizMenu();
			}
		});
		quitButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				getApplication().showApplicationDialog("Quit", "Are you sure you want to quit?", true, new DialogAction() {
					@Override
					public void confirm() {
						Gdx.app.exit();
					}
				});
			}
		});
		tryQuestionButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				getApplication().changeState(ApplicationState.TRY_QUESTION);
			}
		});
		addQuestionButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				getApplication().changeState(ApplicationState.ADD_QUESTION);
			}
		});
		quizMenuBackButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				showMainMenu();
			}
		});
		getApplication().getMainStage().addActor(userInterfaceTable);
	}
	public void showMainMenu() {
		userInterfaceTable.clear();
		userInterfaceTable.add(new Label("Logicflow", Assets.getSkin(), "title")).colspan(4).space(30).left().row();
		userInterfaceTable.add(learnButton).width(130).height(40).space(20);
		userInterfaceTable.add(practiceButton).width(130).height(40).space(20);
		userInterfaceTable.add(quizButton).width(130).height(40).space(20);
		userInterfaceTable.add(quitButton).width(65).height(40).space(20);
	}
	private void showQuizMenu() {
		userInterfaceTable.clear();
		userInterfaceTable.add(new Label("Quiz", Assets.getSkin(), "title")).colspan(3).space(30).left().row();
		userInterfaceTable.add(tryQuestionButton).width(130).height(40).space(20);
		userInterfaceTable.add(addQuestionButton).width(130).height(40).space(20);
		userInterfaceTable.add(quizMenuBackButton).width(65).height(40).space(20);
	}
	@Override
	public void show() {
		userInterfaceTable.setVisible(true);
	}
	@Override
	public void hide() {
		userInterfaceTable.setVisible(false);
	}
}