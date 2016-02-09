package com.alphasystem.app.asciidoctoreditor.ui;

import com.alphasystem.app.asciidoctoreditor.ui.control.AsciiDoctorEditorView;
import com.alphasystem.app.asciidoctoreditor.ui.model.ApplicationConstants;
import com.alphasystem.app.asciidoctoreditor.ui.model.AsciiDocPropertyInfo;
import com.alphasystem.app.asciidoctoreditor.ui.model.Backend;
import com.alphasystem.fx.ui.Browser;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.scene.control.IndexRange;
import javafx.scene.control.TextArea;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.OptionsBuilder;
import org.asciidoctor.ast.DocumentHeader;
import org.asciidoctor.ast.StructuredDocument;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

import static com.alphasystem.util.AppUtil.NEW_LINE;
import static com.alphasystem.util.AppUtil.getResourceAsStream;
import static com.alphasystem.util.nio.NIOFileUtils.copyDir;
import static com.alphasystem.util.nio.NIOFileUtils.fastCopy;
import static java.lang.Character.isWhitespace;
import static java.lang.String.format;
import static java.nio.file.Files.*;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.nio.file.StandardOpenOption.*;
import static org.apache.commons.io.FilenameUtils.getBaseName;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * @author sali
 */
public final class ApplicationController implements ApplicationConstants {

    public static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("AsciiDoctorEditor");
    private static final String DEFAULT_PREVIEW_FILE_NAME = "preview";
    private static final String PREVIEW_FILE_PREFIX = format("_____%s_____", DEFAULT_PREVIEW_FILE_NAME);
    private static final String PREVIEW_FILE_SUFFIX = "_____.html";
    private static ApplicationController instance = new ApplicationController();

    private final Asciidoctor asciidoctor = Asciidoctor.Factory.create();

    private ApplicationController() {
    }

    public static ApplicationController getInstance() {
        return instance;
    }

    public static String getValue(String key, String defaultValue) {
        String value = null;
        try {
            value = RESOURCE_BUNDLE.getString(key);
        } catch (Exception e) {
            // ignore
        }
        value = (value == null) ? defaultValue : value;
        return value;
    }

    public static String getValue(String key) {
        return getValue(key, null);
    }

    private static String getMarkupBegin(String key) {
        return getValue(format("%s.markupBegin", key));
    }

    private static String getMarkupEnd(String key) {
        return getValue(format("%s.markupEnd", key), "");
    }

    private static String formatText(String source, String markupBegin, String markupEnd) {
        return format("%s%s%s", markupBegin, source, markupEnd);
    }

    public void doNewDocAction(final AsciiDocPropertyInfo propertyInfo, boolean skipCopyResources,
                               EventHandler<WorkerStateEvent> onFailed,
                               EventHandler<WorkerStateEvent> onSucceeded) {
        CopyResourcesService service = new CopyResourcesService(skipCopyResources, propertyInfo);
        service.setOnFailed(onFailed);
        service.setOnSucceeded(onSucceeded);
        service.start();
    }

    public void doNewDocAction(final AsciiDocPropertyInfo propertyInfo, EventHandler<WorkerStateEvent> onFailed,
                               EventHandler<WorkerStateEvent> onSucceeded) {
        doNewDocAction(propertyInfo, false, onFailed, onSucceeded);
    }

    public void doOpenAction(final File docFile, EventHandler<WorkerStateEvent> onFailed,
                             EventHandler<WorkerStateEvent> onSucceeded) {
        OpenDocService service = new OpenDocService(docFile);
        service.setOnFailed(onFailed);
        service.setOnSucceeded(onSucceeded);
        service.start();
    }

    public void doSaveAction(final File docFile, final String content, EventHandler<WorkerStateEvent> onFailed,
                             EventHandler<WorkerStateEvent> onSucceeded) {
        SaveDocService service = new SaveDocService(docFile, content);
        service.setOnFailed(onFailed);
        service.setOnSucceeded(onSucceeded);
        service.start();
    }

    public void doExport(final AsciiDocPropertyInfo propertyInfo, final String content, final Backend backend,
                         EventHandler<WorkerStateEvent> onFailed, EventHandler<WorkerStateEvent> onSucceeded) {
        ExportDocumentService service = new ExportDocumentService(propertyInfo, content, backend);
        service.setOnFailed(onFailed);
        service.setOnSucceeded(onSucceeded);
        service.start();
    }

