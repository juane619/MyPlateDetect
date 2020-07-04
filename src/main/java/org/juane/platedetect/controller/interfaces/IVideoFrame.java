package org.juane.platedetect.controller.interfaces;

import javafx.event.ActionEvent;

public interface IVideoFrame {
	public void detectPlateAction(ActionEvent actionEvent);

	public void playAction(ActionEvent actionEvent);

	public void pauseAction(ActionEvent actionEvent);

	public void stopAction(ActionEvent actionEvent);

	public void recordAction(ActionEvent actionEvent);
}
