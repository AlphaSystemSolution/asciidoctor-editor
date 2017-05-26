package com.alphasystem.app.asciidoctoreditor.ui.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.scene.control.IndexRange;

import com.alphasystem.app.asciidoctoreditor.ui.control.AsciiDoctorTextArea;

/**
 * @author sali
 */
public final class ApplicationHelper {

    private static final Pattern PATTERN = Pattern.compile("[.!?\\-,]");

    /**
     * Do not let anyone instantiate this class.
     */
    private ApplicationHelper() {
    }

    public static boolean isEntireWordSelected(AsciiDoctorTextArea editor) {
        final String currentWord = getCurrentWord(editor);
        final String selectedText = editor.getSelectedText();
        return selectedText.equals(currentWord);
    }

    /**
     * Scan the current selection to find its boundary.
     *
     * @param editor current {@link AsciiDoctorTextArea}
     * @param start  selection start
     * @param end    selection end
     * @return return entire word which following selection belong
     */
    public static String getCurrentWord(AsciiDoctorTextArea editor, int start, int end) {
        final StringBuilder builder = new StringBuilder();

        int from = start - 1;
        int to = start;
        if (from >= 0) {
            while (true) {
                String text = editor.getText(from, to);
                text = (text == null) ? null : text.trim();
                if (text == null || text.isEmpty()) {
                    break;
                }
                Matcher matcher = PATTERN.matcher(text);
                final char c = text.charAt(0);
                if (matcher.find() || Character.isWhitespace(c)) {
                    break;
                }
                builder.insert(0, c);
                to = from;
                from -= 1;
            }
        }
        builder.append(editor.getSelectedText());

        from = end;
        to = end + 1;
        while (true) {
            String text = editor.getText(from, to);
            text = (text == null) ? null : text.trim();
            if (text == null || text.isEmpty()) {
                break;
            }
            Matcher matcher = PATTERN.matcher(text);
            final char c = text.charAt(0);
            if (matcher.find() || Character.isWhitespace(c)) {
                break;
            }
            builder.append(c);
            from = to;
            to += 1;
        }

        return builder.toString();
    }

    private static String getCurrentWord(AsciiDoctorTextArea editor) {
        final IndexRange selection = editor.getSelection();
        if (selection != null && selection.getLength() > 0) {
            return getCurrentWord(editor, selection.getStart(), selection.getEnd());
        }
        return null;
    }
}
