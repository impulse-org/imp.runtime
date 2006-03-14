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
import org.eclipse.uide.runtime.SAFARIPluginBase;

public abstract class SAFARIBuilderBase extends IncrementalProjectBuilder {

    protected abstract SAFARIPluginBase getPlugin();

    /**
     * @return true iff the given file is a source file that this builder should compile.
     */
    protected abstract boolean isSourceFile(IFile resource);

    /**
     * @return true iff the given resource is an output folder
     */
    protected abstract boolean isOutputFolder(IResource resource);

    protected abstract void compile(IFile resource);

    protected abstract String getErrorMarkerID();

    protected abstract String getWarningMarkerID();

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

            if (isSourceFile(file) && file.exists()) {
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
        if (fDependencyInfo == null)
            fDependencyInfo= createDependencyInfo(getProject());

        try {
            fSourcesToCompile.clear();
            collectSourcesToCompile();
            clearDependencyInfoForChangedFiles();
            compileNecessarySources();
            fDependencyInfo.dump();
        } catch (CoreException e) {
            getPlugin().writeErrorMsg("Build failed: " + e.getMessage());
        }
        return new IProject[0];
    }

    protected void compileNecessarySources() {
        for(Iterator iter= fSourcesToCompile.iterator(); iter.hasNext(); ) {
            IFile srcFile= (IFile) iter.next();

            clearMarkersOn(srcFile);
            compile(srcFile);
        }
    }

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

    private void clearDependencyInfoForChangedFiles() {
        for(Iterator iter= fSourcesToCompile.iterator(); iter.hasNext(); ) {
            IFile srcFile= (IFile) iter.next();

            fDependencyInfo.clearDependenciesOf(srcFile.getFullPath().toString());
        }
    }

    private void collectSourcesToCompile() throws CoreException {
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
        for(Iterator iter= fSourcesToCompile.iterator(); iter.hasNext(); ) {
            IFile srcFile= (IFile) iter.next();
            Set/*<String path>*/ fileDependents= fDependencyInfo.getDependentsOf(srcFile.getFullPath().toString());

            if (fileDependents != null) {
                for(Iterator iterator= fileDependents.iterator(); iterator.hasNext(); ) {
                    String depPath= (String) iterator.next();
                    IFile depFile= getProject().getFile(depPath);

                    changeDependents.add(depFile);
                }
            }
        }
        fSourcesToCompile.addAll(changeDependents);
        System.out.println("Changed files + dependents:");
        dumpSourceList(fSourcesToCompile);
    }

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

    protected String getMarkerIDFor(int severity) {
	switch(severity) {
	    case IMarker.SEVERITY_ERROR: return getErrorMarkerID();
	    case IMarker.SEVERITY_WARNING: return getWarningMarkerID();
	    case IMarker.SEVERITY_INFO: return getInfoMarkerID();
	    default: return getInfoMarkerID();
	}
    }

    protected void createMarker(IResource errorResource, int startLine, int startChar, int endChar, String descrip, int severity) {
        try {
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
