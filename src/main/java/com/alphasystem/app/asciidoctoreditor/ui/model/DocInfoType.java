package com.alphasystem.app.asciidoctoreditor.ui.model;

/**
 * @author sali
 */
public enum DocInfoType {

    SHARED_HEAD("shared-head", "Shared Head"),
    SHARED_FOOTER("shared-footer", "Shared Footer"),
    PRIVATE_HEAD("private-head", "Private Head"),
    PRIVATE_FOOTER("private-footer", "Private Footer"),
    SHARED("shared", "Shared"),
    PRIVATE("private", "Private");

    private final String value;
    private final String description;

    DocInfoType(String value, String description) {
        this.value = value;
        this.description = description;
    }

    public String getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return getDescription();
    }
}