    public void doBold(TextArea editor) {
        boolean boundaryWord = isBoundaryWord(editor);
        String markupBegin = boundaryWord ? getMarkupBegin(BOLD_BOUNDARY_KEY) : getMarkupBegin(BOLD_NON_BOUNDARY_KEY);
        String markupEnd = boundaryWord ? getMarkupEnd(BOLD_BOUNDARY_KEY) : getMarkupEnd(BOLD_NON_BOUNDARY_KEY);
        int offset = boundaryWord ? 1 : 2;
        applyMarkup(editor, markupBegin, markupEnd, offset);
    }

    public void doItalic(final TextArea editor) {
        boolean boundaryWord = isBoundaryWord(editor);
        String markupBegin = boundaryWord ? getMarkupBegin(ITALIC_BOUNDARY_KEY) : getMarkupBegin(ITALIC_NON_BOUNDARY_KEY);
        String markupEnd = boundaryWord ? getMarkupEnd(ITALIC_BOUNDARY_KEY) : getMarkupEnd(ITALIC_NON_BOUNDARY_KEY);
        int offset = boundaryWord ? 1 : 2;
        applyMarkup(editor, markupBegin, markupEnd, offset);
    }

    public void doUnderline(final TextArea editor) {
        applyMarkup(editor, getMarkupBegin(UNDERLINE_KEY), getMarkupEnd(UNDERLINE_KEY), 0);
    }

    public void doStrikeThrough(final TextArea editor) {
        applyMarkup(editor, getMarkupBegin(STRIKETHROUGH_KEY), getMarkupEnd(STRIKETHROUGH_KEY), 0);
    }

    public void doSubscript(final TextArea editor) {
        applyMarkup(editor, getMarkupBegin(SUBSCRIPT_KEY), getMarkupEnd(SUBSCRIPT_KEY), 0);
    }

    public void doSuperscript(final TextArea editor) {
        applyMarkup(editor, getMarkupBegin(SUPERSCRIPT_KEY), getMarkupEnd(SUPERSCRIPT_KEY), 0);
    }

    public void doHeading(final TextArea editor) {
        String text = editor.getText();
        final int caretPosition = editor.getCaretPosition();
        if (caretPosition < 0) {
            return;
        }
        final String textBeforeCaret = text.substring(0, caretPosition);
        int indexOfNewLine = textBeforeCaret.lastIndexOf('\n');
        final String currentLine = textBeforeCaret.substring(indexOfNewLine + 1);
        final String markup = getMarkupBegin(HEADER_KEY);
        final String markupSingle = format("%s ", markup);
        String insert = currentLine.startsWith(markup) ? markup : markupSingle;
        editor.insertText(indexOfNewLine + 1, insert);
    }

    public void doLink(final TextArea editor) {
        applyMarkup(editor, getMarkupBegin(LINK_KEY), getMarkupEnd(LINK_KEY), 13);
        editor.selectRange(editor.getAnchor(), editor.getCaretPosition() + 3);
    }

    public void doCode(final TextArea editor) {
        applyMarkup(editor, getMarkupBegin(SOURCE_CODE_KEY), getMarkupEnd(SOURCE_CODE_KEY), 13);
    }

    public void doAdmonition(final TextArea editor, final String admonitionTypeKey) {
        final String markupBegin = format("%s%s", getMarkupBegin(admonitionTypeKey), getMarkupBegin(ADMONITION_KEY));
        final String markupEnd = getMarkupEnd(ADMONITION_KEY);
        applyMarkup(editor, markupBegin, markupEnd, 6);
    }

    public void doHtmlSymbols(final TextArea editor, final String key) {
        editor.replaceSelection(getMarkupBegin(key));
    }

    public void doBlock(final TextArea editor, final String blockTypeKey) {
        applyMarkup(editor, getMarkupBegin(blockTypeKey), getMarkupEnd(blockTypeKey), 6);
    }

