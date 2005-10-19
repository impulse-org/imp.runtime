package org.eclipse.uide.core;

import java.util.StringTokenizer;

import org.eclipse.core.runtime.IConfigurationElement;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 2005  All Rights Reserved
 */

/**
 * @author Claffra
 *
 * API representation for org.eclipse.uide.core.languageDescription contributor.
 * Used by org.eclipse.uide.core.LanguageRegistry to discover and manage a language registry.
 */
public class Language {
    protected IConfigurationElement element;
    private String extensions[];
    private String synonyms[];
    /** 
     * Creates a language
     * @param element the extension point contribution this language belongs to
     */
    Language(IConfigurationElement element) {
        this.element = element;
    }
    /**
     * Gets the canonical name for this language
     * @return the canonical language name
     */
    public String getName() {
        return element.getAttribute("language");
    }
    /**
     * Gets a human-readable description of the language. Not used for discovery.
     * @return a description for this language
     */
    public String getDescription() {
        return element.getAttribute("description");
    }
    /**
     * Returns the langugage this language is derived from. For example, HTML is derived from XML.
     * @return the canonical language name this language is derived from
     */
    public String getDerivedFrom() {
        return element.getAttribute("derivedFrom");
    }
    /** 
     * Returns the website with more information about this language
     * @return something like http://www.php.org
     */
    public String getUrl() {
        return element.getAttribute("url");
    }
    /**
     * Returns whether this language supports a given file extension.
     * If the language is "Java", hasExtension("java") yields true.
     * @param extension the file extension (excluding the '.')
     * @return
     */
    public boolean hasExtension(String extension) {
        getExtensions();
        for (int n=0; n<extensions.length; n++) {
            //System.out.println("compare "+extension+" with '"+extensions[n]+"'");
            if (extension.equals(extensions[n]))
                return true;
        }
        return false;
    }
    /**
     * Returns the file extensions that are customary for this language.
     * @return comma-separated list of file extensions (excluding '.'). Example: "cpp,cxx,hpp,hxx".
     */
    public String[] getExtensions() {
        if (extensions == null)
            extensions = parseList(element.getAttribute("extensions"));
	    return extensions;
    }
    /**
     * Returns the synonyms for the canonical name of this language.
     * If the language is "Pascal", its synonyms could be "PASCAL,pascal"
     * @return comma-separated list of synonyms
     */
    public String[] getSynonyms() {
        if (synonyms == null)
            synonyms = parseList(element.getAttribute("synonyms"));
	    return synonyms;
    }
    /**
     * Returns an instance of a class that can validate a given IFile to ensure its contents is really for this language.
     * For instance, Ant build files have extension "xml". However, when opening any XML file, possible candidates
     * need to be consulted to verify the actual content before activating the corresponding language services.
     * @return the language validator instance, if declared by the contributor
     * @return null if no validator was specified
     */
    public LanguageValidator getValidator() {
        try {
            if (element.getAttribute("validator") == null)
                return null;
        	return (LanguageValidator)element.createExecutableExtension("validator");
        }
        catch (Throwable e) {
            return null;
        }
    }

    /*
     * Convert "cxx, cpp,hxx, hpp " into ["cxx", "cpp", "hxx", "hpp"]
     */
    protected String[] parseList(String list) {
	    int length = list.length();
	    int size = length > 0 ? 1 : 0;
	    for (int n=0; n<length; n++) 
	        if (list.charAt(n) == ',')
	            size++;
	    String result[] = new String[size];
	    StringTokenizer st = new StringTokenizer(list,",");
	    for (int n=0; st.hasMoreElements(); n++) 
	        result[n] = st.nextToken().trim(); 
	    return result;
	}
    
	public String toString() {
        return "Language[name="+getName()+",description="+getDescription()+",extensions="+element.getAttribute("extensions")+"]";
    }
}

