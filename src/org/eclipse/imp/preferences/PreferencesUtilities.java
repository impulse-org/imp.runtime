package org.eclipse.uide.preferences;


import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;
import org.eclipse.uide.preferences.fields.SafariBooleanFieldEditor;
import org.eclipse.uide.preferences.fields.SafariDirectoryListFieldEditor;
import org.eclipse.uide.preferences.fields.SafariFileFieldEditor;
import org.eclipse.uide.preferences.fields.SafariStringFieldEditor;


public class SafariPreferencesUtilities {

	
	public Color colorWhite = new Color(null, 255, 255, 255);
	public Color colorBluish = new Color(null, 175, 207, 239);
	public Color colorGreenish = new Color(null, 0, 127, 239);
	public Color colorLightGray = new Color(null, 224, 223, 226);
	
	ISafariPreferencesService service = null;
	
	public SafariPreferencesUtilities(ISafariPreferencesService service)
	{
		this.service = service;
	}
	
	
	public String setField(SafariStringFieldEditor field, Composite composite)
	{
		// TODO:  Add checks on input validity (see below)
		// Note:  so far assumes that the given level is the one to which the
		// field belongs (which should be true but isn't guaranteed (?))

		String level = field.getPreferencesLevel();
		
		// If the level is "project" and project == null then just set
		// the field here (as a special case).
		if (ISafariPreferencesService.PROJECT_LEVEL.equals(level) &&
			service.getProject() == null)
		{
			if (composite.isDisposed()) {
				System.err.println("SafariPreferencesUtilities.setField():  composite is disposed");
			}
			field.setStringValue(null);
			if (!composite.isDisposed()) {
				// Assume that editability on the project level is set
				// appropriately by a project-selection listener
				//field.getTextControl(composite).setEditable(false);
				field.getTextControl(composite).setBackground(colorBluish);
			}
			// Pretend that this was set at the project level?
			// (It was certainly cleared at that level)
			return ISafariPreferencesService.PROJECT_LEVEL;
		}
		
		// Otherwise, we have a legitimate level, so set normally
		String levelFromWhichSet = field.loadWithInheritance();
		
		// Note:  You can evidently load a field even when it's control
		// is disposed.  In that case (evidently) you can change the
		// text in the field but not the background color.	
		
		if (!composite.isDisposed()) {
			/*
			if (level != null && level.equals(levelFromWhichSet)) {
				field.getTextControl(composite).setBackground(colorWhite);
			} else if (level != null && field.getTextControl(composite).getEditable()) {
				field.getTextControl(composite).setBackground(colorBluish);
			}
			*/
			if (field.isInherited()) {
				field.getTextControl(composite).setBackground(colorBluish);
			} else 	{
				field.getTextControl(composite).setBackground(colorWhite);
			}
		} else {
			// If composite.isDisposed(), then both field.getTextControl(composite)
			// and field.getTextControl() will return null; if needed, a text control
			// must be obtained from somewhere else--but I have no idea where that
			// might be.  Not sure why composite.isDisposed() here in the first place,
			// especially considering that the field can be set
		}
		
		return levelFromWhichSet;
	}
	
	
	
