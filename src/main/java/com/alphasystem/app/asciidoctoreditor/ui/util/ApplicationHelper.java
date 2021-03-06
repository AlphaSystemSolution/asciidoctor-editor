package com.alphasystem.app.asciidoctoreditor.ui.util;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.scene.control.IndexRange;

import org.apache.commons.lang3.StringUtils;

import com.alphasystem.app.asciidoctoreditor.ui.control.AsciiDoctorTextArea;
import com.alphasystem.app.asciidoctoreditor.ui.model.AsciiDocMarkup;
import com.alphasystem.app.asciidoctoreditor.ui.model.EditorState;
import com.alphasystem.spring.support.ApplicationContextProvider;
import com.alphasystem.util.AppUtil;

/**
 * @author sali
 */
public final class ApplicationHelper {

    private static final Pattern PUNCTUATION_PATTERN = Pattern.compile("[.,]");
    private static final String REPLACEMENT = "";

    private static AsciiDocMarkup asciiDocMarkup;

    /**
     * Do not let anyone instantiate this class.
     */
    private ApplicationHelper() {
    }

    public static boolean isEntireWordSelected(AsciiDoctorTextArea editor, EditorState editorState) {
        final String currentWord = editorState.getCurrentWord();
        final String selectedText = editor.getSelectedText();
        return selectedText.equals(currentWord);
    }

    public static String getCurrentWordAndMarkup(AsciiDoctorTextArea editor) {
        final IndexRange selection = editor.getSelection();
        if (selection != null && selection.getLength() > 0) {
            return getCurrentWord(editor, selection.getStart(), selection.getEnd());
        } else {
            return getCurrentWordNonSelection(editor, editor.getCurrentParagraph(), editor.getCaretColumn());
        }
    }

    public static String getCurrentWord(AsciiDoctorTextArea editor, EditorState editorState) {
        String currentWord = getCurrentWordAndMarkup(editor);
        if (editorState != null && !StringUtils.isEmpty(currentWord)) {
            if (editorState.isBold()) {
                currentWord = removeMarkup(currentWord, getAsciiDocMarkup().getBold());
            }
            if (editorState.isItalic()) {
                currentWord = removeMarkup(currentWord, getAsciiDocMarkup().getItalic());
            }
            if (editorState.isUnderline()) {
                currentWord = removeMarkup(currentWord, getAsciiDocMarkup().getUnderline());
            }
        }
        return currentWord;
    }

    private static String removeMarkup(String selection, AsciiDocMarkup.Markup markup) {
        return selection.replace(markup.getMarkupBegin(), REPLACEMENT).replace(markup.getMarkupEnd(), REPLACEMENT);
    }

    /**
     * Scan the current selection to find its boundary.
     *
     * @param editor current {@link AsciiDoctorTextArea}
     * @param start  selection start
     * @param end    selection end
     * @return return entire word which following selection belong
     */
    private static String getCurrentWord(AsciiDoctorTextArea editor, int start, int end) {
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
                final char c = text.charAt(0);
                if (isWhitespace(c)) {
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
            final char c = text.charAt(0);
            if (isWhitespace(c)) {
                break;
            }
            builder.append(c);
            from = to;
            to += 1;
        }

        return builder.toString();
    }

    private static String getCurrentWordNonSelection(AsciiDoctorTextArea editor, int paragraph, int column) {
        final String text = editor.getText(paragraph);
        if ((text == null) || (column >= text.length())) {
            return null;
        }
        final char ch = text.charAt(column);
        if (isWhitespace(ch)) {
            return null;
        }

        final StringBuilder builder = new StringBuilder();

        int from = column - 1;
        int to = column;
        if (from >= 0) {
            while (true) {
                String subText = text.substring(from, to).trim();
                if (subText.isEmpty()) {
                    break;
                }
                final char c = subText.charAt(0);
                if (isWhitespace(c)) {
                    break;
                }
                builder.insert(0, c);
                to = from;
                from -= 1;
                if (from < 0) {
                    break;
                }
            }
        }

        from = column;
        to = column + 1;
        if (to < text.length()) {
            while (true) {
                String subText = text.substring(from, to).trim();
                if (subText.isEmpty()) {
                    break;
                }
                final char c = subText.charAt(0);
                if (isWhitespace(c)) {
                    break;
                }
                builder.append(c);
                from = to;
                to += 1;
                if (to > text.length()) {
                    break;
                }
            }
        }

        return builder.toString();
    }

    public static String convertToUnixFilePath(String srcPath) {
        return srcPath.replace(File.separator, "/");
    }

    public static String getRelativePathString(String basePath, String path) {
        final String relativePath = AppUtil.toRelativePath(basePath, path).toString();
        return convertToUnixFilePath(relativePath);
    }

    /**
     * Test whether the ch{@code ch} is either a whitespace character or one of punctuation.
     *
     * @param ch the character to be tested.
     * @return {@code true} if the character is a Java whitespace or one of punctuation
     * character; {@code false} otherwise.
     * @see Character#isWhitespace(char)
     */
    private static boolean isWhitespace(char ch) {
        Matcher matcher = PUNCTUATION_PATTERN.matcher(String.valueOf(ch));
        return matcher.find() || Character.isWhitespace(ch);
    }

    private static AsciiDocMarkup getAsciiDocMarkup() {
        if (asciiDocMarkup == null) {
            asciiDocMarkup = ApplicationContextProvider.getBean(AsciiDocMarkup.class);
        }
        return asciiDocMarkup;
    }

}
