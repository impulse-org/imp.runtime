/**
 * 
 */
package org.eclipse.imp.editor;

public final class ModelTreeContentProvider extends OutlineContentProviderBase {
    public ModelTreeContentProvider(OutlineInformationControl oic) {
        super(oic);
    }

    public Object[] getChildren(Object element) {
        ModelTreeNode node= (ModelTreeNode) element;
        return node.getChildren();
    }

    public Object getParent(Object element) {
        ModelTreeNode node= (ModelTreeNode) element;
        return node.getParent();
    }
}