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

package org.eclipse.imp.ui.explorer;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.imp.editor.EditorUtility;
import org.eclipse.imp.model.ISourceEntity;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

public class OpenActionUtil {
    private OpenActionUtil() {
        // no instance.
    }

    /**
     * Opens the editor on the given element and subsequently selects it.
     */
    public static void open(Object element) throws JavaModelException, PartInitException {
        open(element, true);
    }

    /**
     * Opens the editor on the given element and subsequently selects it.
     */
    public static void open(Object element, boolean activate) throws PartInitException {
        IEditorPart part= EditorUtility.openInEditor(element, activate);
        if (element instanceof ISourceEntity)
            EditorUtility.revealInEditor(part, (ISourceEntity) element);
    }

    /**
     * Filters out source references from the given code resolve results. A utility method that can be called by subclasses.
     */
    public static List filterResolveResults(ISourceEntity[] codeResolveResults) {
        int nResults= codeResolveResults.length;
        List refs= new ArrayList(nResults);
        for(int i= 0; i < nResults; i++) {
            if (codeResolveResults[i] instanceof ISourceReference)
                refs.add(codeResolveResults[i]);
        }
        return refs;
    }

    /**
     * Shows a dialog for resolving an ambiguous java element. Utility method that can be called by subclasses.
     */
    public static ISourceEntity selectJavaElement(ISourceEntity[] elements, Shell shell, String title, String message) {
        int nResults= elements.length;
        if (nResults == 0)
            return null;
        if (nResults == 1)
            return elements[0];
        int flags= JavaElementLabelProvider.SHOW_DEFAULT | JavaElementLabelProvider.SHOW_QUALIFIED | JavaElementLabelProvider.SHOW_ROOT;
        ElementListSelectionDialog dialog= new ElementListSelectionDialog(shell, new JavaElementLabelProvider(flags));
        dialog.setTitle(title);
        dialog.setMessage(message);
        dialog.setElements(elements);
        if (dialog.open() == Window.OK) {
            Object[] selection= dialog.getResult();
            if (selection != null && selection.length > 0) {
                nResults= selection.length;
                for(int i= 0; i < nResults; i++) {
                    Object current= selection[i];
                    if (current instanceof ISourceEntity)
                        return (ISourceEntity) current;
                }
            }
        }
        return null;
    }
}
