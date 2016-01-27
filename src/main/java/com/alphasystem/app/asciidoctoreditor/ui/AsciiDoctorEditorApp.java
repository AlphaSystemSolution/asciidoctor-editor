package com.alphasystem.app.asciidoctoreditor.ui;

import com.alphasystem.app.asciidoctoreditor.ui.control.AsciiDoctorEditor;
import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.File;

/**
 * @author sali
 */
public class AsciiDoctorEditorApp extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Ascii Doctor Editor");
        Screen screen = Screen.getPrimary();
        Rectangle2D bounds = screen.getVisualBounds();

        primaryStage.setX(bounds.getMinX());
        primaryStage.setY(bounds.getMinY());
        primaryStage.setWidth(bounds.getWidth());
        primaryStage.setHeight(bounds.getHeight());

        final File file = new File("C:\\Users\\sali\\development\\asciidoc\\arabic2.adoc");
        AsciiDoctorEditor view = new AsciiDoctorEditor();
        view.setInitialFile(file);
//        view.setShowArabicKeyBoard(true);
        Scene scene = new Scene(view);
        scene.getStylesheets().addAll("/styles/glyphs_custom.css");
        primaryStage.setMaximized(true);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
