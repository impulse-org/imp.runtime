package org.eclipse.uide.preferences.fields;

import java.io.File;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.uide.preferences.ISafariPreferencesService;
import org.eclipse.uide.preferences.SafariPreferencesTab;
import org.osgi.service.prefs.BackingStoreException;


public class SafariStringFieldEditor extends StringFieldEditor
{
	PreferencePage prefPage = null;
	SafariPreferencesTab prefTab = null;
	
	protected ISafariPreferencesService preferencesService = null;
	
	protected String previousValue = null;

	protected Composite parent = null;

    
	public Color colorWhite = new Color(null, 255, 255, 255);
	public Color colorBluish = new Color(null, 175, 207, 239);
	public Color colorGreenish = new Color(null, 0, 127, 239);
	public Color colorLightGray = new Color(null, 224, 223, 226);
	
    /**
     * Creates a string field editor.
     * Use the method <code>setTextLimit</code> to limit the text.
     * 
     * @param name the name of the preference this field editor works on
     * @param labelText the label text of the field editor
     * @param width the width of the text input field in characters,
     *  or <code>UNLIMITED</code> for no limit
     * @param strategy either <code>VALIDATE_ON_KEY_STROKE</code> to perform
     *  on the fly checking (the default), or <code>VALIDATE_ON_FOCUS_LOST</code> to
     *  perform validation only after the text has been typed in
     * @param parent the parent of the field editor's control
     * @since 2.0
     */
    public SafariStringFieldEditor(
			PreferencePage page, SafariPreferencesTab tab,
    		ISafariPreferencesService service, String level, String name, String labelText,
    		int width, int strategy, Composite parent)
    {
    	super(name, labelText, width, strategy, parent);
    	preferencesService = service;
    	preferencesLevel = level;
    	this.parent = parent;
    	prefPage = page;
    	setPage(prefPage);
    	prefTab = tab;
    }
	
	
    /**
     * Creates a string field editor.
     * Use the method <code>setTextLimit</code> to limit the text.
     * 
     * @param name the name of the preference this field editor works on
     * @param labelText the label text of the field editor
     * @param width the width of the text input field in characters,
     *  or <code>UNLIMITED</code> for no limit
     * @param parent the parent of the field editor's control
     */
    public SafariStringFieldEditor(
			PreferencePage page, SafariPreferencesTab tab,
    		ISafariPreferencesService service, String level, String name, String labelText,
    		int width, Composite parent)
    {
        super(name, labelText, width, VALIDATE_ON_KEY_STROKE, parent);
    	preferencesService = service;
    	preferencesLevel = level;
    	this.parent = parent;
    	prefPage = page;
    	setPage(prefPage);
    	prefTab = tab;
    }
    
    /**
     * Creates a string field editor of unlimited width.
     * Use the method <code>setTextLimit</code> to limit the text.
     * 
     * @param name the name of the preference this field editor works on
     * @param labelText the label text of the field editor
     * @param parent the parent of the field editor's control
     */
    public SafariStringFieldEditor(
			PreferencePage page, SafariPreferencesTab tab,
			ISafariPreferencesService service, String level, String name, String labelText, Composite parent)
    {
        super(name, labelText, UNLIMITED, parent);
    	preferencesService = service;
    	preferencesLevel = level;
    	this.parent = parent;
    	prefPage = page;
    	setPage(prefPage);
    	prefTab = tab;
    }

   
    private boolean isInherited = false;
    
    public boolean isInherited() { return isInherited; }
    
    protected void setInherited(boolean inherited) { isInherited = inherited; }
    
    
	
	protected String specialValue = null;
	protected boolean hasSpecialValue = false;
	
	public boolean hasSpecialValue() { return hasSpecialValue; }
	
	public String getSpecialValue() { 
		if (hasSpecialValue) return specialValue;
		throw new IllegalStateException("SafariStringField.getSpecialValue:  called when field does not have a special value");
	}
	
	public void setNoSpecialValue() {
		hasSpecialValue = false;
		specialValue = null;
	}
    
	public void setSpecialValue(String specialValue) {
		hasSpecialValue = true;
		this.specialValue = specialValue;
	}

	
	protected final String emptyValue = "";
	
	
	public boolean isEmptyValueAllowed() {
		return isEmptyStringAllowed();
	}
	
	public void setEmptyValueAllowed(boolean allowed) {
		setEmptyStringAllowed(allowed);
	}
	

