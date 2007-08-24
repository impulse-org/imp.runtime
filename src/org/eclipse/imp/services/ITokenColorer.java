/*
 * (C) Copyright IBM Corporation 2007
 * 
 * This file is part of the Eclipse IMP.
 */
package org.eclipse.imp.services;

import lpg.runtime.IToken;

import org.eclipse.imp.language.ILanguageService;
import org.eclipse.imp.parser.IParseController;
import org.eclipse.jface.text.TextAttribute;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 2005  All Rights Reserved
 */

/**
 * @author Claffra
 *
 */
public interface ITokenColorer extends ILanguageService {
    /**
     * Provide coloring and font to use for a given token in the model.
     * @param model The model that contains the token
     * @param token The token to be colored
     * @return a TextAttribute
     */
    public TextAttribute getColoring(IParseController controller, IToken token);
}