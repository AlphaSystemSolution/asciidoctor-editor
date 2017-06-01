package com.alphasystem.app.asciidoctoreditor.ui.controller;

import java.awt.Desktop;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Optional;

import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.Window;

import com.alphasystem.app.asciidoctoreditor.ui.ApplicationController;
import com.alphasystem.app.asciidoctoreditor.ui.control.AsciiDoctorEditor;
import com.alphasystem.app.asciidoctoreditor.ui.control.AsciiDoctorEditorView;
import com.alphasystem.app.asciidoctoreditor.ui.control.AsciiDoctorTextArea;
import com.alphasystem.app.asciidoctoreditor.ui.control.NewDocumentDialog;
import com.alphasystem.app.asciidoctoreditor.ui.model.Action;
import com.alphasystem.app.asciidoctoreditor.ui.model.ApplicationConstants;
import com.alphasystem.app.asciidoctoreditor.ui.model.ApplicationMode;
import com.alphasystem.app.asciidoctoreditor.ui.model.EditorState;
import com.alphasystem.arabic.ui.keyboard.ArabicKeyboard;
import com.alphasystem.asciidoc.model.AsciiDocumentInfo;
import com.alphasystem.asciidoc.model.Backend;
import com.alphasystem.spring.support.ApplicationContextProvider;

import static com.alphasystem.app.asciidoctoreditor.ui.model.Action.NEW;
import static com.alphasystem.app.asciidoctoreditor.ui.model.Action.OPEN;
import static com.alphasystem.app.asciidoctoreditor.ui.model.Action.SAVE;
import static com.alphasystem.app.asciidoctoreditor.ui.model.ApplicationMode.EMBEDDED;
import static com.alphasystem.app.asciidoctoreditor.ui.model.ApplicationMode.STANDALONE;
import static com.alphasystem.asciidoc.model.Backend.DOC_BOOK;
import static com.alphasystem.asciidoc.model.Backend.HTML;
import static com.alphasystem.asciidoc.model.Backend.WORD;
import static com.alphasystem.fx.ui.util.UiUtilities.defaultCursor;
import static com.alphasystem.fx.ui.util.UiUtilities.waitCursor;
import static com.alphasystem.util.AppUtil.USER_HOME_DIR;
import static com.alphasystem.util.AppUtil.isInstanceOf;
import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * @author sali
 */
public class AsciiDoctorEditorController implements ApplicationConstants {

    private final FileChooser fileChooser = new FileChooser();
    private final ApplicationController applicationController = ApplicationContextProvider.getBean(ApplicationController.class);
    private final NewDocumentDialog newDocumentDialog = new NewDocumentDialog();
    private final Popup keyboardPopup = new Popup();
    private final ArabicKeyboard keyboardView;
    private final ObjectProperty<File> initialFile = new SimpleObjectProperty<>(null, "initialFile");
    private final ReadOnlyBooleanWrapper active = new ReadOnlyBooleanWrapper(false, "active");
    private ObjectProperty<ApplicationMode> applicationMode = new SimpleObjectProperty<>(null, "applicationMode");
    private AsciiDoctorEditor view;
    private AsciiDoctorEditorView currentEditorView;
    private AsciiDoctorTextArea currentEditor;
    private int startCaretPosition = -1;

    @FXML private TabPane tabPane;

    @FXML private MenuBar menuBar;

    @FXML private ToolBar toolBar;

    @FXML private MenuItem newDocumentMenuItem;

    @FXML private Button newDocumentButton;

    @FXML private MenuItem openDocumentMenuItem;

    @FXML private Button openDocumentButton;

    @FXML private MenuItem saveDocumentMenuItem;

    @FXML private Button saveDocumentButton;

    @FXML private Menu exportMenu;

    @FXML private MenuButton exportButton;

    @FXML private MenuItem exportToHtmlMenuItem;

    @FXML private MenuItem exportToHtmlButton;

    @FXML private MenuItem exportToDocBookMenuItem;

    @FXML private MenuItem exportToDocBookButton;

    @FXML private MenuItem exportToWordMenuItem;

    @FXML private MenuItem exportToWordButton;

    @FXML private MenuItem exitMenuItem;

    @FXML private MenuItem undoMenuItem;

    @FXML private Button undoButton;

    @FXML private MenuItem redoMenuItem;

    @FXML private Button redoButton;

    @FXML private MenuItem cutMenuItem;

    @FXML private Button cutButton;

    @FXML private MenuItem copyMenuItem;

    @FXML private Button copyButton;

    @FXML private MenuItem pasteMenuItem;

