/*
 * (C) Copyright IBM Corporation 2007
 * 
 * This file is part of the Eclipse IMP.
 */
/*
 * Created on Feb 6, 2006
 */
package org.eclipse.imp.wizards.fields;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Control;

public class PixelConverter {

    private FontMetrics fFontMetrics;

    public PixelConverter(Control control) {
	GC gc= new GC(control);
	gc.setFont(control.getFont());
	fFontMetrics= gc.getFontMetrics();
	gc.dispose();
    }

    /**
     * see org.eclipse.jface.dialogs.DialogPage#convertHeightInCharsToPixels(int)
     */
    public int convertHeightInCharsToPixels(int chars) {
	return Dialog.convertHeightInCharsToPixels(fFontMetrics, chars);
    }

    /**
     * see org.eclipse.jface.dialogs.DialogPage#convertHorizontalDLUsToPixels(int)
     */
    public int convertHorizontalDLUsToPixels(int dlus) {
	return Dialog.convertHorizontalDLUsToPixels(fFontMetrics, dlus);
    }

    /**
     * see org.eclipse.jface.dialogs.DialogPage#convertVerticalDLUsToPixels(int)
     */
    public int convertVerticalDLUsToPixels(int dlus) {
	return Dialog.convertVerticalDLUsToPixels(fFontMetrics, dlus);
    }

    /**
     * see org.eclipse.jface.dialogs.DialogPage#convertWidthInCharsToPixels(int)
     */
    public int convertWidthInCharsToPixels(int chars) {
	return Dialog.convertWidthInCharsToPixels(fFontMetrics, chars);
    }

}
