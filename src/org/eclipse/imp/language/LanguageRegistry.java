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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.imp.core.ErrorHandler;
import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.imp.preferences.PreferenceCache;
import org.eclipse.imp.runtime.RuntimePlugin;
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
import org.osgi.framework.Bundle;

/*
 * Licensed Materials - Property of IBM, (c) Copyright IBM Corp. 2005, 2008 All
 * Rights Reserved
 */

/**
 * @author Claffra
 * @author rfuhrer@watson.ibm.com
 * @author jurgen@vinju.org
 * 
 * Registry for IMP language contributors.
 */
@SuppressWarnings("restriction")
public class LanguageRegistry {
	private static Map<String, Language> fRegister;

	private static final String EXTENSION = "languageDescription";

	private static IEditorDescriptor universalEditor;

	private static EditorRegistry editorRegistry;

	/**
	 * Initialize the registry. Discover all contributors to the
	 * languageDescription extension point. The registry will not be fully
	 * initialized until the registerLanguages() method has been called.
	 */
	static {
		try {
			fRegister = new HashMap<String, Language>();

			editorRegistry = (EditorRegistry) PlatformUI.getWorkbench()
					.getEditorRegistry();
			initializeUniversalEditorDescriptor(editorRegistry);

			IExtensionPoint extensionPoint = Platform.getExtensionRegistry()
					.getExtensionPoint(RuntimePlugin.IMP_RUNTIME, EXTENSION);

			if (extensionPoint == null) {
				ErrorHandler
						.reportError("Nonexistent extension point called \""
								+ RuntimePlugin.IMP_RUNTIME + "." + EXTENSION);
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
			ErrorHandler.reportError("IMP LanguageRegistry error", e);
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
	public static Language findLanguage(IEditorInput editorInput) {
		Language result = null;

		if (editorInput instanceof IFileEditorInput) {
			IFileEditorInput fileEditorInput = (IFileEditorInput) editorInput;
			result = doFindLanguage(fileEditorInput);
		} else if (editorInput instanceof IPathEditorInput) {
			IPathEditorInput pathInput = (IPathEditorInput) editorInput;
			result = doFindLanguage(pathInput);
		} else if (editorInput instanceof IStorageEditorInput) {
			IStorageEditorInput storageEditorInput = (IStorageEditorInput) editorInput;
			result = doFindLanguage(storageEditorInput);
		} else {
			ErrorHandler.reportError("Unexpected type of IEditorInput: "
					+ editorInput.getClass().getName());
		}

		return result;
	}

	private static Language doFindLanguage(
			IStorageEditorInput storageEditorInput) {
		try {
			IPath path = storageEditorInput.getStorage().getFullPath();
			return findLanguage(path, null);
		} catch (CoreException e) {
			ErrorHandler.reportError("Determining language of editor input "
					+ storageEditorInput.getName());
			return null;
		}
	}

	private static Language doFindLanguage(IPathEditorInput pathInput) {
		IPath path;
		IFile file;
		path = pathInput.getPath();

		file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
		return findLanguage(path, file);
	}

	private static Language doFindLanguage(IFileEditorInput fileEditorInput) {
		IPath path;
		IFile file;
		file = fileEditorInput.getFile();

		if (PreferenceCache.emitMessages)
			RuntimePlugin.getInstance().writeInfoMsg(
					"Determining language of file "
							+ file.getFullPath().toString());
		path = file.getLocation();
		if (path == null) {
			path = file.getFullPath();
		}
		return findLanguage(path, file);
	}

	/**
	 * Determine the source language contained by the resource at the given
	 * path.
	 * 
	 * @param path
	 * @param file
	 *            if non-null, may be used to validate the contents of the file
	 *            (e.g. to distinguish dialects)
	 * @return
	 */
	public static Language findLanguage(IPath path, IFile file) {
		// TODO: use Eclipse content type instead
		String extension = path.getFileExtension();

		// N.B. It's ok for multiple language descriptors to specify the same
		// file name extension;
		// the associated validators should use the file contents to identify
		// the dialects.

		if (extension != null) {
			for (Language lang : fRegister.values()) {
				if (lang.hasExtension(extension)) {
					LanguageValidator validator = lang.getValidator();

					if (validator != null && file != null) {
						if (validator.validate(file)) {
							return lang;
						}
					} else {
						return lang;
					}
				}
			}
		}

		if (PreferenceCache.emitMessages) {
			RuntimePlugin.getInstance().writeInfoMsg(
					"No language support for text/source file of type '"
							+ extension + "'.");
		} else {
			ErrorHandler
					.reportError("No language support for text/source file of type '"
							+ extension + "'.");
		}

		return null;
	}

	public static Language findLanguageByNature(String natureID) {
		for (Language lang : fRegister.values()) {
			String aNatureID = lang.getNatureID();

			if (aNatureID != null && aNatureID.equals(natureID)) {
				return lang;
			}
		}
		return null;
	}

	public static Collection<Language> getLanguages() {
		return Collections.unmodifiableCollection(fRegister.values());
	}

	public static Language findLanguage(String languageName) {
		return fRegister.get(languageName.toLowerCase());
	}

	/**
	 * Registers a language dynamically (as opposed to using an extension
	 * point). This binds the file extensions associated with the language to
	 * the IMP Universal editor.
	 * 
	 * @param language
	 */
	public static void registerLanguage(Language language) {
		register(language);

		List<IFileEditorMapping> mappings = new ArrayList<IFileEditorMapping>();

		Collections.addAll(mappings, editorRegistry.getFileEditorMappings());
		addUniversalEditorMappings(language.getFilenameExtensions(), mappings);
		updateEditorRegistry(mappings);
	}

	/**
	 * Removes stale registrations and then registers all languages declared
	 * using the IMP languageDescription extension point. The file extensions of
	 * all registered languages are bound to the IMP universal editor.
	 * 
	 * This methods is called at earlyStartup time to initialize the registry.
	 */
	public static void registerLanguages() {
		if (PreferenceCache.emitMessages) {
			RuntimePlugin.getInstance().writeInfoMsg(
					"Looking for IMP language description extensions...");
		}

		List<String> langExtens = collectAllLanguageFileNameExtensions();
		List<IFileEditorMapping> newMap = new ArrayList<IFileEditorMapping>();
		
		addNonUniversalEditorMappings(newMap);
		addUniversalEditorMappings(langExtens, newMap);
		updateEditorRegistry(newMap);
	}

	private static void addNonUniversalEditorMappings(List<IFileEditorMapping> newMap) {
		for (IFileEditorMapping mapping : editorRegistry
				.getFileEditorMappings()) {
			IEditorDescriptor defaultEditor = mapping.getDefaultEditor();
			if (defaultEditor == null
					|| !defaultEditor.getId().equals(UniversalEditor.EDITOR_ID)) {
				newMap.add(mapping);
			}
		}
	}

	/**
	 * Adds new mappings to the unversial editor for a set of extensions
	 * @param extensions
	 * @param newMap
	 */
	private static void addUniversalEditorMappings(Iterable<String> extensions,
			List<IFileEditorMapping> newMap) {
		for (String ext : extensions) {
			FileEditorMapping newMapping = new FileEditorMapping(ext);
			newMapping.setDefaultEditor((EditorDescriptor) universalEditor);
			newMap.add(newMapping);
		}
	}

	/**
	 * Commits a new list of editor mappings to the editorRegistry
	 * @param newMap
	 */
	private static void updateEditorRegistry(List<IFileEditorMapping> newMap) {
		editorRegistry.setFileEditorMappings(newMap
				.toArray(new FileEditorMapping[newMap.size()]));
		editorRegistry.saveAssociations();
	}

	private static List<String> collectAllLanguageFileNameExtensions() {
		List<String> allExtens = new ArrayList<String>();

		for (Language lang : fRegister.values()) {
			allExtens.addAll(lang.getFilenameExtensions());
		}

		return allExtens;
	}

	private static void initializeUniversalEditorDescriptor(
			EditorRegistry editorRegistry) {
		final IEditorDescriptor[] allEditors = editorRegistry
				.getSortedEditorsFromPlugins();

		for (IEditorDescriptor editor : allEditors) {
			if (editor.getId().equals(UniversalEditor.EDITOR_ID)) {
				universalEditor = editor;

				if (PreferenceCache.emitMessages) {
					RuntimePlugin.getInstance().writeInfoMsg(
							"Universal editor descriptor: "
									+ universalEditor.getId() + ":"
									+ universalEditor.getLabel());
				}

				return;
			}
		}

		if (universalEditor == null) {
			ErrorHandler.logError(
					"Unable to locate universal editor descriptor", null);
		}
	}

	private static void register(Language language) {
		fRegister.put(language.getName().toLowerCase(), language);

		if (PreferenceCache.emitMessages) {
			RuntimePlugin.getInstance().writeInfoMsg(
					"Registered language description: " + language.getName());
		}
	}
}
