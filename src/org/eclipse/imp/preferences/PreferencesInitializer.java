package org.eclipse.imp.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;

public abstract class PreferencesInitializer extends AbstractPreferenceInitializer
{
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public abstract void initializeDefaultPreferences();

	
	/**
	 * For the given preferences level, clear the valeus of preferences that
	 * are initialized by this initializer.
	 * 
	 * @param level	The name of the preferences level for which preference
	 * 				values are to be cleared.
	 */
	public abstract void clearPreferencesOnLevel(String level);
}