    @FXML private Button pasteButton;

    @FXML private CheckMenuItem boldMenuItem;

    @FXML private ToggleButton boldButton;

    @FXML private CheckMenuItem italicMenuItem;

    @FXML private ToggleButton italicButton;

    @FXML private CheckMenuItem underlineMenuItem;

    @FXML private ToggleButton underlineButton;

    @FXML private MenuItem strikethroughMenuItem;

    @FXML private Button strikethroughButton;

    @FXML private MenuItem subscriptMenuItem;

    @FXML private Button subscriptButton;

    @FXML private MenuItem superscriptMenuItem;

    @FXML private Button superscriptButton;

    @FXML private MenuItem headerMenuItem;

    @FXML private Button headerButton;

    @FXML private MenuItem linkMenuItem;

    @FXML private Button linkButton;

    @FXML private MenuItem codeMenuItem;

    @FXML private Button codeButton;

    @FXML private Menu admonitionMenu;

    @FXML private MenuButton admonitionButton;

    @FXML private MenuItem noteMenuItem;

    @FXML private MenuItem noteButton;

    @FXML private MenuItem tipMenuItem;

    @FXML private MenuItem tipButton;

    @FXML private MenuItem importantMenuItem;

    @FXML private MenuItem importantButton;

    @FXML private MenuItem cautionMenuItem;

    @FXML private MenuItem cautionButton;

    @FXML private MenuItem warningMenuItem;

    @FXML private MenuItem warningButton;

    @FXML private Menu blocksMenu;

    @FXML private MenuButton blocksButton;

    @FXML private MenuItem sideBarMenuItem;

    @FXML private MenuItem sideBarButton;

    @FXML private MenuItem exampleMenuItem;

    @FXML private MenuItem exampleButton;

    @FXML private MenuItem passThroughMenuItem;

    @FXML private MenuItem passThroughButton;

    @FXML private MenuItem quoteMenuItem;

    @FXML private MenuItem quoteButton;

    @FXML private MenuButton htmlEntitiesButton;

    @FXML private MenuItem spaceButton;

    @FXML private MenuItem dashButton;

    @FXML private MenuItem copyrightButton;

    @FXML private MenuItem registeredButton;

    @FXML private Button keyboardButton;

    public AsciiDoctorEditorController() {
        setApplicationMode(null);
        active.set(false);
        fileChooser.setInitialDirectory(USER_HOME_DIR);
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("ADOC File", "*.adoc"));

        keyboardView = new ArabicKeyboard();
        keyboardPopup.setAutoHide(true);
        keyboardPopup.setHideOnEscape(true);
        keyboardPopup.setOnAutoHide(event -> {
            startCaretPosition = -1;
            keyboardView.setClearText(true);
        });
        keyboardPopup.setOnHiding(event -> {
            startCaretPosition = -1;
            keyboardView.setClearText(true);
        });
        keyboardPopup.getContent().add(keyboardView);

        initialFile.addListener((o, ov, nv) -> updateFile(nv));
        applicationMode.addListener((o, ov, nv) -> updateApplicationMode(nv));
        keyboardView.htmlCodeValueProperty().addListener((o, ov, nv) -> {
            if (nv == null) {
                return;
            }
            int oldTextLength = isBlank(ov) ? -1 : ov.length();
            int end = startCaretPosition + oldTextLength;
            if (oldTextLength > -1 && end < currentEditor.getText().length()) {
                try {
                    currentEditor.replaceText(startCaretPosition, end, nv);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                currentEditor.insertText(startCaretPosition, nv);
            }
        });

    }

    public final ObjectProperty<File> initialFileProperty() {
        return initialFile;
    }

    public final void setApplicationMode(final ApplicationMode applicationMode) {
        this.applicationMode.set((applicationMode == null) ? STANDALONE : applicationMode);
    }

    public final void setView(final AsciiDoctorEditor view) {
        this.view = view;
    }