	public String setField(SafariStringFieldEditor field, Composite composite, String value)
	{
		final String whoiam = "SafariPreferenesUtilities.setField(String field, composite, value):  ";
		
		if (field == null)
			throw new IllegalArgumentException(whoiam + "given field is null");
		if (composite == null)
			throw new IllegalArgumentException(whoiam + "given composite is null");
		if (value == null)
			throw new IllegalArgumentException(whoiam + "given value is null");
		
		if (composite.isDisposed())
			throw new IllegalStateException(whoiam + "composite is disposed");
		if (!field.getTextControl(composite).getEditable())
			throw new IllegalStateException(whoiam + "field is not editable");
		if (value.equals("") && !field.isEmptyStringAllowed())
			throw new IllegalArgumentException(whoiam + "value is empty and field does not allow empty values");
		if (ISafariPreferencesService.PROJECT_LEVEL.equals(field.getPreferencesLevel()) &&
				service.getProject() == null)
			throw new IllegalStateException(whoiam + "field represents a project-level preference and project is not set");
		
		String level = field.getPreferencesLevel();
				
		field.setStringValue(value);
		// setString(value) takes care of setting isInherited
		// and presentsDefaultValue, but not ...
		field.getTextControl(composite).setBackground(colorWhite);



		return level;
	}
	
	
	
	
	public String setField(SafariBooleanFieldEditor field, Composite parent)
	{
		// TODO:  Add checks on input validity (see below)
		// Note:  so far assumes that the given level is the one to which the
		// field belongs (which should be true but isn't guaranteed (?))

		String level = field.getPreferencesLevel();
		
		// If the level is "project" and project == null then just set
		// the field here (as a special case).
		// Note:  without some project selected the field should not be
		// editable.  Field will have to be set back to editable when
		// (and probably where) a project is selected.  We might take
		// care of this elsewhere but, until that is verified, keep
		// doing it here.
		// Note also:  loadWithInheritance (which calls setField(..))
		// won't know that project == null and will try toset the field
		// from some higher level
		if (ISafariPreferencesService.PROJECT_LEVEL.equals(level) &&
			service.getProject() == null)
		{
			if (parent == null) {
				System.err.println("SafariPreferencesUtilities.setField():  parent is null");
			}	
			if (parent.isDisposed()) {
				System.err.println("SafariPreferencesUtilities.setField():  parent is disposed");
			}	
			// Don't have a null boolean value to set field to,
			// but would like to show it as "cleared" somehow
			// (presumably "false" shows as empty
			field.setBooleanValue(false);
			if (!parent.isDisposed()) {
				field.getChangeControl().setEnabled(false);
				field.getChangeControl().setBackground(colorBluish);
			}
			// Pretend that this was set at the project level?
			// (It was certainly cleared at that level)
			return ISafariPreferencesService.PROJECT_LEVEL;
		}
		
		// Otherwise, we have a legitimate level, so set normally
		String levelFromWhichSet = field.loadWithInheritance();
		
		// Note:  You can evidently load a field even when it's control
		// is disposed.  In that case (evidently) you can change the
		// text in the field but not the background color.	
		
		if (parent != null && !parent.isDisposed()) {
		//if (!parent.isDisposed()) {
			if (level != null && level.equals(levelFromWhichSet)) {
				field.getChangeControl(parent).setBackground(colorLightGray);
				//field.setRemovable(true);
			} else if (level != null && field.getChangeControl(parent).getEnabled()) {
				field.getChangeControl(parent).setBackground(colorBluish);
			}
		} else {
			// If composite.isDisposed(), then both field.getTextControl(composite)
			// and field.getTextControl() will return null; if needed, a text control
			// must be obtained from somewhere else--but I have no idea where that
			// might be.  Not sure why composite.isDisposed() here in the first place,
			// especially considering that the field can be set
		}
		
		return levelFromWhichSet;
	}
	
	
	
