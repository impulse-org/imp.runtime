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

package org.eclipse.imp.language;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.internal.registry.ExtensionRegistry;
import org.eclipse.core.runtime.*;
import org.eclipse.imp.core.ErrorHandler;
import org.eclipse.imp.editor.EditorInputUtils;
import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.imp.preferences.PreferenceCache;
import org.eclipse.imp.runtime.RuntimePlugin;
import org.eclipse.imp.services.IQuickFixAssistant;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorMapping;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.ide.IDEInternalPreferences;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.registry.MarkerHelpRegistry;
import org.eclipse.ui.internal.ide.registry.MarkerQuery;
import org.eclipse.ui.internal.ide.registry.MarkerQueryResult;
import org.eclipse.ui.internal.registry.EditorDescriptor;
import org.eclipse.ui.internal.registry.EditorRegistry;
import org.eclipse.ui.internal.registry.FileEditorMapping;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.osgi.framework.Bundle;

/**
 * @author Claffra
 * @author rfuhrer@watson.ibm.com
 * @author jurgen@vinju.org
 * @author Stan Sutton (suttons@us.ibm.com)
 * 
 * Registry for IMP language contributors.
 */
@SuppressWarnings("restriction")
public class LanguageRegistry {
	private static Object sStatusCheckMutex = new Object();
	private static boolean sIsFullyInitialized = false;
	
	private static Map<String, Language> sRegister;

	private static IEditorDescriptor sUniversalEditor;

	private static EditorRegistry sEditorRegistry;

	/*
	 * No one should instantiate this class.
	 * TODO:  Perhaps should be a singleton class.
	 */
	private LanguageRegistry() {}
	
	private static Map<String, Language> getRegister() {
		if(sRegister == null) {
			sRegister = new HashMap<String, Language>();
		}
		return sRegister;
	}

