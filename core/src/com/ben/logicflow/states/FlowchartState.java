package com.ben.logicflow.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.ben.logicflow.Application;
import com.ben.logicflow.Application.ApplicationState;
import com.ben.logicflow.Assets;
import com.ben.logicflow.DialogAction;
import com.ben.logicflow.flowchart.FlowchartController;
import com.ben.logicflow.flowchart.FlowchartController.ControllerState;
import com.ben.logicflow.flowchart.VertexType;
import com.ben.logicflow.flowchart.view.ExecutableView;
import com.ben.logicflow.flowchart.view.FlowchartView;
import com.ben.logicflow.flowchart.view.FlowchartView.Direction;
import com.ben.logicflow.flowchart.view.VertexView;
import com.ben.logicflow.states.FlowchartStateListeners.EditListener;
import com.ben.logicflow.states.FlowchartStateListeners.MoveListener;
import com.ben.logicflow.states.FlowchartStateListeners.TextFieldHoverListener;
import com.ben.logicflow.states.FlowchartStateListeners.TextFieldStateListener;

import java.util.ArrayList;

//A class that's fully implemented by sections of the application that have a flowchart editor.
public abstract class FlowchartState extends State {
	/*
	 * FlowchartState and its subclasses manipulate their flowcharts' data via an instance of Controller so that data and representation
	 * are decoupled.
	 */
	private FlowchartController flowchartController;
	/*
	 * As FlowchartState and FlowchartView are responsible for the representation of data, they can have direct references to each other
	 * with hardly any consequences. They use the same library so if the graphics library was changed they'd both have to change anyway.
	 */
	private FlowchartView flowchartView;
	//Used in place of applicationDialog when something other than a message or question needs to be displayed.
	private final Dialog flowchartDialog = new Dialog("", Assets.getSkin());
	private final TextButton cancelButton = new TextButton("Cancel", Assets.getSkin());
	private final Table userInterfaceTable = new Table(Assets.getSkin());
	private final ArrayList<SelectBox> menuBarSelectBoxes = new ArrayList<>();
	private final Label informationLabel = new Label("", Assets.getSkin());
	private Image menuBarBackgroundImage;
	//currentVertex points to the vertex that is currently being used to edit the flowchart.
	private VertexView currentVertex;
	private final boolean allowIO;
	private boolean setTrueBranch;
	//panDelayTimer decrements by delta so that the camera always moves the same speed, independent of the frame rate.
	private float panDelayTimer;
	protected FlowchartState(Application application, boolean allowIO) {
		super(application);
		this.allowIO = allowIO;
		initialiseUserInterface();
	}
	private void initialiseUserInterface() {
		flowchartDialog.getTitleLabel().setAlignment(Align.center);
		cancelButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				getFlowchartDialog().hide(null);
				flowchartController.setState(ControllerState.AVAILABLE);
			}
		});
		userInterfaceTable.setFillParent(true);
		//Add items to the top-left corner of the table instead of the center.
		userInterfaceTable.top().left();
		menuBarBackgroundImage = new Image(Assets.getSkin().get("transparent-black", TextureRegion.class));
		menuBarBackgroundImage.setSize(Gdx.graphics.getWidth(), 55);
		menuBarBackgroundImage.setPosition(0, Gdx.graphics.getHeight() - 55);
		getApplication().getMainStage().addActor(menuBarBackgroundImage);
		informationLabel.setAlignment(Align.right);
		getApplication().getMainStage().addActor(userInterfaceTable);
	}
	protected void initialiseViewSelectBox() {
		final SelectBox<String> selectBox = new SelectBox<>(Assets.getSkin(), "menu-bar");
		selectBox.setItems("Toggle zoom", "Pan up", "Pan down", "Pan left", "Pan right");
		selectBox.getList().addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				if (flowchartController.getState() == ControllerState.AVAILABLE) {
					switch (selectBox.getSelected()) {
						case "Toggle zoom":
							if (getFlowchartView().zoomedOut()) {
								getFlowchartView().zoom(false);
							} else {
								getFlowchartView().zoom(true);
							}
							break;
						case "Pan up":
							getFlowchartView().moveCamera(FlowchartView.Direction.UP);
							break;
						case "Pan down":
							getFlowchartView().moveCamera(FlowchartView.Direction.DOWN);
							break;
						case "Pan left":
							getFlowchartView().moveCamera(FlowchartView.Direction.LEFT);
							break;
						case "Pan right":
							getFlowchartView().moveCamera(FlowchartView.Direction.RIGHT);
							break;
					}
				}
			}
			@Override
			public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
				super.touchUp(event, x, y, pointer, button);
				/*
		 		 * When the selection changes so does the text above the select box's list. This is changed back to the category initially
		 		 * given to the select box to prevent it from changing so it looks like a drop-down menu.
		 		 */
				selectBox.getSelection().set("View");
			}
		});
		addMenuBarSelectBox(selectBox, "View");
	}
	protected void addMenuBarSelectBox(SelectBox<String> selectBox, String category) {
		/*
		 * By default a select box displays its currently selected item above its list. To make certain select boxes look like drop-down
		 * menus, menu bar select boxes are set to display text such as 'File' rather than what's currently selected.
		 */
		selectBox.getSelection().set(category);
		menuBarSelectBoxes.add(selectBox);
		userInterfaceTable.add(selectBox).width(110).height(35).pad(10, 10, 10, 0);
	}
	@Override
	public void update(float delta) {
		if (flowchartView.isVisible()) {
			flowchartController.update(delta);
			panDelayTimer -= delta;
			/*
			 * Allow the camera to be moved using the arrow keys when there's no keyboard focus, the delay timer has run out and when the
			 * user is either connecting symbols or doing nothing.
			 */
			if (noFocus() && (flowchartController.getState() == ControllerState.AVAILABLE || flowchartController.getState() == ControllerState.CONNECT) && panDelayTimer <= 0) {
				final Array<Direction> directions = new Array<>();
				if (getApplication().isKeysDown(false, Keys.UP)) {
					directions.add(Direction.UP);
				}
				if (getApplication().isKeysDown(false, Keys.DOWN)) {
					directions.add(Direction.DOWN);
				}
				if (getApplication().isKeysDown(false, Keys.LEFT)) {
					directions.add(Direction.LEFT);
				}
				if (getApplication().isKeysDown(false, Keys.RIGHT)) {
					directions.add(Direction.RIGHT);
				}
				if (directions.size > 0) {
					/*
					 * The view always moves by a fixed amount but it can only do so when 0.01 seconds worth of frames has passed. This
					 * means it always moves the same speed, independent of frame rate.
					*/
					flowchartView.moveCamera(directions);
					panDelayTimer = 0.01f;
				}
			}
		}
	}
	public void drawFlowchart(boolean blackEdges) {
		if (flowchartView.isVisible()) {
			flowchartController.drawFlowchart(currentVertex, blackEdges);
		}
	}
	//Called when a new vertex is added to the flowchart.
	public void addVertexListeners(VertexView vertex) {
		vertex.addListener(new EditListener(this, flowchartController, vertex, Buttons.RIGHT));
		vertex.addListener(new EditListener(this, flowchartController, vertex, Buttons.LEFT));
		if (vertex.getVertexType() != VertexType.START) {
			vertex.addListener(new MoveListener(flowchartController, vertex));
			//Executable vertices have 3 text fields.
			for (int i = 0; i < 3; i++) {
				((ExecutableView) vertex).getTextField(i).setTextFieldListener(new TextFieldStateListener(flowchartController, (ExecutableView) vertex, i));
				((ExecutableView) vertex).getTextField(i).addListener(new TextFieldHoverListener(this, flowchartController, (ExecutableView) vertex));
			}
		}
	}
	void showEditDialog(VertexView vertex) {
		currentVertex = vertex;
		clearFlowchartDialog();
		flowchartDialog.getTitleLabel().setText("Edit Flowchart");
		final SelectBox<String> selectBox = new SelectBox<>(Assets.getSkin());
		switch (vertex.getVertexType()) {
			case START:
				selectBox.setItems("Add next symbol", "Link to symbol");
				break;
			case DECISION:
				selectBox.setItems("Add next symbol (true branch)", "Add next symbol (false branch)", "Link to symbol (true branch)", "Link to symbol (false branch)", "Remove symbol + true branch", "Remove symbol + false branch");
				break;
			default:
				selectBox.setItems("Add next symbol", "Link to symbol", "Remove symbol");
				break;
		}
		flowchartDialog.getContentTable().add(selectBox).pad(10);
		flowchartDialog.getButtonTable().add(cancelButton).space(20);
		final TextButton confirmButton = new TextButton("OK", Assets.getSkin());
		confirmButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				flowchartDialog.hide(null);
				switch (selectBox.getSelected()) {
					case "Add next symbol": case "Add next symbol (true branch)":
						showAddDialog(true);
						break;
					case "Add next symbol (false branch)":
						showAddDialog(false);
						break;
					case "Link to symbol": case "Link to symbol (true branch)":
						beginVertexLink(true);
						break;
					case "Link to symbol (false branch)":
						beginVertexLink(false);
						break;
					case "Remove symbol": case "Remove symbol + true branch":
						flowchartController.removeVertex(currentVertex, true);
						//Remove the last reference to current vertex to prevent memory leaks.
						currentVertex = null;
						break;
					case "Remove symbol + false branch":
						flowchartController.removeVertex(currentVertex, false);
						currentVertex = null;
						break;
				}
			}
		});
		flowchartDialog.getButtonTable().add(confirmButton).space(20);
		showFlowchartDialog(true);
	}
	private void showAddDialog(final boolean addTrueBranch) {
		clearFlowchartDialog();
		if (addTrueBranch) {
			flowchartDialog.getTitleLabel().setText("Add Next Symbol");
		} else {
			flowchartDialog.getTitleLabel().setText("Add Next False Symbol");
		}
		final SelectBox<String> selectBox = new SelectBox<>(Assets.getSkin());
		if (allowIO) {
			selectBox.setItems("Process", "Decision", "Input/output");
		} else {
			selectBox.setItems("Process", "Decision");
		}
		flowchartDialog.getContentTable().add(selectBox).pad(10);
		flowchartDialog.getButtonTable().add(cancelButton).space(20);
		final TextButton confirmButton = new TextButton("OK", Assets.getSkin());
		confirmButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				flowchartDialog.hide(null);
				VertexType vertexType = null;
				switch (selectBox.getSelected()) {
					case "Process":
						vertexType = VertexType.PROCESS;
						break;
					case "Decision":
						vertexType = VertexType.DECISION;
						break;
					case "Input/output":
						vertexType = VertexType.IO;
						break;
				}
				flowchartController.addVertex(vertexType, currentVertex, addTrueBranch, Gdx.graphics.getWidth() / 2, 100);
				flowchartController.setState(ControllerState.AVAILABLE);
			}
		});
		flowchartDialog.getButtonTable().add(confirmButton).space(20);
		showFlowchartDialog(false);
	}
	public void showFlowchartMessage(String title, String body) {
		getApplication().showApplicationDialog(title, body, false, new DialogAction() {
			@Override
			public void confirm() {
				flowchartController.setState(ControllerState.AVAILABLE);
			}
		});
		flowchartController.setState(ControllerState.DIALOG);
	}
	public void showFatalExecutionErrorMessage(String errorMessage) {
		getApplication().showApplicationDialog("Error", errorMessage, false, new DialogAction() {
			@Override
			public void confirm() {
				flowchartController.stopExecution();
			}
		});
	}
	void clearFlowchartDialog() {
		flowchartDialog.getContentTable().clear();
		flowchartDialog.getButtonTable().clear();
	}
	void showFlowchartDialog(boolean changeState) {
		flowchartDialog.show(getApplication().getMainStage(), null);
		flowchartDialog.setPosition((Gdx.graphics.getWidth() / 2) - (flowchartDialog.getWidth() / 2), (Gdx.graphics.getHeight() / 2) - (flowchartDialog.getHeight() / 2));
		/*
		 * In certain situations the controller needs to maintain its state in order to function properly, such as during execution when an
		 * output dialog appears. The flowchart is still being executed, it's just awaiting user input.
		 */
		if (changeState) {
			flowchartController.setState(ControllerState.DIALOG);
		}
	}
	private void beginVertexLink(boolean setTrueBranch) {
		this.setTrueBranch = setTrueBranch;
		hideMenuBarSelectBoxes();
		//Prevent the user from editing vertices while edge editing is enabled.
		flowchartView.disableVertexUI(false);
		setInformationText("Tip: click on another symbol to connect to it, click on the same symbol to remove the existing link or push escape to cancel.");
		flowchartController.setState(ControllerState.CONNECT);
	}
	protected void newFlowchart() {
		flowchartController.disposeAllVertices();
		flowchartView.reset();
		flowchartController.addStartVertex();
		setInformationText("Tip: click on the start symbol to begin editing the flowchart.");
	}
	@Override
	public void keyPressed() {
		if (flowchartView.isVisible() && noFocus() && getApplication().isKeysDown(true, Keys.ESCAPE)) {
			close();
		}
	}
	@Override
	public void scrolled(int amount) {
		if (flowchartView.isVisible() && flowchartController.getState() == ControllerState.AVAILABLE) {
			if (amount == 1) {
				//Zoom out.
				flowchartView.zoom(true);
			} else if (amount == -1) {
				//Zoom in.
				flowchartView.zoom(false);
			}
		}
	}
	protected boolean noFocus() {
		return getApplication().getFlowchartStage().getKeyboardFocus() == null;
	}
	protected void close() {
		if (flowchartController.getState() == ControllerState.AVAILABLE) {
			getApplication().showApplicationDialog("Quit", "Are you sure you want to quit?", true, new DialogAction() {
				@Override
				public void confirm() {
					newFlowchart();
					getApplication().changeState(ApplicationState.MENU);
				}
			});
		} else {
			flowchartDialog.hide(null);
			getApplication().hideApplicationDialog();
			switch (flowchartController.getState()) {
				case CONNECT:
					setInformationText("");
					flowchartView.enableVertexUI();
					showMenuBarSelectBoxes();
					break;
				case EXECUTE:
					flowchartController.stopExecution();
					break;
			}
			flowchartController.setState(ControllerState.AVAILABLE);
		}
	}
	@Override
	public void show() {
		flowchartController.setVisible(true);
		menuBarBackgroundImage.setVisible(true);
		userInterfaceTable.setVisible(true);
	}
	@Override
	public void hide() {
		flowchartDialog.hide(null);
		userInterfaceTable.setVisible(false);
		menuBarBackgroundImage.setVisible(false);
		flowchartController.setVisible(false);
	}
	public void showMenuBarSelectBoxes() {
		for (SelectBox selectBox : menuBarSelectBoxes) {
			selectBox.setVisible(true);
		}
	}
	public void hideMenuBarSelectBoxes() {
		for (SelectBox selectBox : menuBarSelectBoxes) {
			selectBox.setVisible(false);
		}
	}
	public void showMenuBarBackgroundImage() {
		menuBarBackgroundImage.setVisible(true);
	}
	public void hideMenuBarBackgroundImage() {
		menuBarBackgroundImage.setVisible(false);
	}
	protected FlowchartView getFlowchartView() {
		return flowchartView;
	}
	Dialog getFlowchartDialog() {
		return flowchartDialog;
	}
	TextButton getCancelButton() {
		return cancelButton;
	}
	protected Table getUserInterfaceTable() {
		return userInterfaceTable;
	}
	protected Label getInformationLabel() {
		return informationLabel;
	}
	VertexView getCurrentVertex() {
		return currentVertex;
	}
	public boolean isSetTrueBranch() {
		return setTrueBranch;
	}
	protected void setController(FlowchartController controller) {
		this.flowchartController = controller;
		flowchartView = controller.getView();
		newFlowchart();
	}
	public void setInformationText(String text) {
		informationLabel.setText(text);
	}
}