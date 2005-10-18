package org.eclipse.uide.parser;

import org.eclipse.core.runtime.IProgressMonitor;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 2005  All Rights Reserved
 */

/**
 * @author Claffra
 *
 * TODO add documentation
 */
public interface IModelListener {
    
    /**
     * Notify the listener that the document has been updated and a new AST has been computed
     * @param parseController	the new parse result containing the AST
     * @param monitor the progress monitor; listener should cancel when monitor.isCanceled() is true
     */
    public void update(IParseController parseController, IProgressMonitor monitor);
    
}
