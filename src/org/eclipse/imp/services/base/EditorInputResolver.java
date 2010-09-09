package org.eclipse.imp.services.base;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.imp.editor.EditorInputUtils;
import org.eclipse.imp.services.IEditorInputResolver;
import org.eclipse.ui.IEditorInput;

/**
 * Default implementation of the IEditorInputResolver service interface
 * @author awtaylor
 * Added per patch attached to bug #322035
 */
public class EditorInputResolver implements IEditorInputResolver {
	public IFile getFile(IEditorInput editorInput) {
		return EditorInputUtils.getFile(editorInput);
	}

	public String getNameExtension(IEditorInput editorInput) {
		return EditorInputUtils.getNameExtension(editorInput);
	}

	public IPath getPath(IEditorInput editorInput) {
		return EditorInputUtils.getPath(editorInput);
	}
}
