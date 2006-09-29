package org.eclipse.uide.preferences.fields;

import java.io.File;
import java.util.Stack;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.uide.preferences.ISafariPreferencesService;
import org.eclipse.uide.preferences.PreferenceDialogConstants;
import org.eclipse.uide.preferences.SafariPreferencesTab;

public class SafariFileFieldEditor extends SafariStringButtonFieldEditor
{
	
	public SafariFileFieldEditor(
			PreferencePage page, SafariPreferencesTab tab,
    		ISafariPreferencesService service, String level, String name, String labelText,
    		int width, int strategy, Composite parent)
    {
    	super(page, tab, service, level, name, labelText, width, strategy, parent);
    	this.getChangeControl(parent).setText(PreferenceDialogConstants.BROWSE_LABEL);
    }
	
	
    /**
     * Creates a SAFARI file field editor.
     * Use the method <code>setTextLimit</code> to limit the text.
     * 
     * @param name the name of the preference this field editor works on
     * @param labelText the label text of the field editor
     * @param width the width of the text input field in characters,
     *  or <code>UNLIMITED</code> for no limit
     * @param parent the parent of the field editor's control
     */
    public SafariFileFieldEditor(
			PreferencePage page, SafariPreferencesTab tab,
    		ISafariPreferencesService service, String level, String name, String labelText,
    		int width, Composite parent)
    {
        super(page, tab, service, level, name, labelText, width, VALIDATE_ON_KEY_STROKE, parent);
    	this.getChangeControl(parent).setText(PreferenceDialogConstants.BROWSE_LABEL);
    }
    
    
    /**	
     * Creates a SAFARI string button field editor.
     * Use the method <code>setTextLimit</code> to limit the text.
     * 
     * @param name the name of the preference this field editor works on
     * @param labelText the label text of the field editor
     * @param parent the parent of the field editor's control
     */
    public SafariFileFieldEditor(
			PreferencePage page, SafariPreferencesTab tab,
    		ISafariPreferencesService service, String level, String name, String labelText, Composite parent)
    {
        super(page, tab, service, level, name, labelText, parent);
    	this.getChangeControl(parent).setText(PreferenceDialogConstants.BROWSE_LABEL);
    }
 
    
    /**	
     * Creates a SAFARI string button field editor.
     * Use the method <code>setTextLimit</code> to limit the text.
     * 
     * @param name the name of the preference this field editor works on
     * @param labelText the label text of the field editor
     * @param parent the parent of the field editor's control
     */
/*    
    public SafariFileFieldEditor(
    		PreferencePage page,
    		SafariPreferencesTab tab,
    		ISafariPreferencesService service, String level, String name, String labelText, Composite parent)
    {
        super(service, level, name, labelText, parent);
    	prefPage = page;
    	prefTab = tab;
    }
*/    
    
    /*
     * Below copoied from org.eclipse.jface.preference.FileFieldEditor
     */

    
    /**
     * List of legal file extension suffixes, or <code>null</code>
     * for system defaults.
     */
    private String[] extensions = null;

    /**
     * Indicates whether the path must be absolute;
     * <code>false</code> by default.
     */
    private boolean enforceAbsolute = false;


    /* (non-Javadoc)
     * Method declared on StringButtonFieldEditor.
     * Opens the file chooser dialog and returns the selected file.
     */
    protected String changePressed() {    	
        File f = new File(getTextControl().getText());
        if (!f.exists())
            f = null;
        File d = getFile(f);
        if (d == null)
            return null;

        return d.getAbsolutePath();
    }

    /* (non-Javadoc)
     * Method declared on StringFieldEditor.
     * Checks whether the text input field specifies an existing file.
     */
/*   
   protected boolean checkState() {
        String msg = null;

        String path = getTextControl().getText();
        if (path != null)
            path = path.trim();
        else
            path = "";//$NON-NLS-1$
        if (path.length() == 0) {
            if (!isEmptyStringAllowed())
                msg = getErrorMessage();
        } else {
            File file = new File(path);
            if (file.isFile()) {
                if (enforceAbsolute && !file.isAbsolute())
                    msg = JFaceResources
                            .getString("FileFieldEditor.errorMessage2");//$NON-NLS-1$
            } else {
                msg = getErrorMessage();
            }
        }

        if (msg != null) { // error
            showErrorMessage(msg);
            return false;
        }

        // OK!
        clearErrorMessage();
        return true;
    }
*/
    	
