package com.ben.logicflow.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ScreenUtils;
import com.ben.logicflow.Application;
import com.ben.logicflow.Assets;
import com.ben.logicflow.DialogAction;
import com.ben.logicflow.flowchart.FlowchartController.ControllerState;
import com.ben.logicflow.flowchart.PracticeController;
import com.ben.logicflow.flowchart.model.FlowchartModel;
import com.ben.logicflow.flowchart.view.VertexView;

import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class PracticeState extends FlowchartState {
	private final PracticeController practiceController;
	private String fileName = "";
	//executionDialog stores the current state execution is in, such as if it's awaiting user input.
	private boolean executionDialog;
	public PracticeState(Application application) {
		super(application, true);
		practiceController = new PracticeController(this, application.getFlowchartStage(), application.getFlowchartShapeRenderer());
		setController(practiceController);
		initialiseUserInterface();
		hide();
	}
	private void initialiseUserInterface() {
		initialiseFileSelectBox();
		initialiseExecuteSelectBox();
		initialiseViewSelectBox();
		getUserInterfaceTable().add(getInformationLabel()).expandX().pad(0, 0, 0, 10).right();
	}
	private void initialiseFileSelectBox() {
		final SelectBox<String> selectBox = new SelectBox<>(Assets.getSkin(), "menu-bar");
		selectBox.setItems("New", "Save", "Save As", "Load", "Screenshot", "Close");
		selectBox.getList().addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				if (practiceController.getState() == ControllerState.AVAILABLE) {
					switch (selectBox.getSelected()) {
						case "New":
							newFlowchart();
							break;
						case "Save":
							save();
							break;
						case "Save As":
							showSaveAsDialog();
							break;
						case "Load":
							listFlowcharts();
							break;
						case "Screenshot":
							takeScreenshot();
							break;
						case "Close":
							close();
							break;
					}
				}
			}
			@Override
			public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
				super.touchUp(event, x, y, pointer, button);
				selectBox.getSelection().set("File");
			}
		});
		addMenuBarSelectBox(selectBox, "File");
	}
	private void initialiseExecuteSelectBox() {
		final SelectBox<String> selectBox = new SelectBox<>(Assets.getSkin(), "menu-bar");
		selectBox.setItems("Run", "Step through");
		selectBox.getList().addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				if (practiceController.getState() == ControllerState.AVAILABLE) {
					switch (selectBox.getSelected()) {
						case "Run":
							practiceController.execute(false);
							break;
						case "Step through":
							practiceController.execute(true);
							break;
					}
				}
			}
			@Override
			public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
				super.touchUp(event, x, y, pointer, button);
				selectBox.getSelection().set("Execute");
			}
		});
		addMenuBarSelectBox(selectBox, "Execute");
	}
	public void showInputDialog(final String message, final String variable, final VertexView nextVertex) {
		clearFlowchartDialog();
		getFlowchartDialog().getTitleLabel().setText(message);
		final TextField textField = new TextField("", Assets.getSkin());
		textField.setMaxLength(15);
		textField.setFocusTraversal(false);
		getFlowchartDialog().getContentTable().add(textField).width(70).pad(10);
		final TextButton confirmButton = new TextButton("OK", Assets.getSkin());
		confirmButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				getFlowchartDialog().hide(null);
				final DialogAction dialogAction = new DialogAction() {
					@Override
					public void confirm() {
						showInputDialog(message, variable, nextVertex);
					}
				};
				if (textField.getText().isEmpty()) {
					getApplication().showApplicationDialog("Error", "Blank mandatory field.", false, dialogAction);
					return;
				} else {
					try {
						practiceController.setVariable(variable, Double.parseDouble(textField.getText()));
					} catch (NumberFormatException ignored) {
						getApplication().showApplicationDialog("Error", "Only numerical values are allowed.", false, dialogAction);
						return;
					} catch (FlowchartModel.VariableNameException exception) {
						showFatalExecutionErrorMessage("Invalid variable identifier.");
						return;
					} catch (FlowchartModel.VariableValueException exception) {
						showFatalExecutionErrorMessage("Variable/array element has no associated value.");
						return;
					}
				}
				executionDialog = false;
				practiceController.execute(nextVertex, false);
			}
		});
		getFlowchartDialog().getButtonTable().add(confirmButton).space(20);
		executionDialog = true;
		showFlowchartDialog(false);
		getApplication().getMainStage().setKeyboardFocus(textField);
	}
	public void showOutputDialog(String message, String variable, final VertexView nextVertex) throws FlowchartModel.VariableValueException, FlowchartModel.VariableNameException {
		clearFlowchartDialog();
		if (variable == null) {
			getFlowchartDialog().getTitleLabel().setText("OUTPUT");
		} else {
			getFlowchartDialog().getTitleLabel().setText(message);
		}
		final Label label = new Label("", Assets.getSkin());
		if (variable == null) {
			label.setText(message);
		} else {
			label.setText(String.valueOf(practiceController.getVariable(variable)));
		}
		getFlowchartDialog().getContentTable().add(label).pad(10);
		final TextButton confirmButton = new TextButton("OK", Assets.getSkin());
		confirmButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				getFlowchartDialog().hide(null);
				executionDialog = false;
				practiceController.execute(nextVertex, false);
			}
		});
		getFlowchartDialog().getButtonTable().add(confirmButton).space(20);
		executionDialog = true;
		showFlowchartDialog(false);
	}
	public void showExecutionErrorMessage(String body, final VertexView nextVertex, final VertexView previousVertex) {
		getApplication().showApplicationDialog("Error", body, false, new DialogAction() {
			@Override
			public void confirm() {
				executionDialog = false;
				previousVertex.setDebug(false);
				practiceController.execute(nextVertex, false);
			}
		});
		executionDialog = true;
	}
	protected void newFlowchart() {
		super.newFlowchart();
		fileName = "";
	}
	private void save() {
		if (practiceController.isFlowchartEmpty()) {
			showFlowchartMessage("Error", "Empty flowcharts can't be saved.");
		} else if (fileName.isEmpty()) {
			showSaveAsDialog();
		} else {
			practiceController.save(fileName);
		}
	}
	private void showSaveAsDialog() {
		if (practiceController.isFlowchartEmpty()) {
			showFlowchartMessage("Error", "Empty flowcharts can't be saved.");
		} else {
			clearFlowchartDialog();
			getFlowchartDialog().getTitleLabel().setText("Save Flowchart");
			final TextField textField = new TextField(fileName, Assets.getSkin());
			textField.setMaxLength(100);
			textField.setMessageText("Flowchart name");
			getFlowchartDialog().getContentTable().add(textField).pad(10);
			getFlowchartDialog().getButtonTable().add(getCancelButton()).space(20);
			final TextButton confirmButton = new TextButton("OK", Assets.getSkin());
			confirmButton.addListener(new ClickListener() {
				@Override
				public void clicked(InputEvent event, float x, float y) {
					getFlowchartDialog().hide(null);
					if (textField.getText().isEmpty()) {
						getApplication().showApplicationDialog("Error", "Blank mandatory field.", false, new DialogAction() {
							@Override
							public void confirm() {
								showSaveAsDialog();
							}
						});
					} else {
						fileName = textField.getText();
						try {
							if (showConflictDialog()) {
								fileName = "";
							} else {
								practiceController.save(fileName);
								showFlowchartMessage("File", "Flowchart saved in the flowchart directory.");
							}
						} catch (GdxRuntimeException exception) {
							showFlowchartMessage("Error", "The flowchart directory is protected or in use.");
						}
					}
				}
			});
			getFlowchartDialog().getButtonTable().add(confirmButton).space(20);
			showFlowchartDialog(true);
		}
	}
	private boolean showConflictDialog() {
		final FileHandle fileHandle = new FileHandle(Application.getProperty("FlowchartDirectory"));
		for (FileHandle currentFileHandle : fileHandle.list()) {
			if (currentFileHandle.name().toLowerCase().equals(fileName.toLowerCase() + ".xml")) {
				getApplication().showApplicationDialog("File", "A flowchart already exists with the same name, overwrite file?", true, new DialogAction() {
					@Override
					public void confirm() {
						practiceController.save(fileName);
					}
					@Override
					public void cancel() {
						practiceController.setState(ControllerState.AVAILABLE);
					}
				});
				return true;
			}
		}
		return false;
	}
	private void listFlowcharts() {
		try {
			FileHandle fileHandle = new FileHandle(Application.getProperty("FlowchartDirectory"));
			if (fileHandle.list().length > 0) {
				final Array<String> flowchartNames = new Array<>();
				for (FileHandle currentFileHandle : fileHandle.list()) {
					if (currentFileHandle.nameWithoutExtension().length() > 0 && currentFileHandle.nameWithoutExtension().length() < 101 && currentFileHandle.extension().equals("xml")) {
						flowchartNames.add(currentFileHandle.nameWithoutExtension());
						//Only load the first 375 valid flowchart files so the list doesn't get too large.
						if (flowchartNames.size == 375) {
							break;
						}
					}
				}
				if (flowchartNames.size > 0) {
					showLoadDialog(flowchartNames);
					return;
				}
			}
			/*
			 * libGDX only throws a runtime exception related to file handling when a write operation fails. If the program failed to read
			 * from a directory it wouldn't throw an exception. To make sure an exception is thrown when a directory can't be accessed, a
			 * test file is written to the directory and deleted straight after. If flowchartNames' size is more than 0, the program
			 * almost certainly successfully accessed the directory, so the check is only performed if no flowcharts are found.
			 */
			fileHandle = new FileHandle(Application.getProperty("FlowchartDirectory") + Application.getFileSeparator() + "ignore.txt");
			fileHandle.writeString("Ignore.", false);
			fileHandle.delete();
		} catch (GdxRuntimeException exception) {
			showFlowchartMessage("Error", "The flowchart directory is protected or in use.");
			return;
		}
		showFlowchartMessage("Load Flowchart", "No existing flowcharts.");
	}
	private void showLoadDialog(Array<String> flowchartNames) {
		clearFlowchartDialog();
		getFlowchartDialog().getTitleLabel().setText("Load Flowchart");
		final SelectBox<String> selectBox = new SelectBox<>(Assets.getSkin());
		//The maximum amount of items the list displays. The user has to scroll to see the rest of the items.
		selectBox.setMaxListCount(10);
		selectBox.setItems(flowchartNames);
		getFlowchartDialog().getContentTable().add(selectBox).pad(10);
		getFlowchartDialog().getButtonTable().add(getCancelButton()).space(20);
		final TextButton confirmButton = new TextButton("OK", Assets.getSkin());
		confirmButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				getFlowchartDialog().hide(null);
				if (practiceController.load(selectBox.getSelected())) {
					fileName = selectBox.getSelected();
					setInformationText(fileName + " loaded.");
				} else {
					showFlowchartMessage("Error", "The flowchart's file can't be accessed.");
				}
			}
		});
		getFlowchartDialog().getButtonTable().add(confirmButton).space(20);
		showFlowchartDialog(true);
	}
	//TODO fix screenshot bounds MINOR bug
	private void takeScreenshot() {
		//getFileSeparator() used in dateFormat to use the date as the screenshot's save location and name.
		final DateFormat dateFormat = new SimpleDateFormat("yyyy" + Application.getFileSeparator() + "MM" + Application.getFileSeparator() + "dd" + Application.getFileSeparator() + "SSS" + "ss" + "mm" + "HH");
		final Date date = new Date();
		final FileHandle fileHandle = new FileHandle(Application.getProperty("ScreenshotDirectory") + Application.getFileSeparator() + dateFormat.format(date) + ".png");
		//Set the colour of the start vertex's label to black so it's visible on a white background.
		practiceController.invertLabelColour();
		//pixmap stores the pixels obtained from taking multiple screenshots of the flowchart at different positions.
		final Pixmap pixmap = new Pixmap(Gdx.graphics.getWidth() * 4, Gdx.graphics.getHeight() * 4, Pixmap.Format.RGBA8888);
		/*
		 * Store the position of the camera prior to taking the screenshot as the camera will need to move in order to screenshot a large
		 * area. The camera is initially positioned at the center of the screen, getViewOffsetX/Y() is the distance from that position.
		 * This is taken into account when calculating the camera's current position.
		 */
		final float originalX = getFlowchartView().getViewOffsetX() + (Gdx.graphics.getWidth() / 2);
		final float originalY = getFlowchartView().getViewOffsetY() + (Gdx.graphics.getHeight() / 2);
		//Move the camera getWidth() pixels horizontally every time a column of screenshots is taken.
		for (int i = Gdx.graphics.getWidth() * -2; i <= Gdx.graphics.getWidth() * 2; i += Gdx.graphics.getWidth()) {
			//Move the camera getHeight() pixels vertically every time a screenshot is taken.
			for (int j = Gdx.graphics.getHeight() * 2; j >= Gdx.graphics.getHeight() * -2; j -= Gdx.graphics.getHeight()) {
				getFlowchartView().positionCamera(i, j);
				getApplication().prepareScreenshot();
				/*
				 * Fill pixmap with screenshots starting from the top-left corner of the flowchart. The camera moves vertically opposite
				 * to the direction the pixmap is filled. In other words screenshots taken at the top of the flowchart are stored at the
				 * bottom of the pixmap.
				 */
				pixmap.drawPixmap(captureArea(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight()), Gdx.graphics.getWidth() + i, Gdx.graphics.getHeight() - j);
			}
		}
		getFlowchartView().positionCamera(originalX, originalY);
		practiceController.invertLabelColour();
		try {
			PixmapIO.writePNG(fileHandle, pixmap);
			showFlowchartMessage("File", "Screenshot saved in the screenshot directory.");
		} catch (GdxRuntimeException exception) {
			showFlowchartMessage("Error", "The screenshot directory is protected or in use.");
		}
		pixmap.dispose();
	}
	//Code copied from a page on libGDX's GitHub wiki.
	private Pixmap captureArea(int x, int y, int width, int height) {
		final Pixmap pixmap = ScreenUtils.getFrameBufferPixmap(x, y, width, height);
		final ByteBuffer pixels = pixmap.getPixels();
		final int numBytes = width * height * 4;
		final byte[] lines = new byte[numBytes];
		final int numBytesPerLine = width * 4;
		for (int i = 0; i < height; i++) {
			pixels.position((height - i - 1) * numBytesPerLine);
			pixels.get(lines, i * numBytesPerLine, numBytesPerLine);
		}
		pixels.clear();
		pixels.put(lines);
		return pixmap;
	}
	@Override
	public void keyPressed() {
		super.keyPressed();
		if (getFlowchartView().isVisible() && noFocus()) {
			if (practiceController.getState() == ControllerState.AVAILABLE) {
				if (getApplication().isKeysDown(true, Keys.F9)) {
					//Step through.
					practiceController.execute(true);
				} else if (getApplication().isKeysDown(true, Keys.F10)) {
					//Run
					practiceController.execute(false);
				} else if (getApplication().isKeysDown(true, Keys.CONTROL_LEFT, Keys.S) || getApplication().isKeysDown(true, Keys.CONTROL_RIGHT, Keys.S)) {
					save();
				} else if (getApplication().isKeysDown(true, Keys.CONTROL_LEFT, Keys.ALT_LEFT, Keys.S) || getApplication().isKeysDown(true, Keys.CONTROL_RIGHT, Keys.ALT_RIGHT, Keys.S) || getApplication().isKeysDown(true, Keys.CONTROL_LEFT, Keys.ALT_RIGHT, Keys.S) || getApplication().isKeysDown(true, Keys.CONTROL_RIGHT, Keys.ALT_LEFT, Keys.S)) {
					showSaveAsDialog();
				} else if (getApplication().isKeysDown(true, Keys.CONTROL_LEFT, Keys.L) || getApplication().isKeysDown(true, Keys.CONTROL_RIGHT, Keys.L)) {
					listFlowcharts();
				}
			} else if (practiceController.getState() == ControllerState.EXECUTE && getApplication().isKeysDown(true, Keys.SPACE) && !executionDialog) {
				practiceController.stepAndExecute();
			}
		}
	}
	public void setExecutionDialog(boolean executionDialog) {
		this.executionDialog = executionDialog;
	}
}