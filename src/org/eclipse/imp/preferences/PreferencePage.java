package org.eclipse.imp.preferences;

import org.eclipse.imp.runtime.RuntimePlugin;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FontFieldEditor;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

public class PreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
    public PreferencePage() {
	super(GRID);
	setPreferenceStore(RuntimePlugin.getInstance().getPreferenceStore());
	setDescription("Preferences for the IMP framework");
    }

    public void createFieldEditors() {
	final BooleanFieldEditor emitMessagesField= new BooleanFieldEditor(PreferenceConstants.P_EMIT_MESSAGES,
		"E&mit diagnostic messages from IMP UI", getFieldEditorParent());
	addField(emitMessagesField);

	final IntegerFieldEditor tabWidthField= new IntegerFieldEditor(PreferenceConstants.P_TAB_WIDTH,
		"&Tab width:", getFieldEditorParent());
	tabWidthField.setValidRange(1, 16);
	tabWidthField.setTextLimit(2);
	tabWidthField.setEmptyStringAllowed(false);

	final FontFieldEditor fontField= new FontFieldEditor(PreferenceConstants.P_SOURCE_FONT,
		"Source font:", getFieldEditorParent());

	addField(tabWidthField);
	addField(fontField);

	getPreferenceStore().addPropertyChangeListener(new IPropertyChangeListener() {
	    public void propertyChange(PropertyChangeEvent event) {
		if (event.getProperty().equals(PreferenceConstants.P_EMIT_MESSAGES))
		    PreferenceCache.emitMessages= ((Boolean) event.getNewValue()).booleanValue();
		else if (event.getProperty().equals(PreferenceConstants.P_TAB_WIDTH))
		    PreferenceCache.tabWidth= ((Integer) event.getNewValue()).intValue();
		else if (event.getProperty().equals(PreferenceConstants.P_SOURCE_FONT)) {
		    if (PreferenceCache.sourceFont != null)
			PreferenceCache.sourceFont.dispose();
		    PreferenceCache.sourceFont= new Font(PlatformUI.getWorkbench().getDisplay(), ((FontData[]) event.getNewValue())[0]);
		}
	    }
	});
    }

    public void init(IWorkbench workbench) {}
}
