package com.alphasystem.app.asciidoctoreditor.ui.model;

import java.util.HashMap;
import java.util.Map;

/**
 * @author sali
 */
public enum IconFontName {

    DEFAULT("Default", null), FONT_AWESOME("font-awesome", "font-awesome-min.css");

    private static final Map<String, IconFontName> VALUES_MAP = new HashMap<>();

    static {
        for (IconFontName iconFontName : values()) {
            VALUES_MAP.put(iconFontName.getDispalyName(), iconFontName);
        }
    }

    private final String dispalyName;
    private final String cssFileName;

    IconFontName(String value, String cssFileName) {
        this.dispalyName = value;
        this.cssFileName = cssFileName;
    }

    public static IconFontName fromDisplayName(String value) {
        return (value == null) ? DEFAULT : VALUES_MAP.get(value);
    }

    public String getDispalyName() {
        return dispalyName;
    }

    public String getCssFileName() {
        return cssFileName;
    }

    @Override
    public String toString() {
        return dispalyName;
    }
}
