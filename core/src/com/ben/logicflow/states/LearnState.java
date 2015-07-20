package com.ben.logicflow.states;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.ben.logicflow.Application;
import com.ben.logicflow.Application.ApplicationState;
import com.ben.logicflow.Assets;

public final class LearnState extends State {
	private final Table userInterfaceTable = new Table(Assets.getSkin());
	private final Label titleLabel = new Label("", Assets.getSkin(), "sub-title");
	private final Label informationLabel = new Label("", Assets.getSkin());
	private final Table informationTable = new Table(Assets.getSkin());
	private final TextButton backButton = new TextButton("", Assets.getSkin());
	private final TextButton nextButton = new TextButton("", Assets.getSkin());
	private Section currentSection = Section.ALGORITHMS;
	public LearnState(Application application) {
		super(application);
		initialiseUserInterface();
		hide();
	}
	private void initialiseUserInterface() {
		userInterfaceTable.setFillParent(true);
		userInterfaceTable.add(titleLabel).space(10).left().row();
		informationLabel.setWrap(true);
		userInterfaceTable.add(informationLabel).width(875).space(10, 10, 20, 10).row();
		userInterfaceTable.add(informationTable).row();
		backButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				switch (currentSection) {
					case ALGORITHMS:
						getApplication().changeState(ApplicationState.MENU);
						break;
					case CONTROL_FLOW:
						showAlgorithmsInformation();
						break;
					case FLOWCHARTS:
						showControlFlowInformation();
						break;
					case VARIABLES:
						showFlowchartsInformation();
						break;
				}
			}
		});
		//buttonTable is used so that userInterfaceTable only needs one column, making the buttons on the same row easier to position.
		final Table buttonTable = new Table(Assets.getSkin());
		buttonTable.add(backButton).width(80).height(40).space(120);
		nextButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				switch (currentSection) {
					case ALGORITHMS:
						showControlFlowInformation();
						break;
					case CONTROL_FLOW:
						showFlowchartsInformation();
						break;
					case FLOWCHARTS:
						showVariablesInformation();
						break;
					case VARIABLES:
						getApplication().changeState(ApplicationState.MENU);
						break;
				}
			}
		});
		buttonTable.add(nextButton).width(80).height(40).space(120);
		userInterfaceTable.add(buttonTable);
		getApplication().getMainStage().addActor(userInterfaceTable);
	}
	private void showAlgorithmsInformation() {
		titleLabel.setText("What is an algorithm?");
		informationLabel.setText("An algorithm is a clear set of instructions that solve a certain problem and is independent of any programming language. This means code written in programming languages are not algorithms but implementations of algorithms. A recipe is an example of an algorithm; recipes have clear instructions that are to be carried out one after the other and solve a certain problem, for example, a person not knowing how to cook pasta. Algorithms can be expressed in many different ways, in the case of a recipe it's standard English. The two most common ways to express algorithms in computer science are pseudocode and program flowcharts.");
		informationTable.clear();
		userInterfaceTable.getCell(informationTable).space(0);
		backButton.setText("Menu");
		nextButton.setText("Next");
		currentSection = Section.ALGORITHMS;
	}
	private void showControlFlowInformation() {
		titleLabel.setText("What is a control flow statement?");
		informationLabel.setText("The order in which instructions are followed, the conditions which determine which instructions are followed and the number of times instructions are followed are all important concepts in creating solutions to problems. These concepts are linked to sequence, selection and repetition respectively and are commonly used in algorithms. Concepts such as these, which affect how instructions are followed, are called control flow statements.");
		informationTable.clear();
		informationTable.add(new Label("Control Flow Statement", Assets.getSkin(), "small-sub-title")).space(20);
		informationTable.add(new Label("Example", Assets.getSkin(), "small-sub-title")).space(20);
		informationTable.add(new Label("Key Phrase", Assets.getSkin(), "small-sub-title")).space(20).left().row();
		informationTable.add("Sequence").space(20);
		informationTable.add("Unscrew the broken light bulb and then find another one of the same model.").space(20).left();
		informationTable.add("\"and then\"").space(20).left().row();
		informationTable.add("Selection").space(20);
		informationTable.add("Unscrew the light bulb if it's broken.").space(20).left();
		informationTable.add("\"if it's broken\"").space(20).left().row();
		informationTable.add("Repetition").space(20);
		informationTable.add("Unscrew the broken light bulb until it comes out.").space(20).left();
		informationTable.add("\"until it comes out\"").space(20).left();
		userInterfaceTable.getCell(informationTable).space(30);
		backButton.setText("Back");
		currentSection = Section.CONTROL_FLOW;
	}
	private void showFlowchartsInformation() {
		titleLabel.setText("What is a program flowchart?");
		informationLabel.setText("Program flowcharts are one of the many ways of expressing algorithms. Algorithms are represented graphically; symbols are linked together by arrows and arrowheads help determine the order in which symbols are followed. In other words, arrows are essentially equivalent to 'and then' from the light bulb example. Symbols usually contain text detailing what a particular symbol does. The text represents one or more actions while the symbol represents the type of action. As sequence, selection and repetition are essential to problem solving they can all be used in program flowcharts. Below a table defines symbols commonly used in program flowcharts, as repetition requires multiple symbols it isn't mentioned. Although input and output symbols have the same shape, the text inside them is what differentiates them.");
		informationTable.clear();
		informationTable.add(new Label("Symbol Name", Assets.getSkin(), "small-sub-title")).space(20, 60, 20, 60);
		informationTable.add(new Label("Symbol Shape", Assets.getSkin(), "small-sub-title")).space(20, 60, 20, 60);
		informationTable.add(new Label("Control Flow Statement", Assets.getSkin(), "small-sub-title")).space(20, 60, 20, 60);
		informationTable.add(new Label("Example of Symbol's Text", Assets.getSkin(), "small-sub-title")).space(20, 60, 20, 60).row();
		informationTable.add("Process").space(20, 60, 20, 60);
		informationTable.add("Wide rectangle").space(20, 60, 20, 60);
		informationTable.add("Sequence\n(if multiple statements inside symbol)").space(20, 60, 20, 60);
		informationTable.add("age <- 23").space(20, 60, 20, 60).row();
		informationTable.add("Decision").space(20, 60, 20, 60);
		informationTable.add("Wide diamond").space(20, 60, 20, 60);
		informationTable.add("Selection").space(20, 60, 20, 60);
		informationTable.add("age >= 18?").space(20, 60, 20, 60).row();
		informationTable.add("Input").space(20, 60, 20, 60);
		informationTable.add("Wide parallelogram").space(20, 60, 20, 60);
		informationTable.add("Sequence\n(if multiple statements inside symbol)").space(20, 60, 20, 60);
		informationTable.add("INPUT age").space(20, 60, 20, 60).row();
		informationTable.add("Output").space(20, 60, 20, 60);
		informationTable.add("Wide parallelogram").space(20, 60, 20, 60);
		informationTable.add("Sequence\n(if multiple statements inside symbol)").space(20, 60, 20, 60);
		informationTable.add("OUTPUT age * 12").space(20, 60, 20, 60);
		userInterfaceTable.getCell(informationTable).space(30);
		nextButton.setText("Next");
		currentSection = Section.FLOWCHARTS;
	}
	private void showVariablesInformation() {
		titleLabel.setText("What is a variable?");
		informationLabel.setText("A variable is a piece of data that holds a value that can change while an algorithm or program is running. Variables are identified by their names defined by the person who wrote the algorithm/code. In one of the last sections, 'broken' was an example of a variable. The value of 'broken' could change from false to true if the filament overheated or from true to false if the light bulb was fixed. This value could then determine if the light bulb could emit light.");
		informationTable.clear();
		userInterfaceTable.getCell(informationTable).space(0);
		nextButton.setText("Menu");
		currentSection = Section.VARIABLES;
	}
	@Override
	public void show() {
		showAlgorithmsInformation();
		userInterfaceTable.setVisible(true);
	}
	@Override
	public void hide() {
		userInterfaceTable.setVisible(false);
	}
	private enum Section {
		ALGORITHMS, CONTROL_FLOW, FLOWCHARTS, VARIABLES
	}
}