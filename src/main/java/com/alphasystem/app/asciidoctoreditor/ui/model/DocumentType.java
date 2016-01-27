package com.alphasystem.app.asciidoctoreditor.ui.model;

import java.util.HashMap;
import java.util.Map;

/**
 * @author sali
 */
public enum DocumentType {

    BOOK("book", "Book"), ARTICLE("article", "Article"), MAN_PAGE("manpage", "Man Page");

    private static final Map<String, DocumentType> TYPE_TO_DOCUMENT_MAP = new HashMap<>();
    private static final Map<String, DocumentType> DESCRIPTION_TO_DOCUMENT_MAP = new HashMap<>();

    static {
        final DocumentType[] values = values();
        for (DocumentType value : values) {
            TYPE_TO_DOCUMENT_MAP.put(value.getType(), value);
            DESCRIPTION_TO_DOCUMENT_MAP.put(value.getDescription(), value);
        }
    }

    private final String type;
    private final String description;

    DocumentType(String type, String description) {
        this.type = type;
        this.description = description;
    }

    public static DocumentType fromType(String type){
        return TYPE_TO_DOCUMENT_MAP.get(type);
    }

    public static DocumentType fromDescription(String description){
        return DESCRIPTION_TO_DOCUMENT_MAP.get(description);
    }

    public String getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return description;
    }
}
