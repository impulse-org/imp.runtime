/*******************************************************************************
* Copyright (c) 2007 IBM Corporation.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*    Robert Fuhrer (rfuhrer@watson.ibm.com) - initial API and implementation

*******************************************************************************/

package org.eclipse.imp.editor;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.imp.editor.OutlineLabelProvider.IElementImageProvider;
import org.eclipse.imp.editor.internal.TreeDiffer;
import org.eclipse.imp.parser.IModelListener;
import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.parser.ISourcePositionLocator;
import org.eclipse.imp.services.base.TreeModelBuilderBase;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;

public class IMPOutlinePage extends ContentOutlinePage implements IModelListener {
    private final OutlineContentProviderBase fContentProvider;
    private final TreeModelBuilderBase fModelBuilder;
    private final ILabelProvider fLabelProvider;
    private final IElementImageProvider fImageProvider;
    private final IParseController fParseController;
    private final IRegionSelectionService regionSelector;

    public IMPOutlinePage(IParseController parseController, // OutlineContentProviderBase contentProvider,
            TreeModelBuilderBase modelBuilder,
            ILabelProvider labelProvider, IElementImageProvider imageProvider,
            IRegionSelectionService regionSelector) {
        fParseController= parseController;
//      fContentProvider= contentProvider;
        fModelBuilder= modelBuilder;
        fLabelProvider= labelProvider;
        fImageProvider= imageProvider;
        this.regionSelector= regionSelector;

        fContentProvider= new OutlineContentProviderBase(null) {
            private ModelTreeNode fOldTree= null;

            public Object[] getChildren(Object element) {
                ModelTreeNode node= (ModelTreeNode) element;
                return node.getChildren();
            }
            public Object getParent(Object element) {
                ModelTreeNode node= (ModelTreeNode) element;
                return node.getParent();
            }
            @Override
            public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
                super.inputChanged(viewer, oldInput, newInput);
                if (fOldTree != null) {
                    TreeDiffer treeDiffer= new TreeDiffer((TreeViewer) viewer, fLabelProvider);
                    treeDiffer.diff((ModelTreeNode) oldInput, (ModelTreeNode) newInput);
                }
                fOldTree= (ModelTreeNode) newInput;
            }
        };
    }

    public AnalysisRequired getAnalysisRequired() {
        return IModelListener.AnalysisRequired.SYNTACTIC_ANALYSIS;
    }

    public void update(final IParseController parseController,
            IProgressMonitor monitor) {
        if (getTreeViewer() != null) {
            getTreeViewer().getTree().getDisplay().asyncExec(new Runnable() {
                public void run() {
                    getTreeViewer().setInput(fModelBuilder.buildTree(fParseController.getCurrentAst()));
                }
            });
        }
    }

    @Override
    public void selectionChanged(SelectionChangedEvent event) {
        super.selectionChanged(event);
        ITreeSelection sel= (ITreeSelection) event.getSelection();

        if (sel.isEmpty())
            return;

        ModelTreeNode first= (ModelTreeNode) sel.getFirstElement();
        ISourcePositionLocator locator= fParseController.getNodeLocator();
        Object node= first.getASTNode();
        int startOffset= locator.getStartOffset(node);
        int endOffset= locator.getEndOffset(node);
        int length= endOffset - startOffset + 1;

        regionSelector.selectAndReveal(startOffset, length);
//        IEditorPart activeEditor= PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
//        AbstractTextEditor textEditor= (AbstractTextEditor) activeEditor;
//
//        textEditor.selectAndReveal(startOffset, length);
    }

    public void createControl(Composite parent) {
        super.createControl(parent);
        TreeViewer viewer= getTreeViewer();
        viewer.setContentProvider(fContentProvider);
        if (fLabelProvider != null) {
            viewer.setLabelProvider(fLabelProvider);
        }
        viewer.addSelectionChangedListener(this);
        ModelTreeNode rootNode= fModelBuilder.buildTree(fParseController.getCurrentAst());
        viewer.setInput(rootNode);
     }
}
