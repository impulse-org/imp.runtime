/*
 * (C) Copyright IBM Corporation 2007
 * 
 * This file is part of the Eclipse IMP.
 */
package org.eclipse.imp.parser;

import lpg.runtime.IAst;

import org.eclipse.core.runtime.IPath;

/**
 * Locator implementation that works for LPG-generated AST's using the base IAst interface.
 * @author rfuhrer
 */
public class AstLocator implements ISourcePositionLocator {
    public Object findNode(Object node, int offset) {
	if (!(node instanceof IAst))
	    return node;
        IAst astNode= (IAst) node;

        if (offset >= astNode.getLeftIToken().getStartOffset() && offset <= astNode.getRightIToken().getEndOffset())
            return astNode;

        if (astNode.getAllChildren() == null)
            return null;

        for(int i= 0; i < astNode.getAllChildren().size(); i++) {
            IAst maybe= (IAst) findNode(astNode.getAllChildren().get(i), offset);
            if (maybe != null)
                return maybe;
        }
        return null;
    }

    public Object findNode(Object ast, int startOffset, int endOffset) {
        throw new UnsupportedOperationException();
    }
    
    public int getStartOffset(Object node) {
        IAst n = (IAst) node;
        return n.getLeftIToken().getStartOffset();
    }
    
    public int getEndOffset(Object node) {
        IAst n = (IAst) node;
        return n.getRightIToken().getEndOffset();
    }
    
    public int getLength(Object  node) {
    	return getEndOffset(node) - getStartOffset(node);
    }

    public IPath getPath(Object node) {
	return null;
    }
}
