package org.eclipse.imp.preferences;


import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

public class InstancePreferencesTab extends PreferencesTab {
	
	public InstancePreferencesTab(IPreferencesService prefService) {
		this.prefService = prefService;
		prefUtils = new PreferencesUtilities(prefService);
	}
	
	
	public Composite createInstancePreferencesTab(TabbedPreferencesPage page, final TabFolder tabFolder) {
		
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
		tabItem.setText("Workspace");
		tabItem.setControl(composite);
		PreferencesTab.TabSelectionListener listener = 
			new PreferencesTab.TabSelectionListener(prefPage, tabItem);
		tabFolder.addSelectionListener(listener);
		
		
		/*
		 * Add the elements relating to preferences fields and their associated "details" links.
		 */	
		fields = createFields(page, this, IPreferencesService.INSTANCE_LEVEL, composite, prefService);

		
		PreferencesUtilities.fillGridPlace(composite, 2);
		

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
        bar.setText("Preferences shown with a white background are set on this level.\n\n" +
        			"Preferences shown with a colored background are inherited from a\nhigher level.\n\n" +
        			Markings.MODIFIED_NOTE + "\n\n" +
        			Markings.TAB_ERROR_NOTE);
        
		PreferencesUtilities.fillGridPlace(bottom, 1);
		
		 
		// Put bottons on the bottom
        buttons = prefUtils.createDefaultAndApplyButtons(composite, this);
        Button defaultsButton = (Button) buttons[0];
        Button applyButton = (Button) buttons[1];
		
		return composite;
	}

	

	
//		public void performApply()
//		{
//			for (int i = 0; i < fields.length; i++) {
//				fields[i].store();
//				fields[i].clearModifyMarkOnLabel();
//			}
//		}	
	
		
	
	public void performDefaults() {
		// Clear all preferences at this level and reload them
		// using inheritance (so a value will be found at a higher
		// level if none is set at this level)
		prefService.clearPreferencesAtLevel(IPreferencesService.INSTANCE_LEVEL);

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