	/**
	 * Initialize the registry. Discover all contributors to the
	 * languageDescription extension point. The registry will not be fully
	 * initialized until the registerLanguages() method has been called.
	 */
	private static void preInitEditorRegistry() {
	 	try {
			sEditorRegistry = (EditorRegistry) PlatformUI.getWorkbench().getEditorRegistry();
			initializeUniversalEditorDescriptor(sEditorRegistry);
	
			IExtensionPoint extensionPoint =
			    Platform.getExtensionRegistry().getExtensionPoint(RuntimePlugin.IMP_RUNTIME, ServiceFactory.LANGUAGE_DESCRIPTION_POINT_ID);
	
			if (extensionPoint == null) {
				ErrorHandler.reportError("IMP language descriptor extension point '" +
				        (RuntimePlugin.IMP_RUNTIME + "." + ServiceFactory.LANGUAGE_DESCRIPTION_POINT_ID) +
				        "' non-existent?");
			} else {
				IConfigurationElement[] elements = extensionPoint.getConfigurationElements();
	
				if (elements != null) {
					for (IConfigurationElement element : elements) {
						Bundle bundle = Platform.getBundle(element.getDeclaringExtension().getNamespaceIdentifier());
	
						if (bundle != null) {
							register(new Language(element));
						}
					}
				}
			}
		} catch (InvalidRegistryObjectException e) {
			if (PreferenceCache.emitMessages) {
				RuntimePlugin.getInstance().logException("IMP LanguageRegistry error in preInitEditorRegistry()", e);
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
		if (!sIsFullyInitialized)
			initializeRegistryAsNeeded();
		IPath path= EditorInputUtils.getPath(editorInput);

		return findLanguage(path, docProvider.getDocument(editorInput));
	}

	/**
	 * Determine the source language contained by the resource at the given path.
	 * 
	 * @param path
	 * @param doc if non-null, may be used to validate the contents of the document
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
			RuntimePlugin.getInstance().writeErrorMsg("No language support for text/source file of type '" +
					 extension + "'.");
		} else {
			ErrorHandler.reportError("No language support for text/source file of type '" +
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
     * Registers a language dynamically, either programmatically, or via a bundle
     * being activated some time after startup. This binds the file extensions
     * associated with the language to the IMP Universal editor.
     * 
     * @param language
     */
    public static void registerLanguage(Language language) {
        PrintStream ps= RuntimePlugin.getInstance().getConsoleStream();
        ps.println("Dynamically registering language " + language.getName());
        if (!isFullyInitialized())
            initializeRegistryAsNeeded();
        register(language);
        updateEditorMappings(language);
    }

    private static void updateEditorMappings(Language language) {
        List<IFileEditorMapping> mappings = new ArrayList<IFileEditorMapping>();
        Collections.addAll(mappings, getEditorRegistry().getFileEditorMappings());
        addUniversalEditorMappings(language.getName(), language.getIconPath(), language.getFilenameExtensions(), language.getBundleID(), mappings);
        updateEditorRegistry(mappings);
    }

    /**
	 * Deregisters a language dynamically, either programmatically, or via a bundle
	 * being deactivated some time after startup.
	 * @param language
	 */
	public static void deregisterLanguage(Language language) {
        PrintStream ps= RuntimePlugin.getInstance().getConsoleStream();
        ps.println("Dynamically deregistering language " + language.getName());
		if (!isFullyInitialized())
			initializeRegistryAsNeeded();
		deregister(language);
		updateEditorMappings(language);
	}

	private static EditorRegistry getEditorRegistry() {
		return sEditorRegistry;
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
	 * To prevent thread access errors and other possible concurrency conflicts,
	 * this method executes within a synchronized block.  It both tests and sets
	 * isFullyInitialized within this block, as a means to assure that the registry
	 * will be initialized serially and only once.  Although isFullyInitialized
	 * can be tested from anywhere, this is the only place that it should be set.
	 */
	private static void initializeRegistryAsNeeded() {	
		synchronized(sStatusCheckMutex) {
			if(isFullyInitialized()) {
				return;
			}
			preInitEditorRegistry();
			

			if (PreferenceCache.emitMessages) {
				RuntimePlugin.getInstance().writeInfoMsg(
						"Looking for IMP language description extensions...");
			}

//			List<String> langExtens= collectAllLanguageFileNameExtensions();
			List<IFileEditorMapping> newMap = new ArrayList<IFileEditorMapping>();

            addNonUniversalEditorMappings(newMap);
			for(Language lang : getRegister().values()) {
	            addUniversalEditorMappings(lang.getName(), lang.getIconPath(), lang.getFilenameExtensions(), lang.getBundleID(), newMap);
            }
			
			updateEditorRegistry(newMap);
			setFullyInitialized();
			runLanguageRegistrars();
			

			// The following calls dynamically establish additional extensions for each
			// qualifying language in the registry, so the user doesn't have to write
			// extensions in their plugin's plugin.xml.
			updateMarkerResolutionRegistry();
		}
	}

	private static void runLanguageRegistrars() {
		try {
			IExtensionRegistry extRegistry= Platform.getExtensionRegistry();
			IExtensionPoint extensionPoint = extRegistry.getExtensionPoint(RuntimePlugin.IMP_RUNTIME, "languageRegistrar");
			IConfigurationElement[] elements = extensionPoint.getConfigurationElements();
			for (IConfigurationElement e : elements) {
				ILanguageRegistrar r = (ILanguageRegistrar) e.createExecutableExtension("class");
				r.registerLanguages();
			}
		}
		catch (ClassCastException e) {
			RuntimePlugin.getInstance().logException("starter extension does not implement ILanguageRegistrar", e);
		}
		catch (CoreException e) {
			RuntimePlugin.getInstance().logException("could not run starter extension", e);
		} 
		catch (Throwable e) {
			RuntimePlugin.getInstance().logException("exception while running starter extension", e);
		}
	}

	public static void startup() {
	    initializeRegistryAsNeeded();

	    IExtensionRegistry extRegistry= Platform.getExtensionRegistry();

	    extRegistry.addListener(new IRegistryEventListener() {
	        // TODO handle removal of a language description
	        public void removed(IExtensionPoint[] extensionPoints) { }

	        public void removed(IExtension[] extensions) {
                for(IExtension ext: extensions) {
                    deregisterLanguage(new Language(ext));
                }
	        }

	        public void added(IExtensionPoint[] extensionPoints) { }

	        public void added(IExtension[] extensions) {
	            for(IExtension ext: extensions) {
	                registerLanguage(new Language(ext));
	            }
	        }
	    }, "org.eclipse.imp.runtime.languageDescription");
	}

	private static void addNonUniversalEditorMappings(List<IFileEditorMapping> newMap) {
		for (IFileEditorMapping mapping : getEditorRegistry().getFileEditorMappings()) {
			IEditorDescriptor defaultEditor = mapping.getDefaultEditor();
			if (defaultEditor == null
					|| !defaultEditor.getId().equals(UniversalEditor.EDITOR_ID)) {
				newMap.add(mapping);
			}
		}
	}

    public static class BundleImageDescriptor extends ImageDescriptor {
        private final Bundle bundle;
        private final String iconPath;
        private final String langName;

        public BundleImageDescriptor(String iconPath, Bundle bundle, String langName) {
            this.langName= langName;
            this.bundle= bundle;
            this.iconPath= iconPath;
        }

        @Override
        public ImageData getImageData() {
            InputStream in = getStream();
            ImageData result = null;
            if (in != null) {
                try {
                    result = new ImageData(in);
                } catch (SWTException e) {
                    if (e.code != SWT.ERROR_INVALID_IMAGE) {
                        throw e;
                    // fall through otherwise
                    }
                } finally {
                    try {
                        in.close();
                    } catch (IOException e) {
                        //System.err.println(getClass().getName()+".getImageData(): "+
                        //  "Exception while closing InputStream : "+e);
                    }
                }
            }
            return result;
        }

        private InputStream getStream() {
            InputStream is = null;

            try {
                if (this.iconPath != null) {
                    is = bundle.getResource(iconPath).openStream();
                }
            } catch (IOException e) {
                RuntimePlugin.getInstance().logException("Unable to find icon for language " + langName, e);
                return null;
            }

            if (is == null) {
                return null;
            } else {
                return new BufferedInputStream(is);
            }
        }
    }

    private static class IMPFileEditorMapping extends FileEditorMapping {
        private ImageDescriptor fImageDescriptor;
        private IEditorDescriptor fEditor;

        public IMPFileEditorMapping(final String langName, String extension, final String iconPath, String bundleID) {
            super(extension);
            final Bundle bundle= Platform.getBundle(bundleID);
            fImageDescriptor= new BundleImageDescriptor(iconPath, bundle, langName);
        }

        @SuppressWarnings("unused")
        public void setTheDefaultEditor(IEditorDescriptor editor) {
            fEditor= editor;
        }

        @Override
        public IEditorDescriptor getDefaultEditor() {
            return fEditor;
        }

        @Override
        public ImageDescriptor getImageDescriptor() {
            return fImageDescriptor;
        }
    }

	/**
	 * Adds new mappings to the universal editor for a set of extensions
	 * @param extensions
	 * @param newMap
	 */
	private static void addUniversalEditorMappings(String langName, String langIcon,
	        Iterable<String> extensions, String bundleID, List<IFileEditorMapping> newMap) {
	    IFileEditorMapping[] mappings= sEditorRegistry.getFileEditorMappings();
	    // RMF 3/25/2009 - There doesn't seem to be a way to set the editor's label
	    // programmatically; (1) IEditorDescriptor doesn't expose enough API, (2) the
	    // EditorDescriptor ctor is not visible, (3) the class EditorDescriptor is final,
	    // (4) getEditorRegistry().setFileEditorMappings() will only accept an array
	    // of FileEditorMappings, and (5) FileEditorMapping requires an EditorDescriptor.

        for (String ext : extensions) {
	        IFileEditorMapping mapping= findMappingFor(ext, mappings);

	        if (mapping == null || (mapping != null && mapping.getDefaultEditor() != null && mapping.getDefaultEditor().getId().equals(sUniversalEditor.getId()))) {
	            // Replace the file editor mapping even if it already pointed to the universal editor,
	            // since the persisted association turns into a FileEditorMapping when re-read, thus
	            // losing the icon (which FileEditorMapping gets from the IEditorDescriptor).
	            mapping= new IMPFileEditorMapping(langName, ext, langIcon, bundleID);
	        }
            IEditorDescriptor defaultEditor= mapping.getDefaultEditor();
            FileEditorMapping fem= (FileEditorMapping) mapping;

            if (defaultEditor == null || defaultEditor.getId().equals("")) {
                fem.setDefaultEditor((EditorDescriptor) sUniversalEditor);
	        } else {
	        	// SMS 19 Nov 2008
	        	// Revised else branch according to patch provided by Edward Willink
	        	// Bug #242967, attachment id=109002
	        	boolean gotIt = false;
	        	for (IEditorDescriptor editor : fem.getEditors()) {
	        	    if (editor == sUniversalEditor) {
	        	    	gotIt = true;
	        	    	break;
	        	    }
	        	}
	        	if (!gotIt) {
	        	    // See whether there's an entry pointing to a UniversalEditor-derived class
	        	    for(IEditorDescriptor editor: fem.getEditors()) {
	        	        EditorDescriptor edDesc= (EditorDescriptor) editor;
	        	        String edClassName= edDesc.getClassName();
	        	        IConfigurationElement edElem= edDesc.getConfigurationElement();
	        	        Bundle edBundle= Platform.getBundle(edElem.getNamespaceIdentifier());

	        	        try {
                            Class<?> edClass= edBundle.loadClass(edClassName);
                            if (UniversalEditor.class.isAssignableFrom(edClass)) {
                                gotIt= true;
                                break;
                            }
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
	        	    }
	        	}
	        	if (!gotIt) {
	        		fem.addEditor((EditorDescriptor) sUniversalEditor);
	        	}
	        }
	        newMap.add(mapping);
	    }
	}

	@SuppressWarnings("unused")
    private void addEditorIfNeeded(IMPFileEditorMapping fem, EditorDescriptor editor) {
        // SMS 19 Nov 2008
        // Revised else branch according to patch provided by Edward Willink
        // Bug #242967, attachment id=109002
        boolean gotIt = false;
        for (IEditorDescriptor fileEditor : fem.getEditors()) {
            if (fileEditor.getId().equals(editor.getId())) {
                gotIt = true;
                break;
            }
        }
        if (!gotIt) {
            fem.addEditor(editor);
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
	
	private static void updateMarkerResolutionRegistry() {
		IExtensionRegistry reg = Platform.getExtensionRegistry();
		MarkerHelpRegistry markerHelpRegistry = (MarkerHelpRegistry) IDE
				.getMarkerHelpRegistry();
		for (Language lang : getLanguages()) {
			IQuickFixAssistant qfa = ServiceFactory.getInstance()
					.getQuickFixAssistant(lang);
			if (qfa != null) {
				for (String type : qfa.getSupportedMarkerTypes()) {
					String extension = ""
						+ "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
						+ "<?eclipse version=\"3.0\"?>"
						+ "<plugin>"
						+ "<extension"
						+ "	point=\"org.eclipse.ui.ide.markerResolution\">"
						+ " <markerResolutionGenerator"
						+ "   class=\"org.eclipse.imp.editor.internal.quickfix.MarkerResolutionGenerator\""
						+ "   markerType=\"" + type + "\">"
						+ " </markerResolutionGenerator>" + "</extension>"
						+ "</plugin>";

					InputStream is = new ByteArrayInputStream(extension.getBytes());
					Object ut = ((ExtensionRegistry) reg).getTemporaryUserToken();
					IContributor contributor = ContributorFactoryOSGi
					.createContributor(RuntimePlugin.getInstance()
							.getBundle());
					reg.addContribution(is, contributor, false, null, null, ut);
				}
			}
		}

		// The following makes the "light bulb" decorations appear on problem markers in the
		// Problems View. Without this, the decorations only show up after the view is refreshed.
		for (IConfigurationElement element : reg
				.getConfigurationElementsFor("org.eclipse.ui.ide.markerResolution")) {
			if (element
					.getAttribute("class")
					.equals(
							"org.eclipse.imp.editor.internal.quickfix.MarkerResolutionGenerator")) {
				// add query to the registry
				MarkerQuery query = new MarkerQuery(element
						.getAttribute("markerType"), new String[0]);
				MarkerQueryResult result = new MarkerQueryResult(new String[0]);
				markerHelpRegistry.addResolutionQuery(query, result, element);
			}
		}

		// force the problems view to refresh it's images
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				int limit = IDEWorkbenchPlugin.getDefault().getPreferenceStore().getInt(IDEInternalPreferences.MARKER_LIMITS_VALUE);
				IDEWorkbenchPlugin.getDefault().getPreferenceStore().setValue(IDEInternalPreferences.MARKER_LIMITS_VALUE, limit + 1);
				IDEWorkbenchPlugin.getDefault().getPreferenceStore().setValue(IDEInternalPreferences.MARKER_LIMITS_VALUE, limit);
			}
		});
	}

	@SuppressWarnings("unused")
    private static List<String> collectAllLanguageFileNameExtensions() {
		List<String> allExtens = new ArrayList<String>(getRegister().size());

		for (Language lang : getRegister().values()) {
			allExtens.addAll(lang.getFilenameExtensions());
		}

		return allExtens;
	}

	private static void initializeUniversalEditorDescriptor(EditorRegistry editorRegistry) {
		final IEditorDescriptor[] allEditors = editorRegistry.getSortedEditorsFromPlugins();

		for (IEditorDescriptor editor : allEditors) {
			if (editor.getId().equals(UniversalEditor.EDITOR_ID)) {
				sUniversalEditor = editor;

				if (PreferenceCache.emitMessages) {
					RuntimePlugin.getInstance().writeInfoMsg(
							"Universal editor descriptor: " +
							 sUniversalEditor.getId() + ":" +
							 sUniversalEditor.getLabel());
				}
				return;
			}
		}

		if (sUniversalEditor == null) {
			if (PreferenceCache.emitMessages) {
				RuntimePlugin.getInstance().writeErrorMsg(
					"IMP LanguageRegistry error in initializeUniversalEditorDescroptor(): unable to initialize UniversalEditor");
			} else {
				ErrorHandler.reportError(
					"Unable to locate Universal Editor descriptor", null);
			}
		}
	}

    private static void deregister(Language language) {
        getRegister().remove(language.getName().toLowerCase());
        
        if (PreferenceCache.emitMessages) {
            RuntimePlugin.getInstance().writeInfoMsg("Deregistered language description: " + language.getName());
        }
    }

    private static void register(Language language) {
		getRegister().put(language.getName().toLowerCase(), language);
		
		if (PreferenceCache.emitMessages) {
			RuntimePlugin.getInstance().writeInfoMsg("Registered language description: " + language.getName());
		}
	}

	private static void setFullyInitialized() {
	    sIsFullyInitialized = true;
	}
	
	private static boolean isFullyInitialized() {
	    return sIsFullyInitialized;
	}
}
