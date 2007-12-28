package org.eclipse.imp.ui.explorer;

import java.util.Iterator;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.imp.core.IMPMessages;
import org.eclipse.imp.editor.EditorUtility;
import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.imp.model.ISourceEntity;
import org.eclipse.imp.model.ISourceFolder;
import org.eclipse.imp.model.ISourceProject;
import org.eclipse.imp.ui.ActionMessages;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;
import org.eclipse.jdt.internal.ui.actions.ActionUtil;
import org.eclipse.jdt.ui.actions.SelectionDispatchAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.util.OpenStrategy;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.texteditor.IEditorStatusLine;

/**
 * This action opens a Java editor on a Java element or file.
 * <p>
 * The action is applicable to selections containing elements of type <code>ICompilationUnit</code>, <code>IMember</code> or <code>IFile</code>.
 * 
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * 
 * @since 2.0
 */
public class OpenAction extends SelectionDispatchAction {
    private UniversalEditor fEditor;

    /**
     * Creates a new <code>OpenAction</code>. The action requires that the selection provided by the site's selection provider is of type <code>
     * org.eclipse.jface.viewers.IStructuredSelection</code>.
     * 
     * @param site
     *            the site providing context information for this action
     */
    public OpenAction(IWorkbenchSite site) {
        super(site);
        setText(ActionMessages.OpenAction_label);
        setToolTipText(ActionMessages.OpenAction_tooltip);
        setDescription(ActionMessages.OpenAction_description);
//      PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IJavaHelpContextIds.OPEN_ACTION);
    }

    /**
     * Note: This constructor is for internal use only. Clients should not call this constructor.
     * 
     * @param editor
     *            the Java editor
     */
    public OpenAction(UniversalEditor editor) {
        this(editor.getEditorSite());
        fEditor= editor;
        setText(ActionMessages.OpenAction_declaration_label);
        setEnabled(EditorUtility.getEditorInputModelElement(fEditor, false) != null);
    }

    /*
     * (non-Javadoc) Method declared on SelectionDispatchAction.
     */
    public void selectionChanged(ITextSelection selection) {
    }

    /*
     * (non-Javadoc) Method declared on SelectionDispatchAction.
     */
    public void selectionChanged(IStructuredSelection selection) {
        setEnabled(checkEnabled(selection));
    }

    private boolean checkEnabled(IStructuredSelection selection) {
        if (selection.isEmpty())
            return false;
        for(Iterator iter= selection.iterator(); iter.hasNext();) {
            Object element= iter.next();
            if (element instanceof ISourceEntity)
                continue;
            if (element instanceof IFile)
                continue;
            if (element instanceof IStorage)
                continue;
            return false;
        }
        return true;
    }

    /*
     * (non-Javadoc) Method declared on SelectionDispatchAction.
     */
    public void run(ITextSelection selection) {
        if (!isProcessable())
            return;
        ISourceEntity[] elements= null; // SelectionConverter.codeResolveForked(fEditor, false);
        if (elements == null || elements.length == 0) {
            IEditorStatusLine statusLine= (IEditorStatusLine) fEditor.getAdapter(IEditorStatusLine.class);
            if (statusLine != null)
                statusLine.setMessage(true, ActionMessages.OpenAction_error_messageBadSelection, null);
            getShell().getDisplay().beep();
            return;
        }
        ISourceEntity element= elements[0];
        if (elements.length > 1) {
            element= OpenActionUtil.selectJavaElement(elements, getShell(), getDialogTitle(), ActionMessages.OpenAction_select_element);
            if (element == null)
                return;
        }
//          int type= element.getElementType();
        if (element instanceof ISourceProject || element instanceof ISourceFolder /*|| element instanceof ISourceFolderRoot || element instanceof IPACKAGE_FRAGMENT*/)
            element= EditorUtility.getEditorInputModelElement(fEditor, false);
        run(new Object[] { element });
    }

    private boolean isProcessable() {
        if (fEditor != null) {
            ISourceEntity se= EditorUtility.getEditorInputModelElement(fEditor, false);
            if (se instanceof ICompilationUnit && !JavaModelUtil.isPrimary((ICompilationUnit) se))
                return true; // can process non-primary working copies
        }
        return ActionUtil.isProcessable(getShell(), fEditor);
    }

    /*
     * (non-Javadoc) Method declared on SelectionDispatchAction.
     */
    public void run(IStructuredSelection selection) {
        if (!checkEnabled(selection))
            return;
        run(selection.toArray());
    }

    /**
     * Note: this method is for internal use only. Clients should not call this method.
     * 
     * @param elements
     *            the elements to process
     */
    public void run(Object[] elements) {
        if (elements == null)
            return;
        for(int i= 0; i < elements.length; i++) {
            Object element= elements[i];
            try {
                boolean activateOnOpen= fEditor != null ? true : OpenStrategy.activateOnOpen();
                OpenActionUtil.open(element, activateOnOpen);
            } catch (PartInitException x) {
                String name= null;
                if (element instanceof ISourceEntity) {
                    name= ((ISourceEntity) element).getName();
                } else if (element instanceof IStorage) {
                    name= ((IStorage) element).getName();
                } else if (element instanceof IResource) {
                    name= ((IResource) element).getName();
                }
                if (name != null) {
                    MessageDialog.openError(getShell(), ActionMessages.OpenAction_error_messageProblems, IMPMessages.format(
                            ActionMessages.OpenAction_error_messageArgs, new String[] { name, x.getMessage() }));
                }
            }
        }
    }

    private String getDialogTitle() {
        return ActionMessages.OpenAction_error_title;
    }

//    private void showError(InvocationTargetException e) {
//        ExceptionHandler.handle(e, getShell(), getDialogTitle(), ActionMessages.OpenAction_error_message);
//    }
}
