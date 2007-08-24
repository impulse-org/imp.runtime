package org.eclipse.imp.language;

import java.util.StringTokenizer;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.imp.core.ErrorHandler;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 2005  All Rights Reserved
 */

/**
 * @author Claffra
 * 
 * API representation for org.eclipse.imp.runtime.languageDescription contributor. Used by
 * org.eclipse.imp.core.LanguageRegistry to discover and manage a language registry.
 */
public class Language {
    /**
     * Extension element attribute ID for the language ID associated with a given
     * language descriptor.
     */
    public static final String LANGUAGE_ID_ATTR= "language";

    /**
     * Extension element attribute ID for the base language ID associated with a
     * given language descriptor.
     */
    public static final String DERIVED_FROM_ATTR= "derivedFrom";

    /**
     * Extension element attribute ID for the list of file-name extensions associated
     * with a given language descriptor.
     */
    public static final String EXTENSIONS_ATTR= "extensions";

    /**
     * Extension element attribute ID for the validator associated with a given
     * language descriptor.
     */
    public static final String VALIDATOR_ATTR= "validator";

    /**
     * Extension element attribute ID for the user-readable description of a given
     * language descriptor.
     */
    public static final String DESCRIPTION_ATTR= "description";

    /**
     * Extension element attribute ID for the optional nature ID associated with a
     * given language descriptor.
     */
    public static final String NATURE_ID_ATTR= "natureID";

    /**
     * Extension element attribute ID for the optional list of synonyms associated with a
     * given language descriptor.
     */
    public static final String SYNONYMS_ATTR= "synonyms";

    /**
     * Extension element attribute ID for the optional URL associated with a given
     * language descriptor.
     */
    public static final String URL_ATTR= "url";

    protected IConfigurationElement fConfigElement;

    private String fFilenameExtensions[];

    private String fSynonyms[];

    /**
     * Creates a language
     * 
     * @param element
     *            the extension point contribution this language belongs to
     */
    Language(IConfigurationElement element) {
        this.fConfigElement= element;
    }

    /**
     * Gets the canonical name for this language
     * 
     * @return the canonical language name
     */
    public String getName() {
        return fConfigElement.getAttribute(LANGUAGE_ID_ATTR);
    }

    public String getNatureID() {
	return fConfigElement.getAttribute(NATURE_ID_ATTR);
    }

    /**
     * Gets a human-readable description of the language. Not used for discovery.
     * 
     * @return a description for this language
     */
    public String getDescription() {
        return fConfigElement.getAttribute(DESCRIPTION_ATTR);
    }

    /**
     * Returns the language this language is derived from. For example, HTML is derived from XML.
     * 
     * @return the canonical language name this language is derived from
     */
    public String getDerivedFrom() {
        return fConfigElement.getAttribute(DERIVED_FROM_ATTR);
    }

    /**
     * Returns the website with more information about this language
     * 
     * @return something like http://www.php.org
     */
    public String getUrl() {
        return fConfigElement.getAttribute(URL_ATTR);
    }

    /**
     * Returns whether this language supports a given file extension. If the language is "Java", hasExtension("java")
     * yields true.
     * 
     * @param extension
     *            the file extension (excluding the '.')
     * @return
     */
    public boolean hasExtension(String extension) {
        getFilenameExtensions();
        for(int n= 0; n < fFilenameExtensions.length; n++) {
            // System.out.println("compare "+extension+" with '"+fFilenameExtensions[n]+"'");
            if (extension.equals(fFilenameExtensions[n]))
                return true;
        }
        return false;
    }

    /**
     * Returns the file fFilenameExtensions that are customary for this language.
     * 
     * @return comma-separated list of file filename extensions (excluding '.'). Example: "cpp,cxx,hpp,hxx".
     */
    public String[] getFilenameExtensions() {
        if (fFilenameExtensions == null)
            fFilenameExtensions= parseList(fConfigElement.getAttribute(EXTENSIONS_ATTR));
        return fFilenameExtensions;
    }

    /**
     * Returns the synonyms for the canonical name of this language. If the language is "Pascal", its synonyms could be
     * "PASCAL,pascal"
     * 
     * @return comma-separated list of synonyms
     */
    public String[] getSynonyms() {
        if (fSynonyms == null)
            fSynonyms= parseList(fConfigElement.getAttribute(SYNONYMS_ATTR));
        return fSynonyms;
    }

    /**
     * A convenience method for LanguageRegistry.findLanguage(lang.getDerivedFrom()).
     * @return the base Language, if known to the LanguageRegistry; otherwise, null
     */
    public Language getBaseLanguage() {
        String baseLangName= getDerivedFrom();
        if (baseLangName != null)
            return LanguageRegistry.findLanguage(baseLangName);
        return null;
    }

    /**
     * Returns an instance of a class that can validate a given IFile to ensure its contents is really for this
     * language. For instance, Ant build files have extension "xml". However, when opening any XML file, possible
     * candidates need to be consulted to verify the actual content before activating the corresponding language
     * services.
     * 
     * @return the language validator instance, if declared by the contributor
     * @return null if no validator was specified
     */
    public LanguageValidator getValidator() {
        try {
            if (fConfigElement.getAttribute(VALIDATOR_ATTR) == null)
                return null;
            return (LanguageValidator) fConfigElement.createExecutableExtension(VALIDATOR_ATTR);
        } catch (Throwable e) {
            return null;
        }
    }

    /*
     * Convert "cxx, cpp,hxx, hpp " into ["cxx", "cpp", "hxx", "hpp"]
     */
    protected String[] parseList(String list) {
        int length= list.length();
        int size= length > 0 ? 1 : 0;

        for(int n= 0; n < length; n++)
            if (list.charAt(n) == ',')
                size++;

        String result[]= new String[size];
        StringTokenizer st= new StringTokenizer(list, ",");

        for(int n= 0; st.hasMoreElements(); n++) {
            String exten= st.nextToken().trim();

            if (exten.startsWith(".")) {
        	ErrorHandler.logMessage("Ignoring leading '.' in file-name extension " + exten + " for language '" + getName() + "'.", null);
        	exten= exten.substring(1);
            }
            result[n]= exten;
        }
        return result;
    }

    public String toString() {
        return "Language[name=" + getName() + ",description=" + getDescription() + ",filename extensions=" + fConfigElement.getAttribute(EXTENSIONS_ATTR) + "]";
    }
}
