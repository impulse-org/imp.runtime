/*
 * (C) Copyright IBM Corporation 2007
 * 
 * This file is part of the Eclipse IMP.
 */
package org.eclipse.imp.preferences;

public class PreferenceConstants {
    public static final String P_EMIT_MESSAGES= "emitMessages";

    public static final String P_TAB_WIDTH= "tabWidth";

    public static final String P_SOURCE_FONT= "sourceFont";

    public static final String P_DUMP_TOKENS= "dumpTokens";

    /**
     * A named preference that controls whether the project explorer's selection is linked to the active editor.
     * <p>
     * Value is of type <code>Boolean</code>.
     * </p>
     */
    public static final String LINK_EXPLORER_TO_EDITOR= "org.eclipse.imp.ui.projects.linktoeditor"; //$NON-NLS-1$

    private PreferenceConstants() { }
}