	public String setField(SafariBooleanFieldEditor field, Composite composite, boolean value)
	{
		final String whoiam = "SafariPreferenesUtilities.setField(boolean field, composite, value):  ";
		
		if (field == null)
			throw new IllegalArgumentException(whoiam + "given field is null");
		if (composite == null)
			throw new IllegalArgumentException(whoiam + "given composite is null");
		
		if (composite.isDisposed())
			throw new IllegalStateException(whoiam + "composite is disposed");
		if (!field.getChangeControl(composite).getEnabled())
			throw new IllegalStateException(whoiam + "field is not editable");
		if (ISafariPreferencesService.PROJECT_LEVEL.equals(field.getPreferencesLevel()) &&
				service.getProject() == null)
			throw new IllegalStateException(whoiam + "field represents a project-level preference and project is not set");
		
		String level = field.getPreferencesLevel();
				
		field.setBooleanValue(value);
		field.getChangeControl(composite).setBackground(colorLightGray);
		//field.setRemovable(true);
		// isInherited, levelAtWhichSet should be addressed by field in setBooleanValue

		return level;
	}

	
	public SafariStringFieldEditor makeNewStringField(
			PreferencePage page,
			SafariPreferencesTab tab,
			ISafariPreferencesService service,
			String level, String key, String text,
			Composite parent,
			boolean isEnabled, boolean isEditable,
			boolean hasSpecialValue, String specialValue,
			boolean emptyValueAllowed, String emptyValue,
			boolean isRemovable)
	{
		//System.err.println("SPU.makeNewStringField() starting for key = " + key);
		Composite fieldHolder = new Composite(parent, SWT.EMBEDDED);
		fieldHolder.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

	    boolean onProjectLevelWithNullProject =
	    	level != null && level.equals(ISafariPreferencesService.PROJECT_LEVEL) && service.getProject() == null;
	    boolean notOnARealLevel = level == null;
	    boolean onAFunctioningLevel = !onProjectLevelWithNullProject && !notOnARealLevel;
	    
		SafariStringFieldEditor field = new SafariStringFieldEditor(page, tab, service, level, key, text, fieldHolder);
		
		if (!onProjectLevelWithNullProject) {
			setField(field, fieldHolder);
			addStringPropertyChangeListeners(service, level, field, key, fieldHolder);
		} else {
			//setField(field, fieldHolder);
			//addStringPropertyChangeListeners(service, level, field, key, fieldHolder);
		}
		
		field.getTextControl().setEnabled(isEnabled);
		if (onProjectLevelWithNullProject || notOnARealLevel) {
			field.getTextControl().setEditable(false);
		} else if (onAFunctioningLevel) {
			field.getTextControl().setEditable(isEditable);
		}

		if (hasSpecialValue)
			field.setSpecialValue(specialValue);
		else
			field.setNoSpecialValue();
		field.setEmptyValueAllowed(emptyValueAllowed);
		
		if (level == null) field.setRemovable(false);	// can never remove from a field that doesn't have a stored value
		else if (level.equals(ISafariPreferencesService.DEFAULT_LEVEL)) field.setRemovable(false);	// can never remove from Default level
		else field.setRemovable(isRemovable);
		
		//System.err.println("SPU.makeNewStringField() ending for key = " + key);
		return field;
	}

	// Lacks preference-page and preference-tab parameters, so should be deprecated eventually
	/*
	public SafariFileFieldEditor makeNewFileField(
			ISafariPreferencesService service,
			String level, String key, String text,
			Composite parent,
			boolean isEnabled, boolean isEditable,
			boolean hasSpecialValue, String specialValue,
			boolean emptyValueAllowed, String emptyValue,
			boolean isRemovable)
	{
		//System.err.println("SPU.makeNewFileField() starting for key = " + key);
		
		Composite fieldHolder = new Composite(parent, SWT.EMBEDDED);
		fieldHolder.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

	    boolean onProjectLevelWithNullProject =
	    	level != null && level.equals(ISafariPreferencesService.PROJECT_LEVEL) && service.getProject() == null;
	    boolean onARealLevel = level != null;
	    boolean onAFunctioningLevel = !onProjectLevelWithNullProject && onARealLevel;
	    
		SafariFileFieldEditor field = new SafariFileFieldEditor(service, level, key, text, fieldHolder);
		
		if (!onProjectLevelWithNullProject) {
			setField(field, fieldHolder);
			addStringPropertyChangeListeners(service, level, field, key, fieldHolder);
		} else {
			//setField(field, fieldHolder);
			//addStringPropertyChangeListeners(service, level, field, key, fieldHolder);
		}
		

		if (onProjectLevelWithNullProject || !onARealLevel) {
			field.getTextControl().setEnabled(false);
			field.getTextControl().setEditable(false);
		} else { //if (onAFunctioningLevel) {
			field.getTextControl().setEnabled(isEnabled);
			field.getTextControl().setEditable(isEditable);
		}

		if (onProjectLevelWithNullProject || !onARealLevel) {
			field.getChangeControl(fieldHolder).setEnabled(false);
		} else { //if (onAFunctioningLevel) {
			field.getChangeControl(fieldHolder).setEnabled(isEnabled);
		}
		
		
		if (hasSpecialValue)
			field.setSpecialValue(specialValue);
		else
			field.setNoSpecialValue();
		field.setEmptyValueAllowed(emptyValueAllowed);
		
		if (level == null) field.setRemovable(false);	// can never remove from "Applicable" level (if that's what this is)
		else if (level.equals(ISafariPreferencesService.DEFAULT_LEVEL)) field.setRemovable(false);	// can never remove from Default level
		else field.setRemovable(isRemovable);
		
		//System.err.println("SPU.makeNewFileField() ending for key = " + key);
		return field;
	}
*/
	
