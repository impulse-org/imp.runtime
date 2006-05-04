package org.eclipse.uide.internal.editor;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.eclipse.uide.core.ErrorHandler;
import org.eclipse.uide.core.Language;
import org.eclipse.uide.editor.IOutliner;
import org.eclipse.uide.editor.UniversalEditor;
import org.eclipse.uide.parser.IModelListener;
import org.eclipse.uide.parser.IParseController;
import org.eclipse.uide.runtime.RuntimePlugin;
import org.eclipse.uide.utils.ExtensionPointFactory;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 2005  All Rights Reserved
 */

/**
 * @author Claffra
 */
public class OutlineController implements IContentOutlinePage, IModelListener {
    protected Tree tree;

    private IParseController controller;

    //    private UniversalEditor editor;

    private IOutliner outliner;

    private UIJob job;

    private int DELAY= 50; //500;

    public OutlineController(UniversalEditor editor) {
    //	this.editor= editor;
    }

    public void setLanguage(Language language) {
	outliner= (IOutliner) ExtensionPointFactory.createExtensionPoint(language, RuntimePlugin.UIDE_RUNTIME, "outliner");
    }

    public void createControl(Composite parent) {
    	tree= new Tree(parent, SWT.NONE);
    	tree.addSelectionListener(new SelectionAdapter() {
			    public void widgetSelected(SelectionEvent e) {
			    	try {
					    // RMF 11/4/2005 - Disabled, since not every parser yields a UIDE AST
			//		    TreeItem item= tree.getSelection()[0];
			//		    Ast node= (Ast) item.getData();
			//		    if (node != null) {
						// int start = node.getStartOffset();
						// int end = node.getEndOffset();
			//		    }
					} catch (Throwable ee) {
					    ErrorHandler.reportError("Universal Editor Error", ee);
					}
					super.widgetSelected(e);
			    }
    		});
    	if (outliner != null)
    		outliner.setTree(tree);
    }

    public void dispose() {}

    public Control getControl() {
	return tree;
    }

    public void setActionBars(IActionBars actionBars) {}

    public void setFocus() {}

    public void addSelectionChangedListener(ISelectionChangedListener listener) {}

    public ISelection getSelection() {
	return null;
    }

    public void removeSelectionChangedListener(ISelectionChangedListener listener) {}

    public void setSelection(ISelection selection) {}

    public void update(IParseController result, IProgressMonitor monitor) {
	this.controller= result;
	if (job != null) {
	    job.cancel();
	}
	else {
		job= new UIJob("Outline View Controller") {
		public IStatus runInUIThread(IProgressMonitor monitor) {
		    int offset= 0;
		    try {
			if (outliner != null)
			    outliner.createOutlinePresentation(controller, offset);
		    } catch (Throwable e) {
			ErrorHandler.reportError("Outline View Controller", e);
		    }
		    return Status.OK_STATUS;
		}
	    };

	} // else
	job.schedule(DELAY);
    }
}
