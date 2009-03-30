package org.eclipse.imp.actions;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class ActionMessages {
    private static final String BUNDLE_NAME= "org.eclipse.imp.actions.ActionMessages"; //$NON-NLS-1$

    private static final ResourceBundle RESOURCE_BUNDLE= ResourceBundle.getBundle(BUNDLE_NAME);

    public static String OpenAction_label;
    public static String OpenAction_tooltip;
    public static String OpenAction_description;
    public static String OpenAction_declaration_label;
    public static String OpenAction_select_element;

    public static String OpenAction_error_messageBadSelection;
    public static String OpenAction_error_title;
    public static String OpenAction_error_problem_opening_editor;
    public static String OpenAction_multistatus_message;
    public static String OpenAction_error_message;

    public static String RulerEnableDisableBreakpointAction_0;
    public static String RulerEnableDisableBreakpointAction_1;
    public static String RulerEnableDisableBreakpointAction_2;
    public static String RulerEnableDisableBreakpointAction_3;

    private ActionMessages() {
        // no instance
    }

    /**
     * Returns the resource string associated with the given key in the resource bundle. If there isn't 
     * any value under the given key, the key is returned.
     *
     * @param key the resource key
     * @return the string
     */ 
    public static String getString(String key) {
        try {
            return RESOURCE_BUNDLE.getString(key);
        } catch (MissingResourceException e) {
            return '!' + key + '!';
        }
    }
    
    /**
     * Returns the resource bundle managed by the receiver.
     * 
     * @return the resource bundle
     */
    public static ResourceBundle getResourceBundle() {
        return RESOURCE_BUNDLE;
    }
    
    /**
     * Returns the formatted resource string associated with the given key in the resource bundle. 
     * <code>MessageFormat</code> is used to format the message. If there isn't  any value 
     * under the given key, the key is returned.
     *
     * @param key the resource key
     * @param arg the message argument
     * @return the string
     */ 
    public static String getFormattedString(String key, Object arg) {
        return getFormattedString(key, new Object[] { arg });
    }
    
    /**
     * Returns the formatted resource string associated with the given key in the resource bundle. 
     * <code>MessageFormat</code> is used to format the message. If there isn't  any value 
     * under the given key, the key is returned.
     *
     * @param key the resource key
     * @param args the message arguments
     * @return the string
     */ 
    public static String getFormattedString(String key, Object[] args) {
        return MessageFormat.format(getString(key), args);  
    }   
}