	// With PreferencePage
	public SafariFileFieldEditor makeNewFileField(
			PreferencePage page,
			SafariPreferencesTab tab,
			ISafariPreferencesService service,
			String level, String key, String text,
			Composite parent,
			boolean isEnabled, boolean isEditable,
			boolean hasSpecialValue, String specialValue,
			boolean emptyValueAllowed, String emptyValue,
			boolean isRemovable)
	{
		//System.err.println("SPU.makeNewFileField() starting for key = " + key);
		
		Composite fieldHolder = new Composite(parent, SWT.EMBEDDED);
		fieldHolder.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

	    boolean onProjectLevelWithNullProject =
	    	level != null && level.equals(ISafariPreferencesService.PROJECT_LEVEL) && service.getProject() == null;
	    boolean notOnARealLevel = level == null;
	    boolean onAFunctioningLevel = !onProjectLevelWithNullProject && notOnARealLevel;
	    
		SafariFileFieldEditor field = new SafariFileFieldEditor(page, tab, service, level, key, text, fieldHolder);
		
		if (!onProjectLevelWithNullProject) {
			setField(field, fieldHolder);
			addStringPropertyChangeListeners(service, level, field, key, fieldHolder);
		} else {
			//setField(field, fieldHolder);
			//addStringPropertyChangeListeners(service, level, field, key, fieldHolder);
		}
		
		Text textControl = field.getTextControl();
		// Want to set enabled differently from editable since
		// disabling has the effect of graying out whereas setting
		// not editable leaves the appearance alone and just renders
		// the control inoperative
		textControl.setEnabled(isEnabled);
		//field.setEnabled(isEnabled, fieldHolder);
		if (onProjectLevelWithNullProject || notOnARealLevel) {
			textControl.setEditable(false);
		} else if (onAFunctioningLevel) {
			textControl.setEditable(isEditable);
		}
		// This sort of field has a button, that should be
		// enabled or disabled in conjunction with the text
		field.getChangeControl(fieldHolder).setEnabled(isEnabled);
		
		if (hasSpecialValue)
			field.setSpecialValue(specialValue);
		else
			field.setNoSpecialValue();
		field.setEmptyValueAllowed(emptyValueAllowed);
		
		if (level == null) field.setRemovable(false);	// can never remove from "Applicable" level (if that's what this is)
		else if (level.equals(ISafariPreferencesService.DEFAULT_LEVEL)) field.setRemovable(false);	// can never remove from Default level
		else field.setRemovable(isRemovable);
		
		//System.err.println("SPU.makeNewFileField() ending for key = " + key + " with button enabled = " + field.getChangeControl(fieldHolder).isEnabled());
		
		return field;
	}

	
	public SafariDirectoryListFieldEditor makeNewDirectoryListField(
			PreferencePage page,
			SafariPreferencesTab tab,
			ISafariPreferencesService service,
			String level, String key, String text,
			Composite parent,
			boolean isEnabled, boolean isEditable,
			boolean hasSpecialValue, String specialValue,
			boolean emptyValueAllowed, String emptyValue,
			boolean isRemovable)
	{
		//System.err.println("SPU.makeNewDirectoryField() starting for key = " + key);
		
		Composite fieldHolder = new Composite(parent, SWT.EMBEDDED);
		fieldHolder.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

	    boolean onProjectLevelWithNullProject =
	    	level != null && level.equals(ISafariPreferencesService.PROJECT_LEVEL) && service.getProject() == null;
	    boolean notOnARealLevel = level == null;
	    boolean onAFunctioningLevel = !onProjectLevelWithNullProject && !notOnARealLevel;
	    
	    SafariDirectoryListFieldEditor field = new SafariDirectoryListFieldEditor(page, tab, service, level, key, text, fieldHolder);
		
		if (!onProjectLevelWithNullProject) {
			setField(field, fieldHolder);
			addStringPropertyChangeListeners(service, level, field, key, fieldHolder);
		} else {
			//setField(field, fieldHolder);
			//addStringPropertyChangeListeners(service, level, field, key, fieldHolder);
		}
		
		Text textControl = field.getTextControl();
		// Want to set enabled differently from editable since
		// disabling has the effect of graying out whereas setting
		// not editable leaves the appearance alone and just renders
		// the control inoperative
		textControl.setEnabled(isEnabled);
		if (onProjectLevelWithNullProject || notOnARealLevel) {
			textControl.setEditable(false);
		} else if (onAFunctioningLevel) {
			textControl.setEditable(isEditable);
		}
		// This sort of field has a button, that should be
		// enabled or disabled in conjunction with the text
		field.getChangeControl(fieldHolder).setEnabled(isEnabled);
		
		
		
		if (hasSpecialValue)
			field.setSpecialValue(specialValue);
		else
			field.setNoSpecialValue();
		field.setEmptyValueAllowed(emptyValueAllowed);
		
		if (level == null) field.setRemovable(false);	// can never remove from "Applicable" level (if that's what this is)
		else if (level.equals(ISafariPreferencesService.DEFAULT_LEVEL)) field.setRemovable(false);	// can never remove from Default level
		else field.setRemovable(isRemovable);
		
		//System.err.println("SPU.makeNewDirectoryListField() ending for key = " + key + " with button enabled = " + field.getChangeControl(fieldHolder).isEnabled());
		
		return field;
	}

	
	

