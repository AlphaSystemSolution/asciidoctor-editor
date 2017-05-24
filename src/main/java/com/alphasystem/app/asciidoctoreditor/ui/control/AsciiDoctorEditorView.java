package com.alphasystem.app.asciidoctoreditor.ui.control;

import java.io.File;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

import com.alphasystem.app.asciidoctoreditor.ui.control.skin.AsciiDoctorEditorSkin;
import com.alphasystem.app.asciidoctoreditor.ui.model.EditorState;
import com.alphasystem.asciidoc.model.AsciiDocumentInfo;

import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * @author sali
 */
public class AsciiDoctorEditorView extends Control {

    private final ObjectProperty<AsciiDocumentInfo> propertyInfo = new SimpleObjectProperty<>(null, "propertyInfo");
    private final StringProperty content = new SimpleStringProperty(null, "content");
    private final ReadOnlyObjectWrapper<File> previewFile = new ReadOnlyObjectWrapper<>(null, "previewFile");
    private final BooleanProperty previewSelected = new SimpleBooleanProperty(false, "previewSelected");
    private final EditorState editorState = new EditorState();

    public AsciiDoctorEditorView() {
        super();

        contentProperty().addListener((o, ov, nv) -> {
            setDisable(isBlank(nv));
        });
        propertyInfoProperty().addListener((o, ov, nv) -> {
            File previewFile = nv.getPreviewFile();
            setDisable(previewFile == null || !previewFile.exists());
            this.previewFile.setValue(previewFile);
        });
        setContent("");
        setDisable(true);
        setSkin(new AsciiDoctorEditorSkin(this));
    }

    public final AsciiDocumentInfo getPropertyInfo() {
        return propertyInfo.get();
    }

    public final void setPropertyInfo(AsciiDocumentInfo propertyInfo) {
        this.propertyInfo.set(propertyInfo);
    }

    public final ObjectProperty<AsciiDocumentInfo> propertyInfoProperty() {
        return propertyInfo;
    }

    public final String getContent() {
        return content.get();
    }

    public final void setContent(String content) {
        this.content.set(content);
    }

    public final StringProperty contentProperty() {
        return content;
    }

    public File getPreviewFile() {
        return previewFile.get();
    }

    public ReadOnlyObjectProperty<File> previewFileProperty() {
        return previewFile.getReadOnlyProperty();
    }

    public final BooleanProperty previewSelectedProperty() {
        return previewSelected;
    }

    public final void setPreviewSelected(boolean previewSelected) {
        this.previewSelected.set(previewSelected);
    }

    public final EditorState getEditorState() {
        return editorState;
    }

    public final AsciiDoctorTextArea getEditor() {
        return ((AsciiDoctorEditorSkin) getSkin()).getEditor();
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new AsciiDoctorEditorSkin(this);
    }
}
