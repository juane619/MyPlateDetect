module org.juane.platedetect {
	requires javafx.controls;
	requires javafx.fxml;
	requires transitive javafx.graphics;
	requires opencv;
	requires javafx.base;
	requires java.logging;
	requires java.desktop;
	requires javafx.swing;

	opens org.juane.platedetect to javafx.fxml;

	exports org.juane.platedetect;
	exports org.juane.platedetect.controller;
}