    public AsciiDoctorEditorView openDocument(final File docFile, final String content) throws IOException {
        if (docFile == null) {
            return null;
        }
        AsciiDoctorEditorView editorView = new AsciiDoctorEditorView();
        editorView.setContent(content);
        editorView.setPropertyInfo(readDocPropertyInfo(docFile));

        return editorView;
    }

    public AsciiDocPropertyInfo readDocPropertyInfo(final File docFile) throws IOException {
        AsciiDocPropertyInfo propertyInfo = new AsciiDocPropertyInfo();
        propertyInfo.setSrcFile(docFile);
        File baseDir = docFile.getParentFile();
        boolean existingFie = baseDir != null;
        if (existingFie) {
            // existing file
            StructuredDocument structuredDocument = asciidoctor.readDocumentStructure(docFile, new HashMap<>());
            DocumentHeader header = structuredDocument.getHeader();
            propertyInfo.populateAttributes(header.getAttributes());
        }
        createPreviewFile(propertyInfo, baseDir);
        return propertyInfo;
    }

    public void refreshPreview(OptionsBuilder optionsBuilder, String content, Browser browser) {
        asciidoctor.convert(content, optionsBuilder);
        browser.getWebEngine().reload();
    }

    private void createPreviewFile(AsciiDocPropertyInfo propertyInfo, File baseDir) throws IOException {
        // now populate preview file name, if preview file does not exists copy it
        try (InputStream inputStream = getResourceAsStream(format("templates.%s.html", DEFAULT_PREVIEW_FILE_NAME))) {
            File previewFile = createTempFile(baseDir.toPath(), PREVIEW_FILE_PREFIX, PREVIEW_FILE_SUFFIX).toFile();
            previewFile.deleteOnExit();
            copy(inputStream, previewFile.toPath(), REPLACE_EXISTING);
            propertyInfo.setPreviewFile(previewFile);
        }
    }

    private void copyResources(final AsciiDocPropertyInfo propertyInfo) throws IOException, URISyntaxException {
        File baseDir = new File(propertyInfo.getSrcFile().getParent());
        File stylesDir = new File(baseDir, propertyInfo.getStylesDir());
        if (!stylesDir.exists()) {
            stylesDir.mkdirs();
        }
        copyStyleSheet(propertyInfo, stylesDir);
        final String iconFontName = propertyInfo.getIconFontName();
        if (isNotBlank(iconFontName)) {
            if ("font-awesome".equals(iconFontName)) {
                copyDir(stylesDir.toPath(), "templates/font-awesome/css", getClass());
                copyDir(Paths.get(stylesDir.getParent(), "fonts"), "templates/font-awesome/fonts", getClass());
            }
        }
    }

    private void copyStyleSheet(AsciiDocPropertyInfo propertyInfo, File stylesDir) throws IOException {
        final File srcStyleSheet = propertyInfo.getCustomStyleSheetFile();
        if (srcStyleSheet != null) {
            File targetStyleSheet = new File(stylesDir, srcStyleSheet.getName());
            try (InputStream inputStream = newInputStream(srcStyleSheet.toPath(), READ);
                 OutputStream outputStream = newOutputStream(targetStyleSheet.toPath(), WRITE, CREATE)) {
                fastCopy(inputStream, outputStream);
            }

        }
    }

