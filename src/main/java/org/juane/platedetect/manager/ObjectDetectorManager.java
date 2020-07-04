package org.juane.platedetect.manager;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.juane.platedetect.model.Filters;
import org.juane.platedetect.utils.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;

import javafx.scene.image.ImageView;

public class ObjectDetectorManager {
	private final Logger LOG = Logger.getGlobal();

	private final CascadeClassifier faceCascade;
	private int absoluteFaceSize;

	private boolean haarClassifierLoaded = false;
	private boolean lbpClassifierLoaded = false;

	private Filters filters = null;
	private ImageView view = null;

	public ObjectDetectorManager(final Filters filters, final ImageView view) {
		faceCascade = new CascadeClassifier();
		absoluteFaceSize = 0;
		this.filters = filters;
		this.view = view;
	}

	public void setFilters(final Filters filters) {
		this.filters = filters;
	}

	public Mat detectPlate(final Mat frame) {
		final Mat imgProcessed = frame.clone();
		final Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT,
				new Size(filters.getErosionSize(), filters.getDilationSize()));

		// init
		// convert to grayscale
		final Mat grayImage = new Mat();
		Imgproc.cvtColor(imgProcessed, grayImage, Imgproc.COLOR_BGR2GRAY);

		final Mat erodeDilatedImg = new Mat();
		Imgproc.morphologyEx(grayImage, erodeDilatedImg, Imgproc.MORPH_TOPHAT, kernel);
		Utils.printImageWaiting(view, erodeDilatedImg, "Morf tophat");

		// Find vertical lines
		if (filters.isSobelKernel()) {
			Imgproc.Sobel(erodeDilatedImg, erodeDilatedImg, -1, 2, 0);
			Utils.printImageWaiting(view, erodeDilatedImg, "Sobel");
		}

		// reduce noise with a 3x3 kernel
		Imgproc.GaussianBlur(erodeDilatedImg, imgProcessed,
				new Size(filters.getBlurKernelSize(), filters.getBlurKernelSize()), 3, 3);
		Utils.printImageWaiting(view, imgProcessed, "Blurred");

		// do morphology (return rect to perform morph opers -> close == dilate + erode)
		Imgproc.morphologyEx(imgProcessed, erodeDilatedImg, Imgproc.MORPH_CLOSE, kernel);
		Utils.printImageWaiting(view, erodeDilatedImg, "Morph close");

		// threshold
		final Mat threshold = new Mat();
		Imgproc.threshold(erodeDilatedImg, threshold, filters.getMinThresold(), filters.getMaxThresold(),
				Imgproc.THRESH_OTSU);
		Utils.printImageWaiting(view, threshold, "Thresold");

		final List<MatOfPoint> contours = new ArrayList<>();
		final Mat hierarchy = new Mat();
		Imgproc.findContours(threshold, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE,
				new Point(0, 0));

		final Mat result = new Mat();
		frame.copyTo(result);
		Imgproc.drawContours(result, contours, -1, new Scalar(0, 0, 255), 2);
		System.out.println("Contours before: " + contours.size());
		Utils.printImageWaiting(view, result, "All contours");

		// process contours
		final List<MatOfPoint> squares = new ArrayList<>();
		processContours(contours, squares);
		// processContours(squares, contours); // to test
		System.out.println("Contours after: " + contours.size());

		Imgproc.drawContours(result, squares, -1, // draw all contours
				new Scalar(0, 255, 0), // in blue
				3); // with a thickness of 1

