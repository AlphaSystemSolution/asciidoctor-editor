package com.alphasystem.app.asciidoctoreditor.ui.model;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author sali
 */
@Component
@ConfigurationProperties("markup")
public final class AsciiDocMarkup {

    private Header header;
    private Link link;
    private SourceCode sourceCode;
    private StrikeThrough strikeThrough;
    private Subscript subscript;
    private Superscript superscript;
    private Underline underline;

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public Link getLink() {
        return link;
    }

    public void setLink(Link link) {
        this.link = link;
    }

    public SourceCode getSourceCode() {
        return sourceCode;
    }

    public void setSourceCode(SourceCode sourceCode) {
        this.sourceCode = sourceCode;
    }

    public StrikeThrough getStrikeThrough() {
        return strikeThrough;
    }

    public void setStrikeThrough(StrikeThrough strikeThrough) {
        this.strikeThrough = strikeThrough;
    }

    public Subscript getSubscript() {
        return subscript;
    }

    public void setSubscript(Subscript subscript) {
        this.subscript = subscript;
    }

    public Superscript getSuperscript() {
        return superscript;
    }

    public void setSuperscript(Superscript superscript) {
        this.superscript = superscript;
    }

    public Underline getUnderline() {
        return underline;
    }

    public void setUnderline(Underline underline) {
        this.underline = underline;
    }

    public static class Markup {
        private String markupBegin;
        private String markupEnd;

        public String getMarkupBegin() {
            return markupBegin;
        }

        public void setMarkupBegin(String markupBegin) {
            this.markupBegin = markupBegin;
        }

        public String getMarkupEnd() {
            return markupEnd;
        }

        public void setMarkupEnd(String markupEnd) {
            this.markupEnd = markupEnd;
        }

        @Override
        public String toString() {
            return getMarkupBegin() + "" + getMarkupEnd();
        }
    }

    public static class Header extends Markup {
    }

    public static class Link extends Markup {
    }

    public static class SourceCode extends Markup {
    }

    public static class StrikeThrough extends Markup {
    }

    public static class Subscript extends Markup {
    }

    public static class Superscript extends Markup {
    }

    public static class Underline extends Markup {
    }

}
