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

import org.apache.commons.lang3.StringUtils;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.OptionsBuilder;
import org.asciidoctor.ast.StructuredDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alphasystem.app.asciidoctoreditor.ui.control.AsciiDoctorEditorView;
import com.alphasystem.app.asciidoctoreditor.ui.control.AsciiDoctorTextArea;
import com.alphasystem.app.asciidoctoreditor.ui.model.ApplicationConstants;
import com.alphasystem.app.asciidoctoreditor.ui.model.AsciiDocMarkup;
import com.alphasystem.app.asciidoctoreditor.ui.model.AsciiDocMarkup.Markup;
import com.alphasystem.app.asciidoctoreditor.ui.model.EditorState;
import com.alphasystem.app.asciidoctoreditor.ui.util.ApplicationHelper;
import com.alphasystem.asciidoc.model.AsciiDocumentInfo;
import com.alphasystem.asciidoc.model.Backend;

import static com.alphasystem.app.asciidoctoreditor.ui.util.ApplicationHelper.convertToUnixFilePath;
import static com.alphasystem.app.asciidoctoreditor.ui.util.ApplicationHelper.getRelativePathString;
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
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * @author sali
 */
@Component
public final class ApplicationController implements ApplicationConstants {

    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("AsciiDoctorEditor");
    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationController.class);
    private static final String MARKUP_STYLE_NAME = "markup";
    private static final String PLACE_HOLDER_TEXT = "place holder";
    private final Asciidoctor asciidoctor = Asciidoctor.Factory.create();
    private EditorState currentEditorState;
    @Autowired private AsciiDocMarkup asciiDocMarkup;

    private static String getValue(String key, String defaultValue) {
        String value = null;
        try {
            value = RESOURCE_BUNDLE.getString(key);
        } catch (Exception e) {
            // ignore
        }
        value = (value == null) ? defaultValue : value;
        return value;
    }

    private static String getValue(String key) {
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

    public void setCurrentEditorState(EditorState currentEditorState) {
        this.currentEditorState = currentEditorState;
    }

    public void doNewDocAction(final AsciiDocumentInfo propertyInfo,
                               EventHandler<WorkerStateEvent> onFailed,
                               EventHandler<WorkerStateEvent> onSucceeded) {
        CopyResourcesService service = new CopyResourcesService(propertyInfo);
        service.setOnFailed(onFailed);
        service.setOnSucceeded(onSucceeded);
        service.start();
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
        boolean boundaryWord = ApplicationHelper.isEntireWordSelected(editor, currentEditorState);

        Markup markup;
        if (bold) {
            markup = boundaryWord ? asciiDocMarkup.getBold() : asciiDocMarkup.getBoldPartial();
        } else {
            markup = boundaryWord ? asciiDocMarkup.getItalic() : asciiDocMarkup.getItalicPartial();
        }

        int offset = boundaryWord ? 1 : 2;
        final String styleName = bold ? BOLD_KEY : ITALIC_KEY;
        applyMarkup(editor, styleName, markup, offset);
    }

    public void doUnderline(final AsciiDoctorTextArea editor) {
        applyMarkup(editor, UNDERLINE_KEY, asciiDocMarkup.getUnderline(), 1);
    }

    public void doStrikeThrough(final AsciiDoctorTextArea editor) {
        applyMarkup(editor, STRIKETHROUGH_KEY, asciiDocMarkup.getStrikeThrough(), 1);
    }

    public void doSubscript(final AsciiDoctorTextArea editor) {
        applyMarkup(editor, SUBSCRIPT_KEY, asciiDocMarkup.getSubscript(), 0);
    }

    public void doSuperscript(final AsciiDoctorTextArea editor) {
        applyMarkup(editor, SUPERSCRIPT_KEY, asciiDocMarkup.getSuperscript(), 0);
    }

    public void doHeading(final AsciiDoctorTextArea editor) {
        final int caretPosition = editor.getCaretPosition();
        if (caretPosition < 0) {
            return;
        }
        editor.selectLine();
        String currentLine = editor.getSelectedText();
        final String markup = asciiDocMarkup.getHeader().getMarkupBegin();
        // if there is no heading text then add a dummy one
        if (StringUtils.isEmpty(currentLine)) {
            currentLine = "Heading";
        }
        // there has to be a single space between end of mark up and actual heading text, if there is none then add it now
        if (!currentLine.startsWith(markup)) {
            currentLine = " " + currentLine;
        }
        editor.replaceSelection(markup + currentLine);
    }

    public void doLink(final AsciiDoctorTextArea editor) {
        applyMarkup(editor, LINK_KEY, asciiDocMarkup.getLink(), 1);
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

    public void doArabicStyles(final AsciiDoctorTextArea editor, final String style) {
        System.out.println(style);
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
        final String stylesDir1 = propertyInfo.getStylesDir();
        File stylesDir = null;
        if (stylesDir1 != null) {
            stylesDir = new File(baseDir, stylesDir1);
            if (!stylesDir.exists()) {
                @SuppressWarnings("unused") final boolean mkdirs = stylesDir.mkdirs();
            }
            final File customStyleSheetFile = propertyInfo.getCustomStyleSheetFile();
            if (customStyleSheetFile != null) {
                copyStyleSheet(customStyleSheetFile, stylesDir);
            }
        }


        final String iconFontName = propertyInfo.getIconFontName();
        if (stylesDir != null && isNotBlank(iconFontName)) {
            if ("font-awesome".equals(iconFontName)) {
                copyDir(stylesDir.toPath(), "templates/font-awesome/css", getClass());
                copyDir(get(stylesDir.getParent(), "fonts"), "templates/font-awesome/fonts", getClass());
            }
        }
    }

    private void copyStyleSheet(File srcStyleSheet, File stylesDir) throws IOException {
        File targetStyleSheet = new File(stylesDir, srcStyleSheet.getName());
        try (InputStream inputStream = newInputStream(srcStyleSheet.toPath(), READ);
             OutputStream outputStream = newOutputStream(targetStyleSheet.toPath(), WRITE, CREATE)) {
            fastCopy(inputStream, outputStream);
        }
    }

    private void createNewDocument(AsciiDocumentInfo propertyInfo) throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add(format("= %s", propertyInfo.getDocumentTitle()));
        lines.add(format(":doctype: %s", propertyInfo.getDocumentType()));
        lines.add(":encoding: utf-8");
        lines.add(":lang: en");
        final String baseDir = propertyInfo.getBaseDir();
        lines.add(format(":basedir: %s", convertToUnixFilePath(baseDir)));
        final String stylesDir = propertyInfo.getStylesDir();
        if (stylesDir != null) {
            lines.add(format(":stylesdir: file:/{basedir}/%s", getRelativePathString(baseDir, stylesDir)));
        }
        final String includeDir = propertyInfo.getIncludeDir();
        if (includeDir != null) {
            lines.add(format(":includedir: {basedir}/%s", getRelativePathString(baseDir, includeDir)));
        }
        final String docInfoDir = propertyInfo.getDocInfoDir();
        if (docInfoDir != null) {
            lines.add(format(":docinfodir: {basedir}/%s", getRelativePathString(baseDir, docInfoDir)));
        }
        final String docInfo = propertyInfo.getDocInfo();
        if (!StringUtils.isEmpty(docInfo)) {
            lines.add(format(":docinfo: %s", docInfo));
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
        } else {
            lines.add(":linkcss!:");
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

    private void applyMarkup(AsciiDoctorTextArea editor, String styleName, Markup markup, int offset) {
        final IndexRange range = editor.getSelection();
        int start;
        int end;
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

        String markupBegin = markup.getMarkupBegin();
        String markupEnd = markup.getMarkupEnd();

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

        private final AsciiDocumentInfo propertyInfo;

        private CopyResourcesService(final AsciiDocumentInfo propertyInfo) {
            this.propertyInfo = propertyInfo;
        }

        @Override
        protected Task<File> createTask() {
            return new Task<File>() {
                @Override
                protected File call() throws Exception {
                    copyResources(propertyInfo);
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
