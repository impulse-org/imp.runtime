package org.eclipse.imp.services;

import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.imp.language.ILanguageService;
import org.eclipse.imp.parser.IModelListener;
import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.parser.IModelListener.AnalysisRequired;

public interface IEditorService
	extends IModelListener
{
	
	public String getName();
	
	public void setEditor(UniversalEditor editor);

}
