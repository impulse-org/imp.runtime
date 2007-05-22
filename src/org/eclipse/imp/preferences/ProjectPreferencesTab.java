package org.eclipse.uide.preferences;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.preferences.ProjectSelectionDialog;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.uide.preferences.fields.SafariBooleanFieldEditor;
import org.eclipse.uide.preferences.fields.SafariComboFieldEditor;
import org.eclipse.uide.preferences.fields.SafariRadioGroupFieldEditor;
import org.eclipse.uide.preferences.fields.SafariStringFieldEditor;


public class ProjectPreferencesTab extends SafariPreferencesTab {
	
	protected StringFieldEditor selectedProjectName = null;
	protected List detailsLinks = new ArrayList();

	protected IJavaProject javaProject = null;	

	
	public ProjectPreferencesTab(ISafariPreferencesService prefService) {
		this.prefService = prefService;
		prefUtils = new SafariPreferencesUtilities(prefService);
	}

	public Composite createProjectPreferencesTab(SafariTabbedPreferencesPage page, final TabFolder tabFolder) {
		
		prefPage = page;

		/*
		 * Prepare the body of the tab
		 */
	
		GridLayout layout = null;
		
		final Composite composite= new Composite(tabFolder, SWT.NONE);
	        composite.setFont(tabFolder.getFont());
	        final GridData gd= new GridData(SWT.FILL, SWT.CENTER, true, false);
	        gd.widthHint= 0;
	        gd.heightHint= SWT.DEFAULT;
	        gd.horizontalSpan= 1;
	        composite.setLayoutData(gd);
		
		layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);
		
		// The "tab" on the tab folder
		tabItem = new TabItem(tabFolder, SWT.NONE);	
		tabItem.setText("Project");
		tabItem.setControl(composite);	
		SafariPreferencesTab.TabSelectionListener listener = 
			new SafariPreferencesTab.TabSelectionListener(prefPage, tabItem);
		tabFolder.addSelectionListener(listener);
		
		/*
		 * Add the elements relating to preferences fields and their associated "details" links.
		 */	
		fields = createFields(composite);

		// Clear some space
		SafariPreferencesUtilities.fillGridPlace(composite, 2);

		
		// Disable the details links since no project is selected at the start	
		for (int i = 0; i < detailsLinks.size(); i++) {
			((Link)detailsLinks.get(i)).setEnabled(false);
		}	
		
		SafariPreferencesUtilities.fillGridPlace(composite, 2);

		// Being newly loaded, the fields may be displayed with some
		// indication that they have been modified.  This should reset
		// that marking.
		clearModifiedMarksOnLabels();
		
		
		/*
		 * Put in the elements related to selecting a project
		 */
					
		// To hold the text selection (label + field) and button
		Group groupHolder = new Group(composite, SWT.SHADOW_ETCHED_IN);
		groupHolder.setText("Project selection");
		groupHolder.setLayout(new GridLayout(2, false));
		groupHolder.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		
		// To hold the text selection label + field
		Composite projectFieldHolder = new Composite(groupHolder, SWT.EMBEDDED);
		//layout = new GridLayout();
		//projectFieldHolder.setLayout(layout);
		projectFieldHolder.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		
		selectedProjectName = 
			new StringFieldEditor("SelectedProjectName", "Selected project:  ", projectFieldHolder);
		selectedProjectName.setStringValue("none selected");
		// Clear these here in case there are any saved from a previous interaction with the page
		// (assuming that we should start each  new page with no project selected)
		prefService.clearPreferencesAtLevel(ISafariPreferencesService.PROJECT_LEVEL);
		// Set the project name field to be non-editable
		selectedProjectName.getTextControl(projectFieldHolder).setEditable(false);
		// Set the attribute fields to be non-editable, since without a project selected
		// it makes no sense for them to be able to take values

		createSelectProjectButton(groupHolder, composite, "Select Project");
		addProjectSelectionListener(projectFieldHolder);
				
