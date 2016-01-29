package com.alphasystem.app.asciidoctoreditor.ui.control.skin;

import com.alphasystem.app.asciidoctoreditor.ui.control.NewDocumentView;
import com.alphasystem.app.asciidoctoreditor.ui.model.DocumentType;
import com.alphasystem.app.asciidoctoreditor.ui.model.IconFontName;
import com.alphasystem.app.asciidoctoreditor.ui.model.Icons;
import com.alphasystem.jfx.control.builder.BuilderFactory;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

import java.io.File;

import static com.alphasystem.app.asciidoctoreditor.ui.ApplicationController.*;
import static com.alphasystem.app.asciidoctoreditor.ui.control.NewDocumentView.DEFAULT_STYLES_DIR;
import static com.alphasystem.app.asciidoctoreditor.ui.control.util.UIHelper.getLabel;
import static com.alphasystem.app.asciidoctoreditor.ui.control.util.UIHelper.getToolTipText;
import static com.alphasystem.util.AppUtil.USER_HOME_DIR;
import static de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon.INFO_CIRCLE;
import static javafx.geometry.Pos.CENTER;
import static javafx.scene.control.ContentDisplay.RIGHT;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * @author sali
 */
public class NewDocumentSkin extends SkinBase<NewDocumentView> {

    public static final String REQUIRED_PROPERTY_STYLE = "-fx-font-weight: bold";
    public static final int PREF_COLUMN_COUNT = 20;
    private static final BuilderFactory BUILDER_FACTORY = BuilderFactory.getInstance();

    private final DirectoryChooser directoryChooser = new DirectoryChooser();
    private final FileChooser fileChooser = new FileChooser();

    public NewDocumentSkin(NewDocumentView control) {
        super(control);

        directoryChooser.setInitialDirectory(USER_HOME_DIR);
        fileChooser.setInitialDirectory(USER_HOME_DIR);
        fileChooser.getExtensionFilters().addAll(new ExtensionFilter("CSS File", "*.css"));

        initializeSkin();
    }

    private static ComboBox<DocumentType> createDocTypeComboBox(NewDocumentView view, Label label) {
        ComboBox<DocumentType> docTypeComboBox = new ComboBox<>(DOCUMENT_TYPES);
        docTypeComboBox.valueProperty().bindBidirectional(view.documentTypeProperty());
        label.setLabelFor(docTypeComboBox);
        return docTypeComboBox;
    }

    private static TextField createTextField(boolean disable, Label label) {
        String disableStyle = disable ? "-fx-background-color: lightgray;" : null;
        final TextField source = BUILDER_FACTORY.newTextFieldBuilder().prefColumnCount(PREF_COLUMN_COUNT)
                .disable(disable).style(disableStyle).getSource();
        label.setLabelFor(source);
        return source;
    }

    private static FontAwesomeIconView createInfoIcon() {
        FontAwesomeIconView icon = new FontAwesomeIconView();
        icon.setGlyphName(INFO_CIRCLE.name());
        icon.setGlyphStyle("-fx-fill: blue;");
        icon.setSize("1.0em");
        return icon;
    }

    private static Tooltip createTooltip(String toolTipText) {
        Tooltip toolTip = null;
        if (toolTipText != null) {
            toolTip = BUILDER_FACTORY.newTooltipBuilder().text(toolTipText).getSource();
        }
        return toolTip;
    }

    private static Label createLabel(String text, String toolTipText, String style) {
        return BUILDER_FACTORY.newLabelBuilder().text(text).graphic(createInfoIcon()).contentDisplay(RIGHT)
                .tooltip(createTooltip(toolTipText)).style(style).getSource();
    }

    private static Label createLabel(String text, String toolTipText) {
        return createLabel(text, toolTipText, null);
    }


    private void initializeSkin() {
        BorderPane pane = new BorderPane();
        pane.setStyle("-fx-background-color: transparent;-fx-border-color: lightgray;-fx-border-width: 1px;-fx-padding: 5;-fx-spacing: 4;");

        NewDocumentView view = getSkinnable();

        GridPane gridPane = new GridPane();
        gridPane.setAlignment(CENTER);
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(10));

        final String gap = "    ";

        // First Set
        // 1st set 1st column
        int column = 0;
        int row = -1;
        Label label = createLabel(getLabel("documentType"), getToolTipText("documentType"), REQUIRED_PROPERTY_STYLE);
        gridPane.add(label, column, row + 1);
        gridPane.add(createDocTypeComboBox(view, label), column, row + 2);

        // 1st set 2nd column
        column++;
        gridPane.add(new Label(gap), column, row + 1);
        gridPane.add(new Label(gap), column, row + 1);

        // 1st set 3rd column
        column++;
        label = createLabel(getLabel("documentName"), getToolTipText("documentName"), REQUIRED_PROPERTY_STYLE);
        gridPane.add(label, column, row + 1);
        TextField textField = createTextField(false, label);
        textField.textProperty().bindBidirectional(view.documentNameProperty());
        gridPane.add(textField, column, row + 2);

        // 1st set 4th column
        column++;
        gridPane.add(new Label(gap), column, row + 1);
        gridPane.add(new Label(gap), column, row + 2);

        // Second set
        // 2nd set 1st column
        column = 0;
        row += 2;
        label = createLabel(getLabel("documentDirectory"), getToolTipText("documentDirectory"), REQUIRED_PROPERTY_STYLE);
        gridPane.add(label, column, row + 1);

