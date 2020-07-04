package org.juane.platedetect.controller.interfaces;

import javafx.event.ActionEvent;

public interface IMainFrame {
	public void openAction(ActionEvent actionEvent);

	public void closeAction(ActionEvent actionEvent);

	public void videoModeAction(ActionEvent actionEvent);

	public void imageModeAction(ActionEvent actionEvent);

	public void aboutAction(ActionEvent actionEvent);
}
