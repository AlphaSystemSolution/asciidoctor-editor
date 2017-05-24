package com.alphasystem.app.asciidoctoreditor.ui.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 * @author sali
 */
public final class EditorState {

    private final BooleanProperty bold = new SimpleBooleanProperty(this, "bold");
    private final BooleanProperty italic = new SimpleBooleanProperty(this, "italic");
    private final BooleanProperty underline = new SimpleBooleanProperty(this, "underline");
    private final BooleanProperty strikeThrough = new SimpleBooleanProperty(this, "strikeThrough");
    private final BooleanProperty subScript = new SimpleBooleanProperty(this, "subScript");
    private final BooleanProperty superScript = new SimpleBooleanProperty(this, "superScript");

    public boolean isBold() {
        return bold.get();
    }

    public BooleanProperty boldProperty() {
        return bold;
    }

    public void setBold(boolean bold) {
        this.bold.set(bold);
    }

    public boolean isItalic() {
        return italic.get();
    }

    public BooleanProperty italicProperty() {
        return italic;
    }

    public void setItalic(boolean italic) {
        this.italic.set(italic);
    }

    public boolean isUnderline() {
        return underline.get();
    }

    public BooleanProperty underlineProperty() {
        return underline;
    }

    public void setUnderline(boolean underline) {
        this.underline.set(underline);
    }

    public boolean isStrikeThrough() {
        return strikeThrough.get();
    }

    public BooleanProperty strikeThroughProperty() {
        return strikeThrough;
    }

    public void setStrikeThrough(boolean strikeThrough) {
        this.strikeThrough.set(strikeThrough);
    }

    public boolean isSubScript() {
        return subScript.get();
    }

    public BooleanProperty subScriptProperty() {
        return subScript;
    }

    public void setSubScript(boolean subScript) {
        this.subScript.set(subScript);
    }

    public boolean isSuperScript() {
        return superScript.get();
    }

    public BooleanProperty superScriptProperty() {
        return superScript;
    }

    public void setSuperScript(boolean superScript) {
        this.superScript.set(superScript);
    }
}