        final TextField baseDirectoryField = createTextField(true, label);
        baseDirectoryField.textProperty().bindBidirectional(view.baseDirProperty());
        gridPane.add(baseDirectoryField, column, row + 2);

        // 2nd set 2nd column
        column++;
        gridPane.add(new Label(gap), column, row + 1);
        Button button = new Button("    ...    ");
        button.setOnAction(event -> {
            selectBaseDirectory(baseDirectoryField);
        });
        gridPane.add(button, column, row + 2);

        // 2nd set 3rd column
        column++;
        label = createLabel(getLabel("documentTitle"), getToolTipText("documentTitle"), REQUIRED_PROPERTY_STYLE);
        gridPane.add(label, column, row + 1);
        textField = createTextField(false, label);
        textField.textProperty().bindBidirectional(view.documentTitleProperty());
        gridPane.add(textField, column, row + 2);

        // 2nd set 4th column
        column++;
        gridPane.add(new Label(gap), column, row + 1);
        gridPane.add(new Label(gap), column, row + 2);

        // Third Set
        // 3rd set 1st column
        column = 0;
        row += 2;
        label = createLabel(getLabel("styleSheetDirectory"), getToolTipText("styleSheetDirectory"));
        gridPane.add(label, column, row + 1);
        final TextField stylesDirField = createTextField(false, label);
        stylesDirField.textProperty().bindBidirectional(view.stylesDirProperty());
        stylesDirField.textProperty().addListener((o, ov, nv) -> {
            final boolean blank = isBlank(nv);
            if (blank) {
                stylesDirField.setText(DEFAULT_STYLES_DIR);
            }
        });
        gridPane.add(stylesDirField, column, row + 2);

        // 3rd set 2nd column
        column++;
        label = createLabel(getLabel("linkCSS"), getToolTipText("linkCSS"));
        gridPane.add(label, column, row + 1);
        CheckBox checkBox = new CheckBox();
        checkBox.selectedProperty().bindBidirectional(view.linkCssProperty());
        gridPane.add(checkBox, column, row + 2);

        // 3rd set 3rd column
        column++;
        label = createLabel(getLabel("customStyleSheet"), getToolTipText("customStyleSheet"));
        gridPane.add(label, column, row + 1);
        final TextField styleSheetField = createTextField(false, label);
        styleSheetField.textProperty().bindBidirectional(view.customStyleSheetFileProperty());
        gridPane.add(styleSheetField, column, row + 2);

        // 3rd set 4th column
        column++;
        gridPane.add(new Label(gap), column, row + 1);
        button = new Button("    ...    ");
        button.disableProperty().bind(baseDirectoryField.textProperty().isNull());
        button.setOnAction(event -> {
            selectCustomStyleSheet(styleSheetField);
        });
        gridPane.add(button, column, row + 2);

        // Fourth Set
        // 4th set 1st column
        column = 0;
        row += 2;
        label = createLabel(getLabel("icons"), getToolTipText("icons"));
        gridPane.add(label, column, row + 1);
        final ComboBox<Icons> iconsComboBox = new ComboBox<>(ICONS_TYPES);
        iconsComboBox.valueProperty().bindBidirectional(view.iconsProperty());
        label.setLabelFor(iconsComboBox);
        gridPane.add(iconsComboBox, column, row + 2);

        column++;
        gridPane.add(new Label(gap), column, row + 1);
        gridPane.add(new Label(gap), column, row + 2);

        column++;
        label = createLabel(getLabel("iconsFontName"), getToolTipText("iconsFontName"));
        gridPane.add(label, column, row + 1);
        final ComboBox<IconFontName> iconsFontNameComboBox = new ComboBox<>(ICON_FONT_NAME_TYPES);
        iconsFontNameComboBox.valueProperty().bindBidirectional(view.iconFontNameProperty());
        label.setLabelFor(iconsFontNameComboBox);
        gridPane.add(iconsFontNameComboBox, column, row + 2);

        iconsComboBox.valueProperty().addListener((o, ov, nv) -> {
            boolean disable = nv == null || Icons.DEFAULT.equals(nv);
            if (disable) {
                iconsFontNameComboBox.getSelectionModel().select(0);
            }
            iconsFontNameComboBox.setDisable(disable);
        });
        iconsFontNameComboBox.setDisable(true);

        column = 0;
        row += 2;
        checkBox = BUILDER_FACTORY.newCheckBoxBuilder().text(getLabel("omitLastUpdatedTimeStamp")).graphic(createInfoIcon())
                .contentDisplay(RIGHT).tooltip(createTooltip(getToolTipText("omitLastUpdatedTimeStamp"))).getSource();
        checkBox.selectedProperty().bindBidirectional(view.omitLastUpdatedTimeStampProperty());
        gridPane.add(checkBox, column, row + 1);

        pane.setCenter(gridPane);
        getChildren().addAll(pane);
    }

    private void selectCustomStyleSheet(TextField styleSheetField) {
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

    private void selectBaseDirectory(TextField baseDirectoryField) {
        final File directory = directoryChooser.showDialog(getSkinnable().getScene().getWindow());
        if (directory != null) {
            baseDirectoryField.setText(directory.getPath());
        }
    }

}
