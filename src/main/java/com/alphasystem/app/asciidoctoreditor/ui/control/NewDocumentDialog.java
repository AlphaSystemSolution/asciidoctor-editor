package com.alphasystem.app.asciidoctoreditor.ui.control;

import com.alphasystem.asciidoc.model.AsciiDocumentInfo;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;

import java.io.File;
import java.util.Optional;

import static java.lang.String.format;
import static javafx.event.ActionEvent.ACTION;
import static javafx.scene.control.Alert.AlertType.WARNING;
import static javafx.scene.control.ButtonType.*;

/**
 * @author sali
 */
public class NewDocumentDialog extends Dialog<AsciiDocumentInfo> {

    private final NewDocumentView view = new NewDocumentView();

    public NewDocumentDialog() {
        setTitle("Create New Document");

        getDialogPane().setContent(view);
        getDialogPane().getButtonTypes().addAll(OK, CANCEL);
        final Button okButton = (Button) getDialogPane().lookupButton(OK);
        okButton.disableProperty().bind(view.needRequiredProperty());
        okButton.addEventFilter(ACTION, this::verfiySourceFile);
        setResultConverter(param -> {
            AsciiDocumentInfo propertyInfo = param.getButtonData().isCancelButton() ? null : view.getPropertyInfo();
            // reset dialog
            view.setPropertyInfo(new AsciiDocumentInfo());
            return propertyInfo;
        });
    }

    private void verfiySourceFile(ActionEvent event) {
        final File srcFile = view.getPropertyInfo().getSrcFile();
        // if source files exists then bring dialog to make sure user really wanted to overwrite the file
        if (srcFile.exists()) {
            Alert alert = new Alert(WARNING);
            alert.getButtonTypes().clear();
            alert.getButtonTypes().addAll(YES, NO);
            String text1 = format("File \"%s\" already exists.", srcFile.getPath());
            alert.setHeaderText(text1);
            String text2 = "               Do you really want to overwrite this file?               ";
            alert.setContentText(text2);
            final Optional<ButtonType> result = alert.showAndWait();
            result.filter(NO::equals).ifPresent(no -> event.consume());
        }
    }

}