	public SafariBooleanFieldEditor makeNewBooleanField(
			ISafariPreferencesService service,
			String level, String key, String text,
			Composite parent,
			boolean isEnabled, boolean isEditable,	
			boolean hasSpecialValue, boolean specialValue,
			boolean emptyValueAllowed, boolean emptyValue,
			boolean isRemovable)
	{
		//System.err.println("SPU.makeNewBooleanField() starting for key = " + key);
		Composite fieldHolder = new Composite(parent, SWT.EMBEDDED);	
		fieldHolder.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

	    
		SafariBooleanFieldEditor field =
			new SafariBooleanFieldEditor(service, level, key, text, fieldHolder);
		if (level != null && level.equals(ISafariPreferencesService.PROJECT_LEVEL) && service.getProject() != null) {
			setField(field, fieldHolder);
			addBooleanPropertyChangeListeners(service, level, field, key, fieldHolder);
		} else {
			setField(field, fieldHolder);
			addBooleanPropertyChangeListeners(service, level, field, key, fieldHolder);
		}
		field.getChangeControl().setEnabled(isEnabled);
		// boolean controls have no setEditable() method
		field.setSpecialValue(false);
		field.setEmptyValueAllowed(false);
		
		if (level == null) field.setRemovable(false);
		else if (level.equals(ISafariPreferencesService.DEFAULT_LEVEL)) field.setRemovable(false);
		else field.setRemovable(isRemovable);

		//System.err.println("SPU.makeNewBooleanField() ending for key = " + key);
		return field;
	}	

	
	
	private void addStringPropertyChangeListeners(
		ISafariPreferencesService service, String level, SafariStringFieldEditor field, String key, Composite composite)
	{
		int levelIndex = service.getIndexForLevel(level);
		IEclipsePreferences[] nodes = service.getNodesForLevels();
		
		for (int i = levelIndex + 1; i < nodes.length; i++) {
			if (nodes[i] != null) {
				nodes[i].addPreferenceChangeListener(new SafariStringPreferenceChangeListener(field, key, composite));	
			} else {
				//	System.err.println("JsdivInstancePreferencesPage.addPropetyChangeListeners(..):  no listener added at level = " + i + "; node at that level is null");
			}
		}	
	}

	
	public class SafariStringPreferenceChangeListener implements IEclipsePreferences.IPreferenceChangeListener
	{
		Composite composite = null;
		String key = null;
		SafariStringFieldEditor field = null;
		
		public SafariStringPreferenceChangeListener(SafariStringFieldEditor field, String key, Composite composite) {
			this.field = field;
			this.key = key;	
			this.composite = composite;
		}
		
