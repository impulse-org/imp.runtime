package org.eclipse.uide.model;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.uide.core.ErrorHandler;
import org.eclipse.uide.core.Language;
import org.eclipse.uide.core.LanguageRegistry;

/**
 * A factory for implementations of the various common model interfaces, e.g., ISourceProject,
 * ICompilationUnit, and so on.
 * @author rfuhrer
 */
public class ModelFactory {
    public class ModelException extends Exception {
	private static final long serialVersionUID= -1051581958821533299L;

	public ModelException() {
	    super();
	}
	public ModelException(String message) {
	    super(message);
	}
	public ModelException(String message, Throwable cause) {
	    super(message, cause);
	}
	public ModelException(Throwable cause) {
	    super(cause);
	}
    }

    public static final String NO_SUCH_ELEMENT= "No such element: ";

    public static final String ELEMENT_ALREADY_EXISTS= "Element already exists: ";

    private static ModelFactory sInstance= new ModelFactory();

    public static ModelFactory getInstance() {
	return sInstance;
    }

    /**
     * Implementations of this interface, once registered, can augment the model entities
     * created by the factory before they are returned to the client.<br>
     * This provides a mechanism for language-specific customization of core model entity
     * properties. E.g, the project's search path can be augmented based on a global
     * language-specific preference setting.
     */
    // TODO Implementations of this interface should probably be provided via an extension point
    public interface IFactoryExtender {
	/**
	 * This method gets called for the extender corresponding to each language nature
	 * configured on the given project.
	 */
	void extend(ISourceProject project);

	/**
	 * This method gets called only for the extender of the "host language" contained
	 * within the given compilation unit (i.e. for the language to which the associated
	 * content type maps).
	 */
	void extend(ICompilationUnit unit);
    }

    private ModelFactory() {}

    private Map<IProject, ISourceProject> fProjectMap= new HashMap<IProject, ISourceProject>();

    private Map<Language, IFactoryExtender> fExtenderMap= new HashMap<Language, IFactoryExtender>();

    public void installExtender(IFactoryExtender extender, Language language) {
	fExtenderMap.put(language, extender);
    }

    public static ISourceProject open(IProject project) throws ModelException {
	return getInstance().doOpen(project);
    }

    private ISourceProject doOpen(IProject project) throws ModelException {
	if (!project.exists())
	    throw new ModelException(NO_SUCH_ELEMENT);

	ISourceProject sp= fProjectMap.get(project);

	if (sp == null) {
	    fProjectMap.put(project, sp= new SourceProject(project));

	    // Find each language nature configured on the underlying project, find the
	    // corresponding factory extender, and invoke it before returning the project.
	    try {
		String[] natures= project.getDescription().getNatureIds();

		for(int i= 0; i < natures.length; i++) {
		    String natureID= natures[i];
		    Language lang= LanguageRegistry.findLanguageByNature(natureID);

		    if (lang != null) {
			IFactoryExtender ext= fExtenderMap.get(lang);

			if (ext != null)
			    ext.extend(sp);
		    }
		}
	    } catch (CoreException e) {
		ErrorHandler.reportError(e.getMessage());
	    }
	}
	return sp;
    }

    public static ISourceProject create(IProject project) throws ModelException {
	return getInstance().doCreate(project);
    }

    private ISourceProject doCreate(IProject project) throws ModelException {
	throw new ModelException(ELEMENT_ALREADY_EXISTS);
    }

    public static ICompilationUnit open(IPath path, ISourceProject srcProject) {
	return getInstance().doOpen(path, srcProject);
    }

    private ICompilationUnit doOpen(IPath path, ISourceProject srcProject) {
	ICompilationUnit unit;
	IPath resolvedPath;

	if (path.isAbsolute())
	    unit= new CompilationUnitRef(resolvedPath= path, null);
	else {
	    resolvedPath= srcProject.resolvePath(path);

	    if (resolvedPath == null)
		return null;
	    unit= new CompilationUnitRef(resolvedPath, srcProject);
	}

	// Determine the language of this compilation unit, find the corresponding
	// factory extender, and invoke it before returning the compilation unit.
	Language lang= LanguageRegistry.findLanguage(resolvedPath, null);
	IFactoryExtender ext= fExtenderMap.get(lang);

	if (ext != null)
	    ext.extend(unit);

	return unit;
    }

    public static ICompilationUnit open(IFile file, ISourceProject srcProject) {
	return getInstance().doOpen(file, srcProject);
    }

    private ICompilationUnit doOpen(IFile file, ISourceProject srcProject) {
	if (!file.exists())
	    return null;

	ICompilationUnit unit= new CompilationUnitRef(file.getFullPath(), srcProject);

	// Determine the language of this compilation unit, find the corresponding
	// factory extender, and invoke it before returning the compilation unit.
	Language lang= LanguageRegistry.findLanguage(file.getLocation(), file);
	IFactoryExtender ext= fExtenderMap.get(lang);

	if (ext != null)
	    ext.extend(unit);

	return unit;
    }

    /**
     * 
     * @param projRelPath
     * @param srcProject
     * @return never returns null
     */
    public static ICompilationUnit create(IPath projRelPath, ISourceProject srcProject) throws ModelException {
	return getInstance().doCreate(projRelPath, srcProject);
    }

    private ICompilationUnit doCreate(IPath projRelPath, ISourceProject srcProject) throws ModelException {
	throw new ModelException(ELEMENT_ALREADY_EXISTS);
    }

    /**
     * @param file
     * @param srcProject
     * @return the new ICompilationUnit corresponding to the given file
     */
    public static ICompilationUnit create(IFile file, ISourceProject srcProject) throws ModelException {
	return getInstance().doCreate(file, srcProject);
    }

    private ICompilationUnit doCreate(IFile file, ISourceProject srcProject) throws ModelException {
	return doCreate(file.getProjectRelativePath(), srcProject);
    }
}
