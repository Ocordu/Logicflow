package com.ben.logicflow.flowchart.view;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.ben.logicflow.Assets;

/**
 * Implemented by all symbols but the start symbol. Start symbols only point to other symbols while executable symbols execute the
 * statements input by the user.
 */
public abstract class ExecutableView extends VertexView {
	private final Table table = new Table(Assets.getSkin());
	private final TextField[] textFields = {new TextField("", Assets.getSkin()), new TextField("", Assets.getSkin()), new TextField("", Assets.getSkin())};
	ExecutableView(Stage stage) {
		super(stage);
		stage.addActor(table);
	}
	@Override
	public void dispose() {
		super.dispose();
		table.clear();
		table.remove();
		for (TextField textField : textFields) {
			textField.clear();
			textField.remove();
		}
	}
	void disableUI(boolean disableHighlighting) {
		super.disableUI(disableHighlighting);
		for (TextField textField : textFields) {
			textField.setDisabled(true);
		}
	}
	void enableUI() {
		super.enableUI();
		for (TextField textField : textFields) {
			textField.setDisabled(false);
		}
	}
	Table getTable() {
		return table;
	}
	public TextField getTextField(int index) {
		return textFields[index];
	}
	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		table.setVisible(visible);
	}
	@Override
	public void setPosition(float x, float y) {
		super.setPosition(x, y);
		table.setPosition(getX() + (getWidth() / 2), getY() + (getHeight() / 2));
	}
}