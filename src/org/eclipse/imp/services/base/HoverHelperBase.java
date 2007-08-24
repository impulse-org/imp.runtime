package org.eclipse.imp.services.base;

import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.imp.language.Language;
import org.eclipse.imp.services.IOutliner;

/**

 * 
 * @author 	suttons@us.ibm.com
 * Updates:
 * SMS 12 Aug 2007:  
 */
public abstract class HoverHelperBase
{
	
	protected UniversalEditor fEditor = null;
	
	protected Language fLanguage = null;
	
	public void setEditor(UniversalEditor editor) {
		fEditor = editor;
	}
	
	public void setLanguage(Language language) {
		fLanguage = language;
	}
	
}
