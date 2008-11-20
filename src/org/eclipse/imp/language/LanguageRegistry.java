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

/*
 * (C) Copyright IBM Corporation 2007, 2008
 * 
 * This file is part of the Eclipse IMP.
 */
package org.eclipse.imp.language;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.imp.core.ErrorHandler;
import org.eclipse.imp.editor.EditorInputUtils;
import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.imp.preferences.PreferenceCache;
import org.eclipse.imp.runtime.RuntimePlugin;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorMapping;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.registry.EditorDescriptor;
import org.eclipse.ui.internal.registry.EditorRegistry;
import org.eclipse.ui.internal.registry.FileEditorMapping;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.osgi.framework.Bundle;

/*
 * Licensed Materials - Property of IBM, (c) Copyright IBM Corp. 2005, 2008 All
 * Rights Reserved
 */

/**
 * @author Claffra
 * @author rfuhrer@watson.ibm.com
 * @author jurgen@vinju.org
 * @author Stan Sutton (suttons@us.ibm.com)
 * 
 * Registry for IMP language contributors.
 */
@SuppressWarnings("restriction")
public class LanguageRegistry
{
	private static Object statusCheckMutex = new Object();
	private static boolean isFullyInitialized = false;
	
	private static Map<String, Language> fRegister;

	private static final String EXTENSION = "languageDescription";

	private static IEditorDescriptor universalEditor;

	private static EditorRegistry editorRegistry;

	
	/*
	 * No one should instantiate this class.
	 * TODO:  Perhaps should be a singleton class.
	 */
	private LanguageRegistry() {}
	

	private static Map<String, Language> getRegister() {
		if(fRegister == null) {
			fRegister = new HashMap<String, Language>();
		}
		return fRegister;
	}
	
	
	/**
	 * Initialize the registry. Discover all contributors to the
	 * languageDescription extension point. The registry will not be fully
	 * initialized until the registerLanguages() method has been called.
	 */
	private static void preInitEditorRegistry() {
	 	try {
			editorRegistry = (EditorRegistry) PlatformUI.getWorkbench()
					.getEditorRegistry();
			initializeUniversalEditorDescriptor(editorRegistry);
	
			IExtensionPoint extensionPoint = Platform.getExtensionRegistry()
					.getExtensionPoint(RuntimePlugin.IMP_RUNTIME, EXTENSION);
	
			if (extensionPoint == null) {
				ErrorHandler
						.reportError("Nonexistent extension point called \"" +
						 RuntimePlugin.IMP_RUNTIME + "." + EXTENSION);
			} else {
				IConfigurationElement[] elements = extensionPoint
						.getConfigurationElements();
	
				if (elements != null) {
					for (IConfigurationElement element : elements) {
						Bundle bundle = Platform.getBundle(element
								.getDeclaringExtension()
								.getNamespaceIdentifier());
	
						if (bundle != null) {
							register(new Language(element));
						}
					}
				}
			}
		} catch (InvalidRegistryObjectException e) {
			if (PreferenceCache.emitMessages) {
				RuntimePlugin.getInstance().writeErrorMsg(
						"IMP LanguageRegistry error in preInitEditorRegistry():  InvalidRegistryObjectException:  " + e);
			} else {
				ErrorHandler.reportError("IMP LanguageRegistry error", e);
			}
		}
	}
	
	/**
	 * Returns the language description for a given editor input. First the file
	 * extension is used to discover registered languages. Then each language is
	 * used to ensure it actually supports the content of the file.
	 * 
	 * @param editorInput
	 *            the editorInput to be opened
	 * @return the contributed language description
	 * @return null if no language is contributed with the given
	 *         extension/content
	 */
	public static Language findLanguage(IEditorInput editorInput, IDocumentProvider docProvider) {
		if (!isFullyInitialized)
			initializeRegistryAsNeeded();
		IPath path= EditorInputUtils.getPath(editorInput);

		return findLanguage(path, docProvider.getDocument(editorInput));
	}

