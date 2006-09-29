package org.eclipse.uide.preferences;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;

public abstract class SafariPreferencesTab {

	protected boolean isValid = true;
	
	protected Control[] buttons = null;
	
	
	public void performDefaults() {
		
	}
	
	public void performApply() {
		
	}
	
	public void setValid(boolean state) {
        boolean oldIsValid = isValid;
        isValid = state;
        if (oldIsValid != isValid) {
        	//System.out.println("SPT.setValid():  " + state);
            updateButtons();
        } else {
        	//System.out.println("SPT.setValid():  " + state);
        }
	}
	
	public boolean isValid() {
		return isValid;
	}
	
	public void updateButtons() {
		if (buttons != null) {
        	//System.out.println("SPT.updateButtons():  buttons not null");
			for (int i = 0; i < buttons.length; i++) {
				Button button = (Button) buttons[i];
				if (button != null)
					// TODO:  define string constants for button texts
					if (button.getText().startsWith("Restore"))
						continue;
					button.setEnabled(isValid());
			}
		} else {
        	//System.out.println("SPT.updateButtons():  buttons null");
		}
		
	}
	
}
