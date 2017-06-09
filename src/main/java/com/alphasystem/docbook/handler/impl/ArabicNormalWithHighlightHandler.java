package com.alphasystem.docbook.handler.impl;

import org.docx4j.wml.CTShd;
import org.docx4j.wml.STShd;
import org.docx4j.wml.STThemeColor;

import com.alphasystem.openxml.builder.wml.RPrBuilder;
import com.alphasystem.openxml.builder.wml.WmlBuilderFactory;

/**
 * @author sali
 */
class ArabicNormalWithHighlightHandler extends ArabicHandler {

    ArabicNormalWithHighlightHandler() {
    }

    @Override
    public RPrBuilder applyStyle(RPrBuilder rprBuilder) {
        final RPrBuilder rPrBuilder = super.applyStyle(rprBuilder);
        CTShd shd = WmlBuilderFactory.getCTShdBuilder().withVal(STShd.CLEAR).withColor("auto")
                .withFill("D9D9D9").withThemeFill(STThemeColor.BACKGROUND_1).withThemeFillShade("D9").getObject();
        rprBuilder.withShd(shd);
        return rPrBuilder;
    }
}