	/**
	 * Determine the source language contained by the resource at the given
	 * path.
	 * 
	 * @param path
	 * @param file if non-null, may be used to validate the contents of the file
	 *            (e.g. to distinguish dialects)
	 * @return
	 */
	public static Language findLanguage(IPath path, IDocument doc) {
		// TODO: use Eclipse content type instead
		if (!isFullyInitialized())
			initializeRegistryAsNeeded();
		String extension= path.getFileExtension();
                String docContents= (doc != null) ? doc.get() : null;

		// N.B. It's ok for multiple language descriptors to specify the same
		// file name extension; the associated validators should use the file
                // contents to identify the dialects.
		if (extension != null) {
		    for (Language lang : getRegister().values()) {
		        if (lang.hasExtension(extension)) {
		            LanguageValidator validator = lang.getValidator();

		            if (validator != null && docContents != null) {
		                if (validator.validate(docContents)) {
		                    return lang;
		                }
		            } else {
		                return lang;
		            }
		        }
		    }
		}

		if (PreferenceCache.emitMessages) {
			RuntimePlugin.getInstance().writeErrorMsg(
					"No language support for text/source file of type '" +
					 extension + "'.");
		} else {
			ErrorHandler
					.reportError("No language support for text/source file of type '" +
					 extension + "'.");
		}

		return null;
	}

	public static Language findLanguageByNature(String natureID) {
		if (!isFullyInitialized())
			initializeRegistryAsNeeded();
		for (Language lang : getRegister().values()) {
			String aNatureID = lang.getNatureID();

			if (aNatureID != null && aNatureID.equals(natureID)) {
				return lang;
			}
		}
		return null;
	}

 	public static Collection<Language> getLanguages() {
		if (!isFullyInitialized())
			initializeRegistryAsNeeded();
 		return Collections.unmodifiableCollection(getRegister().values());
 	}
 		 
 	public static Language findLanguage(String languageName) {
		if (!isFullyInitialized())
			initializeRegistryAsNeeded();
 		return getRegister().get(languageName.toLowerCase());
 	}
	
	
	/**
	 * Registers a language dynamically (as opposed to using an extension
	 * point). This binds the file extensions associated with the language to
	 * the IMP Universal editor.
	 * 
	 * @param language
	 */
	public static void registerLanguage(Language language) {
		if (!isFullyInitialized())
			initializeRegistryAsNeeded();
		register(language);

		List<IFileEditorMapping> mappings = new ArrayList<IFileEditorMapping>();
		Collections.addAll(mappings, getEditorRegistry().getFileEditorMappings());
		addUniversalEditorMappings(language.getFilenameExtensions(), mappings);
		updateEditorRegistry(mappings);
	}

	private static EditorRegistry getEditorRegistry() {
		return editorRegistry;
	}
	
	/**
	 * Removes stale registrations and then registers all languages declared
	 * using the IMP languageDescription extension point. The file extensions of
	 * all registered languages are bound to the IMP universal editor.
	 * 
	 * This method is called at earlyStartup time to initialize the registry.
	 * It is also called from within UniversalEditor.createPartControl(..)
	 * (probably as a way to make sure that the registry is initialized if it
	 * hasn't been already).  It is also called from various accessor methods
	 * of the LanguageRegistry class to similarly assure that the registry is
	 * initialized.
	 * 
	 * To prevent thread access errors and other possible concurency conflicts,
	 * this method executes within a synchronized block.  It both tests and sets
	 * isFullyInitialized within this block, as a means to assure that the registry
	 * will be initialized serially and only once.  Although isFullyInitialized
	 * can be tested from anywhere, this is the only place that it should be set.
	 */
	public static void initializeRegistryAsNeeded()
	{	
		synchronized(statusCheckMutex) {
			if(isFullyInitialized()) {
				return;
			}
			preInitEditorRegistry();
			
			if (PreferenceCache.emitMessages) {
				RuntimePlugin.getInstance().writeInfoMsg(
						"Looking for IMP language description extensions...");
			}
	
			List<String> langExtens = collectAllLanguageFileNameExtensions();
			List<IFileEditorMapping> newMap = new ArrayList<IFileEditorMapping>();
			
			addNonUniversalEditorMappings(newMap);
			addUniversalEditorMappings(langExtens, newMap);
			updateEditorRegistry(newMap);

			setFullyInitialized();
		}
	}

