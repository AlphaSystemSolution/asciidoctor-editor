package com.alphasystem.docbook.handler.impl;

import com.alphasystem.docbook.handler.InlineHandlerFactory;
import com.alphasystem.docbook.handler.InlineHandlerService;

/**
 * @author sali
 */
public class ArabicInlineHandlerService extends InlineHandlerService {

    @Override
    public void initializeHandlers() {
        final InlineHandlerFactory instance = InlineHandlerFactory.getInstance();
        instance.registerHandler(ArabicHandler.ARABIC_HEADING_1, new ArabicHeading1Handler());
        instance.registerHandler(ArabicHandler.ARABIC_NORMAL, new ArabicNormalHandler());
        instance.registerHandler(ArabicHandler.ARABIC_TABLE_CAPTION, new ArabicTableCaptionHandler());
    }
}
