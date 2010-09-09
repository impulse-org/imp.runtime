package org.eclipse.imp.services;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.imp.language.ILanguageService;
import org.eclipse.ui.IEditorInput;

public interface IEditorInputResolver extends ILanguageService {
	public IPath getPath(IEditorInput editorInput);

	public IFile getFile(IEditorInput editorInput);

	public String getNameExtension(IEditorInput editorInput);
}
