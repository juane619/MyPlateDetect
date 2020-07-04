package org.juane.platedetect.controller;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.juane.platedetect.utils.Utils;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class MainFrameBackupController {
	@FXML
	public Button startCameraButton;

	@FXML
	public ImageView currentFrameImageView;

	private ScheduledExecutorService timer;
	private final VideoCapture videoCapture = new VideoCapture();
	// a flag to change the button behavior
	private boolean cameraActive = false;
	// the id of the camera to be used
	private static int cameraId = 0;

	@FXML
	public void startCamera(final ActionEvent event) {
		Logger.getGlobal().warning("Start camera");

		if (!cameraActive) {
			// start the video capture
			videoCapture.open(cameraId);

			if (videoCapture.isOpened()) {
				cameraActive = true;

				// grab a frame every 33 ms (30 frames/sec)
				final Runnable frameGrabber = new Runnable() {

					@Override
					public void run() {
						// effectively grab and process a single frame
						final Mat frame = grabFrame();
						// convert and show the frame
						final Image imageToShow = Utils.mat2Image(frame);
						updateImageView(currentFrameImageView, imageToShow);
					}
				};

				timer = Executors.newSingleThreadScheduledExecutor();
				timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);

				// update the button content
				startCameraButton.setText("Stop Camera");
			} else {
				// log the error
				Logger.getGlobal().warning("Impossible to open the camera connection...");
			}
		} else {
			// the camera is not active at this point
			cameraActive = false;
			// update again the button content
			startCameraButton.setText("Start Camera");

			// stop the timer
			this.stopAcquisition();
		}
	}

	/**
	 * Get a frame from the opened video stream (if any)
	 *
	 * @return the {@link Mat} to show
	 */
	private Mat grabFrame() {
		// init everything
		final Mat frame = new Mat();

		// check if the capture is open
		if (videoCapture.isOpened()) {
			try {
				// read the current frame
				videoCapture.read(frame);

				// if the frame is not empty, process it
				if (!frame.empty()) {
					// Imgproc.cvtColor(frame, frame, Imgproc.COLOR_BGR2GRAY);
				}

			} catch (final Exception e) {
				// log the error
				Logger.getGlobal().warning("Exception during the image elaboration: " + e);
			}
		}

		return frame;
	}

	/**
	 * Stop the acquisition from the camera and release all the resources
	 */
	private void stopAcquisition() {
		if (timer != null && !timer.isShutdown()) {
			try {
				// stop the timer
				timer.shutdown();
				timer.awaitTermination(33, TimeUnit.MILLISECONDS);
			} catch (final InterruptedException e) {
				// log any exception
				Logger.getGlobal()
						.warning("Exception in stopping the frame capture, trying to release the camera now... " + e);
			}
		}

		if (videoCapture.isOpened()) {
			// release the camera
			videoCapture.release();
		}
	}

	/**
	 * Update the {@link ImageView} in the JavaFX main thread
	 *
	 * @param view the {@link ImageView} to update
	 * @param image the {@link Image} to show
	 */
	private void updateImageView(final ImageView view, final Image image) {
		Utils.onFXThread(view.imageProperty(), image);
	}

	/**
	 * On application close, stop the acquisition from the camera
	 */
	public void setClosed() {
		this.stopAcquisition();
	}

}
