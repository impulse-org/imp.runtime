/*
 * (C) Copyright IBM Corporation 2007
 * 
 * This file is part of the Eclipse IMP.
 */
package org.eclipse.imp.services;

import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.imp.language.ILanguageService;
import org.eclipse.imp.parser.IParseController;
import org.eclipse.swt.widgets.Tree;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 1998, 2004  All Rights Reserved
 */


/**
 * This interface is to be implemented by contributors to the org.eclipse.imp.runtime.outliner extension point.
 * The Universal IDE Editor will locate a suitable parser for the language being edited.
 * The result of the parser, an Ast describing the syntactical elements in the input, is cached
 * and used to show an outline view of the elements in the editor.
 * 
 * @author Claffra
 */
public interface IOutliner extends ILanguageService {
    
    /**
     * Create a language-specific outline presentation for the parse result.
     * 
     * @param model	the result from the parser (contains an Ast)
     * @param offset		current offset of the caret in the editor
     */
	void createOutlinePresentation(IParseController controller, int offset);
	
	/** 
	 * Set the editor that currently controls the outline view
	 * @param editor
	 */
	void setEditor(UniversalEditor editor);

    
	/**
	 * Set the tree widget that contains the outline view. The tree is fully managed by this IOutliner instance.
	 * @param tree
	 */
	void setTree(Tree tree);
}