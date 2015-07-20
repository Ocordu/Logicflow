package com.ben.logicflow.states;

import com.ben.logicflow.Application;

//Each instance of State represents the user interface of a section of the application.
public abstract class State {
	private final Application application;
	protected State(Application application) {
		this.application = application;
	}
	public void update(float delta) {
	}
	public void keyPressed() {
	}
	public void scrolled(int amount) {
	}
	public void show() {
	}
	public void hide() {
	}
	public void resize(int width, int height) {
	}
	public void dispose() {
	}
	protected Application getApplication() {
		return application;
	}
}