		public void preferenceChange(PreferenceChangeEvent event)
		{
			if (event.getNewValue() == null && event.getOldValue() == null) return;
			if (event.getNewValue() != null && event.getNewValue().equals(event.getOldValue())) return;	
			
			String eventKey = event.getKey();
			if (!composite.isDisposed()) {
				if (eventKey.equals(key)) {
					setField(field, composite);
				}
			}
		}
	}
	
	
	private void addBooleanPropertyChangeListeners(
		ISafariPreferencesService service, String level, SafariBooleanFieldEditor field, String key, Composite composite)
	{	
		int levelIndex = service.getIndexForLevel(level);
		IEclipsePreferences[] nodes = service.getNodesForLevels();
		
		for (int i = levelIndex + 1; i < nodes.length; i++) {
			if (nodes[i] != null) {
				nodes[i].addPreferenceChangeListener(new SafariBooleanPreferenceChangeListener(field, key, composite));	
			} else {
				//System.err.println("JsdivConfigurationPreferencesPage.addPropetyChangeListeners(..):  no listener added at level = " + i + "; node at that level is null");
			}
		}		
	}

	
	
	public class SafariBooleanPreferenceChangeListener implements IEclipsePreferences.IPreferenceChangeListener
	{
		Composite composite = null;
		String key = null;
		SafariBooleanFieldEditor field = null;
		
		public SafariBooleanPreferenceChangeListener(SafariBooleanFieldEditor field, String key, Composite composite) {
			this.field = field;
			this.key = key;	
			this.composite = composite;
			if (!field.getChangeControl().getParent().equals(composite)) {
				System.err.println("SPU.SafariBooleanPreferenceChangeListener():  field and composite don't match!");
				System.err.println("\tfield = " + field.getLabelText());
			}
		}
		
		public void preferenceChange(PreferenceChangeEvent event)
		{
			if (event.getNewValue() == null && event.getOldValue() == null) return;
			if (event.getNewValue() != null && event.getNewValue().equals(event.getOldValue())) return;	

			String eventKey = event.getKey();
			if (!composite.isDisposed()) {
				if (eventKey.equals(key)) {
					//System.out.println("SafariBooleanPreferenceChangeListener.preferenceChange:  key = " + key +
					//	"level = " + field.getPreferencesLevel() + ":  " + event.getOldValue() + "/" + event.getNewValue());
					setField(field, field.getChangeControl().getParent());
				}
			}
		}
	}
	
	
	
	public void createToggleFieldListener(SafariBooleanFieldEditor booleanField, SafariStringFieldEditor stringField, boolean sense)
	{
		// Field-state listener should be sufficient since
		// changes to the control entail changes to the state
		//createFieldControlToggle(booleanField, stringField, sense);
		createFieldStateToggle(booleanField, stringField, sense);
	}
	
	

	public FieldControlToggleListener createFieldControlToggle(
			SafariBooleanFieldEditor booleanField, SafariStringFieldEditor stringField, boolean sense)
	{
		FieldControlToggleListener listener = new FieldControlToggleListener(booleanField, stringField, sense);
		booleanField.getChangeControl().addSelectionListener(listener);
		return listener;
	}

	
	public FieldStateToggleListener createFieldStateToggle(
			SafariBooleanFieldEditor booleanField, SafariStringFieldEditor stringField, boolean sense)
	{
		FieldStateToggleListener listener = new FieldStateToggleListener(booleanField, stringField, sense);
		booleanField.setPropertyChangeListener(listener);
		return listener;
	}

	
	public class FieldControlToggleListener implements SelectionListener
	{
		public SafariBooleanFieldEditor booleanField = null;
		public SafariStringFieldEditor stringField = null;
		boolean sense = true;

		public FieldControlToggleListener(
			SafariBooleanFieldEditor booleanField, SafariStringFieldEditor stringField, boolean sense)
		{
			this.booleanField = booleanField;
			this.stringField = stringField;
			this.sense = sense;
		}
		
		public void widgetSelected(SelectionEvent e) {
			boolean value = booleanField.getBooleanValue();
			value = sense? value : !value;
			stringField.getTextControl().setEditable(value);
			stringField.setEnabled(value, stringField.getParent());
		}

		public void widgetDefaultSelected(SelectionEvent e) {
			boolean value = booleanField.getBooleanValue();
			value = sense? value : !value;
			stringField.getTextControl().setEditable(value);
			stringField.setEnabled(value, stringField.getParent());
		}
	}

	
	
	public class FieldStateToggleListener implements IPropertyChangeListener
	{
		public SafariBooleanFieldEditor booleanField = null;
		public SafariStringFieldEditor stringField = null;
		boolean sense = true;

