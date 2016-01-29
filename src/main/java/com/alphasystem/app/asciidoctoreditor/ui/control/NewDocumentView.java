package com.alphasystem.app.asciidoctoreditor.ui.control;

import com.alphasystem.app.asciidoctoreditor.ui.control.skin.NewDocumentSkin;
import com.alphasystem.app.asciidoctoreditor.ui.model.AsciiDocPropertyInfo;
import com.alphasystem.app.asciidoctoreditor.ui.model.DocumentType;
import com.alphasystem.app.asciidoctoreditor.ui.model.IconFontName;
import com.alphasystem.app.asciidoctoreditor.ui.model.Icons;
import javafx.beans.property.*;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

import java.io.File;
import java.util.Objects;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * @author sali
 */
public class NewDocumentView extends Control {

    public static final String DEFAULT_STYLES_DIR = "<document_base_dir>";
    public static final int MAX_WIDTH = 800;
    private final ObjectProperty<AsciiDocPropertyInfo> propertyInfo = new SimpleObjectProperty<>(null, "propertyInfo");
    private final ObjectProperty<DocumentType> documentType = new SimpleObjectProperty<>(null, "documentType");
    private final StringProperty documentName = new SimpleStringProperty(null, "documentName");
    private final StringProperty documentTitle = new SimpleStringProperty(null, "documentTitle");
    private final StringProperty baseDir = new SimpleStringProperty(null, "baseDir");
    private final StringProperty stylesDir = new SimpleStringProperty(null, "stylesDir");
    private final StringProperty customStyleSheetFile = new SimpleStringProperty(null, "customStyleSheetFile");
    private final BooleanProperty linkCss = new SimpleBooleanProperty(true, "linkCss");
    private final ObjectProperty<Icons> icons = new SimpleObjectProperty<>(null, "icons");
    private final ObjectProperty<IconFontName> iconFontName = new SimpleObjectProperty<>(null, "iconFontName");
    private final StringProperty docInfo2 = new SimpleStringProperty(null, "docInfo2");
    private final BooleanProperty omitLastUpdatedTimeStamp = new SimpleBooleanProperty(null, "omitLastUpdatedTimeStamp");
    private final ReadOnlyBooleanWrapper needRequired = new ReadOnlyBooleanWrapper(null, "needRequired");

    public NewDocumentView() {
        propertyInfoProperty().addListener((o, ov, nv) -> {
            final String docType = nv.getDocumentType();
            setDocumentType(DocumentType.fromType(docType));
            setDocumentName(nv.getDocumentName());
            setDocumentTitle(nv.getDocumentTitle());
            File srcFile = nv.getSrcFile();
            String baseDir = null;
            if (srcFile != null) {
                srcFile = srcFile.getParentFile();
                baseDir = (srcFile == null) ? null : srcFile.getPath();
            }
            setBaseDir(baseDir);
            setStylesDir(nv.getStylesDir());
            final File customStyleSheetFile = nv.getCustomStyleSheetFile();
            if (Objects.nonNull(customStyleSheetFile) && customStyleSheetFile.exists()) {
                setCustomStyleSheetFile(customStyleSheetFile.getPath());
            }
            setLinkCss(nv.isLinkCss());
            setIcons(Icons.fromValue(nv.getIcons()));
            setIconFontName(IconFontName.fromDisplayName(nv.getIconFontName()));
            setDocInfo2(nv.getDocInfo2());
            setOmitLastUpdatedTimeStamp(nv.isOmitLastUpdatedTimeStamp());
        });
        documentTypeProperty().addListener((o, ov, nv) -> {
            getPropertyInfo().setDocumentType(nv.getType());
            updateNeedRequired();
        });
        documentNameProperty().addListener((o, ov, nv) -> {
            getPropertyInfo().setDocumentName(nv);
            updateSrcFile(getBaseDir(), nv);
            updateNeedRequired();
        });
        documentTitleProperty().addListener((o, ov, nv) -> {
            getPropertyInfo().setDocumentTitle(nv);
            updateNeedRequired();
        });
        baseDirProperty().addListener((o, ov, nv) -> {
            updateSrcFile(nv, getDocumentName());
            updateNeedRequired();
        });
        stylesDirProperty().addListener((o, ov, nv) -> {
            String value = (nv != null && nv.equals(DEFAULT_STYLES_DIR)) ? "." : nv;
            getPropertyInfo().setStylesDir(value);
        });
        customStyleSheetFileProperty().addListener((o, ov, nv) -> {
            File customStyleSheetFile = null;
            if (isNotBlank(nv)) {
                customStyleSheetFile = new File(nv);
            }
            getPropertyInfo().setCustomStyleSheetFile(customStyleSheetFile);
        });
        linkCssProperty().addListener((o, ov, nv) -> getPropertyInfo().setLinkCss(nv));
        iconsProperty().addListener((o, ov, nv) -> {
            final boolean none = (nv == null) || Icons.DEFAULT.equals(nv);
            String value = none ? null : nv.getValue();
            getPropertyInfo().setIcons(value);
            if (none) {
                setIconFontName(null);
            }
        });
        iconFontNameProperty().addListener((o, ov, nv) -> {
            String value = ((nv == null) || IconFontName.DEFAULT.equals(nv)) ? null : nv.getDispalyName();
            getPropertyInfo().setIconFontName(value);
        });
        docInfo2Property().addListener((o, ov, nv) -> getPropertyInfo().setDocInfo2(nv));
        omitLastUpdatedTimeStampProperty().addListener((o, ov, nv) -> getPropertyInfo().setOmitLastUpdatedTimeStamp(nv));
        setPropertyInfo(null);
        setMinWidth(MAX_WIDTH);
        setMaxWidth(MAX_WIDTH);
        updateNeedRequired();
        setSkin(new NewDocumentSkin(this));
    }

