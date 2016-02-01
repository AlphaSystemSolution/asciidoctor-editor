package com.alphasystem.app.asciidoctoreditor.ui.control;

import com.alphasystem.app.asciidoctoreditor.ui.controller.AsciiDoctorEditorController;
import com.alphasystem.app.asciidoctoreditor.ui.model.Action;
import com.alphasystem.app.asciidoctoreditor.ui.model.ApplicationMode;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.BorderPane;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import static com.alphasystem.app.asciidoctoreditor.ui.model.ApplicationMode.STANDALONE;
import static java.lang.String.format;
import static java.util.ResourceBundle.getBundle;

/**
 * @author sali
 */
public class AsciiDoctorEditor extends BorderPane {

    private final ObjectProperty<File> initialFile = new SimpleObjectProperty<>(null, "initialFile");

    private final ObjectProperty<Action> action = new SimpleObjectProperty<>(null, "action");

    public AsciiDoctorEditor() {
        this(STANDALONE);
    }

    public AsciiDoctorEditor(ApplicationMode applicationMode) {
        init(applicationMode);
    }

    private void init(ApplicationMode applicationMode) {
        URL fxmlURL = getClass().getResource(format("/fxml/%s.fxml", getClass().getSimpleName()));
        FXMLLoader fxmlLoader = new FXMLLoader(fxmlURL, getBundle("AsciiDoctorEditor"));
        fxmlLoader.setRoot(this);
        try {
            fxmlLoader.load();
            final AsciiDoctorEditorController controller = fxmlLoader.getController();
            controller.initialFileProperty().bind(initialFile);
            controller.setView(this);
            controller.setApplicationMode(applicationMode);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public final void setInitialFile(File initialFile) {
        this.initialFile.set(initialFile);
    }

    public final Action getAction() {
        return action.get();
    }

    public final void setAction(Action action) {
        this.action.set(action);
    }

    public final ObjectProperty<Action> actionProperty() {
        return action;
    }
}
