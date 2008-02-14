/*
 * (C) Copyright IBM Corporation 2007
 * 
 * This file is part of the Eclipse IMP.
 */
package org.eclipse.imp.utils;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IContributor;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.imp.language.ILanguageService;
import org.eclipse.imp.language.Language;
import org.eclipse.imp.language.LanguageRegistry;
import org.eclipse.imp.language.ServiceException;
import org.eclipse.imp.runtime.RuntimePlugin;
import org.osgi.framework.Bundle;

/*
 * Licensed Materials - Property of IBM, (c) Copyright IBM Corp. 2005 All Rights Reserved
 */

/**
 * @author Claffra
 * @author rfuhrer@watson.ibm.com
 * @author jurgen@vinju.org
 */
public class ExtensionPointFactory {
    public static ILanguageService createExtensionPoint(Language language, String extensionPointID) throws ServiceException {
        return createExtensionPoint(language, RuntimePlugin.IMP_RUNTIME, extensionPointID);
    }

    public static Set<ILanguageService> createExtensions(Language language, String extensionPointID) throws ServiceException {
        return createExtensions(language, RuntimePlugin.IMP_RUNTIME, extensionPointID);
    }

    public static String getLanguageID(String pluginID) {
        IExtensionPoint extensionPoint= Platform.getExtensionRegistry().getExtensionPoint(RuntimePlugin.IMP_RUNTIME, RuntimePlugin.LANGUAGE_DESCRIPTOR);
        IConfigurationElement[] configElements= extensionPoint.getConfigurationElements();

        for(int i= 0; i < configElements.length; i++) {
            IContributor contrib= configElements[i].getContributor();
            
            if (contrib.getName().equals(pluginID)) {
                return configElements[i].getAttribute(Language.LANGUAGE_ID_ATTR);
            }
        }
        
        return null;
    }

    public static ILanguageService createExtensionPointForElement(Language language, String pluginID, String extensionPointId, String elementName) throws ServiceException {
        IExtensionPoint extensionPoint= Platform.getExtensionRegistry().getExtensionPoint(pluginID, extensionPointId);

        if (extensionPoint == null) {
        	throw new ServiceException("No such language service extension point defined: " + pluginID + "." + extensionPointId);
        }  
            	
        ILanguageService service= getLanguageContributorForElement(extensionPoint, language.getName(), elementName);

		if (service == null && languageIsDerived(language)) {
            service= createExtensionPointForElement(LanguageRegistry.findLanguage(language.getDerivedFrom()), pluginID, extensionPointId, elementName);
        }

        return service;
    }

	private static boolean languageIsDerived(Language language) {
		final boolean hasParent = language.getDerivedFrom() != null && LanguageRegistry.findLanguage(language.getDerivedFrom()) != null;
		return hasParent;
	}

    public static ILanguageService createExtensionPoint(Language language, String pluginID, String extensionPointId) throws ServiceException {
        return createExtensionPointForElement(language, pluginID, extensionPointId, "class");
    }

    public static Set<ILanguageService> createExtensions(Language language, String pluginID, String extensionPointId) throws ServiceException {
        IExtensionPoint extensionPoint= Platform.getExtensionRegistry().getExtensionPoint(pluginID, extensionPointId);

        if (extensionPoint == null) {
        	throw new ServiceException("No such language service extension point defined: " + pluginID + "." + extensionPointId);
        }
        
        Set<ILanguageService> services = getLanguageContributors(extensionPoint, language.getName());

        if (services.isEmpty() && languageIsDerived(language)) {
			final ILanguageService baseServiceImpl = createExtensionPoint(
					LanguageRegistry.findLanguage(language.getDerivedFrom()),
					pluginID, extensionPointId);
			if (baseServiceImpl != null) {
				services.add(baseServiceImpl);
			}
		}

        return services;
    }

