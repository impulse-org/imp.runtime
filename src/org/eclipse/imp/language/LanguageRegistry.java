package org.eclipse.uide.core;

import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.uide.editor.UniversalEditor;
import org.eclipse.uide.runtime.RuntimePlugin;
import org.osgi.framework.Bundle;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 2005  All Rights Reserved
 */

/**
 * @author Claffra
 * 
 * Registry for Universal IDE language contributors.
 */
public class LanguageRegistry {
    private static Language languages[];
    private static final String EXTENSION= "languageDescription";

    /**
     * Returns the language description for a given editor input. First the file extension is used to discover registered languages. Then each language is
     * used to ensure it actually supports the content of the file.
     * 
     * @param editorInput
     *                the editorInput to be opened
     * @return the contributed language description
     * @return null if no language is contributed with the given extension/content
     */
    public static Language findLanguage(IEditorInput editorInput) {
	if (languages == null)
	    findLanguages();
	String extension= "???";
	if (editorInput instanceof FileEditorInput) {
	    FileEditorInput fileEditorInput= (FileEditorInput) editorInput;
	    IFile file= fileEditorInput.getFile();
	    extension= file.getFileExtension();
	    for(int n= 0; n < languages.length; n++) {
		if (languages[n].hasExtension(extension)) {
		    LanguageValidator validator= languages[n].getValidator();
		    if (validator != null) {
			if (validator.validate(file))
			    return languages[n];
		    } else
			return languages[n];
		}
	    }
	}
	ErrorHandler.reportError("No language support for text/source file of type '" + extension + "'.");
	return null;
    }

    public static void registerLanguages() {
	if (languages == null)
	    findLanguages();

	IEditorRegistry editorRegistry= PlatformUI.getWorkbench().getEditorRegistry();

	for(int n= 0; n < languages.length; n++) {
	    String[] fileNameExtensions= languages[n].getExtensions();
	    for(int i= 0; i < fileNameExtensions.length; i++) {
		editorRegistry.setDefaultEditor("*." + fileNameExtensions[i], UniversalEditor.EDITOR_ID);
	    }
	}
    }

    /**
     * Initialize the registry. Discover all contributors to the languageDescription extension point.
     */
    static void findLanguages() {
	try {
	    IExtensionPoint extensionPoint= Platform.getExtensionRegistry().getExtensionPoint(RuntimePlugin.UIDE_RUNTIME, EXTENSION);
	    if (extensionPoint == null) {
		ErrorHandler.reportError("Nonexisting extension point called \"" + RuntimePlugin.UIDE_RUNTIME + "." + EXTENSION);
	    }
	    ArrayList list= new ArrayList();
	    IConfigurationElement[] elements= extensionPoint.getConfigurationElements();
	    if (elements != null) {
		for(int n= 0; n < elements.length; n++) {
		    IConfigurationElement element= elements[n];
		    Bundle bundle= Platform.getBundle(element.getDeclaringExtension().getNamespace());
		    if (bundle != null) {
			Language language= new Language(element);
			list.add(language);
		    }
		}
	    } else
		System.err.println("Warning: no languages defined.");
	    languages= (Language[]) list.toArray(new Language[list.size()]);
	} catch (Throwable e) {
	    ErrorHandler.reportError("Universal IDE LanguageRegistry Error", e);
	}
    }
}
