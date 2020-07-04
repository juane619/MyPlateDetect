package org.juane.platedetect.manager;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.juane.platedetect.model.Filters;
import org.juane.platedetect.utils.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class VideoManager {
	private final Logger LOG = Logger.getGlobal();

	private String filepath;
	final ImageView view;

	private VideoCapture videoCapture;
	private ScheduledExecutorService timer;
	// a flag to change the button behavior
	private boolean isPlaying = false;
	private boolean isPaused = false;
	private boolean firstLoad = false;

	// filters
	private boolean plateDetection = false;
	private boolean grayScale = false;
	private boolean faceHaarClassifier = false;
	private boolean faceLbpClassifier = false;

	private ObjectDetectorManager objectDetectorManager = null;;

	public VideoManager(final File file, final ImageView view, final Filters filters)
			throws ExceptionInInitializerError {
		if (file != null && file.exists()) {
			filepath = file.getAbsolutePath();
			videoCapture = new VideoCapture(filepath);
			this.view = view;
			objectDetectorManager = new ObjectDetectorManager(filters, view);
		} else {
			throw new ExceptionInInitializerError();
		}
	}

	public void firstLoad() {
		if (!firstLoad && videoCapture != null) {
			videoCapture.open(filepath);

			if (videoCapture.isOpened()) {
				firstLoad = true;
				final Mat frame = grabFrame();
				final Image imageToShow = Utils.mat2Image(frame);
				updateImageView(imageToShow);
			}
		}
	}

	public void play() {
		if (videoCapture != null) {
			if (!isPlaying) {

				videoCapture.open(filepath);
				if (videoCapture.isOpened()) {
					isPlaying = true;
					firstLoad = false;

					// grab a frame every 33 ms (30 frames/sec)
					final Runnable frameGrabber = new Runnable() {

						@Override
						public void run() {
							if (!isPaused) {
								// effectively grab and process a single frame
								final Mat frame = grabFrame();

								// APPLY FILTERS OR DETECTORS
								final Mat processedFrame = processFrame(frame);

								// show the frame
								final Image imageToShow = Utils.mat2Image(processedFrame);
								updateImageView(imageToShow);
								final String prueba = "e";
								final int i = 1;
							}
						}
					};

					timer = Executors.newSingleThreadScheduledExecutor();
					timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);

				} else {
					// log the error
					LOG.info("Impossible to open the camera connection...");
				}
			} else {
				if (isPaused) {
					isPaused = false;
				}
			}
		}
	}

	public void pause() {
		if (isPlaying && !isPaused) {
			isPaused = true;
		}
	}

	public void stop() {
		if (timer != null && !timer.isShutdown()) {
			try {
				// stop the timer
				timer.shutdown();
				timer.awaitTermination(33, TimeUnit.MILLISECONDS);
			} catch (final InterruptedException e) {
				// log any exception
				LOG.info("Exception in stopping the frame capture, trying to release the camera now... " + e);
			}

			isPlaying = false;
		}

		firstLoad();
	}

	/**
	 * Stop the acquisition from the camera and release all the resources
	 */
	public void stopAcquisition() {
		stop();

		if (isPlaying || videoCapture.isOpened()) {
			// release the camera
			videoCapture.release();
			isPlaying = false;
		}

		updateImageView(null);
	}

	public void updatePlateDetection(final boolean plateDetect) {
		plateDetection = plateDetect;
	}

	public void updateGrayScale(final boolean grayScale) {
		this.grayScale = grayScale;
	}

	public void updateFaceHaar(final boolean haarClassifier) {
		faceHaarClassifier = haarClassifier;
	}

	public void updateFaceLBP(final boolean lbpClassifier) {
		faceLbpClassifier = lbpClassifier;
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
			} catch (final Exception e) {
				// log the error
				LOG.info("Exception during the image elaboration: " + e);
			}
		}

		return frame;
	}

	private Mat processFrame(final Mat frame) {
		if (!frame.empty()) {
			Mat newFrame = null;
			if (grayScale) {
				Imgproc.cvtColor(frame, frame, Imgproc.COLOR_BGR2GRAY);
			}
			if (plateDetection) {
				newFrame = objectDetectorManager.detectPlate(frame);
				return newFrame;
			}
			if (faceHaarClassifier) {
				objectDetectorManager.detectFaceByHaar(frame);
			} else if (faceLbpClassifier) {
				objectDetectorManager.detectFaceByLbp(frame);
			}
		}
		return frame;
	}

	/**
	 * Update the {@link ImageView} in the JavaFX main thread
	 *
	 * @param view the {@link ImageView} to update
	 * @param image the {@link Image} to show
	 */
	private void updateImageView(final Image image) {
		Utils.onFXThread(view.imageProperty(), image);
	}
}