    @FXML
    void initialize() {
        tabPane.getSelectionModel().selectedItemProperty().addListener((o, ov, nv) -> {
            if (nv == null) {
                active.set(false);
            } else {
                currentEditorView = (AsciiDoctorEditorView) nv.getContent();
                currentEditor = currentEditorView.getEditor();
                active.set(currentEditor != null);
                currentEditorView.previewSelectedProperty().addListener((o1, ov1, nv2) -> active.set(!nv2));
            }
        });

        exportToHtmlMenuItem.setUserData(HTML);
        exportToHtmlButton.setUserData(HTML);
        exportToDocBookMenuItem.setUserData(DOC_BOOK);
        exportToDocBookButton.setUserData(DOC_BOOK);
        exportToWordMenuItem.setUserData(WORD);
        exportToWordButton.setUserData(WORD);

        noteMenuItem.setUserData(NOTE_KEY);
        noteButton.setUserData(NOTE_KEY);
        tipMenuItem.setUserData(TIP_KEY);
        tipButton.setUserData(TIP_KEY);
        importantMenuItem.setUserData(IMPORTANT_KEY);
        importantButton.setUserData(IMPORTANT_KEY);
        cautionMenuItem.setUserData(CAUTION_KEY);
        cautionButton.setUserData(CAUTION_KEY);
        warningMenuItem.setUserData(WARNING_KEY);
        warningButton.setUserData(WARNING_KEY);

        spaceButton.setUserData(SPACE_KEY);
        dashButton.setUserData(DASH_KEY);
        copyrightButton.setUserData(COPYRIGHT_KEY);
        registeredButton.setUserData(REGISTERED_KEY);

        sideBarMenuItem.setUserData(SIDE_BAR_KEY);
        sideBarButton.setUserData(SIDE_BAR_KEY);
        exampleMenuItem.setUserData(EXAMPLE_KEY);
        exampleButton.setUserData(EXAMPLE_KEY);
        passThroughMenuItem.setUserData(PASS_THROUGH_KEY);
        passThroughButton.setUserData(PASS_THROUGH_KEY);
        quoteMenuItem.setUserData(QUOTE_KEY);
        quoteButton.setUserData(QUOTE_KEY);

        final BooleanBinding notActive = active.not();
        saveDocumentMenuItem.disableProperty().bind(notActive);
        saveDocumentButton.disableProperty().bind(notActive);
        exportMenu.disableProperty().bind(notActive);
        exportButton.disableProperty().bind(notActive);
        undoMenuItem.disableProperty().bind(notActive);
        undoButton.disableProperty().bind(notActive);
        redoMenuItem.disableProperty().bind(notActive);
        redoButton.disableProperty().bind(notActive);
        cutMenuItem.disableProperty().bind(notActive);
        cutButton.disableProperty().bind(notActive);
        copyMenuItem.disableProperty().bind(notActive);
        copyButton.disableProperty().bind(notActive);
        pasteMenuItem.disableProperty().bind(notActive);
        pasteButton.disableProperty().bind(notActive);
        boldMenuItem.disableProperty().bind(notActive);
        boldButton.disableProperty().bind(notActive);
        italicMenuItem.disableProperty().bind(notActive);
        italicButton.disableProperty().bind(notActive);
        underlineMenuItem.disableProperty().bind(notActive);
        underlineButton.disableProperty().bind(notActive);
        strikethroughMenuItem.disableProperty().bind(notActive);
        strikethroughButton.disableProperty().bind(notActive);
        subscriptMenuItem.disableProperty().bind(notActive);
        subscriptButton.disableProperty().bind(notActive);
        superscriptMenuItem.disableProperty().bind(notActive);
        superscriptButton.disableProperty().bind(notActive);
        headerMenuItem.disableProperty().bind(notActive);
        headerButton.disableProperty().bind(notActive);
        linkMenuItem.disableProperty().bind(notActive);
        linkButton.disableProperty().bind(notActive);
        codeMenuItem.disableProperty().bind(notActive);
        codeButton.disableProperty().bind(notActive);
        admonitionMenu.disableProperty().bind(notActive);
        admonitionButton.disableProperty().bind(notActive);
        blocksMenu.disableProperty().bind(notActive);
        blocksButton.disableProperty().bind(notActive);
        htmlEntitiesButton.disableProperty().bind(notActive);
        keyboardButton.disableProperty().bind(notActive);
    }

    // actions

    /**
     * Binds to open tool bar button and menu item.
     */
    @FXML
    public void newDocumentAction() {
        final Optional<AsciiDocumentInfo> result = newDocumentDialog.showAndWait();
        result.ifPresent(asciiDocPropertyInfo -> {
            waitCursor(view);
            EventHandler<WorkerStateEvent> onFailed = this::handleOnFailed;
            EventHandler<WorkerStateEvent> onSucceeded = event -> openAction((File) event.getSource().getValue());
            applicationController.doNewDocAction(asciiDocPropertyInfo, onFailed, onSucceeded);
            fireUpdateAction(NEW);
        });
    }

    /**
     * Binds to open tool bar button and menu item.
     */
    @FXML
    public void openAction() {
        final File file = fileChooser.showOpenDialog(view.getScene().getWindow());
        if (file == null) {
            // user might have cancel the dialog
            return;
        }
        fileChooser.setInitialDirectory(file.getParentFile());
        waitCursor(view);
        openAction(file);
        fireUpdateAction(OPEN);
    }

