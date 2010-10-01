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

import org.eclipse.core.runtime.IPath;
import org.eclipse.imp.runtime.RuntimePlugin;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

/**
 * Common class to represent a hyperlink to a given target location.
 * 
 * @author rfuhrer
 */
public final class TargetLink implements IHyperlink {
    private final String fText;

    private final Object fTarget;

    private final int fStart;

    private final int fLength;

    private final int fTargetStart;

    private final int fTargetLength;

    private IRegionSelectionService fSelectionService;

    /**
     * @param text
     * @param srcStart
     * @param srcLength
     * @param target a workspace-relative or filesystem-absolute IPath to the file,
     * if 'editor' is null; otherwise, an object that indicates the particular target within the source file
     * @param targetStart
     * @param targetLength
     * @param editor may be null, if the target is in another compilation unit
     */
    public TargetLink(String text, int srcStart, int srcLength, Object target, int targetStart, int targetLength, IRegionSelectionService selService) {
        fText= text;
        fStart= srcStart;
        fTarget= target;
        fLength= srcLength;
        fTargetStart= targetStart;
        fTargetLength= targetLength;
        fSelectionService= selService;
    }

    public IRegion getHyperlinkRegion() {
        return new Region(fStart, fLength);
    }

    public String getTypeLabel() {
        return fTarget.getClass().getName();
    }

    public String getHyperlinkText() {
        return new String(fText);
    }

    public void open() {
        if (fSelectionService == null) {
            // Either we're opening up a new editor, or there's an existing one open on the target file.
            // Either way, get a handle to an IEditorPart for the target file, and try to get an
            // IRegionSelectionService interface on it.
            if (!(fTarget instanceof IPath)) {
                RuntimePlugin.getInstance().writeErrorMsg("Unable to link to a target of type other than IPath: " + fTarget.getClass().getName());
                return;
            }
            final IPath targetPath= (IPath) fTarget;
            IEditorDescriptor ed= PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor(targetPath.lastSegment());
            IWorkbenchWindow activeWindow= PlatformUI.getWorkbench().getActiveWorkbenchWindow();

            if (ed == null) {
                MessageDialog.openError(activeWindow.getShell(), "Error", "No editor defined for target file "
                        + targetPath.toPortableString());
                return;
            }

            IEditorPart editor;

            try {
                IWorkbenchPage activePage= activeWindow.getActivePage();
                IEditorInput editorInput= EditorUtility.getEditorInput(targetPath);

                editor= activePage.openEditor(editorInput, ed.getId());
            } catch (PartInitException e) {
                RuntimePlugin.getInstance().logException(e.getLocalizedMessage(), e);
                return;
            }
            // Don't assume the target editor is a text editor; the target might be
            // in a class file or another kind of binary file.
            if (editor instanceof IRegionSelectionService) {
                fSelectionService= (IRegionSelectionService) editor;
            } else {
                fSelectionService= (IRegionSelectionService) editor.getAdapter(IRegionSelectionService.class);
            }
        }
        if (fSelectionService != null) {
            fSelectionService.selectAndReveal(fTargetStart, fTargetLength);
        }
    }
}
