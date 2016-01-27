package com.alphasystem.app.asciidoctoreditor.ui.control.skin;

import com.alphasystem.app.asciidoctoreditor.ui.ApplicationController;
import com.alphasystem.app.asciidoctoreditor.ui.control.AsciiDoctorEditorView;
import com.alphasystem.arabic.ui.Browser;
import com.alphasystem.arabic.ui.keyboard.KeyboardView;
import javafx.scene.control.SkinBase;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.stage.Popup;
import org.asciidoctor.OptionsBuilder;

import static com.alphasystem.fx.ui.util.UiUtilities.wrapInScrollPane;
import static javafx.geometry.Side.BOTTOM;
import static javafx.scene.control.TabPane.TabClosingPolicy.UNAVAILABLE;

/**
 * @author sali
 */
public class AsciiDoctorEditorSkin extends SkinBase<AsciiDoctorEditorView> {

    private ApplicationController applicationController = ApplicationController.getInstance();
    private final Popup keyboardPopup = new Popup();
    private final KeyboardView keyboardView = new KeyboardView();
    private final TextArea editor = new TextArea();
    private final Browser preview = new Browser();
    private final TabPane tabPane = new TabPane();

    public AsciiDoctorEditorSkin(AsciiDoctorEditorView control) {
        super(control);
        AsciiDoctorEditorView view = getSkinnable();
        view.previewFileProperty().addListener((o, ov, nv) -> {
            preview.loadUrl(nv);
        });

        editor.textProperty().bindBidirectional(view.contentProperty());
        preview.loadUrl(view.getPreviewFile());
        initializeSkin();
    }

    private void initializeSkin() {
        keyboardView.setTarget(editor);
        keyboardView.setOnMouseClicked(event -> keyboardView.requestFocus());

        keyboardPopup.setAutoHide(true);
        keyboardPopup.setHideOnEscape(true);
        keyboardPopup.getContent().add(keyboardView);

        BorderPane borderPane = new BorderPane();

        editor.setFont(Font.font("Courier New", 14.0));
        borderPane.setCenter(new StackPane(wrapInScrollPane(editor)));

        tabPane.setTabClosingPolicy(UNAVAILABLE);
        tabPane.setSide(BOTTOM);

        Tab previewTab = new Tab("Preview", preview);
        previewTab.disableProperty().bind(getSkinnable().disabledProperty());
        previewTab.selectedProperty().addListener((o, ov, nv) -> refreshPreview());

        Tab sourceTab = new Tab("Source", borderPane);
        sourceTab.selectedProperty().addListener((o, ov, nv) -> {
            getSkinnable().setPreviewSelected(false);
            editor.requestFocus();
        });
        tabPane.getTabs().addAll(sourceTab, previewTab);

        getChildren().add(tabPane);
    }

    // public methods

    public final TextArea getEditor() {
        return editor;
    }

    // private methods

    private void refreshPreview() {
        getSkinnable().setPreviewSelected(true);
        OptionsBuilder optionsBuilder = getSkinnable().getPropertyInfo().getOptionsBuilder();
        applicationController.refreshPreview(optionsBuilder, editor.getText(), preview);
    }
}
