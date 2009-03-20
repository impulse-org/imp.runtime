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

    /**
     * A named preference that controls whether bracket matching highlighting is turned on or off.
     * <p>
     * Value is of type <code>Boolean</code>.
     * </p>
     */
    public static final String EDITOR_MATCHING_BRACKETS= "matchingBrackets"; //$NON-NLS-1$

    /**
     * A named preference that holds the color used to highlight matching brackets.
     * <p>
     * Value is of type <code>String</code>. A RGB color value encoded as a string 
     * using class <code>PreferenceConverter</code>
     * </p>
     * 
     * @see org.eclipse.jface.resource.StringConverter
     * @see org.eclipse.jface.preference.PreferenceConverter
     */
    public static final String EDITOR_MATCHING_BRACKETS_COLOR=  "matchingBracketsColor"; //$NON-NLS-1$

    /**
     * A named preference that controls whether "fences" (e.g. parens or braces) are automatically closed.
     * <p>
     * Value is of type <code>Boolean</code>.
     * </p>
     */
    public static final String EDITOR_CLOSE_FENCES= "closeFences"; //$NON-NLS-1$

    /**
     * A named preference that controls whether builders should emit diagnostics. Can be overridden
     * by a language-specific builder preference of the same key.
     * <p>
     * Value is of type <code>Boolean</code>.
     * </p>
     */
    public static final String P_EMIT_BUILDER_DIAGNOSTICS= "emitBuilderDiagnostics"; //$NON-NLS-1$

    private PreferenceConstants() { }
}
