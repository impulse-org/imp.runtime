/*
 * (C) Copyright IBM Corporation 2007
 * 
 * This file is part of the Eclipse IMP.
 */
package org.eclipse.imp.editor;

import org.eclipse.core.resources.IResource;

/**
 * Can be added to a ProblemMarkerManager to get notified about problem marker changes. Used to update error ticks.
 * 
 * @author Dr. Robert M. Fuhrer
 */
public interface IProblemChangedListener {
    /**
     * Called when problems changed. This call is posted in an aynch exec, therefore passed resources must not exist.
     * 
     * @param changedResources
     *            A set with elements of type <code>IResource</code> that describe the resources that had an problem
     *            change.
     * @param isMarkerChange
     *            If set to <code>true</code>, the change was a marker change, if <code>false</code>, the change
     *            came from an annotation model modification.
     */
    void problemsChanged(IResource[] changedResources, boolean isMarkerChange);
}
