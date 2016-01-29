package com.alphasystem.app.asciidoctoreditor.ui.control.skin;

import com.alphasystem.app.asciidoctoreditor.ui.ApplicationController;
import com.alphasystem.app.asciidoctoreditor.ui.control.AsciiDoctorEditorView;
import com.alphasystem.arabic.ui.Browser;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.SkinBase;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import org.asciidoctor.OptionsBuilder;

import java.io.IOException;
import java.net.URL;

import static java.util.ResourceBundle.getBundle;

/**
 * @author sali
 */
public class AsciiDoctorEditorSkin2 extends SkinBase<AsciiDoctorEditorView> {

    private final SkinView skinView;

    public AsciiDoctorEditorSkin2(AsciiDoctorEditorView control) {
        super(control);
        skinView = new SkinView();
        getChildren().setAll(skinView);
    }

    public final TextArea getEditor() {
        return skinView.editor;
    }

    private class SkinView extends BorderPane {

        private ApplicationController applicationController = ApplicationController.getInstance();

        @FXML
        private Tab sourceTab;

        @FXML
        private Tab previewTab;

        @FXML
        private TextArea editor;

        @FXML
        private Browser preview;

        private SkinView() {
            init();
        }

        private void init() {
            URL fxmlURL = getClass().getResource("/fxml/AsciiDoctorEditorView.fxml");
            FXMLLoader fxmlLoader = new FXMLLoader(fxmlURL, getBundle("AsciiDoctorEditor"));
            fxmlLoader.setRoot(this);
            fxmlLoader.setController(this);
            try {
                fxmlLoader.load();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @FXML
        void initialize() {
            AsciiDoctorEditorView view = getSkinnable();
            view.previewFileProperty().addListener((o, ov, nv) -> preview.loadUrl(nv));

            editor.textProperty().bindBidirectional(view.contentProperty());
            preview.loadUrl(view.getPreviewFile());

            sourceTab.selectedProperty().addListener((o, ov, nv) -> {
                getSkinnable().setPreviewSelected(false);
                editor.requestFocus();
            });

            previewTab.disableProperty().bind(getSkinnable().disabledProperty());
            previewTab.selectedProperty().addListener((o, ov, nv) -> refreshPreview());
        }

        private void refreshPreview() {
            getSkinnable().setPreviewSelected(true);
            OptionsBuilder optionsBuilder = getSkinnable().getPropertyInfo().getOptionsBuilder();
            applicationController.refreshPreview(optionsBuilder, editor.getText(), preview);
        }

    }
}
