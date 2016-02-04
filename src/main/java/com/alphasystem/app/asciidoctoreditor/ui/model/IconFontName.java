package com.alphasystem.app.asciidoctoreditor.ui.model;

import java.util.HashMap;
import java.util.Map;

/**
 * @author sali
 */
public enum IconFontName {

    DEFAULT("Default"), FONT_AWESOME("font-awesome");

    private static final Map<String, IconFontName> VALUES_MAP = new HashMap<>();

    static {
        for (IconFontName iconFontName : values()) {
            VALUES_MAP.put(iconFontName.getDispalyName(), iconFontName);
        }
    }

    private final String dispalyName;

    IconFontName(String value) {
        this.dispalyName = value;
    }

    public static IconFontName fromDisplayName(String value) {
        return (value == null) ? DEFAULT : VALUES_MAP.get(value);
    }

    public String getDispalyName() {
        return dispalyName;
    }

    @Override
    public String toString() {
        return dispalyName;
    }
}
