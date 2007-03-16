/*
 * Created on Mar 13, 2007
 */
package org.eclipse.uide.model;

import java.util.List;

import lpg.runtime.IMessageHandler;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.uide.core.ILanguageService;
import org.eclipse.uide.core.Language;
import org.eclipse.uide.core.LanguageRegistry;
import org.eclipse.uide.parser.IParseController;
import org.eclipse.uide.utils.ExtensionPointFactory;

public class CompilationUnitRef implements ICompilationUnit {
    final ISourceProject fProject;

    final IPath fPath; // possibly src-folder-relative?

    private final IParseController fParseCtrlr;

    public CompilationUnitRef(IProject proj, IPath path) {
	fProject= new SourceProject(proj);
	fPath= path;
	IWorkspace ws= ResourcesPlugin.getWorkspace();
	Language lang= LanguageRegistry.findLanguage(new FileEditorInput(ws.getRoot().getFile(fPath)));
	fParseCtrlr= (IParseController) ExtensionPointFactory.createExtensionPoint(lang, ILanguageService.PARSER_SERVICE);
    }

    public ISourceProject getProject() {
	return fProject;
    }

    public IPath getPath() {
	return fPath;
    }

    public String getName() {
	if (fPath.isAbsolute())
	    return fPath.toPortableString();
	return fProject.getRawProject().getName() + ":" + fPath;
    }

    public IFile findFile() {
	List<IPathEntry> buildPath= fProject.getBuildPath();
	IProject rawProject= fProject.getRawProject();
	for(IPathEntry path : buildPath) {
	    IFile f= rawProject.getFile(path.getProjectRelativePath());
	    if (f.exists()) {
		return f;
	    }
	}
	return null;
    }

    public Object getAST(IMessageHandler msgHandler, IProgressMonitor monitor) {
	IFile file= findFile();
	fParseCtrlr.initialize(file.getProjectRelativePath(), fProject.getRawProject(), msgHandler);
	return fParseCtrlr.parse(getSource(), false, monitor);
    }

    protected String getSource() {
	return "";
    }

    public String toString() {
	return "<compilation unit " + getName() + ">";
    }
}
