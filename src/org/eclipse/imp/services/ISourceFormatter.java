/*******************************************************************************
* Copyright (c) 2007 IBM Corporation.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*    Robert Fuhrer (rfuhrer@watson.ibm.com) - initial API and implementation

*******************************************************************************/

package org.eclipse.imp.services;

import org.eclipse.imp.language.ILanguageService;
import org.eclipse.imp.parser.IParseController;

/**
 * Essentially the same as IFormattingStrategy, but the format() method
 * takes an IParseController as an argument, so that the language-specific
 * formatter has access to an AST to drive formatting decisions.
 * @author rfuhrer@watson.ibm.com
 */
public interface ISourceFormatter extends ILanguageService {
    /**
     * Informs the strategy about the start of a formatting process in which it will
     * participate.
     *
     * @param initialIndentation the indent string of the first line at which the
     *      overall formatting process starts.
     */
    void formatterStarts(String initialIndentation);

    /**
     * Formats the given string. During the formatting process this strategy must update
     * the given character positions according to the changes applied to the given string.
     *
     * @param parseController the parse controller which may be used to obtain an AST to drive
     *        formatting decisions
     * @param content the initial string to be formatted
     * @param isLineStart indicates whether the beginning of content is a line start in its document
     * @param indentation the indentation string to be used
     * @param positions the character positions to be updated
     * @return the formatted string
     */
    String format(IParseController parseController, String content, boolean isLineStart, String indentation, int[] positions);

    /**
     * Informs the strategy that the formatting process in which it has participated
     * has been finished.
     */
    void formatterStops();
}
