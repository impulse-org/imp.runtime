package org.eclipse.uide.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.core.runtime.IPath;
import org.eclipse.uide.runtime.RuntimePlugin;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 2005  All Rights Reserved
 */

/**
 * @author Claffra
 *
 * TODO add documentation
 */
public class Index implements ILanguageService {
    
	protected String language;

    /* (non-Javadoc)
	 * @see org.eclipse.uide.core.ILanguageService#setLanguage(java.lang.String)
	 */
	public void setLanguage(String language) {
	    this.language = language;
	}
	
	/**
     * Creates an index for the shared elements in this language  
     */
    public Index() {
    }
    
    /**
     * Opens the index file for reading.
     * The index file is stored in the plug-in's state location.
     * If this is the first time the index is opened, it will be created.
     * The inputstream has to be closed after usage.
     * 
     * @return an inputstream containing the index file
     */
    public InputStream load() {
        InputStream is = null;
        try {
	        File file = getPersistentIndexFile();
	        is = new FileInputStream(file);
        }
        catch (IOException e) {
            ErrorHandler.reportError("Cannot open persistent index for "+language);
        }
        return is;
    }
    
	/**
	 * Open the index file for saving. The output stream should be closed after usage.
	 * @return an outputStream the user can write to.
	 */    
    public OutputStream save() {
        OutputStream os = null;
        try {
	        File file = getPersistentIndexFile();
	        os = new FileOutputStream(file);
        }
        catch (IOException e) {
            ErrorHandler.reportError("Cannot open persistent index for "+language);
        }
        return os;
    }

    /**
     * Returns the index file.
     * The index file is stored in the plug-in's state location.
     * If this is the first time the index is opened, it will be created.
     * @return the file where the persistent index is stored
     * @return null when the file cannot be created
     */
    private File getPersistentIndexFile() {
        try {
            IPath path = RuntimePlugin.getDefault().getStateLocation();
	        File file = new File(path.toFile(), language+".index");
	        if (!file.exists()) {
	            file.createNewFile();
	        }
	        return file;
        }
        catch (IOException e) {
            ErrorHandler.reportError("Cannot locate index file for "+language, e);
            return null;
        }
    }
}
