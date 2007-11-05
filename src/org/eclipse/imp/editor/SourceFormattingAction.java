package org.eclipse.imp.editor;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.AbstractTextEditor;

public class SourceFormattingAction implements IWorkbenchWindowActionDelegate {

	private AbstractTextEditor fActiveEditor;

	public void dispose() {
		fActiveEditor = null;
	}

	public void init(IWorkbenchWindow window) {
		System.err.println(((UniversalEditor) window).fFormattingStrategy
				.getClass().getName());
	}

	public void run(IAction action) {
		getActiveEditor();
		IAction format = fActiveEditor.getAction("Format");
		format.run();
	}

	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub

	}

	private void getActiveEditor() {
		fActiveEditor = (AbstractTextEditor) PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage().getActiveEditor();
	}

}
