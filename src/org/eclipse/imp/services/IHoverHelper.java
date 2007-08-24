package org.eclipse.imp.services;

import org.eclipse.imp.language.ILanguageService;
import org.eclipse.imp.parser.IParseController;
import org.eclipse.jface.text.source.ISourceViewer;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 1998, 2004  All Rights Reserved
 */

/**
 * This interface is to be implemented by contributors to the org.eclipse.imp.runtime.hoverHelp extension point.
 * The Universal IDE Editor will locate a suitable parser for the language being edited.
 * The result of the parser, an Ast describing the syntactical elements in the input, is cached
 * and used when the mouse is hovered over elements in the editor.
 * 
 * @author Claffra
 */
public interface IHoverHelper extends ILanguageService {

    /**
     * Provide language-specific hover help for the token at the given offset.
     * 
     * @param model	the result from the parser (contains an Ast)
     * @param offset		the offset in the original input stream
     * @return a human-readable text string describing what the element at the given offset
     */
    public String getHoverHelpAt(IParseController parseController, ISourceViewer viewer, int offset);
}
