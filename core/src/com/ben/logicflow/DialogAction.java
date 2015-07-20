package com.ben.logicflow;

/*
 * An abstract class that will be implemented by anonymous inner classes. This is so the class 'Application' will be responsible for
 * creating dialogs while other classes will only be responsible for their behaviour. A lot cleaner than creating a dialog in every class
 * that needs one. This technique is commonly used in libraries that have event listeners.
 */
public abstract class DialogAction {
	public abstract void confirm();
	//Optional to implement, only needs to be called in question dialogs. This method will execute when the user clicks on the 'No' button.
	public void cancel() {
	}
}