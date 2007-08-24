/*
 * (C) Copyright IBM Corporation 2007
 * 
 * This file is part of the Eclipse IMP.
 */
package org.eclipse.imp.services;

import org.eclipse.imp.parser.IParseController;

/**
 * Essentially the same as IFormattingStrategy, but the format() method
 * takes an IParseController as an argument, so that the language-specific
 * formatter has access to an AST to drive formatting decisions.
 * @author Dr. Robert M. Fuhrer
 */
public interface ISourceFormatter {
    void formatterStarts(String initialIndentation);

    String format(IParseController parseController, String content, boolean isLineStart, String indentation, int[] positions);

    void formatterStops();
}
