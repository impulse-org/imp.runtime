/*
 * Created on Mar 13, 2007
 */
package org.eclipse.uide.model.internal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import lpg.runtime.IMessageHandler;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.uide.core.ErrorHandler;
import org.eclipse.uide.core.ILanguageService;
import org.eclipse.uide.core.Language;
import org.eclipse.uide.core.LanguageRegistry;
import org.eclipse.uide.model.ICompilationUnit;
import org.eclipse.uide.model.ISourceProject;
import org.eclipse.uide.parser.IParseController;
import org.eclipse.uide.utils.ExtensionPointFactory;

public class CompilationUnitRef implements ICompilationUnit {
    /**
     * The containing ISourceProject. May be null if the associated path is workspace-absolute.
     */
    final ISourceProject fProject;

    /**
     * Path may be either workspace-absolute or project-relative. If workspace-absolute,
     * fProject will be null.
     */
    final IPath fPath; // possibly src-folder-relative?

    // TODO This should be created lazily
    private IParseController fParseCtrlr;

    /**
     * @param path may be either workspace-absolute or project-relative
     * @param proj may be null if path is workspace-absolute
     */
    public CompilationUnitRef(IPath path, ISourceProject proj) {
	fProject= proj;
	fPath= path;
    }

    /**
     * @return the associated ISourceProject. May be null if the unit's path is workspace-absolute.
     */
    public ISourceProject getProject() {
	return fProject;
    }

    /**
     * @return the associated IPath, which may be either workspace-absolute or project-relative
     */
    public IPath getPath() {
	return fPath;
    }

    public String getName() {
	if (fPath.isAbsolute())
	    return fPath.toPortableString();
	return fProject.getRawProject().getName() + ":" + fPath;
    }

    public IFile getFile() {
	if (fPath.getDevice() != null && fPath.getDevice().length() > 0)
	    return null; // This is a filesystem-absolute path; can't build an IFile for that
	if (fPath.isAbsolute())
	    return fProject.getRawProject().getWorkspace().getRoot().getFile(fPath);
	return fProject.getRawProject().getFile(fPath);
    }

    public Object getAST(IMessageHandler msgHandler, IProgressMonitor monitor) {
	IFile file= getFile();

//	if (file == null)
//	    return null;

	if (fParseCtrlr == null) {
	    Language lang= LanguageRegistry.findLanguage(fPath, file);

	    fParseCtrlr= (IParseController) ExtensionPointFactory.createExtensionPoint(lang, ILanguageService.PARSER_SERVICE);
	}
	fParseCtrlr.initialize(fPath, fProject, msgHandler);
	return fParseCtrlr.parse(getSource(), false, monitor);
    }

    public String getSource() {
	String absPath= (fPath.getDevice() != null) ? fPath.toOSString() :
	    (fPath.isAbsolute() ? ResourcesPlugin.getWorkspace().getRoot().getLocation().append(fPath).toOSString() :
		fProject.getRawProject().getLocation().append(fPath).toOSString());
        File inFile= new File(absPath);

        if (!inFile.exists() || !inFile.canRead()) {
            throw new IllegalArgumentException(
                "CompilationUnitRef.getSource(): file does not exist or cannot be read: " + this);
        }
		
        // Get a buffered reader for the input file
        FileReader fileReader = null;
        BufferedReader bufferedReader = null;
        try {
            fileReader = new FileReader(inFile);
            bufferedReader = new BufferedReader(fileReader);
        } catch(FileNotFoundException e) {
            ErrorHandler.reportError("CompilationUnitRef.getSource(): file not found: " + this);
            return null;
        }

        StringBuffer result = new StringBuffer();
        try {
            String line1 = null;
            while ((line1= bufferedReader.readLine()) != null) {
                result.append(line1).append("\n");
            }
        } catch (IOException e) {
            ErrorHandler.reportError("CompilationUnitRef.getSource:() IOException reading file; returning what text there is");
        }

        return result.toString();
    }

    public void commit(IProgressMonitor mon) {
	// do nothing
    }

    public String toString() {
	return "<compilation unit " + getName() + ">";
    }
}
