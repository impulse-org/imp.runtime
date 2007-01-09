package org.eclipse.uide.preferences.fields;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.uide.preferences.ISafariPreferencesService;
import org.eclipse.uide.preferences.SafariPreferencesTab;

public class SafariIntegerFieldEditor extends SafariStringFieldEditor {
	
	// From IntegerFieldEditor
    protected int minValidValue = 0;
    protected int maxValidValue = Integer.MAX_VALUE;
    protected static final int DEFAULT_TEXT_LIMIT = 10;

    protected String errorMessage =
    	"Value must be an integer between " + minValidValue + " and " + maxValidValue;
	
    /**
     * Creates an integer field editor
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
    public SafariIntegerFieldEditor(
			PreferencePage page, SafariPreferencesTab tab,
    		ISafariPreferencesService service, String level, String name, String labelText,
    		int width, int strategy, Composite parent)
    {
    	super(page, tab, service, level, name, labelText, width, strategy, parent);
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
    public SafariIntegerFieldEditor(
			PreferencePage page, SafariPreferencesTab tab,
    		ISafariPreferencesService service, String level, String name, String labelText,
    		int width, Composite parent)
    {
        super(page, tab, service, level, name, labelText, width, StringFieldEditor.VALIDATE_ON_KEY_STROKE, parent);
    }
 
    
    /**
     * Creates a string field editor of unlimited width.
     * Use the method <code>setTextLimit</code> to limit the text.
     * 
     * @param name the name of the preference this field editor works on
     * @param labelText the label text of the field editor
     * @param parent the parent of the field editor's control
     */
    public SafariIntegerFieldEditor(
			PreferencePage page, SafariPreferencesTab tab,
			ISafariPreferencesService service, String level, String name, String labelText,
			Composite parent)
    {
    	// Replaced UNLIMITED text width in the following with default
        super(page, tab, service, level, name, labelText, DEFAULT_TEXT_LIMIT, parent);
    }

    
    /**
     * Copied from IntegerFieldEditor.
     * 
     * Sets the range of valid values for this field.
     * 
     * @param min the minimum allowed value (inclusive)
     * @param max the maximum allowed value (inclusive)
     */
    public void setValidRange(int min, int max) {
        minValidValue = min;
        maxValidValue = max;
    }

    
    
    /**
     * Copied from IntegerFieldEditor.
     * 
     * Returns this field editor's current value as an integer.
     *
     * @return the value
     * @exception NumberFormatException if the <code>String</code> does not
     *   contain a parsable integer
     */
    public int getIntValue() throws NumberFormatException {
        return new Integer(getStringValue()).intValue();
    }
    
    
    /* (non-Javadoc)
     * Copied from IntegerFieldEditor (with minor adaptations).
     * Method declared on StringFieldEditor.
     * Checks whether the entered String is a valid integer or not.
     */
    protected boolean checkState() {
    	
        Text text = getTextControl(parent);

        if (text == null)
            return false;

       
        String numberString = text.getText();
        try {
            int number = Integer.valueOf(numberString).intValue();
            if (number >= minValidValue && number <= maxValidValue) {
                clearErrorMessage();
                return true;
            } else {
                setErrorMessage(getLevelName() + ":  " + getLabelText() + "  " + errorMessage);
                return false;
            }
        } catch (NumberFormatException e1) {
            setErrorMessage(getLevelName() + ":  " + getLabelText() + "  " + "Number format exception");
        }

        return false;
    }

    
    public String getLevelName() {
    	if (preferencesLevel.equals(ISafariPreferencesService.DEFAULT_LEVEL)) return "Default";
    	if (preferencesLevel.equals(ISafariPreferencesService.CONFIGURATION_LEVEL)) return "Configuration";
    	if (preferencesLevel.equals(ISafariPreferencesService.INSTANCE_LEVEL)) return "Workspace";
    	if (preferencesLevel.equals(ISafariPreferencesService.PROJECT_LEVEL)) return "Project";
    	return "";
    }
    
    
}