		SafariPreferencesUtilities.fillGridPlace(composite, 3);
		
		/*
		 * Put explanatory notes toward the bottom
		 * (not sure whether WRAP is helpful here; can manually
		 * wrap text in labels with '\n')
		 */
		
		final Composite bottom = new Composite(composite, SWT.BOTTOM | SWT.WRAP);
        layout = new GridLayout();
        bottom.setLayout(layout);
        bottom.setLayoutData(new GridData(SWT.BOTTOM));
        
        Label bar = new Label(bottom, SWT.WRAP);
        GridData data = new GridData();
        data.verticalAlignment = SWT.WRAP;
        bar.setLayoutData(data);
        bar.setText("Preferences are shown here only when a project is selected.\n\n" +
        			"Preferences shown with a white background are set on this level.\n\n" +
        			"Preferences shown with a colored background are inherited from a\nhigher level.\n\n" +
        			Markings.MODIFIED_NOTE + "\n\n" +
        			Markings.TAB_ERROR_NOTE);
        
		SafariPreferencesUtilities.fillGridPlace(bottom, 1);

		/*
		 * Put Restore Defaults and Apply buttons at the very bottom,
		 * disabled if (as expected) there is no project selected and
		 * the tab is otherwise mainly disabled
		 */
        buttons = prefUtils.createDefaultAndApplyButtons(composite, this);
        if (prefService.getProject() == null) {
        	for (int i = 0; i < buttons.length; i++) 
        		buttons[i].setEnabled(false);
        }
		return composite;
	}

	
	private void addProjectSelectionListener(Composite composite)
	{
		prefService.addProjectSelectionListener(new SafariProjectSelectionListener(composite));
	}
	


	private class SafariProjectSelectionListener implements SafariPreferencesService.IProjectSelectionListener
	{
		Composite composite = null;
		IEclipsePreferences.IPreferenceChangeListener currentListener = null;

		SafariProjectSelectionListener(Composite composite) {
			this.composite = composite;
		}
			
		/**
		 * Notification that a project was selected for inclusion in the preferences hierarchy.
		 * The given event must not be <code>null</code>.
		 * 
		 * @param event an event specifying the details about the new node
		 * @see IEclipsePreferences.NodeChangeEvent
		 * @see IEclipsePreferences#addNodeChangeListener(IEclipsePreferences.INodeChangeListener)
		 * @see IEclipsePreferences#removeNodeChangeListener(IEclipsePreferences.INodeChangeListener)
		 */
		public void selection(ISafariPreferencesService.ProjectSelectionEvent event) {
			addressProjectSelection(event, composite);
		}
	}
	

	protected List  currentListeners = new ArrayList();
	protected List 	currentListenerNodes = new ArrayList();
	
	
	protected void addressProjectSelection(ISafariPreferencesService.ProjectSelectionEvent event, Composite composite) {
		// TODO:  Override in subtype with a real implementation
		System.err.println("ProjectPreferencesTab.addressProjectSelection(..):  unimplemented");
	}
	

	
	protected void addProjectPreferenceChangeListeners(SafariBooleanFieldEditor field, String key, Composite composite)
	{
		IEclipsePreferences[] nodes = prefService.getNodesForLevels();
		for (int i = ISafariPreferencesService.PROJECT_INDEX; i < nodes.length; i++) {
			if (nodes[i] != null) {
				SafariPreferencesUtilities.SafariBooleanPreferenceChangeListener listener = 
					prefUtils.new SafariBooleanPreferenceChangeListener(field, key, composite);
				nodes[i].addPreferenceChangeListener(listener);
				currentListeners.add(listener);
				currentListenerNodes.add(nodes[i]);
			} else {
				//System.err.println("ProjectPreferencesTab.addPropetyChangeListeners(..):  no listener added at level = " + i + "; node at that level is null");
			}
		}	
	}
	
	
	protected void addProjectPreferenceChangeListeners(SafariComboFieldEditor field, String key, Composite composite)
	{
		IEclipsePreferences[] nodes = prefService.getNodesForLevels();
		for (int i = ISafariPreferencesService.PROJECT_INDEX; i < nodes.length; i++) {
			if (nodes[i] != null) {
				SafariPreferencesUtilities.SafariComboPreferenceChangeListener listener = 
					prefUtils.new SafariComboPreferenceChangeListener(field, key, composite);
				nodes[i].addPreferenceChangeListener(listener);
				currentListeners.add(listener);
				currentListenerNodes.add(nodes[i]);
			} else {
				//System.err.println("ProjectPreferencesTab.addPropetyChangeListeners(..):  no listener added at level = " + i + "; node at that level is null");
			}
		}	
	}
	
	
	protected void addProjectPreferenceChangeListeners(SafariRadioGroupFieldEditor field, String key, Composite composite)
	{
		IEclipsePreferences[] nodes = prefService.getNodesForLevels();
		for (int i = ISafariPreferencesService.PROJECT_INDEX; i < nodes.length; i++) {
			if (nodes[i] != null) {
				SafariPreferencesUtilities.SafariRadioGroupPreferenceChangeListener listener = 
					prefUtils.new SafariRadioGroupPreferenceChangeListener(field, key, composite);
				nodes[i].addPreferenceChangeListener(listener);
				currentListeners.add(listener);
				currentListenerNodes.add(nodes[i]);
			} else {
				//System.err.println("ProjectPreferencesTab.addPropetyChangeListeners(..):  no listener added at level = " + i + "; node at that level is null");
			}
		}	
	}
	
	protected void addProjectPreferenceChangeListeners(SafariStringFieldEditor field, String key, Composite composite)
	{
		IEclipsePreferences[] nodes = prefService.getNodesForLevels();
		for (int i = ISafariPreferencesService.PROJECT_INDEX; i < nodes.length; i++) {
			if (nodes[i] != null) {
				// SMS 31 Oct 2006
				//SafariProjectPreferenceChangeListener listener = new SafariProjectPreferenceChangeListener(field, key, composite);
				SafariPreferencesUtilities.SafariStringPreferenceChangeListener listener = 
					prefUtils.new SafariStringPreferenceChangeListener(field, key, composite);
				nodes[i].addPreferenceChangeListener(listener);
				currentListeners.add(listener);
				currentListenerNodes.add(nodes[i]);
			} else {
				//System.err.println("ProjectPreferencesTab.addPropetyChangeListeners(..):  no listener added at level = " + i + "; node at that level is null");
			}
		}	
	}
	


	protected void removeProjectPreferenceChangeListeners()
	{
		// Remove all listeners from their respective nodes
		for (int i = 0; i < currentListeners.size(); i++) {
			((IEclipsePreferences) currentListenerNodes.get(i)).removePreferenceChangeListener(
					((IEclipsePreferences.IPreferenceChangeListener)currentListeners.get(i)));
		}
		// Clear the lists
		currentListeners = new ArrayList();
		currentListenerNodes = new ArrayList();
	}

	
	/**
	 * 
	 * @param	composite	the wiget that holds the button
	 * @param	fieldParent	the wiget that holds the field that will be
	 * 						set when the button is pressed
	 * 						(needed for posting a listener)
	 * @param	text		text that appears in the link
	 */
	protected Button createSelectProjectButton(Composite composite, final Composite fieldParent, String text)
	{
		final Button button = new Button(composite, SWT.NONE);
		button.setText(text);
		
		final class CompositeLinkSelectionListener implements SelectionListener {
			ProjectSelectionButtonResponder responder = null;
			// param was Composite parent
			CompositeLinkSelectionListener(ProjectSelectionButtonResponder responder) {
				this.responder = responder;
			}
			
			public void widgetSelected(SelectionEvent e) {
				//doMakeProjectSelectionLinkActivated((Link) e.widget, fieldParent);
				responder.doProjectSelectionActivated((Button) e.widget, fieldParent);
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				//doMakeProjectSelectionLinkActivated((Link) e.widget, fieldParent);
				responder.doProjectSelectionActivated((Button) e.widget, fieldParent);
			}
		}

		CompositeLinkSelectionListener linkSelectionListener =
			new CompositeLinkSelectionListener(new ProjectSelectionButtonResponder());
		
		button.addSelectionListener(linkSelectionListener);
		return button;
	}

	
	
	private class ProjectSelectionButtonResponder {
	
		public void doProjectSelectionActivated(Button button, Composite composite)
		{
			HashSet projectsWithSpecifics = new HashSet();
			try {
				IJavaProject[] projects = JavaCore.create(ResourcesPlugin.getWorkspace().getRoot()).getJavaProjects();
				for (int i= 0; i < projects.length; i++) {
					IJavaProject curr = projects[i];
					//if (hasProjectSpecificOptions(curr.getProject())) {
					//	projectsWithSpecifics.add(curr);
					//}
				}
			} catch (JavaModelException e) {
				System.err.println("ProjectPreferencesTab:  JavaModelException obtaining Java projects; no project selected");
				return;
			}
			
			ProjectSelectionDialog dialog = new ProjectSelectionDialog(button.getShell(), projectsWithSpecifics);
			if (dialog.open() == Window.OK) {
				javaProject = (IJavaProject) dialog.getFirstResult();
			}
	
			if (javaProject != null) {
				IProject project = javaProject.getProject();
				if (project.exists())
					prefService.setProject(project);
				else {
					System.err.println("ProjectPreferencesTab:  Selected project does not exist; no project selected");
					return;
				}
			}
			
			// Enable the details links since now a project is selected
			for (int i = 0; i < detailsLinks.size(); i++) {
				((Link)detailsLinks.get(i)).setEnabled(true);
			}
			
			// Also enable the Restore Defaults and Apply buttons (if they should be enabled)
			for (int i = 0; i < buttons.length; i++) {
				if (((Button)buttons[i]).getText().equals(JFaceResources.getString("defaults"))) {
					buttons[i].setEnabled(true);
				} else if (((Button)buttons[i]).getText().equals(JFaceResources.getString("apply"))) {
					buttons[i].setEnabled(isValid());
				}
			}
			// This will set the enabled state of buttons on the
			// preference page appropriately
	        prefPage.setValid(isValid());
			
			
			//return javaProject.getElementName();
		}	
	}



	public void performApply()
	{
		if (prefService.getProject() == null) {
			// No preferences node into which to store anything
			clearModifiedMarksOnLabels();	// just in case fields still show modified
			return;
		}
		for (int i = 0; i < fields.length; i++) {
			fields[i].store();
			fields[i].clearModifiedMarkOnLabel();
		}
	}	
	
	 
	public boolean performCancel() {
		// Nullify the project in any case
		prefService.setProject(null);
		return true;
	}

	
	public void performDefaults() {
		if (prefService.getProject() == null) {
			// If no project set then there's no preferences
			// file from which to load anything	
			return;
		}
		// Clear all preferences at this level and reload them
		// using inheritance (so a value will be found at a higher
		// level if none is set at this level)
		prefService.clearPreferencesAtLevel(ISafariPreferencesService.PROJECT_LEVEL);
		for (int i = 0; i < fields.length; i++) {
			fields[i].loadWithInheritance();
		}
	}

	
	public boolean performOk()
	{
		if (prefService.getProject() != null) {
			// Store each field
			for (int i = 0; i < fields.length; i++) {
				fields[i].store();
			}
		} else {
			// Clear preferences because we're closing up dialog;
			// note that a project preferences node will exist, if only
			// in a leftover state, even when no project is selected
			prefService.clearPreferencesAtLevel(ISafariPreferencesService.PROJECT_LEVEL);
			//return true;
		}

		// Nullify the project in any case
		prefService.setProject(null);
		
		return true;
	}
	
	
}
