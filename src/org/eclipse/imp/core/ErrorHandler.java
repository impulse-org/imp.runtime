package org.eclipse.uide.core;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;
import org.eclipse.uide.preferences.SAFARIPreferenceCache;
import org.eclipse.uide.runtime.RuntimePlugin;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 2005  All Rights Reserved
 */
/**
 * Utility class for internal error messages
 * 
 * @author Claffra
 */
public class ErrorHandler {
    private static final boolean PRINT= true;
    private static final boolean DUMP= true;
    private static final boolean LOG= false;

    public static void reportError(String message, Throwable e) {
	reportError(message, false, e);
    }

    public static void reportError(String message, boolean showDialog, Throwable e) {
	if (PRINT)
	    System.err.println(message);
	if (DUMP)
	    e.printStackTrace();
	if (LOG)
	    logError(message, e);
	if (showDialog)
	    MessageDialog.openError(null, "SAFARI Error", message);
    }

    public static void reportError(String message) {
	reportError(message, false);
    }

    public static void reportError(String message, boolean showDialog) {
	reportError(message, showDialog, DUMP);
    }

    public static void reportError(final String message, boolean showDialog, boolean noDump) {
	if (PRINT)
	    System.err.println(message);
	if (!noDump)
	    new Error(message).printStackTrace();
	if (LOG || SAFARIPreferenceCache.emitMessages)
	    logError(message, new Error(message));
	if (showDialog) {
	    PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
		public void run() {
		    MessageDialog.openError(PlatformUI.getWorkbench().getDisplay().getActiveShell(), "SAFARI Error", message);
		}
	    });
	}
    }

    public static void logError(String msg, Throwable e) {
	RuntimePlugin.getInstance().logException(msg, e);
    }

    public static void logMessage(String msg, Throwable e) {
	RuntimePlugin.getInstance().logException(msg, e);
    }
}
