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
import org.eclipse.jface.text.IRegion;

/**
 * Describes certain syntactic properties of the language used in support of certain editor features, such
 * as toggling comments, fence matching, and so on.
 * @author rfuhrer@watson.ibm.com
 */
public interface ILanguageSyntaxProperties extends ILanguageService {
    /**
     * if null, the language has no single-line comment syntax
     * @return
     */
    public String getSingleLineCommentPrefix();

    /**
     * @return the character sequence that starts a block comment, if the language has syntax for that, else null
     */
    public String getBlockCommentStart();

    /**
     * @return the character sequence for a line in the middle of a block comment, if the language has syntax
     * for that, else null
     */
    public String getBlockCommentContinuation();

    /**
     * @return the character sequence that ends a block comment, if the language has syntax for that, else null
     */
    public String getBlockCommentEnd();

    /**
     * If -1, there is no comment column
     */
//  public int getSingleLineCommentColumn();

    /**
     * If -1, there is no continuation column
     */
//  public int getLineContinuationColumn();

    /**
     * @return an array of arrays each containing a balanced pair
     * of fence Strings, e.g.:
     *    [ [ "[" "]" ] ["(" ")" ] [ "{" "}" ] [ "/." "./" ]
     */
    public String[][] getFences();

    /**
     * @return the set of characters that are legal in an identifier in this language
     */
    public String getIdentifierConstituentChars();

    /**
     * @param ident an identifier
     * @return the ordered set of character offsets to the individual components (e.g. words) of an identifier.
     * May be null.
     */
    public int[] getIdentifierComponents(String ident);

    /**
     * @return true if the given character is valid at the beginning of an identifier
     */
    public boolean isIdentifierStart(char ch);

    /**
     * @return true if the given character is valid in the middle or end of an identifier
     */
    public boolean isIdentifierPart(char ch);

    /**
     * @return true if the given character is a "whitespace" character
     */
    public boolean isWhitespace(char ch);

    /**
     * Returns the region of source text to select when double-clicking, starting from the given offset.
     */
    public IRegion getDoubleClickRegion(int offset, IParseController pc);
}
