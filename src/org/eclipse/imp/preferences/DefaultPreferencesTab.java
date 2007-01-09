package org.eclipse.uide.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

public class DefaultPreferencesTab extends SafariPreferencesTab
{	

	
	public DefaultPreferencesTab(ISafariPreferencesService prefService) {
		this.prefService = prefService;
		prefUtils = new SafariPreferencesUtilities(prefService);
	}
	
	
	public Composite createDefaultPreferencesTab(SafariTabbedPreferencesPage page, final TabFolder tabFolder) {
		
		prefPage = page;
		
        final Composite composite = new Composite(tabFolder, SWT.NONE);
        composite.setFont(tabFolder.getFont());
        final GridData gd= new GridData(SWT.FILL, SWT.CENTER, true, false);
        gd.widthHint= 0;
        gd.heightHint= SWT.DEFAULT;
        gd.horizontalSpan= 1;
        composite.setLayoutData(gd);
		
		GridLayout gl = new GridLayout();
		gl.numColumns = 2;
		composite.setLayout(gl);
		
		tabItem = new TabItem(tabFolder, SWT.NONE);
		tabItem.setText("Default");
		tabItem.setControl(composite);
		SafariPreferencesTab.TabSelectionListener listener = 
			new SafariPreferencesTab.TabSelectionListener(prefPage, tabItem);
		tabFolder.addSelectionListener(listener);
		

		// Don't want newly created fields to be flagged as modified
		fields = createFields(composite);

		// Being newly loaded, the fields may be displayed with some
		// indication that they have been modified.  This should reset
		// that marking.
		clearModifiedMarksOnLabels();
		
		SafariPreferencesUtilities.fillGridPlace(composite, 2);	
		
		// Put notes on bottom
	
		final Composite bottom = new Composite(composite, SWT.BOTTOM | SWT.WRAP);
        GridLayout layout = new GridLayout();
        bottom.setLayout(layout);
        bottom.setLayoutData(new GridData(SWT.BOTTOM));

        Label bar = new Label(bottom, SWT.WRAP);
        GridData data = new GridData();
        data.verticalAlignment = SWT.WRAP;
        bar.setLayoutData(data);
        bar.setText("These preferences are set programmatically and are not stored\n" +
        			"persistently.  Changes made here apply only to the current execution.\n\n" +
        			"Preferences on the default level cannot be removed.\n\n" +
        			Markings.MODIFIED_NOTE + "\n\n" +
        			Markings.TAB_ERROR_NOTE);
        
        prefUtils.fillGridPlace(bottom, 1);
 
        // Put buttons on the bottom
        buttons = prefUtils.createDefaultAndApplyButtons(composite, this);
        Button defaultsButton = (Button) buttons[0];
        Button applyButton = (Button) buttons[1];
      
		return composite;
	}

	
	
	/**
	 * Should be overridden in language-specific default preferences tab
	 * to make use of language-specific preference initializer.
	 * 
	 * @return 	The preference initializer to be used to initialize
	 * 			preferences in this tab
	 */
	public AbstractPreferenceInitializer getPreferenceInitializer() {
		// TODO:  Override in subclass where the language-specific
		// initializer should be known
		System.out.println("DefaultPreferencesTab.getPreferenceInitializar():  unimplemented; should be overridden with language-specific implementation");
		return null;
	}

	

//	public void performApply()
//	{	
//		for (int i = 0; i < fields.length; i++) {
//			fields[i].store();
//			fields[i].clearModifyMarkOnLabel();
//		}
//	}
	
		
	public void performDefaults() {
		// Clear all preferences at this level and reload them into the
		// preferences store through the initializer
		prefService.clearPreferencesAtLevel(ISafariPreferencesService.DEFAULT_LEVEL);
		AbstractPreferenceInitializer preferenceInitializer = getPreferenceInitializer();	//new PreferenceInitializer();
		preferenceInitializer.initializeDefaultPreferences();

		// Example:  reload each preferences field
		for (int i = 0; i < fields.length; i++) {
			fields[i].load();
		}
	}

//	public boolean performOk() {
//		// Example:  Store each field
//		for (int i = 0; i < fields.length; i++) {
//			fields[i].store();
//		}
//		return true;
//	}
	
	
}
