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

package org.eclipse.imp.editor.internal;

import java.util.ResourceBundle;

import org.eclipse.imp.editor.GotoAnnotationAction;
import org.eclipse.imp.editor.IEditorActionDefinitionIds;
import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.texteditor.BasicTextEditorActionContributor;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.texteditor.RetargetTextEditorAction;

public class TextEditorActionContributor extends BasicTextEditorActionContributor {
    private GotoAnnotationAction fNextAnnotation;

    private GotoAnnotationAction fPreviousAnnotation;

    private RetargetTextEditorAction fShowOutline;

    private RetargetTextEditorAction fToggleComment;

    // mmk 4/8/08
    private RetargetTextEditorAction fIndentSelection;

    public TextEditorActionContributor() {
        super();
        fPreviousAnnotation= new GotoAnnotationAction("PreviousAnnotation.", false); //$NON-NLS-1$
        fNextAnnotation= new GotoAnnotationAction("NextAnnotation.", true); //$NON-NLS-1$
        fShowOutline= new RetargetTextEditorAction(ResourceBundle.getBundle(UniversalEditor.MESSAGE_BUNDLE), "ShowOutline."); //$NON-NLS-1$
        fShowOutline.setActionDefinitionId(UniversalEditor.SHOW_OUTLINE_COMMAND);
        fToggleComment= new RetargetTextEditorAction(ResourceBundle.getBundle(UniversalEditor.MESSAGE_BUNDLE), "ToggleComment."); //$NON-NLS-1$
        fToggleComment.setActionDefinitionId(UniversalEditor.TOGGLE_COMMENT_COMMAND);

        // mmk 4/8/08
        fIndentSelection= new RetargetTextEditorAction(ResourceBundle.getBundle(UniversalEditor.MESSAGE_BUNDLE), "IndentSelection."); //$NON-NLS-1$
        fIndentSelection.setActionDefinitionId(UniversalEditor.INDENT_SELECTION_COMMAND);
    }

    public void init(IActionBars bars, IWorkbenchPage page) {
        super.init(bars, page);
        bars.setGlobalActionHandler(ITextEditorActionDefinitionIds.GOTO_NEXT_ANNOTATION, fNextAnnotation);
        bars.setGlobalActionHandler(ITextEditorActionDefinitionIds.GOTO_PREVIOUS_ANNOTATION, fPreviousAnnotation);
        bars.setGlobalActionHandler(ActionFactory.NEXT.getId(), fNextAnnotation);
        bars.setGlobalActionHandler(ActionFactory.PREVIOUS.getId(), fPreviousAnnotation);
        bars.setGlobalActionHandler(UniversalEditor.SHOW_OUTLINE_COMMAND, fShowOutline);
        bars.setGlobalActionHandler(UniversalEditor.TOGGLE_COMMENT_COMMAND, fToggleComment);

        // mmk 4/8/08
        bars.setGlobalActionHandler(UniversalEditor.INDENT_SELECTION_COMMAND, fIndentSelection);
    }

    /*
     * @see org.eclipse.ui.part.EditorActionBarContributor#contributeToMenu(org.eclipse.jface.action.IMenuManager)
     */
    public void contributeToMenu(IMenuManager menu) {
        super.contributeToMenu(menu);

        IMenuManager navigateMenu= menu.findMenuUsingPath(IWorkbenchActionConstants.M_NAVIGATE);

        if (navigateMenu != null) {
            navigateMenu.appendToGroup(IWorkbenchActionConstants.SHOW_EXT, fShowOutline);
        }

        IMenuManager editMenu= menu.findMenuUsingPath(IWorkbenchActionConstants.M_EDIT);

        if (editMenu != null) {
            editMenu.appendToGroup(IWorkbenchActionConstants.EDIT_END, fToggleComment);
            editMenu.appendToGroup(IWorkbenchActionConstants.EDIT_END, fIndentSelection);
        }
    }

    public void setActiveEditor(IEditorPart part) {
        super.setActiveEditor(part);

        ITextEditor textEditor= null;

        if (part instanceof ITextEditor)
            textEditor= (ITextEditor) part;

        fPreviousAnnotation.setEditor(textEditor);
        fNextAnnotation.setEditor(textEditor);
        fShowOutline.setAction(getAction(textEditor, UniversalEditor.SHOW_OUTLINE_COMMAND));
        fToggleComment.setAction(getAction(textEditor, UniversalEditor.TOGGLE_COMMENT_COMMAND));

        // mmk 4/8/08
        fIndentSelection.setAction(getAction(textEditor, UniversalEditor.INDENT_SELECTION_COMMAND));

        IActionBars bars= getActionBars();

        bars.setGlobalActionHandler(IEditorActionDefinitionIds.FORMAT, getAction(textEditor, "Format")); //$NON-NLS-1$
    }
}
