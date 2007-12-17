/**
 * 
 */
package org.eclipse.imp.services;

import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.imp.language.ILanguageService;
import org.eclipse.jface.action.IAction;

public interface IRefactoringContributor extends ILanguageService {
    public IAction[] getEditorRefactoringActions(UniversalEditor editor);
}