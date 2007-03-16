package org.eclipse.uide.editor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.AbstractTextEditor;

/**
 * Common class to represent a hyperlink to a given target location.
 * @author rfuhrer
 */
public final class TargetLink implements IHyperlink {
    private static final class ExternalEditorInput implements IStorageEditorInput {
	private final IStorage fStorage;

	private ExternalEditorInput(IStorage storage) {
	    super();
	    fStorage= storage;
	}

	public IStorage getStorage() throws CoreException {
	    return fStorage;
	}

	public boolean exists() {
	    return true;
	}

	public ImageDescriptor getImageDescriptor() {
	    return null;
	}

	public String getName() {
	    return fStorage.getName();
	}

	public IPersistableElement getPersistable() {
	    return null;
	}

	public String getToolTipText() {
	    return "";
	}

	public Object getAdapter(Class adapter) {
	    return null;
	}
    }

    private static final class ExternalStorage implements IStorage {
	private final IPath fPath;

	private ExternalStorage(IPath path) {
	    super();
	    fPath= path;
	}

	public InputStream getContents() throws CoreException {
	    File file= new File(fPath.toOSString());
	    try {
	        return new FileInputStream(file);
	    } catch(FileNotFoundException fnf) {
	        return null;
	    }
	}

	public IPath getFullPath() {
	    return fPath;
	}

	public String getName() {
	    return fPath.lastSegment();
	}

	public boolean isReadOnly() {
	    return true;
	}

	public Object getAdapter(Class adapter) {
	    return null;
	}
    }

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
	    String targetStr= (String) fTarget;
	    final IPath targetPath= new Path(targetStr);
	    IEditorDescriptor ed= PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor(targetStr);
//	    boolean isWSRel= targetPath.matchingFirstSegments(wsLoc) == wsLoc.segmentCount();
	    IWorkspaceRoot wsRoot= ResourcesPlugin.getWorkspace().getRoot();
	    IEditorInput editorInput;

	    if (targetPath.isAbsolute() && !wsRoot.getProject(targetPath.segment(0)).exists()) {
		// http://wiki.eclipse.org/index.php/FAQ_How_do_I_open_an_editor_on_a_file_outside_the_workspace%3F
		final IStorage storage= new ExternalStorage(targetPath);
		editorInput= new ExternalEditorInput(storage);
	    } else {
		IPath wsLoc= wsRoot.getLocation();
		IFile file= wsRoot.getFile(targetPath.isAbsolute() ? targetPath : targetPath.removeFirstSegments(wsLoc.segmentCount()));
		editorInput= new FileEditorInput(file);
	    }

	    try {
		IEditorPart editor= PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(editorInput, ed.getId());

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
