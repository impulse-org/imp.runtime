/*******************************************************************************
* Copyright (c) 2007 IBM Corporation.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*    Robert Fuhrer (rfuhrer@watson.ibm.com) - initial API and implementation
*******************************************************************************/

package org.eclipse.imp.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.imp.runtime.RuntimePlugin;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.PlatformUI;

/**
 * Initializes IMP framework-wide preferences to reasonable default values.
 * @author rfuhrer@watson.ibm.com
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {
    public void initializeDefaultPreferences() {
        ColorRegistry registry= null;
        if (PlatformUI.isWorkbenchRunning())
                registry= PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getColorRegistry();
        IPreferenceStore store= RuntimePlugin.getInstance().getPreferenceStore();

        store.setDefault(PreferenceConstants.P_EMIT_MESSAGES, false);
        store.setDefault(PreferenceConstants.P_EMIT_BUILDER_DIAGNOSTICS, false);

        // RMF 7/16/2008 - Somehow JFaceResources.getFont(symbolicName) and JFaceResources.getFontDescriptor()
        // return different answers. Seems that getFontDescriptor() gives us a better answer, though.
        FontData[] fontData= JFaceResources.getFontDescriptor("org.eclipse.jdt.ui.editors.textfont").getFontData();

        if (fontData != null && fontData.length > 0)
            PreferenceConverter.setDefault(store, PreferenceConstants.P_SOURCE_FONT, fontData);

        store.setDefault(PreferenceConstants.P_TAB_WIDTH, 8);
        store.setDefault(PreferenceConstants.P_DUMP_TOKENS, false);
        store.setDefault(PreferenceConstants.EDITOR_MATCHING_BRACKETS, true);

        PreferenceConverter.setDefault(store, PreferenceConstants.EDITOR_MATCHING_BRACKETS_COLOR,
                findRGB(registry, RuntimePlugin.IMP_RUNTIME + "." + PreferenceConstants.EDITOR_MATCHING_BRACKETS_COLOR, new RGB(192, 192,192)));
    }

    /**
     * Returns the RGB for the given key in the given color registry.
     * 
     * @param registry the color registry
     * @param key the key for the constant in the registry
     * @param defaultRGB the default RGB if no entry is found
     * @return RGB the RGB
     * @since 3.3
     */
    private static RGB findRGB(ColorRegistry registry, String key, RGB defaultRGB) {
            if (registry == null)
                    return defaultRGB;
                    
            RGB rgb= registry.getRGB(key);
            if (rgb != null)
                    return rgb;
            
            return defaultRGB;
    }
}
