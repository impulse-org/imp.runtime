/*
 * (C) Copyright IBM Corporation 2007
 * 
 * This file is part of the Eclipse IMP.
 */
package org.eclipse.imp.wizards.fields;

/**
 * Change listener used by <code>DialogField</code>
 */
public interface IDialogFieldListener {
    /**
     * The dialog field has changed.
     */
    void dialogFieldChanged(DialogField field);
}