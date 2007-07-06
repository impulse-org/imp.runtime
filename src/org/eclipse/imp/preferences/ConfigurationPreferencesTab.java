package org.eclipse.uide.preferences;

	
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;


public class ConfigurationPreferencesTab  extends SafariPreferencesTab
{

	public ConfigurationPreferencesTab(ISafariPreferencesService prefService) {
		this.prefService = prefService;
		prefUtils = new SafariPreferencesUtilities(prefService);
	}
	
	
	public Composite createConfigurationPreferencesTab(SafariTabbedPreferencesPage page, final TabFolder tabFolder) {
		
		prefPage = page;

        final Composite composite= new Composite(tabFolder, SWT.NONE);
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
		tabItem.setText("Configuration");
		tabItem.setControl(composite);
		SafariPreferencesTab.TabSelectionListener listener = 
			new SafariPreferencesTab.TabSelectionListener(prefPage, tabItem);
		tabFolder.addSelectionListener(listener);
		
		
		/*
		 * Add the elements relating to preferences fields and their associated "details" links.
		 */	
		//fields = createFields(composite);
		fields = createFields(page, this, ISafariPreferencesService.CONFIGURATION_LEVEL, composite, prefService);
		
		SafariPreferencesUtilities.fillGridPlace(composite, 2);
		
		// Don't want newly created fields to be flagged as modified
		clearModifiedMarksOnLabels();
		
		
		// Put notes on bottom
		
		final Composite bottom = new Composite(composite, SWT.BOTTOM | SWT.WRAP);
        GridLayout layout = new GridLayout();
        bottom.setLayout(layout);
        bottom.setLayoutData(new GridData(SWT.BOTTOM));
        
        Label bar = new Label(bottom, SWT.WRAP);
        GridData data = new GridData();
        data.verticalAlignment = SWT.WRAP;
        bar.setLayoutData(data);
        bar.setText("Preferences shown with a white (or neutral) background are set on this level.\n\n" +
        			"Preferences shown with a colored background are inherited from a\nhigher level.\n\n" +
        			Markings.MODIFIED_NOTE + "\n\n" +
        			Markings.TAB_ERROR_NOTE);
        
		SafariPreferencesUtilities.fillGridPlace(bottom, 1);
		
		
		// Put buttons on bottom
        buttons = prefUtils.createDefaultAndApplyButtons(composite, this);
        //Button defaultButton = (Button) buttons[0];
        //Button applyButton = (Button) buttons[1];

		return composite;
	}

	

//	public void performApply()
//	{
//		for (int i = 0; i < fields.length; i++) {
//			fields[i].store();
//			fields[i].clearModifyMarkOnLabel();
//		}
//	}	
	
	
	
	public void performDefaults() {
		// Clear all preferences at this level and reload them
		// using inheritance (so a value will be found at a higher
		// level if none is set at this level)
		prefService.clearPreferencesAtLevel(ISafariPreferencesService.CONFIGURATION_LEVEL);

		for (int i = 0; i < fields.length; i++) {
			fields[i].loadWithInheritance();
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