	public String getEmptyValue() {
		if (isEmptyStringAllowed())
			return emptyValue;
		throw new IllegalStateException("SafariStringFieldEditor.getEmptyValue:  called when field does not allow an empty value");
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
        if (getTextControl() != null) {
        	String value = null;
        	if (preferencesLevel != null) {
        		// The "normal" case, in which field corresponds to a preferences level
        		value = preferencesService.getStringPreference(preferencesLevel, getPreferenceName());
        		levelFromWhichLoaded = preferencesLevel;
        		setInherited(false);
        	}
        	else {
        		// Not normal, exactly, but possible if loading is being done into a
        		// field that is not associated with a specific level
        		value = preferencesService.getStringPreference(getPreferenceName());
        		levelFromWhichLoaded = preferencesService.getApplicableLevel(getPreferenceName(), preferencesLevel);
    			setInherited(true);	
        	}
            if (ISafariPreferencesService.DEFAULT_LEVEL.equals(levelFromWhichLoaded))
            	setPresentsDefaultValue(true);
        	previousValue = value;
            setStringValue(value);
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
            refreshValidState();
        }
    }

 
    /* (non-Javadoc)
     * Method declared on FieldEditor.
     */
    protected void doLoadDefault() {
        if (getTextControl() != null) {
            String value = preferencesService.getStringPreference(ISafariPreferencesService.DEFAULT_LEVEL,	getPreferenceName());
            setStringValue(value);
        }
        // Comments on valueChanged() says it is not called when 
        // a value is initialized or restored from default
        //valueChanged();
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
        	if (ISafariPreferencesService.DEFAULT_LEVEL.equals(level))
        		setPresentsDefaultValue(true);
        	doLoadLevel(level);
            refreshValidState();
        }
    }


    /* (non-Javadoc)
     * 
     */
    protected void doLoadLevel(String level) {
        if (getTextControl() != null) {
        	String value = null;
        	if (preferencesLevel != null) {
        		value = preferencesService.getStringPreference(level, getPreferenceName());
        	} else {
        		// TODO:  Check whether this is the right thing to do
        		value = preferencesService.getStringPreference(getPreferenceName());
        	}
        	setStringValue(value);
        }
        //valueChanged();
    }


    protected String levelFromWhichLoaded = null;
    
    public String getLevelFromWhichLoaded() {
    	return levelFromWhichLoaded;
    }
    
 
    public String loadWithInheritance() {
        if (preferencesService != null) {
        	levelFromWhichLoaded = doLoadWithInheritance();
            //if (preferencesService.isDefault(getPreferenceName()))
        	if (ISafariPreferencesService.DEFAULT_LEVEL.equals(levelFromWhichLoaded))
            	setPresentsDefaultValue(true);
            refreshValidState();
        }
        return levelFromWhichLoaded;
    }


	/*
     * Load into the text field the value for this preference that is either
     * the value defined on this preferences level, if any, or the value inherited
     * from the next applicable level, if any.  Return the level at which the
     * value loaded was found.  Load nothing and return null if no value is found.
     */
    protected String doLoadWithInheritance()
    {
    	String levelLoaded = null;
    	
    	String[] levels = ISafariPreferencesService.levels;
    	int fieldLevelIndex = 0;

    	// If we're loading with inheritance for some field that is
    	// not attached to a preferences level (such as the "applicable"
    	// field, which inherits values from all of the real fields)
    	// then assume that we should just search from the bottom up
    	String tmpPreferencesLevel = 
    		(preferencesLevel == null)?
    			levels[0]:
    			preferencesLevel;
    	
    	// Find the index of the level to which this field belongs
    	for (int i = 0; i < levels.length; i++) {
    		if (tmpPreferencesLevel.equals(levels[i])) {
    			fieldLevelIndex = i;
    			break;
    		}
    	}
    	
    	String value = null;
    	int levelAtWhichFound = -1;
    	
    	for (int level = fieldLevelIndex; level < levels.length; level++) {
       		value = preferencesService.getStringPreference(levels[level], getPreferenceName());
       		if (value == null) continue;
       		if (value.equals("") && !isEmptyStringAllowed()) continue;
       		levelAtWhichFound = level;
       		levelLoaded = levels[levelAtWhichFound];
       		break;	
    	}
    	
    	// Set the field to the value we found
        setStringValue(value);
        
    	// We loaded it at this level or inherited it from some other level
    	setInherited(fieldLevelIndex != levelAtWhichFound);
    	
    	// Since we just loaded some new text, it won't be modified yet
    	textModified = false;
    	
    	// TODO:  Check on use of previous value
       	previousValue = value;
       	
       	// Set the background color of the field according to where found
        Text text = getTextControl();
        if (isInherited())
        	text.setBackground(colorBluish);
        else
        	text.setBackground(colorWhite);
  	
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
        if (!textModified) return;
        
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
        
        // If we've just stored the field, we've addressed any modifications
   		//System.out.println("STFE.store:  setting fieldModified to FALSE");
   		textModified = false;
   		levelFromWhichLoaded = preferencesLevel;
   		// If we've stored the field then it's not inherited, so be sure it's
   		// color indicates that.
   		// Note that for the checkbox wiget (which is the only one used so far)
   		// the background color is the color behind the label (not the checkbox
   		// itself), so it should be light gray like the background in the rest
   		// of the tab.
   		// TODO:  figure out how to determine the actual prevailing background
   		// color and use that here
   		getTextControl().setBackground(colorWhite);
    	
    }
    
    private boolean textModified = false;
    
    protected void doStore() {
    	String 	value = getTextControl().getText();
    	boolean isEmpty = value.equals(emptyValue);			// Want empty value, but can't call method to retrieve it
    														// with fields where empty is not allowed
    	// getText() will return an empty string if the field is empty,
    	// and empty strings can be stored in the preferences service,
    	// but an empty string is recognized by the preferences service
    	// as a valid value--when usually it is not.  Once it is recognized
    	// as a valid value, it precludes the searching of subsequent
    	// levels that might contain a non-empty (and actually valid) value.
    	// We would like to be able to store a null value with the preferences
    	// service so as to not short-circuit the search process, but we can't	
    	// do that.  So, if the field value is empty, we have to eliminate the
    	// preference entirely.  (Will that work in general???)
    	if (isEmpty && !isEmptyStringAllowed()) {
    		// We have an empty value where that isn't allowed, so clear the
    		// preference.  Expect that clearing the preferences at a level will
    		// trigger a loading with inheritance at that level
    		preferencesService.clearPreferenceAtLevel(preferencesLevel, getPreferenceName());
    		// If the preference value was previously empty (e.g., if previously inherited)
    		// then clearing the preference node now doesn't cause a change event, so
    		// doesn't trigger reloading with inheritance.  So we should just load the
    		// field again to make sure any inheritance occurs if needed
    		loadWithInheritance();
    		return;
    	}
    	if (isInherited() && !textModified) {				// If inherited, why do we care whether it's modified?
    		// We have a value	 but it's inherited			// shouldn't want to store in any case, should we?
    		// (left over from after the last time we cleared the field)
    		// so don't need (or want) to store it	
    		return;
    	}
    	// We have a value (possibly empty, if that is allowed) that has changed
    	// from the previous value, so store it
    	preferencesService.setStringPreference(preferencesLevel, getPreferenceName(), value);

        // If we've just stored the field, we've addressed any modifications
   		//System.out.println("STFE.doStore:  setting fieldModified to FALSE");
   		textModified = false;
   		// "Level from which loaded" (or set, as the case may be) is now this level
   		levelFromWhichLoaded = preferencesLevel;
   		// If we've stored the field then it's not inherited, so be sure it's
   		// color indicates that.
   		// For text fields, the background color is the backgroud color within
   		// the field, so don't have to worry about matching anything else
   		getTextControl().setBackground(colorWhite);
    	
    	IEclipsePreferences node = preferencesService.getNodeForLevel(preferencesLevel);
    	try {
    		if (node != null) node.flush();
    	} catch (BackingStoreException e) {
    		System.err.println("SafariStringFieldEditor.	():  BackingStoreException flushing node;  node may not have been flushed:" + 
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
    	
    String preferencesLevel = null;

    	
    public	void setPreferencesLevel(String level) {
    	if (!preferencesService.isaPreferencesLevel(level)) {
    		throw new IllegalArgumentException("SafariStringFieldEditor.setPreferencesLevel:  given level = " + level + " is invalid");
    	}
    	if (level.equals(preferencesService.PROJECT_LEVEL) && preferencesService.getProject() == null) {
    		throw new IllegalStateException("SafariStringFieldEditor.setPreferenceLevel:  given level is '" + preferencesService.PROJECT_LEVEL +
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
    public String getStringValue() {
    	String value = getTextControl().getText();
    	return value;
    }

    
    /**
     * Sets this field editor's value.
     *
     * @param value the new value, or <code>null</code> meaning the empty string
     */
    public void setStringValue(String value) {	
        if (getTextControl() != null) {
            if (value == null)
                value = "";//$NON-NLS-1$
            previousValue = getTextControl().getText();
            if (!previousValue.equals(value) ||
            		levelFromWhichLoaded == null || !levelFromWhichLoaded.equals(preferencesLevel))
            {
            	getTextControl().setText(value);
            	textModified = true;
            	levelFromWhichLoaded = preferencesLevel;
            	// Not to be called when initialized or set to the default
            	// which means not when loaded in our case); however,
            	// setting the value directly is like entering a value
            	// from the keyboard, so consider it changed
            	valueChanged();
            }
            // setInherited(false) is done in valueChanged(); and probably shouldn't
            // be done when not valueChanged()
            //setInherited(false);
            /*
            if (levelFromWhichLoaded == null || !levelFromWhichLoaded.equals(preferencesLevel)) {
            	// even if the text hasn't changed its value, if its
            	// changed its origin, then consider it modified
            	// (may affect whether it is stored for this level
            	// which it should be, even if it's the same as the
            	// text that was previously inherited)
            	textModified = true;
            	levelFromWhichLoaded = preferencesLevel;
            }
            */
            setPresentsDefaultValue(preferencesLevel.equals(ISafariPreferencesService.DEFAULT_LEVEL));
        }
    }

    protected boolean textNull = true;

    public Text getTextControl() {
    	//return super.getTextControl();

    	if (!parent.isDisposed()) {
    		if (textNull) {
	        	// Should actually create checkbox if it doesn't exist
	        	// so should really never be null
	        	Text text = super.getTextControl(parent);
	        	textNull = text == null;
	        	if (!textNull) {
	    	    	text.addModifyListener(
	    	    			new ModifyListener() {
	    	    				public void modifyText(ModifyEvent e) {
	    	    					//System.out.println("STFE.text modify listener (from getTextControl):  textModified set to true");
	    	    					textModified = true;
	    	    					setInherited(false);
	    	    				}
	    	    			}
	    	    	);
		            text.addDisposeListener(new DisposeListener() {
		                public void widgetDisposed(DisposeEvent event) {
		                    //System.out.println("STFE.text dispose listener (from getTextControl):  textNull set to true");
		                    textNull = true;
		                }
		            });
	        	}
	        }
	    	return super.getTextControl(parent);
        }
    	return null;
    }
    
    
    public Composite getParent() {
    	return parent;
    }
    
    /**
     * Informs this field editor's listener, if it has one, about a change
     * to the value (<code>VALUE</code> property) provided that the old and
     * new values are different.
     * <p>
     * This hook is <em>not</em> called when the text is initialized 
     * (or reset to the default value) from the preference store.
     * </p>
     * This extension converts an inherited field to a local one.
     * This is done because we don't want to allow inehrited fields to
     * be edited as such.  This extension also maintains the local copy
     * of the previous value.	
     */
    protected void valueChanged() {
    	// If we're editing (or otherwise directly updating) this field,
    	// then treat it as if the value is set locally
    	Text control = getTextControl();
    	isInherited = false;
    	control.setBackground(colorWhite);
    	
    	// If we've just set the value (say, without editing)
    	// then treat the field as if it's enabled and editable
    	// (we might be able to affect the field without editing
    	// it by setting the value directly, which seems to be
    	// possible even when the field is disabled and not
    	// editable)
/*    	
    	if (!control.getEnabled()) {
    		control.setEnabled(true);
    	}
    	if (!control.getEditable()) {
    		control.setEditable(true);
    	}
*/    	
    	super.valueChanged();
    	
    	// Maintain our local copy of the previous value
    	// (since we can't access oldValue in the parent)
        String newValue = control.getText();
        if (!newValue.equals(previousValue)) {
            previousValue = newValue;
        }
    }

    
    
    /**
     * Checks whether the text input field contains a valid value or not.
     *
     * @return <code>true</code> if the field value is valid,
     *   and <code>false</code> if invalid
     */
    protected boolean checkState()
    {
        boolean result = false;
    
        if (isEmptyStringAllowed()) {
            result = true;
        }

        if (!result && getTextControl() == null) {
        	setErrorMessage("Text control is null; no valid value represented");
            result = false;
        }

        if (!result && getTextControl() != null) {
        	String txt = getStringValue();
        	result = (txt.trim().length() > 0) || isEmptyStringAllowed();
        }
        
        result = result && doCheckState();


        return notifyState(result);
    }
    
    
    private boolean notifyState(boolean state)
    {

        if (state)
            clearErrorMessage();
        else
        	showErrorMessage(getErrorMessage());
        
    	if (prefPage != null)
    		prefPage.setValid(state);
    	if (prefTab != null)
    		prefTab.setValid(state);
    	
    	//System.out.println("SFFE.doCheckState():  returning " + state);
    	return state;
    }
 
    
}
