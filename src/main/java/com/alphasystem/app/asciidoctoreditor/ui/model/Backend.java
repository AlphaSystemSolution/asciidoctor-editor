package com.alphasystem.app.asciidoctoreditor.ui.model;

/**
 * @author sali
 */
public enum Backend {

    HTML("html", "html"), DOC_BOOK("docbook", "xml");

    private final String value;
    private final String extension;

    Backend(String value, String extension) {
        this.value = value;
        this.extension = extension;
    }

    public String getValue() {
        return value;
    }

    public String getExtension() {
        return extension;
    }
}
