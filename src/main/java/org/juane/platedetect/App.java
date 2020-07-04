package org.juane.platedetect;

import java.io.IOException;

import org.juane.platedetect.controller.MainFrameController;
import org.opencv.core.Core;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * JavaFX App
 */
public class App extends Application {

	// Necesary to load video in windows
	static {
		try {
			System.load("C:\\Users\\juane\\Downloads\\opencv\\build\\x64\\vc14\\bin\\opencv_ffmpeg320_64.dll");
		} catch (final UnsatisfiedLinkError e) {
			System.err.println("Native code library failed to load.\n" + e);
			System.exit(1);
		}
	}

	private static Scene scene;

	@Override
	public void start(final Stage stage) throws IOException {
		try {

			// load the FXML resource
			final FXMLLoader loader = new FXMLLoader(getClass().getResource("mainFrame.fxml"));
			// store the root element so that the controllers can use it
			final BorderPane rootElement = (BorderPane) loader.load();
			// create and style a scene
			final Scene scene = new Scene(rootElement, 800, 600);
			// create the stage with the given title and the previously created
			// scene
			stage.setTitle("My Plate Detect");
			stage.setScene(scene);
			// show the GUI
			stage.show();

			// set the proper behavior on closing the application
			final MainFrameController controller = loader.getController();

			controller.init();

			stage.setOnCloseRequest(new EventHandler<WindowEvent>() {

				@Override
				public void handle(final WindowEvent event) {
					// TODO Auto-generated method stub
					controller.closeAction(null);
				}

			});

		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	public static void setRoot(final String fxml) throws IOException {
		scene.setRoot(loadFXML(fxml));
	}

	private static Parent loadFXML(final String fxml) throws IOException {
		final FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
		return fxmlLoader.load();
	}

	public static void main(final String[] args) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

		launch();
	}

}