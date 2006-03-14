package org.eclipse.uide.core;

import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorMapping;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.registry.EditorDescriptor;
import org.eclipse.ui.internal.registry.EditorRegistry;
import org.eclipse.ui.internal.registry.FileEditorMapping;
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
 * Registry for SAFARI language contributors.
 */
public class LanguageRegistry {
    private static Language sLanguages[];

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
	if (sLanguages == null)
	    findLanguages();
	String extension= "???";

        if (editorInput instanceof FileEditorInput) {
	    FileEditorInput fileEditorInput= (FileEditorInput) editorInput;
	    IFile file= fileEditorInput.getFile();

            extension= file.getFileExtension();
	    if (extension == null)
		return null;
	    for(int n= 0; n < sLanguages.length; n++) {
		if (sLanguages[n].hasExtension(extension)) {
		    LanguageValidator validator= sLanguages[n].getValidator();

                    if (validator != null) {
			if (validator.validate(file))
			    return sLanguages[n];
		    } else
			return sLanguages[n];
		}
	    }
	}
	ErrorHandler.reportError("No language support for text/source file of type '" + extension + "'.");
	return null;
    }

    public static void registerLanguages() {
	if (sLanguages == null)
	    findLanguages();

	// Parts of the following code suggested by comments on the following bug:
	//    https://bugs.eclipse.org/bugs/show_bug.cgi?id=27980
	// It uses internal platform classes, given that there is no API for
	// dynamically registering editors as of 3.1.

	if (false) {
	    EditorRegistry editorRegistry= (EditorRegistry) PlatformUI.getWorkbench().getEditorRegistry();
	    IFileEditorMapping[] currentMap= editorRegistry.getFileEditorMappings();
	    // HACK RMF 2/2/2005 - The following is totally bogus; we don't need to create an
	    // "external program" descriptor for our editor, but there are no ctors/factory
	    // methods available to do what we want...
	    EditorDescriptor universalEditor= EditorDescriptor.createForProgram(UniversalEditor.EDITOR_ID);

	    universalEditor.setOpenMode(EditorDescriptor.OPEN_INTERNAL);

	    FileEditorMapping[] newMap = new FileEditorMapping[currentMap.length + sLanguages.length];
	    for(int i= 0 ; i < currentMap.length; i++) {
		newMap[i]= (FileEditorMapping) currentMap[i];
	    }

	    for(int n= 0; n < sLanguages.length; n++) {
		String[] fileNameExtensions= sLanguages[n].getFilenameExtensions();
		for(int i= 0; i < fileNameExtensions.length; i++) {
		    FileEditorMapping newType = new FileEditorMapping(fileNameExtensions[i]);

		    newType.setDefaultEditor(universalEditor);
		    newMap[currentMap.length + n]= newType;
		}
	    }
	    editorRegistry.setFileEditorMappings(newMap);
	    editorRegistry.saveAssociations();
	}
    }

    /**
     * Initialize the registry. Discover all contributors to the languageDescription extension point.
     */
    static void findLanguages() {
	try {
	    IExtensionPoint extensionPoint= Platform.getExtensionRegistry().getExtensionPoint(RuntimePlugin.UIDE_RUNTIME, EXTENSION);

            if (extensionPoint == null) {
		ErrorHandler.reportError("Nonexistent extension point called \"" + RuntimePlugin.UIDE_RUNTIME + "." + EXTENSION);
                return;
	    }

            ArrayList langList= new ArrayList();
	    IConfigurationElement[] elements= extensionPoint.getConfigurationElements();

            if (elements != null) {
		for(int n= 0; n < elements.length; n++) {
		    IConfigurationElement element= elements[n];
		    Bundle bundle= Platform.getBundle(element.getDeclaringExtension().getNamespace());

                    if (bundle != null) {
			Language language= new Language(element);

                        langList.add(language);
		    }
		}
	    } else
		System.err.println("Warning: no languages defined.");
            sLanguages= (Language[]) langList.toArray(new Language[langList.size()]);
	} catch (Throwable e) {
	    ErrorHandler.reportError("SAFARI LanguageRegistry error", e);
	}
    }
}
