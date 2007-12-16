package org.eclipse.imp.editor.internal;

import org.eclipse.imp.editor.ModelTreeNode;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.TreeViewer;

public class TreeDiffer {
    private final TreeViewer fTreeViewer;
    private final ILabelProvider fLabelProvider;

//  private static final String[] ALL_PROPS= new String[0];

    public TreeDiffer(TreeViewer viewer, ILabelProvider labelProvider) {
        fTreeViewer= viewer;
        fLabelProvider= labelProvider;
    }

    public void diff(ModelTreeNode oldNode, ModelTreeNode newNode) {
        if (1 == 1) return;
        if (oldNode == newNode)
            return;
        if (fLabelProvider.getText(oldNode).equals(fLabelProvider.getText(newNode)))
            return;
        fTreeViewer.update(newNode, null);
        ModelTreeNode[] oldChildren= oldNode.getChildren();
        ModelTreeNode[] newChildren= newNode.getChildren();
        int oi= 0, ni= 0;
        for(; oi < oldChildren.length && ni < newChildren.length; ) {
            ModelTreeNode oldChild= oldChildren[oi];
            ModelTreeNode newChild= newChildren[ni];

            if (oldChild == newChild || fLabelProvider.getText(oldChild).equals(fLabelProvider.getText(newChild))) {
                
            }
        }
    }
}
