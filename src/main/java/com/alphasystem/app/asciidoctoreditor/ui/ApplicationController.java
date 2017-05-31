package com.alphasystem.app.asciidoctoreditor.ui;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.scene.control.IndexRange;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.OptionsBuilder;
import org.asciidoctor.ast.StructuredDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alphasystem.app.asciidoctoreditor.ui.control.AsciiDoctorEditorView;
import com.alphasystem.app.asciidoctoreditor.ui.control.AsciiDoctorTextArea;
import com.alphasystem.app.asciidoctoreditor.ui.model.ApplicationConstants;
import com.alphasystem.app.asciidoctoreditor.ui.util.ApplicationHelper;
import com.alphasystem.asciidoc.model.AsciiDocumentInfo;
import com.alphasystem.asciidoc.model.Backend;

import static com.alphasystem.docbook.DocumentBuilder.buildDocument;
import static com.alphasystem.util.nio.NIOFileUtils.copyDir;
import static com.alphasystem.util.nio.NIOFileUtils.fastCopy;
import static java.lang.String.format;
import static java.nio.file.Files.newInputStream;
import static java.nio.file.Files.newOutputStream;
import static java.nio.file.Files.write;
import static java.nio.file.Paths.get;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.READ;
import static java.nio.file.StandardOpenOption.WRITE;
import static org.apache.commons.io.FilenameUtils.getBaseName;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * @author sali
 */
