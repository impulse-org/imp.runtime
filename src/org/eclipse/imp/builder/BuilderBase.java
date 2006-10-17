/*
 * Created on Nov 1, 2005
 */
package org.eclipse.uide.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.uide.runtime.SAFARIPluginBase;

public abstract class SAFARIBuilderBase extends IncrementalProjectBuilder {
    /**
     * @return the plugin associated with this builder instance
     */
    protected abstract SAFARIPluginBase getPlugin();

    /**
     * @return true iff the given file is a source file that this builder should compile.
     */
    protected abstract boolean isSourceFile(IFile resource);

    /**
     * @return true iff the given file is a source file that this builder should scan
     * for dependencies, but not compile as a top-level compilation unit.<br>
     * <code>isNonRootSourceFile()</code> and <code>isSourceFile()</code> should never
     * return true for the same file.
     */
    protected abstract boolean isNonRootSourceFile(IFile resource);

    /**
     * @return true iff the given resource is an output folder
     */
    protected abstract boolean isOutputFolder(IResource resource);

    /**
     * Does whatever is necessary to "compile" the given "source file".
     * @param resource the "source file" to compile
     * @param monitor used to indicate progress in the UI
     */
    protected abstract void compile(IFile resource, IProgressMonitor monitor);

    /**
     * Collects compilation-unit dependencies for the given file, and records
     * them via calls to <code>fDependency.addDependency()</code>.
     */
    protected abstract void collectDependencies(IFile file);

    /**
     * @return the ID of the marker type to be used to indicate compiler errors
     */
    protected abstract String getErrorMarkerID();

    /**
     * @return the ID of the marker type to be used to indicate compiler warnings
     */
    protected abstract String getWarningMarkerID();

    /**
     * @return the ID of the marker type to be used to indicate compiler information
     * messages
     */
    protected abstract String getInfoMarkerID();

    private final ResourceVisitor fResourceVisitor= new ResourceVisitor();

    private final DeltaVisitor fDeltaVisitor= new DeltaVisitor();

    protected DependencyInfo fDependencyInfo;

    private final Collection/*<IFile>*/ fSourcesToCompile= new ArrayList();

    private final class DeltaVisitor implements IResourceDeltaVisitor {
        public boolean visit(IResourceDelta delta) throws CoreException {
            return processResource(delta.getResource());
        }
    }

    private class ResourceVisitor implements IResourceVisitor {
        public boolean visit(IResource res) throws CoreException {
            return processResource(res);
        }
    }

    private boolean processResource(IResource resource) {
        if (resource instanceof IFile) {
            IFile file= (IFile) resource;

            if (file.exists()) {
        	if (isSourceFile(file) || isNonRootSourceFile(file))
        	    fSourcesToCompile.add(file);
            }
            return false;
        } else if (isOutputFolder(resource))
            return false;
        return true;
    }

    protected DependencyInfo createDependencyInfo(IProject project) {
        return new DependencyInfo(project);
    }

    protected IProject[] build(int kind, Map args, IProgressMonitor monitor) {
        if (fDependencyInfo == null || kind == FULL_BUILD || kind == CLEAN_BUILD)
            fDependencyInfo= createDependencyInfo(getProject());

        try {
            fSourcesToCompile.clear();
            collectSourcesToCompile(monitor);
            clearDependencyInfoForChangedFiles();
            compileNecessarySources(monitor);
            fDependencyInfo.dump();
        } catch (CoreException e) {
            getPlugin().writeErrorMsg("Build failed: " + e.getMessage());
        }
        return new IProject[0];
    }

    protected void compileNecessarySources(IProgressMonitor monitor) {
        for(Iterator iter= fSourcesToCompile.iterator(); iter.hasNext(); ) {
            IFile srcFile= (IFile) iter.next();

            clearMarkersOn(srcFile);
            if (isSourceFile(srcFile))
        	compile(srcFile, monitor);
            else if (isNonRootSourceFile(srcFile)) // predicate is implied, but clearer this way
        	collectDependencies(srcFile);
        }
    }

    /**
     * Clears all problem markers (all markers whose type derives from IMarker.PROBLEM)
     * from the given file. A utility method for the use of derived builder classes.
     */
    protected void clearMarkersOn(IFile file) {
	try {
	    file.deleteMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
	} catch (CoreException e) {
	}
    }

    private void dumpSourceList(Collection/*<IFile>*/ sourcesToCompile) {
        for(Iterator iter= sourcesToCompile.iterator(); iter.hasNext(); ) {
            IFile srcFile= (IFile) iter.next();

            System.out.println("  " + srcFile.getFullPath());
        }
    }

