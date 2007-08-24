/*
 * (C) Copyright IBM Corporation 2007
 * 
 * This file is part of the Eclipse IMP.
 */
package org.eclipse.imp.services;

import org.eclipse.imp.parser.IParseController;
import org.eclipse.swt.graphics.Point;

public interface IASTFindReplaceTarget {
    String getSelectionText();

    boolean isEditable();

    /**
     * x coordinate is offset of start of selection, y coordinate is length of selection
     */
    Point getSelection();

    boolean canPerformFind();

    IParseController getParseController();
}
