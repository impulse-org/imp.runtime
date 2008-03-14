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
