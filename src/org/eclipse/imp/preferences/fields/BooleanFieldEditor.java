package org.eclipse.uide.preferences.fields;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.uide.preferences.ISafariPreferencesService;
import org.osgi.service.prefs.BackingStoreException;


public class SafariBooleanFieldEditor extends BooleanFieldEditor
{
	protected ISafariPreferencesService preferencesService = null;
	protected String preferencesLevel = null;
	protected Composite parent = null;
	protected boolean wasSelected;			// formerly previousValue
    private boolean fieldModified = false;	// formerly textModified
    private boolean nodeModified = false;

    
	public Color colorWhite = new Color(null, 255, 255, 255);
	public Color colorBluish = new Color(null, 175, 207, 239);
	public Color colorGreenish = new Color(null, 0, 127, 239);
	public Color colorLightGray = new Color(null, 224, 223, 226);

	protected SafariBooleanFieldEditor(ISafariPreferencesService service, String level) {
		super();
    	preferencesService = service;
    	preferencesLevel = level;
	}
	
	
	/**
     * Creates a boolean field editor in the given style.
     * 
     * @param name the name of the preference this field editor works on
     * @param labelText the label text of the field editor
     * @param style the style, either <code>DEFAULT</code> or
     *   <code>SEPARATE_LABEL</code>
     * @param parent the parent of the field editor's control
     * @see #DEFAULT
     * @see #SEPARATE_LABEL
     */
    public SafariBooleanFieldEditor(
    		ISafariPreferencesService service, String level, String name, String labelText, int style, final Composite parent)
    {
    	super(name, labelText, style, parent);
    	preferencesService = service;
    	preferencesLevel = level;
    	this.parent = parent;
    }
	
    
    /**
     * Creates a boolean field editor.
     * 
     * @param name the name of the preference this field editor works on
     * @param labelText the label text of the field editor
     * @param parent the parent of the field editor's control
     */
    public SafariBooleanFieldEditor(
   		ISafariPreferencesService service, String level, String name, String labelText, Composite parent)
    {
        super(name, labelText, parent);
    	preferencesService = service;
    	preferencesLevel = level;
    	this.parent = parent;
    }

    
    /**
     * Checks whether the text input field contains a valid value or not.
     *
     * @return <code>true</code> if the field value is valid,
     *   and <code>false</code> if invalid
     *   
     *   SMS 24 Aug 2006:  Copied from StringFieldEditor and revised here
     *   because of potential for NPE in original
     */
/*    
    protected boolean checkState() {
        boolean result = false;
        if (isEmptyStringAllowed())
            result = true;

        if (getTextControl() == null)
            result = false;

        if (getTextControl() != null) {		// SMS:  Added this check
        	// TODO:  Figure out why could textControl be null in the superclass?
        	String txt = getTextControl().getText();
        	result = (txt.trim().length() > 0) || isEmptyStringAllowed();
        }
        
        // call hook for subclasses
        result = result && doCheckState();

        if (result)
            clearErrorMessage();
        else
            showErrorMessage(getErrorMessage());

        return result;
    }
*/    
    
    private boolean isInherited = false;
    
    public boolean isInherited() { return isInherited; }
    
    protected void setInherited(boolean inherited) { isInherited = inherited; }
    
    
	
	protected boolean specialValue = false;
	protected boolean hasSpecialValue = false;
	
	public boolean hasSpecialValue() { return hasSpecialValue; }
	
	public boolean getSpecialValue() { 
		if (hasSpecialValue) return specialValue;
		throw new IllegalStateException("SafariStringField.getSpecialValue:  called when field does not have a special value");
	}
	
	public void setNoSpecialValue() {
		hasSpecialValue = false;
		specialValue = false;
	}
    
	public void setSpecialValue(boolean specialValue) {
		hasSpecialValue = true;
		this.specialValue = specialValue;
	}

	
	protected final boolean emptyValue = false;

	public boolean isEmptyValueAllowed() {
		return false;
	}
	
	
	public void setEmptyValueAllowed(boolean allowed) {
		if (allowed) {
			throw new IllegalArgumentException("SafairBooleanFieldEditor.setEmptyValue:  attempt to allow emtpy values when those are prohibited by type");
		}
	}
	
	public boolean getEmptyValue() {
		if (isEmptyValueAllowed())
			return emptyValue;
		throw new IllegalStateException("SafairBooleanFieldEditor.getEmptyValue:  called when field does not allow an empty value");
	}
	
	
	protected boolean isRemovable = false;
	
	public boolean isRemovable() { return isRemovable; }
	
	public void setRemovable(boolean isRemovable) {
		this.isRemovable = isRemovable;
	}
	
    
    /**
     * Initializes this field editor with the preference value from
     * the preference service.
     */
    public void load() {
        if (preferencesService != null) {
            //isDefaultPresented = false;
        	doLoad();
            refreshValidState();
        }
    }


