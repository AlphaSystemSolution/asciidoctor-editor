package com.alphasystem.app.asciidoctoreditor.ui.model;

import java.util.HashMap;
import java.util.Map;

/**
 * @author sali
 */
public enum Icons {

    DEFAULT("Default"), FONT("font");

    private static final Map<String, Icons> VALUES_MAP = new HashMap<>();

    static {
        for (Icons icons : values()) {
            VALUES_MAP.put(icons.getValue(), icons);
        }
    }

    private final String value;

    Icons(String value) {
        this.value = value;
    }

    public static Icons fromValue(String value) {
        return (value == null) ? DEFAULT : VALUES_MAP.get(value);
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
