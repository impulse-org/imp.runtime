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

public abstract class TokenColorerBase implements ITokenColorer {

    protected TextAttribute keywordAttribute;

//  protected abstract boolean isKeyword(int kind, IParseController parseController);

    public TextAttribute getColoring(IParseController controller, IToken token) {
//        switch (token.getKind()) {
//            default:
//                if (isKeyword(token.getKind(), controller))
//                     return keywordAttribute;
//               else
        return null;
//        }
    }

    public TokenColorerBase() {
    	this(new TextAttribute(Display.getDefault().getSystemColor(SWT.COLOR_DARK_MAGENTA), null, SWT.BOLD));
    }

    public TokenColorerBase(TextAttribute keywordAttribute) {
        this.keywordAttribute = keywordAttribute;
    }

}