    /**
     * Clears the dependency information maintained for all files marked as
     * having changed (i.e. in <code>fSourcesToCompile</code>).
     */
    private void clearDependencyInfoForChangedFiles() {
        for(Iterator iter= fSourcesToCompile.iterator(); iter.hasNext(); ) {
            IFile srcFile= (IFile) iter.next();

            fDependencyInfo.clearDependenciesOf(srcFile.getFullPath().toString());
        }
    }

    /**
     * Visits the project delta, if any, or the entire project, and determines the set
     * of files needed recompilation, and adds them to <code>fSourcesToCompile</code>.
     * @param monitor
     * @throws CoreException
     */
    private void collectSourcesToCompile(IProgressMonitor monitor) throws CoreException {
        IResourceDelta delta= getDelta(getProject());

        if (delta != null) {
            getPlugin().maybeWriteInfoMsg("==> Scanning resource delta for project '" + getProject().getName() + "'... <==");
            delta.accept(fDeltaVisitor);
            getPlugin().maybeWriteInfoMsg("Delta scan completed for project '" + getProject().getName() + "'...");
        } else {
            getPlugin().maybeWriteInfoMsg("==> Scanning for source files in project '" + getProject().getName() + "'... <==");
            getProject().accept(fResourceVisitor);
            getPlugin().maybeWriteInfoMsg("Source file scan completed for project '" + getProject().getName() + "'...");
        }
        collectChangeDependents();
    }

    private void collectChangeDependents() {
        Collection changeDependents= new ArrayList();

        System.out.println("Changed files:");
        dumpSourceList(fSourcesToCompile);
        scanSourceList(fSourcesToCompile, changeDependents);
        fSourcesToCompile.addAll(changeDependents);
        System.out.println("Changed files + dependents:");
        dumpSourceList(fSourcesToCompile);
    }

    private void scanSourceList(Collection srcList, Collection changeDependents) {
	for(Iterator iter= srcList.iterator(); iter.hasNext(); ) {
            IFile srcFile= (IFile) iter.next();
            Set/*<String path>*/ fileDependents= fDependencyInfo.getDependentsOf(srcFile.getFullPath().toString());

            if (fileDependents != null) {
                for(Iterator iterator= fileDependents.iterator(); iterator.hasNext(); ) {
                    String depPath= (String) iterator.next();
                    IFile depFile= getProject().getWorkspace().getRoot().getFile(new Path(depPath));

                    changeDependents.add(depFile);
                }
            }
        }
    }

    /**
     * Refreshes all resources in the entire project tree containing the given resource.
     * Crude but effective.
     */
    protected void doRefresh(final IResource resource) {
        new Thread() {
            public void run() {
        	try {
        	    resource.getProject().refreshLocal(IResource.DEPTH_INFINITE, null);
        	} catch (CoreException e) {
        	    e.printStackTrace();
        	}
            }
        }.start();
    }

    /**
     * @return the ID of the marker type for the given marker severity (one of
     * <code>IMarker.SEVERITY_*</code>). If the severity is unknown/invalid,
     * returns <code>getInfoMarkerID()</code>.
     */
    protected String getMarkerIDFor(int severity) {
	switch(severity) {
	    case IMarker.SEVERITY_ERROR: return getErrorMarkerID();
	    case IMarker.SEVERITY_WARNING: return getWarningMarkerID();
	    case IMarker.SEVERITY_INFO: return getInfoMarkerID();
	    default: return getInfoMarkerID();
	}
    }

    /**
     * Utility method to create a marker on the given resource using the given
     * information.
     * @param errorResource
     * @param startLine the line with which the error is associated
     * @param startChar the offset of the first character with which the error is associated
     * @param endChar the offset of the last character with which the error is associated
     * @param descrip a human-readable text message to appear in the "Problems View"
     * @param severity the message severity, one of <code>IMarker.SEVERITY_*</code>
     */
    protected void createMarker(IResource errorResource, int startLine, int startChar, int endChar, String descrip, int severity) {
        try {
        	// TODO:  Address this situation properly after demo
        	// Issue is resources that are templates and not in user's workspace
        	if (!errorResource.exists())
        		return;
        	
            IMarker m= errorResource.createMarker(getMarkerIDFor(severity));
    
            m.setAttribute(IMarker.SEVERITY, severity);
            m.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_NORMAL);
            m.setAttribute(IMarker.LINE_NUMBER, startLine);
            m.setAttribute(IMarker.MESSAGE, descrip);
            if (startChar >= 0)
        	m.setAttribute(IMarker.CHAR_START, startChar);
            if (endChar >= 0)
        	m.setAttribute(IMarker.CHAR_END, endChar);
        } catch (CoreException e) {
            getPlugin().writeErrorMsg("Unable to create marker: " + e.getMessage());
        }
    }
}
