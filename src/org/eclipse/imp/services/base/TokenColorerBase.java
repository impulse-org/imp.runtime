/*
 * (C) Copyright IBM Corporation 2007
 * 
 * This file is part of the Eclipse IMP.
 */
package org.eclipse.imp.services.base;

import lpg.runtime.IToken;

import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.services.ITokenColorer;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;

public class TokenColorerBase implements ITokenColorer {

    protected TextAttribute keywordAttribute;

    public TextAttribute getColoring(IParseController controller, IToken token) {
        switch (token.getKind()) {
            default:
                if (controller.isKeyword(token.getKind()))
                     return keywordAttribute;
               else return null;
        }
    }

    public TokenColorerBase() {
    	this(new TextAttribute(Display.getDefault().getSystemColor(SWT.COLOR_DARK_MAGENTA), null, SWT.BOLD));
    }

    public TokenColorerBase(TextAttribute keywordAttribute) {
        this.keywordAttribute = keywordAttribute;
    }

}
