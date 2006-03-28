/**
 * 
 */
package org.eclipse.uide.editor;

import org.eclipse.jdt.ui.actions.JdtActionConstants;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.texteditor.BasicTextEditorActionContributor;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;

public class TextEditorActionContributor extends BasicTextEditorActionContributor {
    private GotoAnnotationAction fNextAnnotation;

    private GotoAnnotationAction fPreviousAnnotation;

    public TextEditorActionContributor() {
        super();
        fPreviousAnnotation= new GotoAnnotationAction("PreviousAnnotation.", false); //$NON-NLS-1$
        fNextAnnotation= new GotoAnnotationAction("NextAnnotation.", true); //$NON-NLS-1$
    }

    public void init(IActionBars bars, IWorkbenchPage page) {
        super.init(bars, page);
        bars.setGlobalActionHandler(ITextEditorActionDefinitionIds.GOTO_NEXT_ANNOTATION, fNextAnnotation);
        bars.setGlobalActionHandler(ITextEditorActionDefinitionIds.GOTO_PREVIOUS_ANNOTATION, fPreviousAnnotation);
        bars.setGlobalActionHandler(ActionFactory.NEXT.getId(), fNextAnnotation);
        bars.setGlobalActionHandler(ActionFactory.PREVIOUS.getId(), fPreviousAnnotation);
    }

    public void setActiveEditor(IEditorPart part) {
        super.setActiveEditor(part);

        ITextEditor textEditor= null;

        if (part instanceof ITextEditor)
            textEditor= (ITextEditor) part;

        fPreviousAnnotation.setEditor(textEditor);
        fNextAnnotation.setEditor(textEditor);

        IActionBars bars= getActionBars();
        bars.setGlobalActionHandler(JdtActionConstants.FORMAT, getAction(textEditor, "Format")); //$NON-NLS-1$
    }
}