	/* (non-Javadoc)
     * Method declared on FieldEditor.
     */
    protected void doLoad()
    {
        if (super.getChangeControl(parent) != null) {
        	boolean value;
        	if (preferencesLevel != null) {
        		// The "normal" case, in which field corresponds to a preferences level
        		value = preferencesService.getBooleanPreference(preferencesLevel, getPreferenceName());
        		levelFromWhichLoaded = preferencesLevel;
        		setInherited(false);
        	}
        	else {
        		// Not normal, exactly, but possible if loading is being done into a
        		// field that is not associated with a specific level
        		value = preferencesService.getBooleanPreference(getPreferenceName());
        		levelFromWhichLoaded = preferencesService.getApplicableLevel(getPreferenceName(), preferencesLevel);
    			setInherited(true);
        	}
            if (preferencesService.isDefault(getPreferenceName(), preferencesLevel))
            	// wasSelected = value;
            	setPresentsDefaultValue(true);
        	valueChanged(wasSelected, value);
        	//super.getChangeControl(parent).setSelection(value);
        	setBooleanValue(value);

        }
    }

    
    /**
     * Initializes this field editor with the default preference value
     * from the preference store.
     */
    public void loadDefault() {
        if (preferencesService != null) {
        	setPresentsDefaultValue(true);
            doLoadDefault();
            //refreshValidState();
        }
    }

 
    /* (non-Javadoc)
     * Method declared on FieldEditor.
     */
    protected void doLoadDefault() {
        if (super.getChangeControl(parent) != null) {
            boolean value = preferencesService.getBooleanPreference(ISafariPreferencesService.DEFAULT_LEVEL, getPreferenceName());
        	setBooleanValue(value);	// calls valueChanged(..), which resets wasSelected as appropriate
        	setPresentsDefaultValue(true);
        	// Not setting isInherited here because the value was set
        	// directly rather than through the inheritance process.
        	// Will need to keep track of presentsDefaultValue to know
        	// whether values not inherited are local to their field.
        }
     }

    
    
    // SMS 22 Aug 2006
    // I made up the following pair of operations "by level"
    
    /**
     * Initializes this field editor with the default preference value
     * from the preference store.
     */
    public void loadLevel(String level) {
        if (preferencesService != null &&
        	preferencesService.isaPreferencesLevel(level))
        {
        	if (level == ISafariPreferencesService.DEFAULT_LEVEL)
        		setPresentsDefaultValue(true);
        	doLoadLevel(level);
            //refreshValidState();
        }
    }


    /* (non-Javadoc)
     * 
     */
    protected void doLoadLevel(String level) {
        if (super.getChangeControl(parent) != null) {
        	boolean value;
        	if (preferencesLevel != null) {
        		value = preferencesService.getBooleanPreference(level, getPreferenceName());
        	} else {
        		// TODO:  Check whether this is the right thing to do
        		value = preferencesService.getBooleanPreference(getPreferenceName());
        	}
        	//super.getChangeControl(parent).setSelection(value);
        	setBooleanValue(value);
        	//wasSelected = value;
        	valueChanged(wasSelected, value);
        }
    }


    protected String levelFromWhichLoaded = null;
    
    public String getLevelFromWhichLoaded() {
    	return levelFromWhichLoaded;
    }
    
 
    public String loadWithInheritance() {
    	if (preferencesService != null && !parent.isDisposed()) {
        	levelFromWhichLoaded = doLoadWithInheritance();
            if (preferencesService.isDefault(getPreferenceName(), preferencesLevel))
            	setPresentsDefaultValue(true);
            refreshValidState();
        }
        return levelFromWhichLoaded;
    }


