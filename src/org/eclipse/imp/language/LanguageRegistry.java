package org.eclipse.uide.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IFileEditorMapping;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.registry.EditorDescriptor;
import org.eclipse.ui.internal.registry.EditorRegistry;
import org.eclipse.ui.internal.registry.FileEditorMapping;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.uide.editor.UniversalEditor;
import org.eclipse.uide.preferences.SAFARIPreferenceCache;
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
	IFile file= null;

        if (editorInput instanceof IFileEditorInput) {
	    IFileEditorInput fileEditorInput= (IFileEditorInput) editorInput;
	    file= fileEditorInput.getFile();

	    if (SAFARIPreferenceCache.emitMessages)
		RuntimePlugin.getInstance().writeInfoMsg("Determining language of file " + file.getFullPath().toString());
//	    else
//		ErrorHandler.reportError("Determining language of file " + file.getFullPath().toString());
            extension= file.getFileExtension();
	} else if (editorInput instanceof IPathEditorInput) {
	    IPathEditorInput pathInput= (IPathEditorInput) editorInput;
	    IPath path= pathInput.getPath();

	    extension= path.getFileExtension();
	    file= ResourcesPlugin.getWorkspace().getRoot().getFile(path);
	} else if (editorInput instanceof IStorageEditorInput) {
	    IStorageEditorInput storageEditorInput= (IStorageEditorInput) editorInput;
	    try {
		IPath path= storageEditorInput.getStorage().getFullPath();

		extension= path.getFileExtension();
	    } catch (CoreException e) {
		ErrorHandler.reportError("Determining language of editor input " + storageEditorInput.getName());
	    }
	}

        if (extension == null)
            return null;
        for(int n= 0; n < sLanguages.length; n++) {
            if (sLanguages[n].hasExtension(extension)) {
        	LanguageValidator validator= sLanguages[n].getValidator();

        	if (validator != null && file != null) {
        	    if (validator.validate(file))
        		return sLanguages[n];
        	} else
        	    return sLanguages[n];
            }
        }
        if (SAFARIPreferenceCache.emitMessages)
            RuntimePlugin.getInstance().writeInfoMsg("No language support for text/source file of type '" + extension + "'.");
        else
            ErrorHandler.reportError("No language support for text/source file of type '" + extension + "'.");
	return null;
    }

    public static List/*<Language>*/ getLanguages() {
	return Collections.unmodifiableList(Arrays.asList(sLanguages));
    }

    public static Language findLanguage(String languageName) {
	String lowerName= languageName.toLowerCase();
        for(int i= 0; i < sLanguages.length; i++) {
            if (sLanguages[i].getName().toLowerCase().equals(lowerName))
                return sLanguages[i];
        }
        return null;
    }

    public static void registerLanguages() {
	if (sLanguages == null)
	    findLanguages();

	if (SAFARIPreferenceCache.emitMessages)
	    RuntimePlugin.getInstance().writeInfoMsg("Looking for SAFARI language description extensions...");

	// The following uses internal platform classes, given that there is no
	// API for dynamically registering editors as of 3.1. See Bugzilla bug
	//   https://bugs.eclipse.org/bugs/show_bug.cgi?id=110602
	EditorRegistry editorRegistry= (EditorRegistry) PlatformUI.getWorkbench().getEditorRegistry();
	IFileEditorMapping[] currentMap= editorRegistry.getFileEditorMappings();
	IEditorDescriptor universalEditor= findUniversalEditorDescriptor(editorRegistry);

	if (universalEditor == null) {
	    ErrorHandler.logError("registerLanguages(): unable to proceed without universal editor descriptor.", null);
	    return;
	}

	List/*<String>*/ langExtens= collectAllLanguageFileNameExtensions();
	List/*<FileEditorMapping>*/ newMap= new ArrayList();

	// First, add only those mappings that don't already point to the universal editor
	for(int i= 0; i < currentMap.length; i++) {
	    final IEditorDescriptor defaultEditor= currentMap[i].getDefaultEditor();
	    if (defaultEditor == null || !defaultEditor.getId().equals(UniversalEditor.EDITOR_ID))
		newMap.add(currentMap[i]);
	}

	// Create editor mappings for all file-name extensions SAFARI has been configured to handle.
	for(Iterator extenIter= langExtens.iterator(); extenIter.hasNext(); ) {
	    String exten= (String) extenIter.next();
	    FileEditorMapping newMapping= new FileEditorMapping(exten);

	    newMapping.setDefaultEditor((EditorDescriptor) universalEditor);
	    newMap.add(newMapping);
	}
	editorRegistry.setFileEditorMappings((FileEditorMapping[]) newMap.toArray(new FileEditorMapping[newMap.size()]));
	editorRegistry.saveAssociations();
    }

    private static List/*<String>*/ collectAllLanguageFileNameExtensions() {
	List/*<String>*/ allExtens= new ArrayList();
	for(int i= 0; i < sLanguages.length; i++) {
	    Language lang= sLanguages[i];
	    String[] langExts= lang.getFilenameExtensions();
	    for(int e= 0; e < langExts.length; e++)
		allExtens.add(langExts[e]);
	}
	return allExtens;
    }

    private static IEditorDescriptor findUniversalEditorDescriptor(EditorRegistry editorRegistry) {
	final IEditorDescriptor[] allEditors= editorRegistry.getSortedEditorsFromPlugins();
	IEditorDescriptor universalEditor= null;

	for(int i= 0; i < allEditors.length; i++) {
	    IEditorDescriptor editor= allEditors[i];

	    if (editor.getId().equals(UniversalEditor.EDITOR_ID)) {
		universalEditor= editor;
		break;
	    }
	}
	if (universalEditor == null)
	    ErrorHandler.logError("Unable to locate universal editor descriptor", null);
	else if (SAFARIPreferenceCache.emitMessages)
	    RuntimePlugin.getInstance().writeInfoMsg("Universal editor descriptor: " + universalEditor.getId() + ":" + universalEditor.getLabel());
	else
	    System.out.println("Universal editor descriptor: " + universalEditor.getId() + ":" + universalEditor.getLabel());
	return universalEditor;
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

                        // Following is called just to get Language to parse the
                        // file-name extension attribute, and do some validation.
                        language.getFilenameExtensions();

                        if (SAFARIPreferenceCache.emitMessages)
                            RuntimePlugin.getInstance().writeInfoMsg("Found language description extension for " + language.getName());
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
