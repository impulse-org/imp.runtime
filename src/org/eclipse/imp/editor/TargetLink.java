package org.eclipse.uide.editor;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.AbstractTextEditor;

/**
 * Common class to represent a hyperlink to a given target location.
 * @author rfuhrer
 */
public final class TargetLink implements IHyperlink {
    private final String fText;

    private final Object fTarget;

    private final int fStart;

    private final int fLength;

    private final int fTargetStart;

    private final int fTargetLength;

    private AbstractTextEditor fEditor;

    /**
     * @param text
     * @param srcStart
     * @param srcLength
     * @param target a String filePath, if 'editor' is null
     * @param targetStart
     * @param targetLength
     * @param editor may be null, if the target is in another compilation unit
     */
    public TargetLink(String text, int srcStart, int srcLength, Object target, int targetStart, int targetLength, AbstractTextEditor editor) {
        super();
        fText= text;
        fStart= srcStart;
	fEditor= editor;
        fTarget= target;
        fLength= srcLength;
        fTargetStart= targetStart;
        fTargetLength= targetLength;
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
	if (fEditor == null) {
	    String filePath= (String) fTarget;
	    IEditorDescriptor ed= PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor(filePath);
	    final IWorkspaceRoot wsRoot= ResourcesPlugin.getWorkspace().getRoot();
	    String wsLoc= wsRoot.getLocation().toOSString();
	    String wsRelFilePath= filePath.startsWith(wsLoc) ?
		    filePath.substring(wsLoc.length()) : filePath;
	    IFile file= wsRoot.getFile(new Path(wsRelFilePath));

	    try {
		IEditorPart editor= PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(new FileEditorInput(file), ed.getId());

		// Don't assume the target editor is a text editor; the target might be
		// in a class file or another kind of binary file.
		if (editor instanceof AbstractTextEditor)
		    fEditor= (AbstractTextEditor) editor;
	    } catch (PartInitException e) {
		e.printStackTrace();
	    }
	}
	if (fEditor != null)
	    fEditor.selectAndReveal(fTargetStart, fTargetLength);
    }
}