	private static void addNonUniversalEditorMappings(List<IFileEditorMapping> newMap) {
		for (IFileEditorMapping mapping : getEditorRegistry()
				.getFileEditorMappings()) {
			IEditorDescriptor defaultEditor = mapping.getDefaultEditor();
			if (defaultEditor == null
					|| !defaultEditor.getId().equals(UniversalEditor.EDITOR_ID)) {
				newMap.add(mapping);
			}
		}
	}

	/**
	 * Adds new mappings to the universal editor for a set of extensions
	 * @param extensions
	 * @param newMap
	 */
	private static void addUniversalEditorMappings(Iterable<String> extensions,
			List<IFileEditorMapping> newMap) {
	    IFileEditorMapping[] mappings= editorRegistry.getFileEditorMappings();

	    for (String ext : extensions) {
	        IFileEditorMapping mapping= findMappingFor(ext, mappings);
	        if (mapping == null) {
	            mapping= new FileEditorMapping(ext);
                    FileEditorMapping fem= (FileEditorMapping) mapping;
	        }
	        IEditorDescriptor defaultEditor= mapping.getDefaultEditor();
                FileEditorMapping fem= (FileEditorMapping) mapping;
	        if (defaultEditor == null || defaultEditor.getId().equals("")) {
	            fem.setDefaultEditor((EditorDescriptor) universalEditor);
	        } else {
	        	// SMS 19 Nov 2008
	        	// Revised else branch according to patch provided by Edward Willink
	        	// Bug #242967, attachment id=109002
	        	boolean gotIt = false;
	        	for (IEditorDescriptor editor : fem.getEditors()) {
	        	    if (editor == universalEditor) {
	        	    	gotIt = true;
	        	    	break;
	        	    }
	        	}
	        	if (!gotIt)
	        		fem.addEditor((EditorDescriptor) universalEditor);
	        }
	        newMap.add(mapping);
	    }
	}

	private static IFileEditorMapping findMappingFor(String ext, IFileEditorMapping[] mappings) {
	    for(int i= 0; i < mappings.length; i++) {
	        if (mappings[i].getExtension().equals(ext)) {
	            return mappings[i];
	        }
	    }
	    return null;
	}


	/**
	 * Commits a new list of editor mappings to the editorRegistry
	 * @param newMap
	 */
	private static void updateEditorRegistry(final List<IFileEditorMapping> newMap) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				getEditorRegistry().setFileEditorMappings(newMap.toArray(new FileEditorMapping[newMap.size()]));
				// TODO Do we really want to save the associations persistently?
				getEditorRegistry().saveAssociations();
			}
		});
	}

	private static List<String> collectAllLanguageFileNameExtensions() {
		List<String> allExtens = new ArrayList<String>();

		for (Language lang : getRegister().values()) {
			allExtens.addAll(lang.getFilenameExtensions());
		}

		return allExtens;
	}

	private static void initializeUniversalEditorDescriptor(
			EditorRegistry editorRegistry)
	{
		final IEditorDescriptor[] allEditors = editorRegistry
				.getSortedEditorsFromPlugins();

		for (IEditorDescriptor editor : allEditors) {
			if (editor.getId().equals(UniversalEditor.EDITOR_ID)) {
				universalEditor = editor;

				if (PreferenceCache.emitMessages) {
					RuntimePlugin.getInstance().writeInfoMsg(
							"Universal editor descriptor: " +
							 universalEditor.getId() + ":" +
							 universalEditor.getLabel());
				}
				return;
			}
		}

		if (universalEditor == null) {
			if (PreferenceCache.emitMessages) {
				RuntimePlugin.getInstance().writeErrorMsg(
					"IMP LanguageRegistry error in initializeUniversalEditorDescroptor():  unable to initialize Universal Editor");
			} else {
				ErrorHandler.reportError(
					"Unable to locate Universal Editor descriptor", null);
			}
		}
	}

	private static void register(Language language) {
		getRegister().put(language.getName().toLowerCase(), language);
		
		if (PreferenceCache.emitMessages) {
			RuntimePlugin.getInstance().writeInfoMsg(
					"Registered language description: " + language.getName());
		}
	}
	
	
	private static void setFullyInitialized() {
			isFullyInitialized = true;
	}
	
	private static boolean isFullyInitialized() {
			return isFullyInitialized;
	}

	
}