		return result;
	}

	private void processContours(final List<MatOfPoint> contours, final List<MatOfPoint> squares) {

		final MatOfPoint2f approx = new MatOfPoint2f();
		final Iterator it = contours.iterator();

		while (it.hasNext()) {
			final MatOfPoint elem = (MatOfPoint) it.next();

			final Point[] elemArray = elem.toArray();
			final MatOfPoint2f cnt = new MatOfPoint2f(elemArray);

			// final double peri = Imgproc.arcLength(cnt, true);
			// Imgproc.approxPolyDP(cnt, approx, peri * 0.02, true);
			// if (approx.total() == 4) {
			final RotatedRect rect = Imgproc.minAreaRect(cnt);

			if (!verifySizes(rect)) {
				it.remove();
			} else {
				// System.out.println("Angle: " + rect.angle);
				// System.out.println("Size: " + rect.size);
				// if (rect.angle < -10 && rect.angle > -85) {
				squares.add(elem);
				verifySizes(rect);
				// }
			}
			// final double areaRatio = Math
			// .abs(Imgproc.contourArea(contours.get(i)) / rect.size.width * rect.size.height);
			// if (areaRatio > .95) {
			// squares.add(contours.get(i));
			// }
		}

		// for (int i = 0; i < contours.size(); i++) {

		// }
		// }

		// final List<MatOfPoint> hullList = new ArrayList<>();
		//
		// for (final MatOfPoint cnt : squares) {
		// final MatOfInt hull = new MatOfInt();
		// Imgproc.convexHull(cnt, hull);
		// final Point[] contourArray = cnt.toArray();
		// final Point[] hullPoints = new Point[hull.rows()];
		// final List<Integer> hullContourIdxList = hull.toList();
		// for (int i = 0; i < hullContourIdxList.size(); i++) {
		// hullPoints[i] = contourArray[hullContourIdxList.get(i)];
		// }
		// hullList.add(new MatOfPoint(hullPoints));
		// }
		//
		//
	}

	private boolean verifySizes(final RotatedRect rect) {
		final double error = filters.getSigma();
		// Spain car plate size: 52x11 aspect 4,7272
		final double aspect = 4.7272;
		// Set a min and max area. All other patchs are discarded
		final int min = (int) (15 * aspect * 15); // minimum area
		final int max = (int) (125 * aspect * 125); // maximum area
		// Get only patchs that match to a respect ratio.
		final double rmin = aspect - aspect * error;
		final double rmax = aspect + aspect * error;

		final int area = (int) (rect.size.height * rect.size.width);
		double r = rect.size.width / rect.size.height;
		if (r < 1) {
			r = rect.size.height / rect.size.width;
		}

		if ((area < min || area > max) || (r < rmin || r > rmax)) {
			return false;
		} else {
			return true;
		}
	}

	private void drawContours(final List<MatOfPoint> contours, final Mat original, final Mat frame) {
		final Mat dest = new Mat(original.size(), CvType.CV_8UC3, Scalar.all(0));

		// original.copyTo(dest, cannyOutput);
	}

	// helper function:
	// finds a cosine of angle between vectors
	// from pt0->pt1 and from pt0->pt2
	private double angle(final Point pt1, final Point pt2, final Point pt0) {
		final double dx1 = pt1.x - pt0.x;
		final double dy1 = pt1.y - pt0.y;
		final double dx2 = pt2.x - pt0.x;
		final double dy2 = pt2.y - pt0.y;
		return (dx1 * dx2 + dy1 * dy2) / Math.sqrt((dx1 * dx1 + dy1 * dy1) * (dx2 * dx2 + dy2 * dy2) + 1e-10);
	}

	private void detectBackup() {

		// final Mat pyr = new Mat();
		// final Mat timg = new Mat();
		// final Mat gray0 = new Mat(frame.size(), CvType.CV_8U);
		// final Mat gray = new Mat();
		//
		// Imgproc.pyrDown(frame, pyr);
		// Imgproc.pyrUp(pyr, frame, frame.size());
		//
		// final List<MatOfPoint> squares = new ArrayList<>();
		// final List<MatOfPoint> contours = new ArrayList<>();
		// final List<Mat> timgList = new ArrayList<Mat>(Arrays.asList(new Mat[] { frame }));
		// final List<Mat> gray0List = new ArrayList<Mat>(Arrays.asList(new Mat[] { gray0 }));
		// // find squares in every color plane of the image
		// for (int c = 0; c < 1; c++) {
		// // Core.mixChannels(timgList, gray0List, new MatOfInt(c, 0));
		// // try several threshold levels
		// for (int l = 0; l < N; l++) {
		// // hack: use Canny instead of zero threshold level.
		// // Canny helps to catch squares with gradient shading
		// if (l == 0) {
		// // apply Canny. Take the upper threshold from slider
		// // and set the lower to 0 (which forces edges merging)
		// Imgproc.Canny(gray0, frame, 20, thresh, 7, false);
		// // dilate canny output to remove potential
		// // holes between edge segments
		// // Imgproc.dilate(gray, gray, new Mat(), new Point(-1, -1), 1);
		// } else {
		// // apply threshold if l!=0:
		// // tgray(x,y) = gray(x,y) < (l+1)*255/N ? 255 : 0
		// Imgproc.threshold(gray0, gray, (l + 1) * 255 / N, 255, Imgproc.THRESH_BINARY);
		// }
		// // find contours and store them all as a list
		// // Imgproc.cvtColor(gray, frame, Imgproc.COLOR_GRAY2BGR);
		// // Imgproc.findContours(gray, contours, gray, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
		// if (c == 0) {
		// // Imgproc.drawContours(frame, contours, -1, new Scalar(0, 255, 0), 3);
		// }
		// if (c == 1) {
		// // Imgproc.drawContours(frame, contours, -1, new Scalar(255, 0, 0), 3);
		// }
		// if (c == 2) {
		// // Imgproc.drawContours(frame, contours, -1, new Scalar(0, 0, 255), 3);
		// }
		//
		// System.out.println("Size: " + contours.size());
		// }
		//
		// }
		//
		// final int thresh = 50, N = 1;
		//

		// gray.copyTo(frame);
		// final MatOfPoint2f approx = new MatOfPoint2f();
		// // test each contour
		// for (int i = 0; i < contours.size(); i++) {
		// // approximate contour with accuracy proportional
		// // to the contour perimeter
		// final MatOfPoint2f matOfPoint2f = new MatOfPoint2f();
		// matOfPoint2f.fromList(contours.get(i).toList());
		// Imgproc.approxPolyDP(matOfPoint2f, approx, Imgproc.arcLength(matOfPoint2f, true) * 0.02, true);
		// // square contours should have 4 vertices after approximation
		// // relatively large area (to filter out noisy contours)
		// // and be convex.
		// // Note: absolute value of an area is used because
		// // area may be positive or negative - in accordance with the
		// // contour orientation
		// if (approx.total() == 4 && Math.abs(Imgproc.contourArea(contours.get(i))) > 1000
		// && Imgproc.isContourConvex(contours.get(i))) {
		// double maxCosine = 0;
		// final Point approxArray[] = approx.toArray();
		// for (int j = 2; j < 5; j++) {
		// // find the maximum cosine of the angle between joint edges
		//
		// final double cosine = Math
		// .abs(angle(approxArray[j % 4], approxArray[j - 2], approxArray[j - 1]));
		// maxCosine = Math.max(maxCosine, cosine);
		// }
		// // if cosines of all angles are small
		// // (all angles are ~90 degree) then write quandrange
		// // vertices to resultant sequence
		// if (maxCosine < 0.3) {
		// squares.add(contours.get(i));
		// }
		// }
		// }
	}

	private Mat findAndDrawPlates(final Mat maskedImage, final Mat frame) {
		// init
		final List<MatOfPoint> contours = new ArrayList<>();
		final Mat hierarchy = new Mat();

		// find contours
		Imgproc.findContours(maskedImage, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

		System.out.println("Size: " + contours.size());

		// if any contour exist...
		if (hierarchy.size().height > 0 && hierarchy.size().width > 0) {
			// for each contour, display it in blue
			for (int idx = 0; idx >= 0; idx = (int) hierarchy.get(0, idx)[0]) {
				Imgproc.drawContours(frame, contours, idx, new Scalar(250, 0, 0), 3);
			}
		}

		return frame;
	}

	/**
	 * Filters contours by shape. Iterates through the list of contours and approximates their shape. Compares the
	 * vertices of the shape to the desired vertices and removes the contour if they do not match.
	 *
	 * @param contours list of contours
	 * @param vertices vertices of the desired shape
	 * @param accuracy the accuracy of approximation
	 * @see Imgproc#approxPolyDP(MatOfPoint2f, MatOfPoint2f, double, boolean)
	 */
	public static void detectContoursByShape(final List<MatOfPoint> contours, final int vertices,
			final double accuracy) {
		final MatOfPoint2f matOfPoint2f = new MatOfPoint2f();
		final MatOfPoint2f approxCurve = new MatOfPoint2f();

		for (int idx = contours.size() - 1; idx >= 0; idx--) {
			final MatOfPoint contour = contours.get(idx);

			matOfPoint2f.fromList(contour.toList());
			Imgproc.approxPolyDP(matOfPoint2f, approxCurve, Imgproc.arcLength(matOfPoint2f, true) * accuracy, true);
			final long total = approxCurve.total();

			if (total != vertices) {
				contours.remove(idx);
			}
		}
	}

	public void detectFaceByHaar(final Mat frame) {
		loadHaarClassifier();

		detectAndDisplay(frame);
	}

	public void detectFaceByLbp(final Mat frame) {
		loadLbpClassifier();

		detectAndDisplay(frame);
	}

	private void detectAndDisplay(final Mat frame) {
		final MatOfRect faces = new MatOfRect();
		final Mat grayFrame = new Mat();

		// convert the frame in gray scale
		Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_BGR2GRAY);
		// equalize the frame histogram to improve the result
		Imgproc.equalizeHist(grayFrame, grayFrame);

		// compute minimum face size (20% of the frame height, in our case)
		if (absoluteFaceSize == 0) {
			final int height = grayFrame.rows();
			if (Math.round(height * 0.2f) > 0) {
				absoluteFaceSize = Math.round(height * 0.2f);
			}
		}

		// detect faces
		faceCascade.detectMultiScale(grayFrame, faces, 1.1, 2, 0 | Objdetect.CASCADE_SCALE_IMAGE,
				new Size(absoluteFaceSize, absoluteFaceSize), new Size());

		// each rectangle in faces is a face: draw them!
		final Rect[] facesArray = faces.toArray();
		for (int i = 0; i < facesArray.length; i++) {
			Imgproc.rectangle(frame, facesArray[i].tl(), facesArray[i].br(), new Scalar(0, 255, 0), 3);
		}
	}

	private void loadHaarClassifier() {
		if (!haarClassifierLoaded) {
			lbpClassifierLoaded = false;
			haarClassifierLoaded = true;

			final File file = new File(
					"C:\\Users\\juane\\eclipse-workspace\\platedetect\\src\\main\\resources\\classifiers\\haarcascades\\haarcascade_frontalface_alt.xml");
			final boolean exists = file.exists();
			faceCascade.load(
					"C:\\Users\\juane\\eclipse-workspace\\platedetect\\src\\main\\resources\\classifiers\\haarcascades\\haarcascade_frontalface_alt.xml");
		}
	}

	private void loadLbpClassifier() {
		if (!lbpClassifierLoaded) {
			lbpClassifierLoaded = true;
			haarClassifierLoaded = false;

			final File file = new File(
					"C:\\Users\\juane\\eclipse-workspace\\platedetect\\src\\main\\resources\\classifiers\\haarcascades\\haarcascade_frontalface_alt.xml");
			final boolean exists = file.exists();
			faceCascade.load(
					"C:\\Users\\juane\\eclipse-workspace\\platedetect\\src\\main\\resources\\classifiers\\haarcascades\\haarcascade_frontalface_alt.xml");
		}
	}

}
