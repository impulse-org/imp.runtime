/*
 * (C) Copyright IBM Corporation 2007
 * 
 * This file is part of the Eclipse IMP.
 */
package org.eclipse.imp.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.imp.runtime.RuntimePlugin;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Initializes Imp framework-wide preferences to reasonable default values.
 * @author rfuhrer@watson.ibm.com
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {
    public void initializeDefaultPreferences() {
	IPreferenceStore store= RuntimePlugin.getInstance().getPreferenceStore();

	store.setDefault(PreferenceConstants.P_EMIT_MESSAGES, false);
//	store.setDefault(PreferenceConstants.P_SOURCE_FONT, 9);
	store.setDefault(PreferenceConstants.P_TAB_WIDTH, 8);
        store.setDefault(PreferenceConstants.P_DUMP_TOKENS, false);
    }
}
