package org.eclipse.uide.editor;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.text.Assert;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.uide.runtime.RuntimePlugin;
import org.eclipse.uide.utils.ExtensionPointFactory;

/**
 * @author Dr. Robert M. Fuhrer
 */
public class EditorErrorTickUpdater implements IProblemChangedListener {
    private UniversalEditor fEditor;

    private ILabelProvider fLabelProvider;

    public EditorErrorTickUpdater(UniversalEditor editor) {
        Assert.isNotNull(editor);
        fEditor= editor;
        fLabelProvider= (ILabelProvider) ExtensionPointFactory.createExtensionPoint(fEditor.fLanguage, RuntimePlugin.UIDE_RUNTIME, "labelProvider");
//        fLabelProvider.addLabelDecorator(new ProblemsLabelDecorator(null));
        fEditor.getProblemMarkerManager().addListener(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see IProblemChangedListener#problemsChanged(IResource[], boolean)
     */
    public void problemsChanged(IResource[] changedResources, boolean isMarkerChange) {
        if (!isMarkerChange)
            return;

        IEditorInput input= fEditor.getEditorInput();

        if (input != null) { // might run async, tests needed
            IFile fileInput= (IFile) input;

            if (fileInput != null) {
                for(int i= 0; i < changedResources.length; i++) {
                    if (changedResources[i].equals(fileInput)) {
                        updateEditorImage(fileInput);
                    }
                }
            }
        }
    }

    public void updateEditorImage(IFile file) {
        Image titleImage= fEditor.getTitleImage();
        if (titleImage == null) {
            return;
        }
        Image newImage= fLabelProvider.getImage(file);
        if (titleImage != newImage) {
            postImageChange(newImage);
        }
    }

    private void postImageChange(final Image newImage) {
        Shell shell= fEditor.getEditorSite().getShell();
        if (shell != null && !shell.isDisposed()) {
            shell.getDisplay().syncExec(new Runnable() {
                public void run() {
                    fEditor.updatedTitleImage(newImage);
                }
            });
        }
    }

    public void dispose() {
        fLabelProvider.dispose();
        fEditor.getProblemMarkerManager().removeListener(this);
    }
}
