package com.alphasystem.app.asciidoctoreditor.ui;

import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.alphasystem.app.asciidoctoreditor.ui.control.AsciiDoctorEditor;
import com.alphasystem.bootfx.starter.application.AbstractJavaFxApplicationSupport;

/**
 * @author sali
 */
@SpringBootApplication
public class AsciiDoctorEditorApp extends AbstractJavaFxApplicationSupport {

    private String windowTitle;

    public static void main(String[] args) {
        launchApp(AsciiDoctorEditorApp.class, args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle(windowTitle);
        Screen screen = Screen.getPrimary();
        Rectangle2D bounds = screen.getVisualBounds();

        primaryStage.setX(bounds.getMinX());
        primaryStage.setY(bounds.getMinY());
        primaryStage.setWidth(bounds.getWidth());
        primaryStage.setHeight(bounds.getHeight());

        AsciiDoctorEditor view = new AsciiDoctorEditor();
        Scene scene = new Scene(view);
        scene.getStylesheets().addAll("/styles/glyphs_custom.css");
        primaryStage.setMaximized(true);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Value("${app.ui.title}")
    public void setWindowTitle(String windowTitle) {
        this.windowTitle = windowTitle;
    }
}
