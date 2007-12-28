/*
 * (C) Copyright IBM Corporation 2007
 * 
 * This file is part of the Eclipse IMP.
 */
package org.eclipse.imp.core;

import java.text.MessageFormat;
import org.eclipse.osgi.util.NLS;

public class IMPMessages extends NLS {

    private static final String BUNDLE_NAME= "org.eclipse.imp.core.core";//$NON-NLS-1$

    public static String Assert_assertion_failed;

    public static String Assert_null_argument;

    public static String manager_filesToIndex;

    public static String manager_indexingInProgress;

    public static String process_name;

    public static String engine_searching;

    public static String engine_searching_indexing;

    public static String engine_searching_matching;

    public static String StatusBarUpdater_num_elements_selected;

    public static String OpenWithMenu_label;

    public static String EditorUtility_concatModifierStrings;

    private IMPMessages() {
    }

    public static String format(String message, Object object) {
        return MessageFormat.format(message, new Object[] { object});
    }

    public static String format(String message, Object[] objects) {
        return MessageFormat.format(message, objects);
    }

    static {
        NLS.initializeMessages(BUNDLE_NAME, IMPMessages.class);
    }
}