		public FieldStateToggleListener(
			SafariBooleanFieldEditor booleanField, SafariStringFieldEditor stringField, boolean sense)
		{
			this.booleanField = booleanField;
			this.stringField = stringField;
			this.sense = sense;
		}
		
	    public void propertyChange(PropertyChangeEvent event) {
			boolean value = ((Boolean)event.getNewValue()).booleanValue();
			value = sense? value : !value;
			stringField.getTextControl().setEditable(value);
			stringField.setEnabled(value, stringField.getParent());
	    }
	}
	
	
	
	
	public Link createDetailsLink(Composite detailsHolder, final SafariStringFieldEditor field, final Composite fieldHolder, String text)
	{
		Link link = new Link(detailsHolder, SWT.NONE);
		link.setFont(detailsHolder.getFont());
		// Blanks added ahead of text to better align vertically links
		// for text fields with links for boolean fields.  This is a
		// kludge and should be done some better way, but right now
		// it's not worth the effort to figure that out.
		link.setText("  <A>" + text + "</A>");
		
		final class DetailsLinkListener implements SelectionListener {
			DetailsLinkListener(Composite fieldHolder) {
			}
			public void widgetSelected(SelectionEvent e) {
				doDetailsLinkActivated((Link) e.widget, field, fieldHolder);
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				doDetailsLinkActivated((Link) e.widget, field, fieldHolder);
			}
		}
		DetailsLinkListener detailsLinkListener = new DetailsLinkListener(detailsHolder);
		
		link.addSelectionListener(detailsLinkListener);
	
		return link;	
	}
	

	
	
	public Link createDetailsLinkDefault(Composite detailsHolder, final SafariBooleanFieldEditor field, final Composite fieldHolder, String text)
	{
		Link link = new Link(detailsHolder, SWT.NONE);
		link.setFont(detailsHolder.getFont());
		link.setText("<A>" + text + "</A>");
		
		final class DetailsLinkListener implements SelectionListener {
			DetailsLinkListener(Composite fieldHolder) {
			}
			public void widgetSelected(SelectionEvent e) {
				doDetailsLinkActivated((Link) e.widget, field, fieldHolder);
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				doDetailsLinkActivated((Link) e.widget, field, fieldHolder);
			}
		}
		DetailsLinkListener detailsLinkListener = new DetailsLinkListener(detailsHolder);
		
		link.addSelectionListener(detailsLinkListener);
	
		return link;	
	}

	
	public Link createDetailsLink(Composite /*detailsHolder*/ parent, final SafariBooleanFieldEditor field, final Composite fieldHolder, String text)
	{
		Composite detailsHolder = new Composite(parent, SWT.EMBEDDED);
	    GridLayout gl = new GridLayout();
	    detailsHolder.setLayout(gl);
	    
		Link link = new Link(detailsHolder, SWT.NONE);
		link.setFont(detailsHolder.getFont());
		link.setText("<A>" + text + "</A>");
		
		final class DetailsLinkListener implements SelectionListener {
			//DetailsLinkListener(Composite fieldHolder) {
			//}
			public void widgetSelected(SelectionEvent e) {
				doDetailsLinkActivated((Link) e.widget, field, fieldHolder);
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				doDetailsLinkActivated((Link) e.widget, field, fieldHolder);
			}
		}
		DetailsLinkListener detailsLinkListener = new DetailsLinkListener(/*detailsHolder*/);
		
		link.addSelectionListener(detailsLinkListener);
	
		return link;	
	}

	
	
	void doDetailsLinkActivated(Link link, SafariStringFieldEditor field, Composite fieldHolder) {
		DetailsDialogForStringFields dialog = new DetailsDialogForStringFields(fieldHolder.getShell(), field, fieldHolder, service);
		dialog.open();
	}
	
	
	final void doDetailsLinkActivated(Link link, SafariBooleanFieldEditor field, Composite fieldHolder) {
		DetailsDialogForBooleanFields dialog = new DetailsDialogForBooleanFields(fieldHolder.getShell(), field, fieldHolder, service);
		dialog.open();
	}
	
	
	
