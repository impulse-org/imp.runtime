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

package org.eclipse.imp.editor;

import java.util.ResourceBundle;

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

    private GotoNextTargetAction fNextTarget;

    private GotoPreviousTargetAction fPreviousTarget;

    private SelectEnclosingAction fSelectEnclosing;

    public TextEditorActionContributor() {
        super();
        fPreviousAnnotation= new GotoAnnotationAction("PreviousAnnotation.", false); //$NON-NLS-1$
        fNextAnnotation= new GotoAnnotationAction("NextAnnotation.", true); //$NON-NLS-1$
        fShowOutline= new RetargetTextEditorAction(ResourceBundle.getBundle(UniversalEditor.MESSAGE_BUNDLE), "ShowOutline."); //$NON-NLS-1$
        fShowOutline.setActionDefinitionId(IEditorActionDefinitionIds.SHOW_OUTLINE);
        fNextTarget= new GotoNextTargetAction();
        fPreviousTarget= new GotoPreviousTargetAction();
        fSelectEnclosing= new SelectEnclosingAction();
    }

    public void init(IActionBars bars, IWorkbenchPage page) {
        super.init(bars, page);
        bars.setGlobalActionHandler(ITextEditorActionDefinitionIds.GOTO_NEXT_ANNOTATION, fNextAnnotation);
        bars.setGlobalActionHandler(ITextEditorActionDefinitionIds.GOTO_PREVIOUS_ANNOTATION, fPreviousAnnotation);
        bars.setGlobalActionHandler(ActionFactory.NEXT.getId(), fNextAnnotation);
        bars.setGlobalActionHandler(ActionFactory.PREVIOUS.getId(), fPreviousAnnotation);
        bars.setGlobalActionHandler(IEditorActionDefinitionIds.GOTO_NEXT_TARGET, fNextTarget);
        bars.setGlobalActionHandler(IEditorActionDefinitionIds.GOTO_PREVIOUS_TARGET, fPreviousTarget);
        bars.setGlobalActionHandler(IEditorActionDefinitionIds.SELECT_ENCLOSING, fSelectEnclosing);
    }

    /*
     * @see org.eclipse.ui.part.EditorActionBarContributor#contributeToMenu(org.eclipse.jface.action.IMenuManager)
     */
    public void contributeToMenu(IMenuManager menu) {
        super.contributeToMenu(menu);

        IMenuManager navigateMenu= menu.findMenuUsingPath(IWorkbenchActionConstants.M_NAVIGATE);

        if (navigateMenu != null) {
            navigateMenu.appendToGroup(IWorkbenchActionConstants.SHOW_EXT, fShowOutline);
            navigateMenu.add(fNextTarget);
            navigateMenu.add(fPreviousTarget);
        }

        IMenuManager editMenu= menu.findMenuUsingPath(IWorkbenchActionConstants.M_EDIT);

        if (editMenu != null) {
//          editMenu.appendToGroup(IWorkbenchActionConstants.EDIT_END, fToggleComment);
//          editMenu.appendToGroup(IWorkbenchActionConstants.EDIT_END, fCorrectIndentation);
            editMenu.appendToGroup(IWorkbenchActionConstants.EDIT_END, fSelectEnclosing);
        }
    }

    public void setActiveEditor(IEditorPart part) {
        super.setActiveEditor(part);

        ITextEditor textEditor= null;

        if (part instanceof ITextEditor)
            textEditor= (ITextEditor) part;

        fPreviousAnnotation.setEditor(textEditor);
        fNextAnnotation.setEditor(textEditor);
        fNextTarget.setEditor(textEditor);
        fPreviousTarget.setEditor(textEditor);
        fSelectEnclosing.setEditor(textEditor);
        fShowOutline.setAction(getAction(textEditor, IEditorActionDefinitionIds.SHOW_OUTLINE));

        IActionBars bars= getActionBars();

        bars.setGlobalActionHandler(IMPActionConstants.FORMAT, getAction(textEditor, "Format")); //$NON-NLS-1$
        bars.setGlobalActionHandler(IMPActionConstants.SHIFT_LEFT, getAction(textEditor, "ShiftLeft")); //$NON-NLS-1$
        bars.setGlobalActionHandler(IMPActionConstants.SHIFT_RIGHT, getAction(textEditor, "ShiftRight")); //$NON-NLS-1$
        bars.setGlobalActionHandler(IMPActionConstants.TOGGLE_COMMENT, getAction(textEditor, IEditorActionDefinitionIds.TOGGLE_COMMENT));
        bars.setGlobalActionHandler(IMPActionConstants.CORRECT_INDENTATION, getAction(textEditor, IEditorActionDefinitionIds.CORRECT_INDENTATION));
        bars.setGlobalActionHandler(IMPActionConstants.OPEN, getAction(textEditor, IEditorActionDefinitionIds.OPEN_EDITOR));
    }
}