    /**
     * Helper to open the file chooser dialog.
     * @param startingDirectory the directory to open the dialog on.
     * @return File The File the user selected or <code>null</code> if they
     * do not.
     */
    private File getFile(File startingDirectory) {

        FileDialog dialog = new FileDialog(getShell(), SWT.OPEN);
        if (startingDirectory != null)
            dialog.setFileName(startingDirectory.getPath());
        if (extensions != null)
            dialog.setFilterExtensions(extensions);
        String file = dialog.open();
        if (file != null) {
            file = file.trim();
            if (file.length() > 0)
                return new File(file);
        }

        return null;
    }

    /**
     * Sets this file field editor's file extension filter.
     *
     * @param extensions a list of file extension, or <code>null</code> 
     * to set the filter to the system's default value
     */
    public void setFileExtensions(String[] extensions) {
        this.extensions = extensions;
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
/*    
    protected void valueChanged() {
    	// TODO:  The following line is from the original, but check
    	// whether it still applies (when is valueChanged() called?)
    	// Alternatively, could modify setPresentsDefaultValue to
    	// compute whether the default is set (rather than to set it
    	// directly)
    	setPresentsDefaultValue(false);
        
//        System.out.println("SSFE.valueChanged():  newValue = " + getTextControl().getText());
        
        boolean oldState = 	isValid();
        refreshValidState();
        

        if (isValid() != oldState) {
            fireStateChanged(IS_VALID, oldState, isValid());
            if (prefPage != null)
            	prefPage.setValid(isValid());
        }

            
        // SMS 24 Aug 2006:  Added check for null control
        // and branch for null case
        if (getTextControl() ==  null) {
        	if (previousValue != null) {
                fireValueChanged(VALUE, previousValue, null);
                previousValue = null;
                if (prefPage != null)
                	prefPage.setValid(isValid());
        	}
        	return;
        }
        String newValue = getTextControl().getText();
        if (!newValue.equals(previousValue)) {
            fireValueChanged(VALUE, previousValue, newValue);
            if (prefPage != null)
            	prefPage.setValid(isValid());
            previousValue = newValue;
        }
    }
*/
    
    // getErrorMessage() is defined on StringFieldEditor,
    // and can be set with setErrorMessage(String);
    
    
    protected boolean doCheckState()
    {	
        
        String msg = null;

        // Here we check for empty or null strings, although
        // this may very well be checked at a higher level
        // (so we might not ever get here with this problem)
        String path = getTextControl().getText();
        if (path != null)
            path = path.trim();
        else
            path = "";//$NON-NLS-1$
        if (path.length() == 0 && !isEmptyStringAllowed()) {
                msg = "Path length is zero when empty string is not allowed";
                setErrorMessage(msg);
                return false;
        }
        
        // Check for balanced quotes
        final String singleQuote = "'";
        final String doubleQuote = "\"";
        Stack stack = new Stack();
        for (int i = 0; i < path.length(); i++) {
        	if (path.charAt(i) == '\'') {
        		if (!stack.empty() && singleQuote.equals(stack.peek()))
        			stack.pop();
        		else
        			stack.push(singleQuote);
        	}
        	if (path.charAt(i) == '"') {
        		if (!stack.empty() && doubleQuote.equals(stack.peek()))
        			stack.pop();
        		else
        			stack.push(doubleQuote);
        	}
        }
        if (stack.size() != 0)
        	return false;

        // Now validate list segments between quotes
        path = path.replace("\"", "'");
        String[] splits = path.split("'");       
        boolean splitsVerified = true;
        int start = path.startsWith("'") ? 1 : 0;
        for (int i = start; i < splits.length; i++) {
        	splitsVerified = splitsVerified && doCheckState(splits[i]);
        	if (!splitsVerified) return false;
        }
        return true;
    	
    }
    
    
    protected boolean doCheckState(String path)
    {	// This is the real work of the original doCheckState()
    	String msg = null;
        File file = new File(path);
        if (file.isFile()) {
            if (enforceAbsolute && !file.isAbsolute())
                msg = JFaceResources
                        .getString("FileFieldEditor.errorMessage2");//$NON-NLS-1$
        } else {
            msg = 	//etErrorMessage();
            	"Path does not designate a valid file";
        }

	    boolean result = true;
	    if (msg != null) { // error
	        setErrorMessage(msg);
	    	result = false;
	    } else {	// OK!
	        // don't clear any prior error message,
	    	// although probably if we're here there
	    	// isn't one
	    	result = true;
	    }
	
		return result;
    }
    

}
