package org.eclipse.uide.editor;

import org.eclipse.swt.graphics.Point;
import org.eclipse.uide.parser.IParseController;

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