    private void createNewDocument(AsciiDocPropertyInfo propertyInfo) throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add(format("= %s", propertyInfo.getDocumentTitle()));
        lines.add(format(":doctype: %s", propertyInfo.getDocumentType()));
        lines.add(":encoding: utf-8");
        lines.add(":lang: en");
        final String stylesDir = propertyInfo.getStylesDir();
        if (stylesDir != null && !".".equals(stylesDir)) {
            lines.add(format(":stylesdir: %s", stylesDir));
        }
        final String icons = propertyInfo.getIcons();
        if (!isBlank(icons)) {
            lines.add(format(":icons: %s", icons));
        }
        final String iconFontName = propertyInfo.getIconFontName();
        if (iconFontName != null) {
            lines.add(format(":iconfont-name: %s", iconFontName));
            lines.add(":iconfont-remote!:");
        }
        final boolean linkCss = propertyInfo.isLinkCss();
        if (linkCss) {
            lines.add(":linkcss:");
        }
        final boolean docInfo2 = propertyInfo.isDocInfo2();
        if (docInfo2) {
            lines.add(":docinfo2:");
        }
        final boolean omitLastUpdatedTimeStamp = propertyInfo.isOmitLastUpdatedTimeStamp();
        if (omitLastUpdatedTimeStamp) {
            lines.add(":last-update-label!:");
        }
        lines.add("");
        lines.add("");
        write(propertyInfo.getSrcFile().toPath(), lines, CREATE, WRITE);
    }

    private boolean isBoundaryWord(TextArea editor) {
        IndexRange selection = editor.getSelection();
        boolean boundaryWord = true;
        try {
            final int selectionStart = selection.getStart();
            String text = editor.getText(selectionStart - 1, selectionStart);
            boundaryWord = isWhitespace(text.charAt(0));
            if (boundaryWord) {
                final int selectionEnd = selection.getEnd();
                text = editor.getText(selectionEnd, selectionEnd + 1);
                boundaryWord = isWhitespace(text.charAt(0));
            }
        } catch (Exception ex) {
            // ignore
        }
        return boundaryWord;
    }

    private void applyMarkup(TextArea editor, String markupBegin, String markupEnd, int offset) {
        editor.replaceSelection(formatText(editor.getSelectedText(), markupBegin, markupEnd));
        if (isBlank(editor.getSelectedText())) {
            for (int i = 1; i <= offset; i++) {
                editor.backward();
            }
        }
        editor.requestFocus();
    }

    private class OpenDocService extends Service<String> {

        private final File docFile;

        private OpenDocService(File docFile) {
            this.docFile = docFile;
        }

        @Override
        protected Task<String> createTask() {
            return new Task<String>() {
                @Override
                protected String call() throws Exception {
                    List<String> lines = null;
                    try {
                        lines = Files.readAllLines(Paths.get(docFile.toURI()));
                    } catch (IOException e) {
                        throw e;
                    }
                    StringBuilder builder = new StringBuilder();
                    builder.append(lines.get(0));
                    for (int i = 1; i < lines.size(); i++) {
                        builder.append(NEW_LINE).append(lines.get(i));
                    }
                    return builder.toString();
                }
            };
        }
    }

    private class SaveDocService extends Service<File> {

        private final File destFile;
        private final String content;

        private SaveDocService(File destFile, String content) {
            this.destFile = destFile;
            this.content = content;
        }

        @Override
        protected Task<File> createTask() {
            return new Task<File>() {
                @Override
                protected File call() throws Exception {
                    return write(destFile.toPath(), content.getBytes(), WRITE).toFile();
                }
            };
        }
    }

    private class CopyResourcesService extends Service<File> {

        private final boolean skipCopyResources;
        private final AsciiDocPropertyInfo propertyInfo;

        private CopyResourcesService(boolean skipCopyResources, final AsciiDocPropertyInfo propertyInfo) {
            this.skipCopyResources = skipCopyResources;
            this.propertyInfo = propertyInfo;
        }

        @Override
        protected Task<File> createTask() {
            return new Task<File>() {
                @Override
                protected File call() throws Exception {
                    if (!skipCopyResources) {
                        copyResources(propertyInfo);
                    }
                    createNewDocument(propertyInfo);
                    return propertyInfo.getSrcFile();
                }
            };
        }
    }

    private class ExportDocumentService extends Service<AsciiDocPropertyInfo> {

        private final AsciiDocPropertyInfo propertyInfo;
        private final String content;

        private ExportDocumentService(AsciiDocPropertyInfo propertyInfo, String content, Backend backend) {
            this.propertyInfo = propertyInfo;
            final File srcFile = propertyInfo.getSrcFile();
            final String baseName = getBaseName(srcFile.getName());
            final String fileName = format("%s.%s", baseName, backend.getExtension());
            propertyInfo.setPreviewFile(new File(srcFile.getParentFile(), fileName));
            propertyInfo.setBackend(backend.getValue());
            this.content = content;
        }

        @Override
        protected Task<AsciiDocPropertyInfo> createTask() {
            return new Task<AsciiDocPropertyInfo>() {
                @Override
                protected AsciiDocPropertyInfo call() throws Exception {
                    asciidoctor.convert(content, propertyInfo.getOptionsBuilder());
                    return propertyInfo;
                }
            };
        }
    }

}