    /**
     * Delegates call to {@link ApplicationController#doOpenAction(File, EventHandler, EventHandler)} to open
     * document. Upon successful return an editor will be opened with the content of current file.
     *
     * @param file file to open
     * @see ApplicationController#doOpenAction(File, EventHandler, EventHandler)
     * @see #openDocument(File, String)
     */
    private void openAction(final File file) {
        EventHandler<WorkerStateEvent> onFailed = event -> defaultCursor(view);
        EventHandler<WorkerStateEvent> onSucceeded = event -> {
            try {
                openDocument(file, (String) event.getSource().getValue());
            } catch (IOException e) {
                throw new UncheckedIOException(e.getMessage(), e);
            } finally {
                defaultCursor(view);
            }
        };
        applicationController.doOpenAction(file, onFailed, onSucceeded);
    }

    /**
     * Opens a document editor. This method will be called from the <code>onSucceeded</code> call back of method
     * {@link #openAction(File)}.
     *
     * @param file    file to open, if null then creates a dummy file to open a new document
     * @param content content of the document
     * @throws FileNotFoundException if File to open is not found
     * @see #openAction(File)
     */
    private void openDocument(File file, String content) throws IOException {
        if (file == null || !file.exists()) {
            String path = file == null ? "Not provided" : file.getPath();
            throw new FileNotFoundException(format("File {%s} does not exists.", path));
        }
        addTab(applicationController.openDocument(file, content));
    }

    /**
     * Binds to save tool bar button and menu item.
     */
    @FXML
    public void saveAction() {
        saveAction(false);
        fireUpdateAction(SAVE);
    }

    /**
     * Saves current file. If flag <code>saveAs</code> is true or current base dir is null then file dialog will be
     * displayed to choose file.
     *
     * @param saveAs if <i>true</i> then file will be saved as different file (dialog will be displayed to choose new file,
     *               otherwise save current file.
     * @see ApplicationController#doSaveAction(File, String, EventHandler, EventHandler)
     */
    private void saveAction(boolean saveAs) {
        if (currentEditorView != null) {
            File destFile = currentEditorView.getPropertyInfo().getSrcFile();
            File baseDir = destFile.getParentFile();
            boolean showFileDialog = saveAs || baseDir == null;
            if (showFileDialog) {
                destFile = fileChooser.showSaveDialog(view.getScene().getWindow());
                if (destFile == null) {
                    // user might have cancel the dialog
                    return;
                }
            } // end of if "showFileDialog"
            EventHandler<WorkerStateEvent> onFailed = this::handleOnFailed;
            EventHandler<WorkerStateEvent> onSucceeded = event -> saveDocument();
            applicationController.doSaveAction(destFile, currentEditor.getText(), onFailed, onSucceeded);
        }
    }

    /**
     * Callback method of <code>onSucceeded</code> of {@link ApplicationController#doSaveAction(File, String, EventHandler, EventHandler)}.
     * This is dummy method does nothing since file must have been saved already.
     */
    private void saveDocument() {
        // do nothing file should have been saved already
    }

    @FXML
    public void exportAction(ActionEvent actionEvent) {
        final MenuItem source = (MenuItem) actionEvent.getSource();
        final Backend backend = (Backend) source.getUserData();
        waitCursor(view);
        EventHandler<WorkerStateEvent> onFailed = this::handleOnFailed;
        EventHandler<WorkerStateEvent> onSucceeded = event -> defaultCursor(view);
        applicationController.doExport(new AsciiDocumentInfo(currentEditorView.getPropertyInfo()),
                currentEditor.getText(), backend, onFailed, onSucceeded);
    }

