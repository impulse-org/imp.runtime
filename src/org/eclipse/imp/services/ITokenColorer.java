package org.eclipse.uide.editor;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.uide.core.ILanguageService;
import org.eclipse.uide.parser.IParseController;

import com.ibm.lpg.IToken;

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