    public final AsciiDocPropertyInfo getPropertyInfo() {
        AsciiDocPropertyInfo pi = this.propertyInfo.get();
        if (pi == null) {
            setPropertyInfo(new AsciiDocPropertyInfo());
            pi = this.propertyInfo.get();
        }
        return pi;
    }

    public final void setPropertyInfo(AsciiDocPropertyInfo propertyInfo) {
        this.propertyInfo.set(propertyInfo == null ? new AsciiDocPropertyInfo() : propertyInfo);
    }

    public final ObjectProperty<AsciiDocPropertyInfo> propertyInfoProperty() {
        return propertyInfo;
    }

    public final DocumentType getDocumentType() {
        return documentType.get();
    }

    public final void setDocumentType(DocumentType documentType) {
        this.documentType.set(documentType);
    }

    public final ObjectProperty<DocumentType> documentTypeProperty() {
        return documentType;
    }

    public final String getDocumentName() {
        return documentName.get();
    }

    public final void setDocumentName(String documentName) {
        this.documentName.set(documentName);
    }

    public final StringProperty documentNameProperty() {
        return documentName;
    }

    public final String getDocumentTitle() {
        return documentTitle.get();
    }

    public final void setDocumentTitle(String documentTitle) {
        this.documentTitle.set(documentTitle);
    }

    public final StringProperty documentTitleProperty() {
        return documentTitle;
    }

    public final String getBaseDir() {
        return baseDir.get();
    }

    public final void setBaseDir(String baseDir) {
        this.baseDir.set(baseDir);
    }

    public final StringProperty baseDirProperty() {
        return baseDir;
    }

    public final StringProperty stylesDirProperty() {
        return stylesDir;
    }

    public final void setStylesDir(String stylesDir) {
        this.stylesDir.set(stylesDir);
    }

    public final String getCustomStyleSheetFile() {
        return customStyleSheetFile.get();
    }

    public final void setCustomStyleSheetFile(String customStyleSheetFile) {
        this.customStyleSheetFile.set(customStyleSheetFile);
    }

    public final StringProperty customStyleSheetFileProperty() {
        return customStyleSheetFile;
    }

    public final BooleanProperty linkCssProperty() {
        return linkCss;
    }

    public final void setLinkCss(boolean linkCss) {
        this.linkCss.set(linkCss);
    }

    public final ObjectProperty<Icons> iconsProperty() {
        return icons;
    }

    public final void setIcons(Icons icons) {
        this.icons.set(icons);
    }

    public ObjectProperty<IconFontName> iconFontNameProperty() {
        return iconFontName;
    }

    public void setIconFontName(IconFontName iconFontName) {
        this.iconFontName.set(iconFontName);
    }

    public final String getDocInfo2() {
        return docInfo2.get();
    }

    public final void setDocInfo2(String docInfo2) {
        this.docInfo2.set(docInfo2);
    }

    public final StringProperty docInfo2Property() {
        return docInfo2;
    }

    public boolean getOmitLastUpdatedTimeStamp() {
        return omitLastUpdatedTimeStamp.get();
    }

    public void setOmitLastUpdatedTimeStamp(boolean omitLastUpdatedTimeStamp) {
        this.omitLastUpdatedTimeStamp.set(omitLastUpdatedTimeStamp);
    }

    public BooleanProperty omitLastUpdatedTimeStampProperty() {
        return omitLastUpdatedTimeStamp;
    }

    public final ReadOnlyBooleanProperty needRequiredProperty() {
        return needRequired.getReadOnlyProperty();
    }

    private void updateSrcFile(final String baseDir, final String documentName) {
        if (isNotBlank(baseDir) && isNotBlank(documentName)) {
            getPropertyInfo().setSrcFile(new File(baseDir, format("%s.adoc", documentName)));
        }
    }

    private void updateNeedRequired() {
        needRequired.set((getDocumentType() == null) || isBlank(getDocumentName()) || isBlank(getDocumentTitle())
                || isBlank(getBaseDir()));
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new NewDocumentSkin(this);
    }
}
