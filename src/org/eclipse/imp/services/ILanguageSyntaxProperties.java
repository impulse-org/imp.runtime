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

public interface ILanguageSyntaxProperties extends ILanguageService {
    /**
     * if null, the language has no single-line comment syntax
     * @return
     */
    public String getSingleLineCommentPrefix();

    public String getBlockCommentStart();

    public String getBlockCommentContinuation();

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

    public String getIdentifierConstituentChars();

    public int[] getIdentifierComponents(String ident);
}
