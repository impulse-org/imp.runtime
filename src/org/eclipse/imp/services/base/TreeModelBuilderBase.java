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

package org.eclipse.imp.services.base;

import java.util.Stack;

import org.eclipse.imp.editor.ModelTreeNode;
import org.eclipse.imp.language.ILanguageService;

public abstract class TreeModelBuilderBase implements ILanguageService {
    protected ModelTreeNode fModelRoot;

    private Stack<ModelTreeNode> fItemStack= new Stack<ModelTreeNode>();

    public final ModelTreeNode buildTree(Object rootASTNode) {
        fItemStack.push(fModelRoot= createTopItem(new ModelTreeNode(rootASTNode)));
        visitTree(rootASTNode);
        fItemStack.pop();
        return fModelRoot;
    }

    protected abstract void visitTree(Object root);

    protected ModelTreeNode createTopItem(Object n) {
        ModelTreeNode treeNode= new ModelTreeNode(n);
        return treeNode;
    }

    protected ModelTreeNode createSubItem(Object n) {
        final ModelTreeNode parent= fItemStack.peek();
        ModelTreeNode treeNode= new ModelTreeNode(n, parent);
        parent.addChild(treeNode);
        return treeNode;
    }

    protected ModelTreeNode pushSubItem(Object n) {
        return fItemStack.push(createSubItem(n));
    }

    protected void popSubItem() {
        fItemStack.pop();
    }
}
