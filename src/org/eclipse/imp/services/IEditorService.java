package org.eclipse.imp.services;

import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.imp.parser.IModelListener;

public interface IEditorService
	extends IModelListener
{
	
	public String getName();
	
	public void setEditor(UniversalEditor editor);

}