	/*
     * Load into the boolean field the value for this preference that is either
     * the value defined on this preferences level, if any, or the value inherited
     * from the next applicable level, if any.  Return the level at which the
     * value loaded was found.  Load nothing and return null if no value is found.
     */
    protected String doLoadWithInheritance()
    {
    	String levelLoaded = null;
    	
    	String[] levels = ISafariPreferencesService.levels;
    	int fieldLevelIndex = preferencesService.getIndexForLevel(preferencesLevel);
    		
    	// If we're loading with inheritance for some field that is
    	// not attached to a preferences level (such as the "applicable"
    	// field, which inherits values from all of the real fields)
    	// then assume that we should just search from the bottom up
    	String tmpPreferencesLevel = (preferencesLevel == null)?
    			levels[0] :
    			preferencesLevel;
     	
    	boolean value = false;
    	int levelAtWhichFound = -1;
    	
    	IEclipsePreferences[] nodes = preferencesService.getNodesForLevels();
    	
    	for (int level = fieldLevelIndex; level < levels.length; level++) {
    		// Must have a node from which to get a value
    		if (nodes[level] == null) continue;
    		
   			// Get the value from the node, not the service, because we can
    		// check the node to see whether there is a value there for this
    		// preference
    		String result = nodes[level].get(getPreferenceName(), null);
    		if (result != null) {
    			// We have a value at the node; get it as a boolean
    			// (presumably we could also convert the result to a boolean)
    			value = nodes[level].getBoolean(getPreferenceName(), false);
    			
    			levelAtWhichFound = level;
    			levelLoaded = levels[levelAtWhichFound];
    			break;
    		}
    	}
    	
    	// Ok, now have all necessary information to set everyting that
    	// needs to be set
    	setBooleanValue(value);	// calls valueChanged(..), which sets modified flag and 
    	setInherited(fieldLevelIndex != levelAtWhichFound);
    	if (!isInherited())
    		getChangeControl().setBackground(colorLightGray);
    	else
    		getChangeControl().setBackground(colorBluish);	
    	setPresentsDefaultValue(levelAtWhichFound == ISafariPreferencesService.DEFAULT_INDEX);
    	fieldModified = true;

    	// Loading the field will (or at least may) change the field value
    	//System.out.println("SBFE.doLoadWithInheritance:  setting fieldModified to TRUE");
        //System.out.println("doLoadWithInheritance:  preferencesName = " + getPreferenceName() + "; preferenceLevel = " + preferencesLevel + "; levelLoaded = " + levelLoaded);
        
        return levelLoaded;
    }

    
    
    /**
     * Stores this field editor's value back into the preference store.
     */
    public void store() {
    	// Don't store if the preferences service is null, since that may
    	// represent an illegal state and anyway we need to refer to it below
    	if (preferencesService == null) {
    		throw new IllegalStateException("SafairBooleanFieldEditor.setPreferenceLevel:  attempt to store when preferences service is null");
    	}
    	
    	// Can't store the value If there is no valid level (this isn't
    	// necessarily an error, but it does prevent storing)
        if (preferencesLevel == null) return;

        // Don't store a value that comes from some other level
        // Note:  presentsDefaultValue is true if the value was set direclty from
        // the default level, which isn't inheritance but which still means that
        // the value isn't associated with the local preferences node (unless
        // the local node is the default node, but then we don't store default
        // preferences in any case)
        if (isInherited) return;
        if (presentsDefaultValue()) return;
        
        // Don't bother storing if the field hasn't been modified
        if (!fieldModified) return;
        
        // Don't store the value if the field's level is the project level
        // but no project is selected
        if (ISafariPreferencesService.PROJECT_LEVEL.equals(preferencesLevel) &&
        		preferencesService.getProject() == null) return;
        
        // If the level is the default level, go ahead and store it even
        // though preferences on the default level aren't persistent:
        // the preference still needs to be stored into the default preference
        // node, and the flushing of that node doesn't have any effect.
        // In other words, do not return if the level is the default level

        // Store the value
        doStore();
        

    }
    

    
    protected void doStore()
    {				
    	// All these conditions now addressed in store()
    	/*
    	if (isInherited() || !fieldModified) {
    		if (isInherited() && !fieldModified) return;	// nothing changed, nothing to store
    		if (isInherited() && fieldModified) return;		// something changed, but not to be stored here
    		if (!isInherited && !fieldModified) return;		// nothing changed, already stored
    		return;
    	}
    	*/
    	
    	boolean	value = getBooleanValue();
    	
    	// Not inherited, and modified:  field must have been set on this level, so store it.
    	// Storing here should trigger preference-change listeners at each level below this.
   		preferencesService.setBooleanPreference(preferencesLevel, getPreferenceName(), value);
   		
    	// Now write out the node
    	IEclipsePreferences node = preferencesService.getNodeForLevel(preferencesLevel);

        // If we've just stored the field, we've addressed any modifications
   		//System.out.println("SBFE.doStore:  setting fieldModified to FALSE");
   		fieldModified = false;
   		// If we've stored the field then it's not inherited, so be sure it's
   		// color indicates that.
   		// Note that for the checkbox wiget (which is the only one used so far)
   		// the background color is the color behind the label (not the checkbox
   		// itself), so it should be light gray like the background in the rest
   		// of the tab.
   		// TODO:  figure out how to determine the actual prevailing background
   		// color and use that here
   		getChangeControl().setBackground(colorLightGray);
    	
    	try {
    		if (node != null) node.flush();
    	} catch (BackingStoreException e) {
    		System.err.println("SBFE.doStore():  BackingStoreException flushing node;  node may not have been flushed:" + 
    				"\n\tnode path = " + node.absolutePath() + ", preferences level = "  + preferencesLevel);
    	}
    }
    
    
    /*
     * Preferences are stored by level, so we need to provide some
     * way to represent and set the applicable level.  Note that
     * preferences can be reset at the default level during exeuction
     * but default level preferences are never stored between
     * executions. 
     */
    	
 

    	
    public	void setPreferencesLevel(String level) {
    	if (!preferencesService.isaPreferencesLevel(level)) {
    		throw new IllegalArgumentException("SafairBooleanFieldEditor.setPreferencesLevel:  given level = " + level + " is invalid");
    	}
    	if (level.equals(preferencesService.PROJECT_LEVEL) && preferencesService.getProject() == null) {
    		throw new IllegalStateException("SafairBooleanFieldEditor.setPreferenceLevel:  given level is '" + preferencesService.PROJECT_LEVEL +
    				"' but project is not defined for preferences service");
    	}
    	preferencesLevel = level;
    }
    
    
    public String getPreferencesLevel() {
    	return preferencesLevel;	
    }
    
    
    
