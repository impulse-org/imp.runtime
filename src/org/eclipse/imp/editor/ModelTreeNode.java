/**
 * 
 */
package org.eclipse.imp.editor;

public class ModelTreeNode {
    private static final ModelTreeNode[] NO_CHILDREN= new ModelTreeNode[0];

    private ModelTreeNode[] fChildren= NO_CHILDREN;

    private ModelTreeNode fParent;

    private final Object fASTNode;

    public ModelTreeNode(Object astNode) {
        fASTNode= astNode;
    }

    public ModelTreeNode(Object astNode, ModelTreeNode parent) {
        fASTNode= astNode;
        fParent= parent;
    }

    public void setChildren(ModelTreeNode[] children) {
        fChildren= children;
        for(int i= 0; i < children.length; i++) {
            children[i].fParent= this;
        }
    }

    public void addChild(ModelTreeNode child) {
        ModelTreeNode[] newChildren= new ModelTreeNode[fChildren.length + 1];
        System.arraycopy(fChildren, 0, newChildren, 0, fChildren.length);
        newChildren[fChildren.length]= child;
        fChildren= newChildren;
    }

    public ModelTreeNode[] getChildren() {
        return fChildren;
    }

    public ModelTreeNode getParent() {
        return fParent;
    }

    public Object getASTNode() {
        return fASTNode;
    }

    public String toString() {
        StringBuilder sb= new StringBuilder();

        sb.append(fASTNode.toString());
        if (fChildren.length > 0) {
            sb.append(" [");
            for(int i= 0; i < fChildren.length; i++) {
                sb.append(fChildren[i].toString());
            }
            sb.append(" ]");
        }
        return sb.toString();
    }
}