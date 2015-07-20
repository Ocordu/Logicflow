package com.ben.logicflow;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.ben.logicflow.states.*;
import com.ben.logicflow.states.quiz.AddQuestionState;
import com.ben.logicflow.states.quiz.TryQuestionState;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

public final class Application extends ApplicationAdapter {
	//Split the UI into two Stages so it's easier to manage.
	private Stage mainStage;
	private Stage flowchartStage;
	private ShapeRenderer flowchartShapeRenderer;
	private State[] states;
	private ApplicationState currentState = ApplicationState.MENU;
	private static final Properties PROPERTIES = new Properties();
	//FILE_SEPARATOR stores the file separator's characters used in directories by the operating system.
	private static final String FILE_SEPARATOR = System.getProperty("file.separator");
	private static final String DEFAULT_DIRECTORY = System.getProperty("user.home") + FILE_SEPARATOR + "Logicflow";
	private static final String ALTERNATIVE_DIRECTORY = System.getProperty("user.dir");
	//keysDown stores the current sequence of keys pressed by the user to facilitate events that rely on simultaneously held-down keys.
	private final ArrayList<Integer> keysDown = new ArrayList<>();
	private Dialog applicationDialog;
	@Override
	public void create() {
		Gdx.graphics.setDisplayMode(Gdx.graphics.getDesktopDisplayMode().width, Gdx.graphics.getDesktopDisplayMode().height, true);
		initialiseMainStage();
		initialiseFlowchartStage();
		flowchartShapeRenderer = new ShapeRenderer();
		//Enable keyboard input for both stages.
		final InputMultiplexer inputMultiplexer = new InputMultiplexer(mainStage, flowchartStage);
		Gdx.input.setInputProcessor(inputMultiplexer);
		Assets.initialise();
		applicationDialog = new Dialog("", Assets.getSkin());
		applicationDialog.getTitleLabel().setAlignment(Align.center);
		/*
		 * The elements of states must be stored in this order so they have the same positions as their corresponding constants in the
		 * ApplicationState enum. This means the ordinal of currentState can be conveniently used to refer to the active state in states.
		 */
		states = new State[]{new MenuState(this), new LearnState(this), new PracticeState(this), new TryQuestionState(this), new AddQuestionState(this)};
		initialiseProperties();
	}
	private void initialiseMainStage() {
		mainStage = new Stage(new ScreenViewport());
		mainStage.addListener(new InputListener() {
			@Override
			public boolean keyDown(InputEvent event, int keycode) {
				if (!keysDown.contains(keycode)) {
					keysDown.add(keycode);
				}
				//This shows the convenience of exploiting the ordinal method in place of an overly long selection statement.
				states[currentState.ordinal()].keyPressed();
				return false;
			}
			@Override
			public boolean keyUp(InputEvent event, int keycode) {
				/*
				 * As keysDown consists of instances and not primitive data types, keycode has to be wrapped as an instance of Integer.
				 * The reason this wasn't necessary with keysDown.add() was because the compiler successfully autoboxed keycode.
				 */
				keysDown.remove(Integer.valueOf(keycode));
				return false;
			}
			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				//Remove keyboard focus when the user clicks on something other than a text field.
				if (!(event.getTarget() instanceof TextField)) {
					mainStage.setKeyboardFocus(null);
				}
				return false;
			}
			@Override
			public boolean scrolled(InputEvent event, float x, float y, int amount) {
				states[currentState.ordinal()].scrolled(amount);
				return false;
			}
		});
	}
	private void initialiseFlowchartStage() {
		flowchartStage = new Stage(new ScreenViewport());
		flowchartStage.addListener(new InputListener() {
			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				if (!(event.getTarget() instanceof TextField)) {
					flowchartStage.setKeyboardFocus(null);
				}
				return false;
			}
		});
	}
	private void initialiseProperties() {
		if (!loadProperties(DEFAULT_DIRECTORY)) {
			if (!loadProperties(ALTERNATIVE_DIRECTORY)) {
				File file = new File(DEFAULT_DIRECTORY + FILE_SEPARATOR + "settings");
				//Create the directory required for the settings file if it doesn't exist.
				file.mkdirs();
				final DialogAction dialogAction = new DialogAction() {
					@Override
					public void confirm() {
						((MenuState) states[ApplicationState.MENU.ordinal()]).showMainMenu();
					}
				};
				if (createDefaultProperties(DEFAULT_DIRECTORY)) {
					showApplicationDialog("Settings", "Configuration file created in the default directory.", false, dialogAction);
				} else {
					file = new File(ALTERNATIVE_DIRECTORY + FILE_SEPARATOR + "settings");
					file.mkdirs();
					if (createDefaultProperties(ALTERNATIVE_DIRECTORY)) {
						showApplicationDialog("Settings", "Configuration file created in the alternative directory.", false, dialogAction);
					} else {
						showApplicationDialog("Error", "Configuration file failed to be created, default settings loaded.", false, dialogAction);
					}
				}
			}
		}
	}
	private boolean loadProperties(String directory) {
		try {
			PROPERTIES.load(new FileInputStream(directory + FILE_SEPARATOR + "settings" + FILE_SEPARATOR + "settings.ini"));
			if (validProperties()) {
				((MenuState) states[ApplicationState.MENU.ordinal()]).showMainMenu();
			} else {
				showApplicationDialog("Error", "Invalid configuration file, default settings loaded.", false, new DialogAction() {
					@Override
					public void confirm() {
						loadDefaultProperties(ALTERNATIVE_DIRECTORY);
						((MenuState) states[ApplicationState.MENU.ordinal()]).showMainMenu();
					}
				});
			}
			return true;
		} catch (IOException exception) {
			return false;
		}
	}
	private boolean validProperties() {
		//The properties file must only contain the properties specified in the user manual.
		if (PROPERTIES.size() == 9 && PROPERTIES.keySet().containsAll(Arrays.asList("FlowchartDirectory", "ScreenshotDirectory", "ServerIP", "ServerPort", "NoServerAuthentication", "DatabaseName", "DatabaseUsername", "DatabasePassword", "QuestionCollection"))) {
			for (Object value : PROPERTIES.values()) {
				/**
				 * A value must be specified for every property. Although values are stored as objects, Java's documentation says values
				 * can be cast to Strings.
				 */
				if (((String) value).length() < 1) {
					return false;
				}
			}
			//Test if the value of ServerPort is an integer.
			try {
				Integer.parseInt(PROPERTIES.getProperty("ServerPort"));
			} catch (NumberFormatException exception) {
				return false;
			}
			return PROPERTIES.getProperty("NoServerAuthentication").toLowerCase().equals("true") || PROPERTIES.getProperty("NoServerAuthentication").toLowerCase().equals("false");
		}
		return false;
	}
	private void loadDefaultProperties(String directory) {
		PROPERTIES.clear();
		PROPERTIES.put("FlowchartDirectory", directory + FILE_SEPARATOR + "flowcharts");
		PROPERTIES.put("ScreenshotDirectory", directory + FILE_SEPARATOR + "screenshots");
		PROPERTIES.put("ServerIP", "localhost");
		PROPERTIES.put("ServerPort", "27017");
		PROPERTIES.put("NoServerAuthentication", "true");
		PROPERTIES.put("DatabaseName", "logicflowQuiz");
		PROPERTIES.put("DatabaseUsername", "admin");
		PROPERTIES.put("DatabasePassword", "password");
		PROPERTIES.put("QuestionCollection", "questions");
	}
	private boolean createDefaultProperties(String directory) {
		loadDefaultProperties(directory);
		try {
			final FileWriter fileWriter = new FileWriter(new File(directory + FILE_SEPARATOR + "settings" + FILE_SEPARATOR + "settings.ini"));
			PROPERTIES.store(fileWriter, "Configuration");
			return true;
		} catch (IOException ignored) {
			return false;
		}
	}
	@Override
	public void render() {
		updateModel();
		updateView();
	}
	//Update the state of the application.
	private void updateModel() {
		/*
		 * getDeltaTime() returns the change in time between the current and previous frame. As the frame rate is set to 60 frame per
		 * second this should usually return a number close to 1/60. This will be used to update stopwatches, timers and other time-based
		 * data as the frame rate may vary.
		 */
		states[currentState.ordinal()].update(Gdx.graphics.getDeltaTime());
		flowchartShapeRenderer.setProjectionMatrix(flowchartStage.getCamera().combined);
		mainStage.act();
		flowchartStage.act();
	}
	//Update the graphics of the application.
	private void updateView() {
		Gdx.gl.glClearColor(0.215f, 0.215f, 0.215f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		drawFlowchart(false);
		flowchartStage.draw();
		mainStage.draw();
	}
	private void drawFlowchart(boolean blackEdges) {
		if (states[currentState.ordinal()] instanceof FlowchartState) {
			((FlowchartState) states[currentState.ordinal()]).drawFlowchart(blackEdges);
		}
	}
	public void showApplicationDialog(String title, String body, boolean question, final DialogAction dialogAction) {
		setApplicationDialogContents(title, body);
		String confirmButtonText;
		if (question) {
			final TextButton cancelButton = new TextButton("No", Assets.getSkin());
			cancelButton.addListener(new ClickListener() {
				@Override
				public void clicked(InputEvent event, float x, float y) {
					applicationDialog.hide(null);
					if (dialogAction != null) {
						dialogAction.cancel();
					}
				}
			});
			applicationDialog.getButtonTable().add(cancelButton).space(20);
			confirmButtonText = "Yes";
		} else {
			confirmButtonText = "OK";
		}
		final TextButton confirmButton = new TextButton(confirmButtonText, Assets.getSkin());
		confirmButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				applicationDialog.hide(null);
				if (dialogAction != null) {
					dialogAction.confirm();
				}
			}
		});
		applicationDialog.getButtonTable().add(confirmButton).space(20);
		/*
		 * The second argument is the action the dialog should perform as it appears, such as fading in. As it's set to null it won't
		 * perform any action.
		 */
		applicationDialog.show(mainStage, null);
		/*
		 * Positions in libGDX are relative to the bottom-left corner of an object. This positions the object so it's at the center of the
		 * screen.
		 */
		applicationDialog.setPosition((Gdx.graphics.getWidth() / 2) - (applicationDialog.getWidth() / 2), (Gdx.graphics.getHeight() / 2) - (applicationDialog.getHeight() / 2));
	}
	private void setApplicationDialogContents(String title, String body) {
		applicationDialog.getContentTable().clear();
		applicationDialog.getButtonTable().clear();
		applicationDialog.getTitleLabel().setText(title);
		final Label bodyLabel = new Label(body, Assets.getSkin());
		//Check if the dialog will look too wide.
		if (bodyLabel.getWidth() >= 500) {
			bodyLabel.setWrap(true);
			applicationDialog.getContentTable().add(bodyLabel).width(500).pad(10);
		} else {
			applicationDialog.getContentTable().add(bodyLabel).pad(10);
		}
	}
	public void hideApplicationDialog() {
		applicationDialog.hide(null);
	}
	public void changeState(ApplicationState newState) {
		states[currentState.ordinal()].hide();
		currentState = newState;
		states[currentState.ordinal()].show();
	}
	public boolean isKeysDown(boolean strict, Integer... keys) {
		//If strict is true then keysDown must only contain the elements otherwise the function returns false.
		if (strict) {
			return keysDown.size() == Arrays.asList(keys).size() && keysDown.containsAll(Arrays.asList(keys));
		} else {
			return keysDown.containsAll(Arrays.asList(keys));
		}
	}
	/**
	 * Draw the user's flowchart with a transparent background and black edges, ignoring the UI. This is so screenshots don't use too much
	 * ink if they're printed out.
	 */
	public void prepareScreenshot() {
		flowchartShapeRenderer.setProjectionMatrix(flowchartStage.getCamera().combined);
		Gdx.gl.glClearColor(1, 1, 1, 0);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		drawFlowchart(true);
		flowchartStage.draw();
	}
	@Override
	public void resize(int width, int height) {
		for (State state : states) {
			state.resize(width, height);
		}
		mainStage.getViewport().update(width, height, true);
		flowchartStage.getViewport().update(width, height, true);
	}
	@Override
	public void dispose() {
		for (State state : states) {
			state.dispose();
		}
		flowchartShapeRenderer.dispose();
		flowchartStage.dispose();
		mainStage.dispose();
		Assets.dispose();
	}
	public Stage getMainStage() {
		return mainStage;
	}
	public Stage getFlowchartStage() {
		return flowchartStage;
	}
	public ShapeRenderer getFlowchartShapeRenderer() {
		return flowchartShapeRenderer;
	}
	public static String getProperty(String key) {
		return PROPERTIES.getProperty(key);
	}
	public static String getFileSeparator() {
		return FILE_SEPARATOR;
	}
	public enum ApplicationState {
		MENU, LEARN, PRACTICE, TRY_QUESTION, ADD_QUESTION
	}
}