    /**
     * Returns the field editor's value.
     *
     * @return the current value
     */
    public boolean getBooleanValue() {
            //return super.getChangeControl(parent).getSelection();
    	return getChangeControl(parent).getSelection();
    }

    
    /**
     * Sets this field editor's value through the supertype.
     * Sets local flags for fieldModified (true) and isInherited (false).
     * (We need a local version of fieldModified since that information
     * isn't available through the supertype.  We need a local version
     * of isInherited because the supertype doesn't address inheritance.)
     * Does not set presentsDefault since that isn't known here (would
     * presumably be known to the caller).
     * Calls valueChanged(..), so callers don't have to do that.
     * valueChanged(..) also sets the previous value, wasSelected.
     *
     * @param value the new value
     */
    public void setBooleanValue(boolean value) {
    	Button button = getChangeControl(parent);
        if (button != null && !button.isDisposed()) {
            	button.setSelection(value);
                valueChanged(wasSelected, value);
                fieldModified = true;
                setInherited(false);
                levelFromWhichLoaded = preferencesLevel;
        } else if (button.isDisposed()){
        	throw new IllegalStateException("SafariBooleanFieldEditor.setBooleanValue:  button is disposed");
        } else if (button == null) {
        	throw new IllegalStateException("SafariBooleanFieldEditor.setBooleanValue:  button is null");
        }
    }


    private boolean checkBoxNull = true;
    
    /*
     * This overrides the corresopnding superclass method so that we can set
     * a listener on the control for our purposes.
     * 
     */
    public Button getChangeControl() {
    	if (!parent.isDisposed()) {
    		if (checkBoxNull) {
	        	// Should actually create checkbox if it doesn't exist
	        	// so should really never be null
	        	Button checkBox = super.getChangeControl(parent);
	        	checkBoxNull = checkBox == null;
	        	if (!checkBoxNull) {
		            checkBox.addSelectionListener(new SelectionAdapter() {
		                public void widgetSelected(SelectionEvent e) {
		                   // boolean isSelected = checkBox.getSelection();
		                   // valueChanged(wasSelected, isSelected);
		                   // wasSelected = isSelected;
		                	
		                    boolean isSelected = getChangeControl(parent).getSelection();
		                    valueChanged(wasSelected, isSelected);
		                    //wasSelected = isSelected;	// done in valueChanged(..)
		                    // Changing the button will change the field
		                    //System.out.println("SBFE.button selection listener (from getChangeControl):  fieldModified set to TRUE");
		                    setBooleanValue(isSelected);	// right to set field?
							fieldModified = true;
							setInherited(false);
		                }
		            });
		            checkBox.addDisposeListener(new DisposeListener() {
		                public void widgetDisposed(DisposeEvent event) {
		                    //System.out.println("SBFE.button dispose listener (from getChangeControl):  checkBoxNull set to true");
		                    checkBoxNull = true;
		                }
		            });
	        	}
	        }
	    	return super.getChangeControl(parent);
        }
        return null;
    }
    
    
    public Button getChangeControl(Composite parent) {
    	return super.getChangeControl(parent);
    }
    
    
    /**
     * Informs this field editor's listener, if it has one, about a change
     * to the value (<code>VALUE</code> property) provided that the old and
     * new values are different.
     * <p>
     * This hook is <em>not</em> called when the text is initialized 
     * (or reset to the default value) from the preference store.
     * </p>
     */
    // Why not set fieldChanged, too?
    protected void valueChanged(boolean oldValue, boolean newValue) {

    	setInherited(false);
    	getChangeControl().setBackground(colorWhite);
    	
        setPresentsDefaultValue(false);
        if (oldValue != newValue) {
            //fireStateChanged(VALUE, oldValue, newValue);
            wasSelected = newValue;
        }
        super.valueChanged(oldValue,newValue);

    }

}
