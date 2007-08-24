package org.eclipse.imp.builder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

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
                "BuilderUtils.extractContentsToString(): file name is null or empty");
        }

        // Check that the inFile exists and can be read
        File inFile= new File(inFileName);

        if (!inFile.exists() || !inFile.canRead()) {
            throw new IllegalArgumentException(
                "BuilderUtils.extractContentsToString(): file does not exist or cannot be read " +
                "(name = " + inFileName + ")");
        }
        return getFileContents(inFile);
    }

    /**
     * @return the text contents of the given file as a String, without
     * translating line terminating characters.
     */
    public static String getFileContents(IFile file) throws CoreException {
	// Files know their length, so read the whole thing in one shot.
        File javaFile= new File(file.getLocation().toOSString());
        return getFileContents(javaFile);
    }

    public static String getFileContents(File file) {
	try {
            FileReader fileReader= new FileReader(file);
            int len= (int) file.length();
            char[] buf= new char[len];

            fileReader.read(buf, 0, len);
            return new String(buf);
        } catch(FileNotFoundException fnf) {
            System.err.println(fnf.getMessage());
            return "";
        } catch(IOException io) {
            System.err.println(io.getMessage());
            return "";
        }
    }

    /**
     * @return the text contents of the given Reader, without translating
     * line terminating characters.
     */
    public static String getFileContents(Reader reader) {
	// In this case we don't know the length in advance, so we have to
	// accumulate the reader's contents one buffer at a time.
	StringBuilder sb= new StringBuilder(4096);
	char[] buff= new char[4096];
	int len;

	while(true) {
	    try {
		len= reader.read(buff);
	    } catch (IOException e) {
		break;
	    }
	    if (len < 0)
		break;
	    sb.append(buff, 0, len);
	}
	return sb.toString();
    }
}