public final class ApplicationController implements ApplicationConstants {

    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("AsciiDoctorEditor");
    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationController.class);
    private static final String MARKUP_STYLE_NAME = "markup";
    private static final String PLACE_HOLDER_TEXT = "place holder";
    private static ApplicationController instance = new ApplicationController();

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

    private final Asciidoctor asciidoctor = Asciidoctor.Factory.create();

    private ApplicationController() {
    }

    public void doNewDocAction(final AsciiDocumentInfo propertyInfo, boolean skipCopyResources,
                               EventHandler<WorkerStateEvent> onFailed,
                               EventHandler<WorkerStateEvent> onSucceeded) {
        CopyResourcesService service = new CopyResourcesService(skipCopyResources, propertyInfo);
        service.setOnFailed(onFailed);
        service.setOnSucceeded(onSucceeded);
        service.start();
    }

    public void doNewDocAction(final AsciiDocumentInfo propertyInfo, EventHandler<WorkerStateEvent> onFailed,
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

    public void doExport(final AsciiDocumentInfo propertyInfo, final String content, final Backend backend,
                         EventHandler<WorkerStateEvent> onFailed, EventHandler<WorkerStateEvent> onSucceeded) {
        ExportDocumentService service = new ExportDocumentService(propertyInfo, content, backend);
        service.setOnFailed(onFailed);
        service.setOnSucceeded(onSucceeded);
        service.start();
    }

    public void doExportToWord(final AsciiDocumentInfo propertyInfo, final String content,
                               EventHandler<WorkerStateEvent> onFailed, EventHandler<WorkerStateEvent> onSucceeded) {
        ExportToWordService service = new ExportToWordService(propertyInfo, content);
        service.setOnFailed(onFailed);
        service.setOnSucceeded(onSucceeded);
        service.start();
    }

    public void doBold(AsciiDoctorTextArea editor) {
        doBoldOrItalic(editor, true);
    }

    public void doItalic(final AsciiDoctorTextArea editor) {
        doBoldOrItalic(editor, false);
    }

    private void doBoldOrItalic(AsciiDoctorTextArea editor, boolean bold) {
        String markupBeginBoundaryKey = bold ? BOLD_BOUNDARY_KEY : ITALIC_BOUNDARY_KEY;
        String markupBeginNonBoundaryKey = bold ? BOLD_NON_BOUNDARY_KEY : ITALIC_NON_BOUNDARY_KEY;
        boolean boundaryWord = ApplicationHelper.isEntireWordSelected(editor);
        String markupBegin = boundaryWord ? getMarkupBegin(markupBeginBoundaryKey) : getMarkupBegin(markupBeginNonBoundaryKey);
        String markupEnd = boundaryWord ? getMarkupEnd(markupBeginBoundaryKey) : getMarkupEnd(markupBeginNonBoundaryKey);
        int offset = boundaryWord ? 1 : 2;
        final String styleName = bold ? BOLD_KEY : ITALIC_KEY;
        applyMarkup(editor, styleName, markupBegin, markupEnd, offset);
    }

    public void doUnderline(final AsciiDoctorTextArea editor) {
        applyMarkup(editor, UNDERLINE_KEY, getMarkupBegin(UNDERLINE_KEY), getMarkupEnd(UNDERLINE_KEY), 1);
    }

    public void doStrikeThrough(final AsciiDoctorTextArea editor) {
        applyMarkup(editor, STRIKETHROUGH_KEY, getMarkupBegin(STRIKETHROUGH_KEY), getMarkupEnd(STRIKETHROUGH_KEY), 1);
    }

    public void doSubscript(final AsciiDoctorTextArea editor) {
        applyMarkup(editor, getMarkupBegin(SUBSCRIPT_KEY), getMarkupEnd(SUPERSCRIPT_KEY), 0);
    }

    public void doSuperscript(final AsciiDoctorTextArea editor) {
        applyMarkup(editor, getMarkupBegin(SUPERSCRIPT_KEY), getMarkupEnd(SUPERSCRIPT_KEY), 0);
    }

    public void doHeading(final AsciiDoctorTextArea editor) {
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

    public void doLink(final AsciiDoctorTextArea editor) {
        applyMarkup(editor, getMarkupBegin(LINK_KEY), getMarkupEnd(LINK_KEY), 13);
        editor.selectRange(editor.getAnchor(), editor.getCaretPosition() + 3);
    }

    public void doCode(final AsciiDoctorTextArea editor) {
        applyMarkup(editor, getMarkupBegin(SOURCE_CODE_KEY), getMarkupEnd(SOURCE_CODE_KEY), 13);
    }

    public void doAdmonition(final AsciiDoctorTextArea editor, final String admonitionTypeKey) {
        final String markupBegin = format("%s%s", getMarkupBegin(admonitionTypeKey), getMarkupBegin(ADMONITION_KEY));
        final String markupEnd = getMarkupEnd(ADMONITION_KEY);
        applyMarkup(editor, markupBegin, markupEnd, 6);
    }

    public void doHtmlSymbols(final AsciiDoctorTextArea editor, final String key) {
        editor.replaceSelection(getMarkupBegin(key));
    }

    public void doBlock(final AsciiDoctorTextArea editor, final String blockTypeKey) {
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

    private AsciiDocumentInfo readDocPropertyInfo(final File docFile) throws IOException {
        AsciiDocumentInfo propertyInfo = new AsciiDocumentInfo();
        propertyInfo.setSrcFile(docFile);
        File baseDir = docFile.getParentFile();
        if (baseDir != null) {
            // existing file
            StructuredDocument structuredDocument = asciidoctor.readDocumentStructure(docFile, new HashMap<>());
            propertyInfo.populateAttributes(structuredDocument.getHeader().getAttributes());
        }
        return propertyInfo;
    }

    public String refreshPreview(OptionsBuilder optionsBuilder, String content) {
        return asciidoctor.convert(content, optionsBuilder);
    }

    private void copyResources(final AsciiDocumentInfo propertyInfo) throws IOException, URISyntaxException {
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
                copyDir(get(stylesDir.getParent(), "fonts"), "templates/font-awesome/fonts", getClass());
            }
        }
    }

    private void copyStyleSheet(AsciiDocumentInfo propertyInfo, File stylesDir) throws IOException {
        final File srcStyleSheet = propertyInfo.getCustomStyleSheetFile();
        if (srcStyleSheet != null) {
            File targetStyleSheet = new File(stylesDir, srcStyleSheet.getName());
            try (InputStream inputStream = newInputStream(srcStyleSheet.toPath(), READ);
                 OutputStream outputStream = newOutputStream(targetStyleSheet.toPath(), WRITE, CREATE)) {
                fastCopy(inputStream, outputStream);
            }

        }
    }

    private void createNewDocument(AsciiDocumentInfo propertyInfo) throws IOException {
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

    private void applyMarkup(AsciiDoctorTextArea editor, String markupBegin, String markupEnd, int offset) {
        editor.replaceSelection(formatText(editor.getSelectedText(), markupBegin, markupEnd));
        if (isBlank(editor.getSelectedText())) {
            for (int i = 1; i <= offset; i++) {
                editor.backward();
            }
        }
        editor.requestFocus();
    }

    private void applyMarkup(AsciiDoctorTextArea editor, String styleName, String markupBegin, String markupEnd, int offset) {
        final IndexRange range = editor.getSelection();
        int start = -1;
        int end = 0;
        final boolean hasSelection = (range != null) && (range.getLength() > 0);
        if (hasSelection) {
            // get the start and end of current range
            start = range.getStart();
            end = range.getEnd();
        } else {
            // when there is no selected text
            start = editor.getCaretPosition();
            end = start + PLACE_HOLDER_TEXT.length();

            // if nothing was selected and user just clicked the button then insert the place holder text
            editor.insertText(start, PLACE_HOLDER_TEXT);
        }

        if (start < 0) {
            // don't know where to insert
            LOGGER.warn("Unable find start index of insertion for style: {}, start index: {}, end index: {}",
                    styleName, start, end);
            return;
        }

        // gets all the style at the current range
        // if there are other style(s) applied at this range then we won't override previous styles
        List<String> styles = new ArrayList<>();
        styles.addAll(editor.getStyleAtPosition(end - 1));

        if (styles.contains(styleName)) {
            // style is already exists, we need to remove it
            return;
        }

        // insert the markup begin and apply class
        editor.insertText(start, markupBegin);
        editor.setStyleClass(start, start + markupBegin.length(), MARKUP_STYLE_NAME);

        // update the start and end values
        start += markupBegin.length();
        end += markupBegin.length();

        // insert markup end
        editor.insertText(end, markupEnd);

        // apply the style to the actual text
        styles.add(styleName);
        editor.setStyle(start, end, styles);

        // apply the style for markup end
        editor.setStyleClass(end, end + markupEnd.length(), MARKUP_STYLE_NAME);

        if (hasSelection) {
            // re-select the text
            editor.selectRange(start, editor.getCaretPosition() - offset);
        }
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
                    return new String(Files.readAllBytes(docFile.toPath()));
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
                    return write(destFile.toPath(), content.getBytes()).toFile();
                }
            };
        }
    }

    private class CopyResourcesService extends Service<File> {

        private final boolean skipCopyResources;
        private final AsciiDocumentInfo propertyInfo;

        private CopyResourcesService(boolean skipCopyResources, final AsciiDocumentInfo propertyInfo) {
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

    private class ExportDocumentService extends Service<AsciiDocumentInfo> {

        private final AsciiDocumentInfo documentInfo;
        private final String content;

        private ExportDocumentService(AsciiDocumentInfo documentInfo, String content, Backend backend) {
            this.documentInfo = documentInfo;
            final File srcFile = documentInfo.getSrcFile();
            final String baseName = getBaseName(srcFile.getName());
            final String fileName = format("%s.%s", baseName, backend.getExtension());
            documentInfo.setBackend(backend.getValue());
            this.content = content;
        }

        @Override
        protected Task<AsciiDocumentInfo> createTask() {
            return new Task<AsciiDocumentInfo>() {
                @Override
                protected AsciiDocumentInfo call() throws Exception {
                    asciidoctor.convert(content, documentInfo.getOptionsBuilder());
                    return documentInfo;
                }
            };
        }
    }

    private class ExportToWordService extends Service<Path> {

        private final AsciiDocumentInfo documentInfo;
        private final String content;

        private ExportToWordService(final AsciiDocumentInfo documentInfo, final String content) {
            this.documentInfo = documentInfo;
            this.content = content;
        }

        @Override
        protected Task<Path> createTask() {
            return new Task<Path>() {
                @Override
                protected Path call() throws Exception {
                    return buildDocument(content, documentInfo);
                }
            };
        }
    }

}
