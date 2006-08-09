package org.eclipse.uide.utils;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.uide.core.ErrorHandler;
import org.eclipse.uide.core.ILanguageService;
import org.eclipse.uide.core.Language;
import org.eclipse.uide.core.LanguageRegistry;
import org.eclipse.uide.preferences.SAFARIPreferenceCache;
import org.eclipse.uide.runtime.RuntimePlugin;
import org.osgi.framework.Bundle;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 2005  All Rights Reserved
 */

/**
 * @author Claffra
 * @author rfuhrer@watson.ibm.com
 */
public class ExtensionPointFactory {
    public static ILanguageService createExtensionPoint(Language language, String pluginID, String extensionPointId) {
	if (language == null) {
	    ErrorHandler.reportError("Cannot obtain service on null language: " + extensionPointId);
	    return null;
	}
	ILanguageService service= null;

	try {
	    IExtensionPoint extensionPoint= Platform.getExtensionRegistry().getExtensionPoint(pluginID, extensionPointId);

	    if (extensionPoint != null)
		service= getLanguageContributor(extensionPoint, language.getName());
	    else
		ErrorHandler.reportError("No such language service extension point defined: " + pluginID + "." + extensionPointId);
	} catch (Throwable e) {
	    ErrorHandler.reportError("Error finding \"" + extensionPointId + "\" service for \"" + language + "\"", e);
	}

	// Check for a base language implementation and use that if available
	if (service == null && language.getDerivedFrom() != null) {
		// SMS 9 Aug 2006
		// When a user is defining a new language, if the user fills in a value for "derived from"
		// that does not correspond to a SAFARI-supported language, then a call to
		// LanguageRegistry.findLanguage(language.getDerivedFrom()) will return null,
		// so check for that before using the return value in a call to languageServiceExists(..)
		// (that was not done originally)
		// Note:  Check the explanatory text for that attribute in the NewLanguage wizard
		// to assure that it provides a warning to provide only SAFARI-supported languages
		if (LanguageRegistry.findLanguage(language.getDerivedFrom()) == null) {
			service = null;
		} else {
		    service= createExtensionPoint(LanguageRegistry.findLanguage(language.getDerivedFrom()), pluginID, extensionPointId);		
		}
	}

	if (service == null)
	    service= createDefaultImpl(language, pluginID, extensionPointId);

	// if (service != null)
	//   service.setLanguage(language.getName());
	return service;
    }

    /**
     * Uses reflection and the language name to find and return a default
     * implementation for the given language service, if it exists.
     * Otherwise, returns null.
     */
    private static ILanguageService createDefaultImpl(Language language, String pluginID, String extensionPointId) {
	ILanguageService service= null;
	try {
	    String className= "Default" + Character.toUpperCase(extensionPointId.charAt(0)) + extensionPointId.substring(1);
	    String defaultClass= pluginID + ".defaults." + className;

	    service= (ILanguageService) Class.forName(defaultClass).newInstance();
	} catch (ClassNotFoundException e) {
	    if (SAFARIPreferenceCache.emitMessages)
		RuntimePlugin.getInstance().writeInfoMsg("No language-specific or default implementation found for service " + extensionPointId + " and language " + language.getName());
	    else
		ErrorHandler.reportError("No language-specific or default implementation found for service " + extensionPointId + " and language " + language.getName(), false, true);
	} catch (Throwable ee) {
	    ErrorHandler.reportError("Universal Editor Error", ee);
	}
	return service;
    }

    public static boolean languageServiceExists(String pluginID, String extensionPointID, Language language) {
	IExtensionPoint extensionPoint= Platform.getExtensionRegistry().getExtensionPoint(pluginID, extensionPointID);
	IConfigurationElement[] elements= extensionPoint.getConfigurationElements();
	String lowerLang= language.getName().toLowerCase();

	if (elements != null) {
	    for(int n= 0; n < elements.length; n++) {
		IConfigurationElement element= elements[n];
		Bundle bundle= Platform.getBundle(element.getDeclaringExtension().getNamespace());

		if (bundle != null) {
		    final String attrValue= element.getAttribute("language");

		    if (attrValue != null && lowerLang.equals(attrValue.toLowerCase()))
			return true;
		}
	    }
	}
	if (language.getDerivedFrom() != null) {
		// SMS 9 Aug 2006
		// When a user is defining a new language, if the user fills in a value for "derived from"
		// that does not correspond to a SAFARI-supported language, then a call to
		// LanguageRegistry.findLanguage(language.getDerivedFrom()) will return null,
		// so check for that before using the return value in a call to languageServiceExists(..)
		// (that was not done originally)
		// Note:  Check the explanatory text for that attribute in the NewLanguage wizard
		// to assure that it provides a warning to provide only SAFARI-supported languages
		if (LanguageRegistry.findLanguage(language.getDerivedFrom()) == null) {
			return false;
		}
	    return languageServiceExists(pluginID, extensionPointID, LanguageRegistry.findLanguage(language.getDerivedFrom()));
	}
	return false;
    }

    public static ILanguageService getLanguageContributor(IExtensionPoint extensionPoint, String language) throws CoreException {
	IConfigurationElement[] elements= extensionPoint.getConfigurationElements();
	String lowerLang= language.toLowerCase();

	if (elements != null) {
	    for(int n= 0; n < elements.length; n++) {
		IConfigurationElement element= elements[n];
		Bundle bundle= Platform.getBundle(element.getDeclaringExtension().getNamespace());

		if (bundle != null) {
		    final String attrValue= element.getAttribute("language");

		    if (attrValue != null && lowerLang.equals(attrValue.toLowerCase())) {
			return (ILanguageService) element.createExecutableExtension("class");
		    }
		}
	    }
	}
	return null;
    }
}
