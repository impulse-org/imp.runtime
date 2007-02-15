package org.eclipse.uide.builder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class BuilderUtils {


    /**
     * Operates on a file with contents that have a textual representation
     * and returns the contents in the form of a String.
     * 
     * @param inFileName  Name of a file that contains some content suitable
     *                    for a String representation.  Used as given (whether
     *                    absolute or relative)
     */
    static public String extractContentsToString(String inFileName)
    {
        // Check the given file name
        if ((inFileName == null) || (inFileName.length() == 0)) {
            throw new IllegalArgumentException(
                "ReportExtractionUtilities.extractReportsToFiles:  inFileName is null or empty");
        }
        
        // Check that the inFile exists and can be read
        File inFile = new File(inFileName);
        if (!inFile.exists() || !inFile.canRead()) {
            throw new IllegalArgumentException(
                "BuilderUtils.extractContentsToString:  inFile does not exsit or cannot be read " +
                "(name = " + inFileName + ")");        	
        }
		
        // Now do the work ...
		
        // Get a buffered reader for the input file
        FileReader fileReader = null;
        BufferedReader bufferedReader = null;
        try {
            fileReader = new FileReader(inFile);
            bufferedReader = new BufferedReader(fileReader);
        } catch(FileNotFoundException e) {
            System.err.println("BuilderUtils.extractContentsToString:  inFile not found for reading; returning " +
                    "(name = " + inFileName + ")");	
            return null;
        }

        String contents = getTextFromBufferedReader(bufferedReader);
        if (contents.length() == 0) return null;
        return contents;
    }

    /**
     * Returns the contents of a BufferedReader in the form of a String
     * 
     * @param bufferedReader  A BufferedReader
     * @return                A String with the text in the Reader;
     *                        empty (not null) if no text is found
     */
    static public String getTextFromBufferedReader(BufferedReader bufferedReader)
    {
        String line1 = null;
        String result = null;
        try {
            while ((line1 = bufferedReader.readLine()) != null) {
                result = (result == null? line1 : result + line1);
                result = result + "\n";
            }
        } catch (IOException e) {
            System.err.println("BuilderUtils.getTextFromBufferedReader:  IOException getting text; returning what text there is");
            if (result == null) result = "";
        }

        return result;
    }
	
}
