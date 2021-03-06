package com.alphasystem.app.asciidoctoreditor.ui.control.skin;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.SkinBase;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import org.controlsfx.control.CheckComboBox;

import com.alphasystem.app.asciidoctoreditor.ui.control.NewDocumentView;
import com.alphasystem.app.asciidoctoreditor.ui.model.DocInfoType;
import com.alphasystem.app.asciidoctoreditor.ui.model.DocumentType;
import com.alphasystem.app.asciidoctoreditor.ui.model.IconFontName;
import com.alphasystem.app.asciidoctoreditor.ui.model.Icons;

import static com.alphasystem.util.AppUtil.USER_HOME_DIR;
import static java.util.ResourceBundle.getBundle;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * @author sali
 */
public class NewDocumentSkin extends SkinBase<NewDocumentView> {

    public NewDocumentSkin(NewDocumentView control) {
        super(control);
        getChildren().setAll(new SkinView());
    }

    private class SkinView extends BorderPane {

        private final DirectoryChooser directoryChooser = new DirectoryChooser();
        private final FileChooser fileChooser = new FileChooser();

        @FXML
        private ComboBox<DocumentType> documentTypeComboBox;

        @FXML
        private TextField documentNameField;

        @FXML
        private TextField baseDirectoryField;

        @FXML
        private TextField documentTitleField;

        @FXML
        private TextField stylesDirField;

        @FXML
        private TextField styleSheetField;

        @FXML
        private TextField includeDirField;

        @FXML
        private TextField docInfoDirField;

        @FXML
        private ComboBox<Icons> iconsComboBox;

        @FXML
        private ComboBox<IconFontName> iconsFontNameComboBox;

        @FXML
        private CheckComboBox<DocInfoType> docInfoComboBox;

        @FXML
        private CheckBox omitLastUpdatedTimeStampCheckBox;

        SkinView() {
            directoryChooser.setInitialDirectory(USER_HOME_DIR);
            fileChooser.setInitialDirectory(USER_HOME_DIR);
            fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("CSS File", "*.css"));

            init();
        }

        private void init() {
            URL fxmlURL = getClass().getResource("/fxml/NewDocumentView.fxml");
            FXMLLoader fxmlLoader = new FXMLLoader(fxmlURL, getBundle("AsciiDoctorEditor"));
            fxmlLoader.setRoot(this);
            fxmlLoader.setController(this);
            try {
                fxmlLoader.load();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @FXML
        void initialize() {
            final NewDocumentView view = getSkinnable();

            final DocInfoType[] docInfoTypes = DocInfoType.values();
            docInfoComboBox.getItems().addAll(DocInfoType.SHARED_HEAD, DocInfoType.SHARED_FOOTER, DocInfoType.PRIVATE_HEAD, DocInfoType.PRIVATE_FOOTER);
            docInfoComboBox.getCheckModel().getCheckedIndices().addListener((ListChangeListener<? super Integer>) c -> {
                while (c.next()) {
                    if (c.wasAdded()) {
                        c.getAddedSubList().forEach(o -> view.getDocInfoTypes().add(docInfoTypes[o]));
                    }
                    if (c.wasRemoved()) {
                        c.getRemoved().forEach(o -> view.getDocInfoTypes().remove(docInfoTypes[o]));
                    }
                }
            });
            documentTypeComboBox.getSelectionModel().select(1);
            documentTypeComboBox.valueProperty().bindBidirectional(view.documentTypeProperty());
            documentNameField.textProperty().bindBidirectional(view.documentNameProperty());
            baseDirectoryField.textProperty().bindBidirectional(view.baseDirProperty());
            documentTitleField.textProperty().bindBidirectional(view.documentTitleProperty());
            stylesDirField.textProperty().bindBidirectional(view.stylesDirProperty());
            styleSheetField.textProperty().bindBidirectional(view.customStyleSheetFileProperty());
            includeDirField.textProperty().bindBidirectional(view.includeDirProperty());
            docInfoDirField.textProperty().bindBidirectional(view.docInfoDirProperty());
            iconsComboBox.valueProperty().bindBidirectional(view.iconsProperty());
            iconsFontNameComboBox.valueProperty().bindBidirectional(view.iconFontNameProperty());
            iconsComboBox.valueProperty().addListener((o, ov, nv) -> {
                boolean disable = nv == null || Icons.DEFAULT.equals(nv);
                if (disable) {
                    iconsFontNameComboBox.getSelectionModel().select(0);
                }
                iconsFontNameComboBox.setDisable(disable);
            });
            iconsFontNameComboBox.setDisable(true);
            omitLastUpdatedTimeStampCheckBox.selectedProperty().bindBidirectional(view.omitLastUpdatedTimeStampProperty());
        }

        @FXML
        void selectBaseDirectory() {
            final File directory = directoryChooser.showDialog(getSkinnable().getScene().getWindow());
            if (directory != null) {
                baseDirectoryField.setText(directory.getPath());
                directoryChooser.setInitialDirectory(directory);
            }
        }

        @FXML
        void selectStyleDirectory() {
            final File directory = directoryChooser.showDialog(getSkinnable().getScene().getWindow());
            if (directory != null) {
                stylesDirField.setText(directory.getPath());
            }
        }

        @FXML
        void selectCustomStyleSheet() {
            final NewDocumentView view = getSkinnable();
            final String customStyleSheetFileName = view.getCustomStyleSheetFile();
            if (isNotBlank(customStyleSheetFileName)) {
                File customStyleSheetFile = new File(customStyleSheetFileName);
                fileChooser.setInitialDirectory(customStyleSheetFile.getParentFile());
                fileChooser.setInitialFileName(customStyleSheetFile.getName());
            }
            final File file = fileChooser.showOpenDialog(view.getScene().getWindow());
            if (file != null) {
                styleSheetField.setText(file.getPath());
            }
        }

        @FXML
        void selectIncludeDirectory() {
            final File directory = directoryChooser.showDialog(getSkinnable().getScene().getWindow());
            if (directory != null) {
                includeDirField.setText(directory.getPath());
            }
        }

        @FXML
        void selectDocInfoDirectory() {
            final File directory = directoryChooser.showDialog(getSkinnable().getScene().getWindow());
            if (directory != null) {
                docInfoDirField.setText(directory.getPath());
            }
        }
    }
}
