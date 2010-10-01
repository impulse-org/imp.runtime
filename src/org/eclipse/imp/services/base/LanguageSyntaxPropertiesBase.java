/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Robert Fuhrer (rfuhrer@watson.ibm.com) - initial API and implementation
 *******************************************************************************/

package org.eclipse.imp.services.base;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.services.ILanguageSyntaxProperties;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;

/**
 * Base class for implementations of ILanguageSyntaxProperties that provides reasonably generic
 * implementations of various interface methods, e.g., getIdentifierComponents() and
 * isIdentifierStart().
 * @author rfuhrer, ataylor
 */
public abstract class LanguageSyntaxPropertiesBase implements ILanguageSyntaxProperties {
    /* (non-Javadoc)
     * @see org.eclipse.imp.services.ILanguageSyntaxProperties#getIdentifierComponents(java.lang.String)
     */
    public int[] getIdentifierComponents(String ident) {
        List<Integer> listResult= new LinkedList<Integer>();
        for(int i=0; i < ident.length(); i++) {
            if (i > 0 && (Character.isLowerCase(ident.charAt(i-1)) && Character.isUpperCase(ident.charAt(i)) || ident.charAt(i) == '_')) {
                listResult.add(i);
            }
        }
        int[] result= new int[listResult.size()];
        int i= 0;
        for(Integer idx: listResult) {
            result[i++]= idx;
        }
        return result;
    }

    /* (non-Javadoc)
     * @see org.eclipse.imp.services.ILanguageSyntaxProperties#getIdentifierConstituentChars()
     */
    public String getIdentifierConstituentChars() {
        return "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_";
    }

    public boolean isIdentifierStart(char ch) {
        return Character.isLetter(ch);
    }

    public boolean isIdentifierPart(char ch) {
        return getIdentifierConstituentChars().indexOf(ch) >= 0;
    }

    public boolean isWhitespace(char ch) {
        return Character.isWhitespace(ch);
    }

    public String[][] getFences() {
        return new String[][] { { "'", "'" }, { "\"", "\"" }, { "(", ")" }, { "[", "]" }, { "{", "}" } };
    }

    public IRegion getDoubleClickRegion(int offset, IParseController pc) {
        return new Region(offset, 1);
    }
}
