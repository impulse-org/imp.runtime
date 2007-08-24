/*
 * (C) Copyright IBM Corporation 2007
 * 
 * This file is part of the Eclipse IMP.
 */
package org.eclipse.imp.core;

import org.eclipse.osgi.util.NLS;

public class IMPMessages extends NLS {

    private static final String BUNDLE_NAME= "org.eclipse.imp.core.core";//$NON-NLS-1$

    public static String Assert_assertion_failed;

    public static String Assert_null_argument;

    private IMPMessages() {
    }

    static {
        NLS.initializeMessages(BUNDLE_NAME, IMPMessages.class);
    }
}