    @SuppressWarnings("deprecation")
	public static boolean languageServiceExists(String pluginID, String extensionPointID, Language language) {
        if (language == null)
            return false;

        IExtensionPoint extensionPoint= Platform.getExtensionRegistry().getExtensionPoint(pluginID, extensionPointID);
        IConfigurationElement[] elements= extensionPoint.getConfigurationElements();
        String lowerLang= language.getName().toLowerCase();

        if (elements != null) {
            for(int n= 0; n < elements.length; n++) {
                IConfigurationElement element= elements[n];
                Bundle bundle= Platform.getBundle(element.getDeclaringExtension().getNamespace());

                if (bundle != null) {
                    final String attrValue= element.getAttribute(Language.LANGUAGE_ID_ATTR);

                    if (attrValue != null && lowerLang.equals(attrValue.toLowerCase())) {
                        return true;
                    }
                }
            }
        }
        
        if (languageIsDerived(language)) {
            return languageServiceExists(pluginID, extensionPointID, LanguageRegistry.findLanguage(language.getDerivedFrom()));
        }
        
        return false;
    }

    @SuppressWarnings("deprecation")
	public static Set<ILanguageService> getLanguageContributors(IExtensionPoint extensionPoint, String language) throws ServiceException {
        IConfigurationElement[] elements= extensionPoint.getConfigurationElements();
        Set<ILanguageService> result= new HashSet<ILanguageService>();
        String lowerLang= language.toLowerCase();

        if (elements != null) {
            for(int n= 0; n < elements.length; n++) {
                IConfigurationElement element= elements[n];
                Bundle bundle= Platform.getBundle(element.getDeclaringExtension().getNamespace());

                if (bundle != null) {
                    final String attrValue= element.getAttribute(Language.LANGUAGE_ID_ATTR);

                    if (attrValue != null && lowerLang.equals(attrValue.toLowerCase())) {
                        try {
                            final ILanguageService executable= (ILanguageService) element.createExecutableExtension("class");
                            result.add(executable);
                        } catch (CoreException e) {
                            throw new ServiceException("Unable to instantiate implementation of " + extensionPoint + " for language '" + language + "'.", e);
                        } catch (ClassCastException e) {
                        	throw new ServiceException("Unable to instantiate implementation of " + extensionPoint + " for language '" + language + "' because it does not implement ILanguageService.", e);
                        }
                    }
                }
            }
        }
        
        return result;
    }

    @SuppressWarnings("deprecation")
	public static ILanguageService getLanguageContributorForElement(IExtensionPoint extensionPoint, String language, String elementName) throws ServiceException {
        IConfigurationElement[] elements= extensionPoint.getConfigurationElements();
        String lowerLang= language.toLowerCase();

        if (elements != null) {
            for(int n= 0; n < elements.length; n++) {
                IConfigurationElement element= elements[n];
                Bundle bundle= Platform.getBundle(element.getDeclaringExtension().getNamespace());

                if (bundle != null) {
                    final String attrValue= element.getAttribute(Language.LANGUAGE_ID_ATTR);

                    if (attrValue != null && lowerLang.equals(attrValue.toLowerCase())) {
                    	try {
                          return (ILanguageService) element.createExecutableExtension(elementName);
                    	} 
                    	catch (ClassCastException e) {
                    		throw new ServiceException("Extension does not point to a class that implements an ILanguageService:" + element, e);
                    	} 
                    	catch (CoreException e) {
							throw new ServiceException("Extension is totally invalid:" + element,e );
						}
                    }
                }
            }
        }
        
        return null;
    }

    public static ILanguageService getLanguageContributor(IExtensionPoint extensionPoint, String language) throws ServiceException  {
        return getLanguageContributorForElement(extensionPoint, language, "class");
    }

    @SuppressWarnings("deprecation")
	public static URL getResourceURL(String language, IExtensionPoint extensionPoint, String label) {
        IConfigurationElement[] elements= extensionPoint.getConfigurationElements();
        String lowerLabel= label.toLowerCase();
        String lowerLang= language.toLowerCase();

        if (elements != null) {
            for(int n= 0; n < elements.length; n++) {
                IConfigurationElement element= elements[n];
                Bundle bundle= Platform.getBundle(element.getDeclaringExtension().getNamespace());

                if (bundle != null) {
                    final String attrValue= element.getAttribute(Language.LANGUAGE_ID_ATTR);

                    if (attrValue != null && lowerLang.equals(attrValue.toLowerCase())) {
                        String resourceName= element.getAttribute(lowerLabel);
                        return bundle.getResource(resourceName);
                    }
                }
            }
        }

        return null;
    }
}
