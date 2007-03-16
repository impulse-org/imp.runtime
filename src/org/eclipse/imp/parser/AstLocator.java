package org.eclipse.uide.parser;

import org.eclipse.core.runtime.IPath;

/**
 * Locator implementation for generic AST class.
 * @deprecated The generic AST and all its kin are deprecated; use a language-specific AST.
 * @author rfuhrer
 */
public class AstLocator implements IASTNodeLocator {
    public Object findNode(Object ast, int offset) {
        Ast root= (Ast) ast;

        if (root.token != null) {
            if (offset >= root.token.getStartOffset() && offset <= root.token.getEndOffset())
                return ast;
            else
                return null;
        }
        if (root.children == null)
            return null;
        for(int i= 0; i < root.children.size(); i++) {
            Ast maybe= (Ast) findNode(root.children.get(i), offset);
            if (maybe != null)
                return maybe;
        }
        return null;
    }

    public Object findNode(Object ast, int startOffset, int endOffset) {
        throw new UnsupportedOperationException();
    }
    
    public int getStartOffset(Object node) {
        Ast n = (Ast) node;
        return n.getToken().getStartOffset();
    }
    
    public int getEndOffset(Object node) {
        Ast n = (Ast) node;
        return n.getToken().getEndOffset();
    }
    
    public int getLength(Object  node) {
    	return getEndOffset(node) - getStartOffset(node);
    }

    public IPath getPath(Object node) {
	return null;
    }
}
