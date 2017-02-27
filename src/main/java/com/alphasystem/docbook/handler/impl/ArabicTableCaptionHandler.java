package com.alphasystem.docbook.handler.impl;

import com.alphasystem.openxml.builder.wml.RPrBuilder;
import org.docx4j.wml.Color;

import static com.alphasystem.openxml.builder.wml.WmlBuilderFactory.getColorBuilder;
import static org.docx4j.wml.STThemeColor.TEXT_2;

/**
 * @author sali
 */
class ArabicTableCaptionHandler extends ArabicHandler {

    ArabicTableCaptionHandler() {
        super(40);
    }

    @Override
    public RPrBuilder applyStyle(RPrBuilder rprBuilder) {
        final Color color = getColorBuilder().withVal("099BDD").withThemeColor(TEXT_2).getObject();
        return super.applyStyle(rprBuilder).withColor(color);
    }
}
