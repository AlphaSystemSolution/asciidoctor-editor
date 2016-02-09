package com.alphasystem.app.asciidoctoreditor.ui.model;

import org.asciidoctor.AttributesBuilder;
import org.asciidoctor.OptionsBuilder;

import java.io.File;
import java.nio.file.Path;
import java.util.Map;

import static com.alphasystem.app.asciidoctoreditor.ui.model.Backend.HTML;
import static com.alphasystem.util.AppUtil.*;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.asciidoctor.SafeMode.SAFE;

/**
 * @author sali
 */
public final class AsciiDocPropertyInfo {

    private final AttributesBuilder attributesBuilder;
    private final OptionsBuilder optionsBuilder;
    private String documentType;
    private String documentName;
    private String documentTitle;
    private String backend;
    private String stylesDir;
    private File customStyleSheetFile;
    private boolean linkCss;
    private String icons;
    private String iconFontName;
    private boolean docInfo2;
    private String sourceLanguage;
    private boolean omitLastUpdatedTimeStamp;
    private File srcFile;
    private File previewFile;

    public AsciiDocPropertyInfo() {
        attributesBuilder = AttributesBuilder.attributes();
        optionsBuilder = OptionsBuilder.options().attributes(attributesBuilder);
        setDocumentType(null);
        setBackend(null);
        setStylesDir(null);
        setIcons(null);
        setLinkCss(true);
        setOmitLastUpdatedTimeStamp(true);
        optionsBuilder.compact(true).safe(SAFE);
    }

    /**
     * Copy Constructor
     *
     * @param src source object, cannot be null.
     * @throws IllegalArgumentException
     */
    public AsciiDocPropertyInfo(AsciiDocPropertyInfo src) throws IllegalArgumentException {
        this();
        if (src == null) {
            throw new IllegalArgumentException("source object cannot be null");
        }
        setDocumentType(src.getDocumentType());
        setDocumentName(src.getDocumentName());
        setDocumentTitle(src.getDocumentTitle());
        setDocInfo2(src.isDocInfo2());
        setIcons(src.getIcons());
        setIconFontName(src.getIconFontName());
        setLinkCss(src.isLinkCss());
        setSrcFile(src.getSrcFile());
        setPreviewFile(src.getPreviewFile());
        setStylesDir(src.getStylesDir());
        setCustomStyleSheetFile(src.getCustomStyleSheetFile());
        setSourceLanguage(src.getSourceLanguage());
        setOmitLastUpdatedTimeStamp(src.isOmitLastUpdatedTimeStamp());
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

    public String getBackend() {
        return backend;
    }

    public void setBackend(String backend) {
        this.backend = (backend == null) ? HTML.getValue() : backend;
        optionsBuilder.backend(this.backend);
    }

    public String getStylesDir() {
        return stylesDir;
    }

    public void setStylesDir(String stylesDir) {
        this.stylesDir = isBlank(stylesDir) ? "css" : stylesDir;
        if (linkCss) {
            attributesBuilder.stylesDir(this.stylesDir);
        }
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
        if (this.linkCss) {
            attributesBuilder.linkCss(this.linkCss).stylesDir(stylesDir);
        }
    }

    public String getIcons() {
        return icons;
    }

    public void setIcons(String icons) {
        this.icons = icons;
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

    public boolean isDocInfo2() {
        return docInfo2;
    }

    public void setDocInfo2(boolean docInfo2) {
        this.docInfo2 = docInfo2;
    }

    public String getSourceLanguage() {
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
