package com.alphasystem.app.asciidoctoreditor.ui.control.util;

import static com.alphasystem.app.asciidoctoreditor.ui.ApplicationController.getValue;
import static java.lang.String.format;

/**
 * @author sali
 */
public final class UIHelper {

    private UIHelper() {
    }

    public static String getLabel(String key) {
        return getValue(format("%s.text", key));
    }

    public static String getToolTipText(String key) {
        return getValue(format("%s.toolTip", key));
    }


}
