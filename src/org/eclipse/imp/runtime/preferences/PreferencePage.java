package org.eclipse.uide.runtime.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.uide.runtime.RuntimePlugin;

public class PreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
    public PreferencePage() {
	super(GRID);
	setPreferenceStore(RuntimePlugin.getInstance().getPreferenceStore());
	setDescription("Preferences for the SAFARI framework");
    }

    public void createFieldEditors() {
	final BooleanFieldEditor emitMessagesField= new BooleanFieldEditor(PreferenceConstants.P_EMIT_MESSAGES,
		"E&mit diagnostic messages from SAFARI UI", getFieldEditorParent());
	addField(emitMessagesField);

	getPreferenceStore().addPropertyChangeListener(new IPropertyChangeListener() {
	    public void propertyChange(PropertyChangeEvent event) {
		if (event.getProperty().equals(PreferenceConstants.P_EMIT_MESSAGES))
		    SAFARIPreferenceCache.emitMessages= ((Boolean) event.getNewValue()).booleanValue();
	    }
	});
    }

    public void init(IWorkbench workbench) {}
}
