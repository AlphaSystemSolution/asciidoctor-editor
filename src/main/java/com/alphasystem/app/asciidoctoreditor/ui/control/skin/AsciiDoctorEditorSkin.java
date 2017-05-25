package com.alphasystem.app.asciidoctoreditor.ui.control.skin;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.IndexRange;
import javafx.scene.control.SkinBase;
import javafx.scene.control.Tab;
import javafx.scene.layout.BorderPane;

import org.asciidoctor.AttributesBuilder;
import org.asciidoctor.OptionsBuilder;
import org.fxmisc.richtext.StyleSpans;

import com.alphasystem.app.asciidoctoreditor.ui.ApplicationController;
import com.alphasystem.app.asciidoctoreditor.ui.control.AsciiDoctorEditorView;
import com.alphasystem.app.asciidoctoreditor.ui.control.AsciiDoctorTextArea;
import com.alphasystem.app.asciidoctoreditor.ui.model.EditorState;
import com.alphasystem.fx.ui.Browser;

import static com.alphasystem.app.asciidoctoreditor.ui.model.ApplicationConstants.BOLD_KEY;
import static com.alphasystem.app.asciidoctoreditor.ui.model.ApplicationConstants.ITALIC_KEY;
import static com.alphasystem.app.asciidoctoreditor.ui.model.ApplicationConstants.STRIKETHROUGH_KEY;
import static com.alphasystem.app.asciidoctoreditor.ui.model.ApplicationConstants.SUBSCRIPT_KEY;
import static com.alphasystem.app.asciidoctoreditor.ui.model.ApplicationConstants.SUPERSCRIPT_KEY;
import static com.alphasystem.app.asciidoctoreditor.ui.model.ApplicationConstants.UNDERLINE_KEY;
import static java.util.ResourceBundle.getBundle;

/**
 * @author sali
 */
public class AsciiDoctorEditorSkin extends SkinBase<AsciiDoctorEditorView> {

    private final SkinView skinView;

    public AsciiDoctorEditorSkin(AsciiDoctorEditorView control) {
        super(control);
        skinView = new SkinView();
        getChildren().setAll(skinView);
    }

    private static boolean isStyle(Collection<String> s, String style) {
        return s.contains(style);
    }

    private static boolean isStyle(final StyleSpans<Collection<String>> styles, final String style) {
        return styles.styleStream().anyMatch(s -> isStyle(s, style));
    }

    public final AsciiDoctorTextArea getEditor() {
        return skinView.editor;
    }

    private void beingUpdated(AsciiDoctorTextArea editor, AsciiDoctorEditorView view) {
        EditorState state = view.getEditorState();

        boolean bold, italic, underline, strikeThrough, subScript, superScript;

        final IndexRange selection = editor.getSelection();
        if (selection != null && selection.getLength() != 0) {
            final StyleSpans<Collection<String>> styles = editor.getStyleSpans(selection);
            bold = isStyle(styles, BOLD_KEY);
            italic = isStyle(styles, ITALIC_KEY);
            underline = isStyle(styles, UNDERLINE_KEY);
            strikeThrough = isStyle(styles, STRIKETHROUGH_KEY);
            subScript = isStyle(styles, SUBSCRIPT_KEY);
            superScript = isStyle(styles, SUPERSCRIPT_KEY);
        } else {
            int p = editor.getCurrentParagraph();
            int col = editor.getCaretColumn();
            final Collection<String> styles = editor.getStyleAtPosition(p, col);
            bold = isStyle(styles, BOLD_KEY);
            italic = isStyle(styles, ITALIC_KEY);
            underline = isStyle(styles, UNDERLINE_KEY);
            strikeThrough = isStyle(styles, STRIKETHROUGH_KEY);
            subScript = isStyle(styles, SUBSCRIPT_KEY);
            superScript = isStyle(styles, SUPERSCRIPT_KEY);
        }

        state.setBold(bold);
        state.setItalic(italic);
        state.setUnderline(underline);
        state.setStrikeThrough(strikeThrough);
        state.setSubScript(subScript);
        state.setSuperScript(superScript);
    }

    private class SkinView extends BorderPane {

        private ApplicationController applicationController = ApplicationController.getInstance();

        @FXML
        private Tab sourceTab;

        @FXML
        private Tab previewTab;

        @FXML
        private AsciiDoctorTextArea editor;

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

            editor.beingUpdatedProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue) {
                    beingUpdated(editor, view);
                }
            });
            editor.setWrapText(true);
            // TODO:
            // editor.textProperty().addListener((observable, oldValue, newValue) -> view.setContent(newValue));
            view.contentProperty().addListener((observable, oldValue, newValue) -> {
                editor.replaceSelection(newValue);
            });

            sourceTab.selectedProperty().addListener((o, ov, nv) -> {
                getSkinnable().setPreviewSelected(false);
                editor.requestFocus();
            });

            previewTab.disableProperty().bind(getSkinnable().disabledProperty());
            previewTab.selectedProperty().addListener((o, ov, nv) -> refreshPreview());
        }

        private void refreshPreview() {
            getSkinnable().setPreviewSelected(true);
            final AttributesBuilder attributesBuilder = AttributesBuilder.attributes().linkCss(false);
            OptionsBuilder optionsBuilder = getSkinnable().getPropertyInfo().getOptionsBuilder().headerFooter(true)
                    .toFile(false).attributes(attributesBuilder);
            preview.loadContent(applicationController.refreshPreview(optionsBuilder, editor.getText()));
        }

    }
}
