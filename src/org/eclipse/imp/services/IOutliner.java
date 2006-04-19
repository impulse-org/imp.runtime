package org.eclipse.uide.editor;

import org.eclipse.swt.widgets.Tree;
import org.eclipse.uide.core.ILanguageService;
import org.eclipse.uide.parser.IParseController;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 1998, 2004  All Rights Reserved
 */


/**
 * This interface is to be implemented by contributors to the org.eclipse.uide.runtime.outliner extension point.
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