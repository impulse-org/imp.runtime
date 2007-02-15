package org.eclipse.uide.preferences;


import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.uide.preferences.ISafariPreferencesService;


/**
 * A multi-tab preferences page for Safari-supported languages.
 * The various tabs nominally represent the same sets of preferences
 * as set on different levels (default, workspace configuration,
 * workspace instance, and project).
 * 
 * @author suttons@us.ibm.com
 */
public abstract class SafariTabbedPreferencesPage extends PreferencePage implements IWorkbenchPreferencePage {
	
	// To hold tabs created by specializations of this class;
	// used below in methods that respond to buttons
	SafariPreferencesTab[] tabs = new SafariPreferencesTab[4];

	// To be provided by a language-specific preferences page
	// that is specialized from this one
	protected ISafariPreferencesService prefService = null;

	
	public SafariTabbedPreferencesPage() {
		this.noDefaultAndApplyButton();
	}

	protected Control createContents(Composite parent) {
		
		// Create a tab folder to put onto the page
		final TabFolder tabFolder = new TabFolder(parent, SWT.NONE);
        final GridData gd= new GridData(SWT.FILL, SWT.CENTER, true, false);
        gd.widthHint= 0;
        gd.heightHint= SWT.DEFAULT;
        gd.horizontalSpan= 1;
        tabFolder.setLayoutData(gd);

        // Create the tabs that go into the tab folder	
        tabs = createTabs(prefService, this, tabFolder);
        
        // The validity of the page depends on the validity of its tabs,
        // so refresh the valid state of the page now that all of the
        // tabs have been created
        notifyState(true);

        // Set the font on the page
		Dialog.applyDialogFont(parent);

		return tabFolder;
	}

	
	/**
	 * Create the tabs that represent the different levels of preferences
	 * shown on this page.  Nominally these are the default, workspace configuraiton,
	 * workspace instance, and project levels.
	 * 
	 * @param prefService	The service that manages the preferences by level
	 * @param page			The page on which the tabs are to be created (that is, this page)
	 * @param tabFolder		The tab folder, on this page, that will contain the created tabs
	 * @return				An array containing the created tabs
	 */
	protected abstract SafariPreferencesTab[] createTabs(
			ISafariPreferencesService prefService, SafariTabbedPreferencesPage page, TabFolder tabFolder);	 
	

	
	/**
	 * 
	 */
	public boolean notifyState(boolean state) {
		boolean allValid = true;
		for (int i = 0; i < tabs.length && allValid; i++) {
			allValid = allValid && tabs[i] != null && tabs[i].isValid();
		}
		setValid(allValid);
		return allValid;
	}
	
	
	/*
	 * The following four operations provide a page-level response to the pressing of
	 * buttons on the page.  Note, though, that a preference page may not have all of
	 * these buttons--buttons not present on the page may instead be present on individual
	 * tabs on the page.
	 */
	

	/**
	 * Respond to pressing of the Apply button by saving the prevailing preferences.
	 * 
	 * Note:  In a system of multiple preference levels with preference-value
	 * inheritance, this may only save values on the levels on which they
	 * are stored, i.e., not on levels where they apply through inheritance only.
	 * 
	 * @see org.eclipse.jface.preference.PreferencePage#performApply()
	 */
	public void performApply()
	{
		for (int i = 0; i < tabs.length; i++) {
			tabs[i].performApply();
		}
	}
	
	
	/**
	 * Respond to pressing of Cancel button by cancelling in-progress
	 * preference updates on each level.
	 * 
	 * @see org.eclipse.jface.preference.IPreferencePage#performCancel()
	 */
    public boolean performCancel()
    {
    	// SMS 4 Dec 2006
    	// Previously just used to return true; now trying to
    	// allow for a negative return
    	boolean result = true;
		for (int i = 0; i < tabs.length; i++) {
			result = result && tabs[i].performCancel();
		}

        return result;
    }

    
    /**
     * Respond to pressing of Restore Defaults button by restoring default
     * values on each level.
     * 
     * Note:  In a system of multiple preference levels with preference-value
     * inheritance, the default value on levels other than the default level
     * may be considered to be the level inherited from the next higher level,
     * so this may entail removing the preferences stored on each level other
     * than the default level.  On the default level, the programmed default
     * values should be restored.
     * 
     * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
     */
	public void performDefaults()
	{
		// SMS 4 Dec 2006
		// Need to check visibility of a composite that contains the tab;
		// be sure that the right one is checked here ...
		for (int i = 0; i < tabs.length; i++) {
			if (tabs[i].getTabItem().getControl().isVisible())
				tabs[i].performDefaults();
		}	

	}

	
	/**
	 * Respond to pressing of the Save button by saving the prevailing preferences.
	 * 
	 * Note:  In a system of multiple preference levels with preference-value
	 * inheritance, this may only save values on the levels on which they
	 * are stored, i.e., not on levels where they apply through inheritance only.
	 * 
	 * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
	 */
	public boolean performOk()
	{
		// SMS 4 Dec 2006
		// Not sure of the effect of returning false, but
		// should probably allow for that	
		boolean result = true;
		for (int i = tabs.length-1; i >= 0; i--) {
			result = result && tabs[i].performOk();
		}
		
		return result;
	}
	

	
	/**
	 * For IWorkbenchPreferencePage
	 * 
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}

	
	/**
	 * Get the tabs used on this preference page
	 * 
	 * @return	The tabs used on this preference page
	 */
	protected SafariPreferencesTab[] getTabs() {
		return tabs;
	}



}
