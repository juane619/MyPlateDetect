package org.juane.platedetect.controller;

import java.io.IOException;

import org.juane.platedetect.App;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class VideoFrameController {
	@FXML
	public Label videoLabel;

	@FXML
	public void switchToPrimary() throws IOException {
		App.setRoot("primary");
	}

	public void changeAction() {
		videoLabel.setText("PROBANDO");
	}
}