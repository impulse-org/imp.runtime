package org.eclipse.uide.core;

import org.eclipse.osgi.util.NLS;

public class SAFARIMessages extends NLS {

    private static final String BUNDLE_NAME= "org.eclipse.safari.core.core";//$NON-NLS-1$

    public static String Assert_assertion_failed;

    public static String Assert_null_argument;

    private SAFARIMessages() {
    }

    static {
        NLS.initializeMessages(BUNDLE_NAME, SAFARIMessages.class);
    }
}
