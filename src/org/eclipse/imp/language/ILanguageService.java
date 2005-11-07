package org.eclipse.uide.core;


/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 1998, 2004  All Rights Reserved
 */


/**
 * This interface is to be implemented by services to the Universal Editor.
 * 
 * @author Claffra
 * 
 */
public interface ILanguageService {
    /**
     * Sets the language for this service. Examples would be "Java", "C", "Pascal".
     * The language name has to be registered through the <tt>org.eclipse.uide.runtime.languageDescription</tt> extension point.
     * 
     * @param language the canonical name of the language this service is meant for
     */
	//public void setLanguage(String language);
}