    @FXML
    public void exportToWordAction() {
        waitCursor(view);
        EventHandler<WorkerStateEvent> onFailed = this::handleOnFailed;
        EventHandler<WorkerStateEvent> onSucceeded = event -> {
            defaultCursor(view);
            final Path path = (Path) event.getSource().getValue();
            try {
                Desktop.getDesktop().open(path.toFile());
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
        applicationController.doExportToWord(new AsciiDocumentInfo(currentEditorView.getPropertyInfo()),
                currentEditor.getText(), onFailed, onSucceeded);
    }

    private void handleOnFailed(WorkerStateEvent event) {
        defaultCursor(view);
        final Throwable ex = event.getSource().getException();
        throw new RuntimeException(ex.getMessage(), ex);
    }

    @FXML
    public void exitAction() {
        final Window window = view.getScene().getWindow();
        if (isInstanceOf(Stage.class, window)) {
            ((Stage) window).close();
        }
    }

    @FXML
    public void undoAction() {
        currentEditor.undo();
    }

    @FXML
    public void redoAction() {
        currentEditor.redo();
    }

    @FXML
    public void cutAction() {
        currentEditor.cut();
    }

    @FXML
    public void copyAction() {
        currentEditor.copy();
    }

    @FXML
    public void pasteAction() {
        currentEditor.paste();
    }

    @FXML
    public void boldAction() {
        applicationController.doBold(currentEditor);
    }

    @FXML
    public void italicAction() {
        applicationController.doItalic(currentEditor);
    }

    @FXML
    public void underlineAction() {
        applicationController.doUnderline(currentEditor);
    }

    @FXML
    public void strikethroughAction() {
        applicationController.doStrikeThrough(currentEditor);
    }

    @FXML
    public void subscriptAction() {
        applicationController.doSubscript(currentEditor);
    }

    @FXML
    public void superscriptAction() {
        applicationController.doSuperscript(currentEditor);
    }

    @FXML
    public void headerAction() {
        applicationController.doHeading(currentEditor);
    }

    @FXML
    public void linkAction() {
        applicationController.doLink(currentEditor);
    }

    @FXML
    public void codeAction() {
        applicationController.doCode(currentEditor);
    }

    @FXML
    public void admonitionAction(ActionEvent event) {
        final MenuItem source = (MenuItem) event.getSource();
        applicationController.doAdmonition(currentEditor, (String) source.getUserData());
    }

    @FXML
    public void blocksAction(ActionEvent event) {
        final MenuItem source = (MenuItem) event.getSource();
        applicationController.doBlock(currentEditor, (String) source.getUserData());
    }

    @FXML
    public void htmlSymbolsAction(ActionEvent event) {
        final MenuItem source = (MenuItem) event.getSource();
        applicationController.doHtmlSymbols(currentEditor, (String) source.getUserData());
    }

    @FXML
    public void showKeyboard(ActionEvent event) {
        if (keyboardPopup.isShowing()) {
            keyboardPopup.hide();
            startCaretPosition = -1;
        } else {
            startCaretPosition = currentEditor.getCaretPosition();
            Button button = (Button) event.getSource();
            final Bounds bounds = button.localToScreen(button.getBoundsInLocal());
            keyboardPopup.show(button, bounds.getMinX(), bounds.getMinY() + bounds.getHeight());
        }
    }

    // action helpers

    private void addTab(AsciiDoctorEditorView editorView) {
        updateToolBar(editorView);
        Tab tab = new Tab(editorView.getPropertyInfo().getSrcFile().getName(), editorView);
        tabPane.getTabs().add(tab);
        tabPane.getSelectionModel().select(tab);
        if (EMBEDDED.equals(applicationMode.get())) {
            view.setCenter(null);
            view.setCenter(editorView);
        }
        editorView.getEditor().end();
        editorView.getEditor().requestFocus();
        defaultCursor(view);
    }

    private void updateToolBar(AsciiDoctorEditorView editorView) {
        final EditorState editorState = editorView.getEditorState();
        editorState.boldProperty().addListener((observable, oldValue, newValue) -> {
            boldButton.setSelected(newValue);
            boldMenuItem.setSelected(newValue);
        });
        editorState.italicProperty().addListener((observable, oldValue, newValue) -> {
            italicButton.setSelected(newValue);
            italicMenuItem.setSelected(newValue);
        });
        editorState.underlineProperty().addListener((observable, oldValue, newValue) -> {
            underlineButton.setSelected(newValue);
            underlineMenuItem.setSelected(newValue);
        });
    }

    private void updateFile(File file) {
        final boolean validFile = file != null && file.exists();
        if (validFile) {
            openAction(file);
        } else {
            if (EMBEDDED.equals(applicationMode.get())) {
                String path = (file == null) ? "Not provided" : file.getPath();
                throw new RuntimeException(format("File {%s} has to be a valid file", path));
            }
        }
    }

    private void updateApplicationMode(ApplicationMode nv) {
        if (EMBEDDED.equals(nv)) {
            newDocumentButton.setDisable(true);
            openDocumentButton.setDisable(true);
            final VBox top = (VBox) view.getTop();
            top.getChildren().clear();
            top.getChildren().add(toolBar);
        }
    }

    private void fireUpdateAction(Action action) {
        view.setAction(null);
        view.setAction(action);
    }
}
