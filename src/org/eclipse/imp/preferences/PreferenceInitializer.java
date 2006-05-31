package org.eclipse.uide.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.uide.runtime.RuntimePlugin;

/**
 * Initializes SAFARI framework-wide preferences to reasonable default values.
 * @author rfuhrer@watson.ibm.com
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {
    public void initializeDefaultPreferences() {
	IPreferenceStore store= RuntimePlugin.getInstance().getPreferenceStore();

	store.setDefault(PreferenceConstants.P_EMIT_MESSAGES, false);
    }
}
