package com.alphasystem.app.asciidoctoreditor.ui.model;

import org.asciidoctor.AttributesBuilder;
import org.asciidoctor.OptionsBuilder;

import java.io.File;
import java.nio.file.Path;
import java.util.Map;

import static com.alphasystem.util.AppUtil.*;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.asciidoctor.SafeMode.SAFE;

/**
 * @author sali
 */
public final class AsciiDocPropertyInfo {

    private final AttributesBuilder attributesBuilder = AttributesBuilder.attributes();
    private final OptionsBuilder optionsBuilder = OptionsBuilder.options().attributes(attributesBuilder);
    private String documentType;
    private String documentName;
    private String documentTitle;
    private String stylesDir;
    private File customStyleSheetFile;
    private boolean linkCss;
    private String icons;
    private String iconFontName;
    private String docInfo2;
    private String sourceLanguage;
    private boolean omitLastUpdatedTimeStamp;
    private File srcFile;
    private File previewFile;

    public AsciiDocPropertyInfo() {
        setDocumentType(null);
        setStylesDir(null);
        setIcons(null);
        setLinkCss(true);
        optionsBuilder.compact(true).safe(SAFE);
    }

    private static String getFailSafeString(Map<String, Object> attributes, String key) {
        return getFailSafeString(attributes.get(key));
    }

    private static String getFailSafeString(Object value) {
        String result = null;
        if (isInstanceOf(String.class, value)) {
            result = (String) value;
        }
        return isBlank(result) ? null : result;
    }

    public AttributesBuilder getAttributesBuilder() {
        return attributesBuilder;
    }

    public OptionsBuilder getOptionsBuilder() {
        return optionsBuilder;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = isBlank(documentType) ? "book" : documentType;
        attributesBuilder.docType(this.documentType);
    }

    public String getDocumentName() {
        return documentName;
    }

    public void setDocumentName(String documentName) {
        this.documentName = documentName;
    }

    public String getDocumentTitle() {
        return documentTitle;
    }

    public void setDocumentTitle(String documentTitle) {
        this.documentTitle = documentTitle;
    }

    public String getStylesDir() {
        return stylesDir;
    }

    public void setStylesDir(String stylesDir) {
        this.stylesDir = isBlank(stylesDir) ? "css" : stylesDir;
        attributesBuilder.stylesDir(this.stylesDir);
    }

    public File getCustomStyleSheetFile() {
        return customStyleSheetFile;
    }

    public void setCustomStyleSheetFile(File customStyleSheetFile) {
        this.customStyleSheetFile = customStyleSheetFile;
        if (this.customStyleSheetFile != null) {
            String styleSheetName = this.customStyleSheetFile.getName();
            attributesBuilder.styleSheetName(styleSheetName);
        }
    }

    public boolean isLinkCss() {
        return linkCss;
    }

    public void setLinkCss(boolean linkCss) {
        this.linkCss = linkCss;
        attributesBuilder.linkCss(this.linkCss);
    }

    public String getIcons() {
        return icons;
    }

    public void setIcons(String icons) {
        this.icons = isBlank(icons) ? "font" : icons;
        attributesBuilder.icons(this.icons);
    }

    public String getIconFontName() {
        return iconFontName;
    }

    public void setIconFontName(String iconFontName) {
        this.iconFontName = iconFontName;
        boolean localFontName = isNotBlank(this.iconFontName);
        attributesBuilder.iconFontName(localFontName ? this.iconFontName : null).iconFontRemote(!localFontName);
    }

    public final String getDocInfo2() {
        return docInfo2;
    }

    public final void setDocInfo2(String docInfo2) {
        this.docInfo2 = docInfo2;
    }

    public final String getSourceLanguage() {
        return sourceLanguage;
    }

    public void setSourceLanguage(String sourceLanguage) {
        this.sourceLanguage = sourceLanguage;
        attributesBuilder.sourceLanguage(this.sourceLanguage);
    }

    public boolean isOmitLastUpdatedTimeStamp() {
        return omitLastUpdatedTimeStamp;
    }

    public void setOmitLastUpdatedTimeStamp(boolean omitLastUpdatedTimeStamp) {
        this.omitLastUpdatedTimeStamp = omitLastUpdatedTimeStamp;
    }

    public File getSrcFile() {
        return srcFile;
    }

    public void setSrcFile(File srcFile) {
        this.srcFile = (srcFile == null) ? USER_HOME_DIR : srcFile;
        optionsBuilder.baseDir(this.srcFile.getParentFile());
        setPreview(this.srcFile, previewFile);
    }

    public File getPreviewFile() {
        return previewFile;
    }

    public void setPreviewFile(File previewFile) {
        this.previewFile = previewFile;
        setPreview(srcFile, this.previewFile);
    }

    public void populateAttributes(Map<String, Object> attributes) {
        Object value;
        String s = getFailSafeString(attributes, "doctype");
        if (s != null) {
            setDocumentType(s);
        }
        s = getFailSafeString(attributes, "docname");
        if (s != null) {
            setDocumentName(s);
        }
        s = getFailSafeString(attributes, "doctitle");
        if (s != null) {
            setDocumentTitle(s);
        }
        s = getFailSafeString(attributes, "stylesdir");
        if (s != null) {
            setStylesDir(s);
        }
        s = getFailSafeString(attributes, "stylesheet");
        if (s != null) {
            File parent = new File(getSrcFile().getAbsoluteFile(), getStylesDir());
            setCustomStyleSheetFile(new File(parent, s));
        }
        value = attributes.get("linkcss");
        setLinkCss(value != null);
        s = getFailSafeString(attributes, "iconfont-name");
        if (s != null) {
            setIconFontName(s);
        }
    }

    private void setPreview(File srcFile, File previewFile) {
        if (srcFile == null || previewFile == null) {
            return;
        }
        File baseDir = srcFile.getParentFile();
        Path relativePath = toRelativePath(baseDir, previewFile.getParentFile());
        File destFolder = new File(baseDir, relativePath.toFile().getPath());
        File destFile = new File(destFolder, previewFile.getName());
        optionsBuilder.toDir(destFolder).toFile(destFile).inPlace(baseDir.equals(destFolder));
    }
}
