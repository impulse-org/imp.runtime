package org.eclipse.uide.runtime;

import org.eclipse.ui.IStartup;
import org.eclipse.ui.plugin.*;
import org.eclipse.uide.core.LanguageRegistry;
import org.eclipse.jface.resource.ImageDescriptor;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class RuntimePlugin extends SAFARIPluginBase implements IStartup {
    public static final String UIDE_RUNTIME= "org.eclipse.uide.runtime"; // must match plugin ID in MANIFEST.MF

    // The singleton instance.
    private static RuntimePlugin sPlugin;

    public RuntimePlugin() {
	sPlugin= this;
    }

    /**
     * Returns the singleton instance.
     */
    public static RuntimePlugin getInstance() {
	return sPlugin;
    }

    public String getID() {
	return UIDE_RUNTIME;
    }

    /**
     * This method is called upon plug-in activation
     */
    public void start(BundleContext context) throws Exception {
	super.start(context);
    }

    /**
     * This method is called when the plug-in is stopped
     */
    public void stop(BundleContext context) throws Exception {
	super.stop(context);
	sPlugin= null;
    }

    /**
     * Returns an image descriptor for the image file at the given plug-in relative path.
     * 
     * @param path
     *                the path
     * @return the image descriptor
     */
    public static ImageDescriptor getImageDescriptor(String path) {
	return AbstractUIPlugin.imageDescriptorFromPlugin(UIDE_RUNTIME, path);
    }

    public void earlyStartup() {
	LanguageRegistry.registerLanguages();
    }
}