	/*
	 * Elements relating to project selection
	 * (As of 13 Sep 2006 these are only needed in the project preferences tab,
	 * so I have not made versions of them publically available in the utilties
	 * class.  They might be represented here if and when there is a need for
	 * creating project selection/deselection links in other places.)
	 */
	
	
	/**
	 * 
	 * @param	composite	the wiget that holds the link
	 * @param	fieldParent	the wiget that holds the field that will be
	 * 						set when the link is selected (needed for
	 * 						posting a listener)
	 * @param	text		text that appears in the link
	 */
	/*
	public Link createMakeProjectSelectionLink(
			Composite composite, final Composite fieldParent, String text,
			ProjectSelectionLinkResponder responder)
	{
		final Link link= new Link(composite, SWT.NONE);
		link.setFont(composite.getFont());
		link.setText("<A>" + text + "</A>");
		
		final class CompositeLinkSelectionListener implements SelectionListener {
			ProjectSelectionLinkResponder responder = null;
			CompositeLinkSelectionListener(ProjectSelectionLinkResponder responder) {
				this.responder = responder;
			}
			
			public void widgetSelected(SelectionEvent e) {
				//doMakeProjectSelectionLinkActivated((Link) e.widget, fieldParent);
				responder.doProjectSelectionActivated((Link) e.widget, fieldParent);
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				//doMakeProjectSelectionLinkActivated((Link) e.widget, fieldParent);
				responder.doProjectSelectionActivated((Link) e.widget, fieldParent);
			}
		}
		
		
		CompositeLinkSelectionListener linkSelectionListener =
			new CompositeLinkSelectionListener(responder);
		
		link.addSelectionListener(linkSelectionListener);
		return link;
	}
	
	
	public interface ProjectSelectionLinkResponder {
		public abstract void doProjectSelectionActivated(Link link, Composite parent);
	}
	*/
	
	
	
	public Control[] createDefaultAndApplyButtons(Composite parent, final SafariPreferencesTab buttonHolder)
	{
	       
        Composite buttonBar = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 0;
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.makeColumnsEqualWidth = false;
        buttonBar.setLayout(layout);
        
        GridData bbgd = new GridData(SWT.END);
        buttonBar.setLayoutData(bbgd);

        //contributeButtons(buttonBar);
        boolean createDefaultAndApplyButton = true;
        if (createDefaultAndApplyButton) {
            layout.numColumns = layout.numColumns + 2;
			String[] labels = JFaceResources.getStrings(new String[] {
					"defaults", "apply" }); //$NON-NLS-2$//$NON-NLS-1$
			//int widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
			Button defaultsButton = new Button(buttonBar, SWT.PUSH);
			defaultsButton.setText(labels[0]);
			Dialog.applyDialogFont(defaultsButton);
			//GridData 
			GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
			Point minButtonSize = defaultsButton.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
			//data.widthHint = Math.max(widthHint, minButtonSize.x);
			defaultsButton.setLayoutData(data);
			defaultsButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
						buttonHolder.performDefaults();
				}
			});

            Button applyButton = new Button(buttonBar, SWT.PUSH);
			applyButton.setText(labels[1]);
			Dialog.applyDialogFont(applyButton);
			data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
			minButtonSize = applyButton.computeSize(SWT.DEFAULT, SWT.DEFAULT,
					true);
			//data.widthHint = Math.max(widthHint, minButtonSize.x);
			applyButton.setLayoutData(data);
			applyButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					buttonHolder.performApply();
				}
			});
            applyButton.setEnabled(true);		//isValid());
            Dialog.applyDialogFont(buttonBar);
        } else {
            /* Check if there are any other buttons on the button bar.
             * If not, throw away the button bar composite.  Otherwise
             * there is an unusually large button bar.
             */
            if (buttonBar.getChildren().length < 1)
                buttonBar.dispose();
        }
        
        return buttonBar.getChildren();
		
	}
	
	
	
	/*
	 * For laying out grid data in 	wigets for preferences pages (or anythink else)
	 */
	
	
	public static void fillGridPlace(Composite composite, int num) {
		int count = num < 0 ? 0 : num;
		for (int i = 1; i <= num; i++) {
			Label label = new Label(composite, SWT.NONE);
			label.setText("This space intentionally left blank");
			label.setVisible(false);
		}
	}

	
}
