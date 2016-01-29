package com.alphasystem.app.asciidoctoreditor.ui.control;

import com.alphasystem.app.asciidoctoreditor.ui.control.skin.AsciiDoctorEditorSkin2;
import com.alphasystem.app.asciidoctoreditor.ui.model.AsciiDocPropertyInfo;
import javafx.beans.property.*;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.control.TextArea;

import java.io.File;

import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * @author sali
 */
public class AsciiDoctorEditorView extends Control {

    private final ObjectProperty<AsciiDocPropertyInfo> propertyInfo = new SimpleObjectProperty<>(null, "propertyInfo");
    private final StringProperty content = new SimpleStringProperty(null, "content");
    private final ReadOnlyObjectWrapper<File> previewFile = new ReadOnlyObjectWrapper<>(null, "previewFile");
    private final BooleanProperty previewSelected = new SimpleBooleanProperty(false, "previewSelected");

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
        setSkin(new AsciiDoctorEditorSkin2(this));
    }

    public final AsciiDocPropertyInfo getPropertyInfo() {
        return propertyInfo.get();
    }

    public final void setPropertyInfo(AsciiDocPropertyInfo propertyInfo) {
        this.propertyInfo.set(propertyInfo);
    }

    public final ObjectProperty<AsciiDocPropertyInfo> propertyInfoProperty() {
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

    public final TextArea getEditor() {
        return ((AsciiDoctorEditorSkin2) getSkin()).getEditor();
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new AsciiDoctorEditorSkin2(this);
    }
}
