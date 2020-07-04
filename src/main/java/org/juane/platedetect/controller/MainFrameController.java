package org.juane.platedetect.controller;

import java.io.File;
import java.util.logging.Logger;

import org.juane.platedetect.controller.interfaces.IMainFrame;
import org.juane.platedetect.controller.interfaces.IVideoFrame;
import org.juane.platedetect.manager.VideoManager;
import org.juane.platedetect.model.Filters;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

public class MainFrameController implements IMainFrame, IVideoFrame {
	Logger LOG = Logger.getGlobal();

	@FXML
	public Label fileLabel;
	@FXML
	public ButtonBar videoControlsButtonBar;
	@FXML
	public Pane mainPane;
	@FXML
	public ImageView currentFrameImageView;
	@FXML
	public Label mouseLocationLabel;
	@FXML
	public CheckBox plateDetectCheckbox;
	@FXML
	public VBox filterControlsVBox;
	@FXML
	public Slider minThresoldSlider;
	@FXML
	public Slider maxThresoldSlider;
	@FXML
	public TextField blurKernelTextField;
	@FXML
	public CheckBox sobelKernelCheckbox;
	@FXML
	public TextField sobelKernelTextField;
	@FXML
	public TextField sigmaTextField;
	@FXML
	public TextField erosionSizeTextField;
	@FXML
	public TextField dilationSizeTextField;
	@FXML
	public ColorPicker minColorPicker;
	@FXML
	public ColorPicker maxColorPicker;

	final FileChooser fileChooser = new FileChooser();
	private String fileLoaded;
	private VideoManager videoManager;
	private final Filters filters = new Filters();

	public void init() {
		// set a fixed width for the frame
		currentFrameImageView.setFitWidth(600);
		// preserve image ratio
		currentFrameImageView.setPreserveRatio(true);

		fileChooser.setInitialDirectory(new File("C:\\Users\\juane\\eclipse-workspace\\platedetect\\assets"));

		minThresoldSlider.setOnMouseReleased(new EventHandler<MouseEvent>() {

			@Override
			public void handle(final MouseEvent event) {
				filtersChanged(null);
			}
		});

		maxThresoldSlider.setOnMouseReleased(new EventHandler<MouseEvent>() {

			@Override
			public void handle(final MouseEvent event) {
				filtersChanged(null);
			}
		});

		blurKernelTextField.focusedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(final ObservableValue<? extends Boolean> observable, final Boolean oldValue,
					final Boolean newValue) {
				if (!newValue) {
					filtersChanged(null);
				}
			}
		});

		sobelKernelTextField.focusedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(final ObservableValue<? extends Boolean> observable, final Boolean oldValue,
					final Boolean newValue) {
				if (!newValue) {
					filtersChanged(null);
				}
			}
		});

		sigmaTextField.focusedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(final ObservableValue<? extends Boolean> observable, final Boolean oldValue,
					final Boolean newValue) {
				if (!newValue) {
					filtersChanged(null);
				}
			}
		});

		erosionSizeTextField.focusedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(final ObservableValue<? extends Boolean> observable, final Boolean oldValue,
					final Boolean newValue) {
				if (!newValue) {
					filtersChanged(null);
				}
			}
		});

		dilationSizeTextField.focusedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(final ObservableValue<? extends Boolean> observable, final Boolean oldValue,
					final Boolean newValue) {
				if (!newValue) {
					filtersChanged(null);
				}
			}
		});

		currentFrameImageView.setOnMouseMoved(new EventHandler<MouseEvent>() {

			@Override
			public void handle(final MouseEvent event) {
				mouseLocationLabel
						.setText("X: " + Math.round(event.getScreenX()) + " Y: " + Math.round(event.getScreenY()));
			}
		});

		updateFilters();
	}

	//////
	// Main Frame Actions
	//////
	@Override
	public void openAction(final ActionEvent actionEvent) {
		LOG.info("Open button");

		fileChooser.setTitle("Open Resource File");

		final File file = fileChooser.showOpenDialog(mainPane.getScene().getWindow());
		if (file != null) {
			LOG.info(file.toString());

			fileLoaded = file.getName();

			if (videoManager != null) {
				videoManager.stopAcquisition();
				videoManager = null;
			}
			videoManager = new VideoManager(file, currentFrameImageView, filters);
			videoManager.firstLoad();

			// load current state of selected filters
			videoManager.updatePlateDetection(plateDetectCheckbox.isSelected());

			fileLoadedGUIAction(file);
		}
	}

	@Override
	public void closeAction(final ActionEvent actionEvent) {
		LOG.info("Close button");
		if (videoManager != null) {
			videoManager.stopAcquisition();
		}
		fileClosedGUIAction();
	}

	@Override
	public void videoModeAction(final ActionEvent actionEvent) {
		LOG.info("Video mode button");

	}

	@Override
	public void imageModeAction(final ActionEvent actionEvent) {
		LOG.info("Image mode button");

	}

	@Override
	public void aboutAction(final ActionEvent actionEvent) {
		LOG.info("About button");
	}

	//////
	// Video Frame Actions
	//////

	public void filtersChanged(final ActionEvent actionEvent) {
		updateFilters();
	}

	@Override
	public void detectPlateAction(final ActionEvent actionEvent) {
		LOG.info("Detect plate checkbox");

		videoManager.updatePlateDetection(plateDetectCheckbox.isSelected());
	}

	@Override
	public void playAction(final ActionEvent actionEvent) {
		LOG.info("play button");
		videoManager.play();
	}

	@Override
	public void pauseAction(final ActionEvent actionEvent) {
		LOG.info("pause button");
		videoManager.pause();
	}

	@Override
	public void stopAction(final ActionEvent actionEvent) {
		LOG.info("stop button");
		videoManager.stop();
	}

	@Override
	public void recordAction(final ActionEvent actionEvent) {
		LOG.info("record button");
	}

	////

	// GUI update
	private void fileLoadedGUIAction(final File file) {
		fileLabel.setText(fileLoaded);
		videoControlsButtonBar.setDisable(false);
		filterControlsVBox.setDisable(false);
	}

	private void updateFilters() {
		filters.updateFilters(minThresoldSlider.getValue(), maxThresoldSlider.getValue(),
				Integer.parseInt(blurKernelTextField.getText()), sobelKernelCheckbox.isSelected(),
				Integer.parseInt(sobelKernelTextField.getText()), Double.parseDouble(sigmaTextField.getText()),
				minColorPicker.getValue(), maxColorPicker.getValue(),
				Double.parseDouble(erosionSizeTextField.getText()),
				Double.parseDouble(dilationSizeTextField.getText()));
	}

	private void fileClosedGUIAction() {
		fileLoaded = "None";
		fileLabel.setText(fileLoaded);
		videoControlsButtonBar.setDisable(true);
		filterControlsVBox.setDisable(true);
	}

	//////
}
