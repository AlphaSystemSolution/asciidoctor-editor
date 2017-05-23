package com.alphasystem.app.asciidoctoreditor.ui.control;

import org.fxmisc.richtext.StyleClassedTextArea;

/**
 * @author sali
 */
public class AsciiDoctorTextArea extends StyleClassedTextArea {

    public AsciiDoctorTextArea() {
        this(false);
    }

    public AsciiDoctorTextArea(boolean preserveStyle) {
        super(preserveStyle);
        setStyle("-fx-font-family: \"Courier New\"; -fx-font-size: 16pt;");
    }

    @Override
    public String getUserAgentStylesheet() {
        return "editor.css";
    }

    /**
     * Moves the caret position backward. If there is no selection, then the
     * caret position is moved one character backward. If there is a selection,
     * then the caret position is moved to the beginning of the selection and
     * the selection cleared.
     *
     * @expert This function is intended to be used by experts, primarily
     *         by those implementing new Skins or Behaviors. It is not common
     *         for developers or designers to access this function directly.
     */
    public void backward() {
        // user has moved caret to the left
        final int textLength = getLength();
        final int dot = getCaretPosition();
        final int mark = getAnchor();
        if (dot != mark) {
            int pos = Math.min(dot, mark);
            selectRange(pos, pos);
        } else if (dot > 0 && textLength > 0) {
            int pos = Character.offsetByCodePoints(getText(), dot, -1);
            selectRange(pos, pos);
        }
        deselect();
    }

    /**
     * Moves the caret to after the last char of the text. This function
     * also has the effect of clearing the selection.
     */
    public void end() {
        // user wants to go to end
        final int textLength = getLength();
        if (textLength > 0) {
            selectRange(textLength, textLength);
        }
    }
}
