/*
 * Created on Nov 1, 2005
 */
package org.eclipse.uide.core;

import java.util.Map;

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
import org.eclipse.uide.runtime.UIDEPluginBase;

public abstract class UIDEBuilderBase extends IncrementalProjectBuilder {

    protected abstract UIDEPluginBase getPlugin();

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

    protected IProject[] build(int kind, Map args, IProgressMonitor monitor) {
        try {
            if (kind == IncrementalProjectBuilder.FULL_BUILD) {
        	fullBuild(monitor);
            } else {
        	IResourceDelta delta= getDelta(getProject());

        	if (delta == null) {
        	    fullBuild(monitor);
        	} else {
        	    incrementalBuild(delta, monitor);
        	}
            }
        } catch (CoreException e) {
            getPlugin().writeErrorMsg("Build failed: " + e.getMessage());
        }
        return new IProject[0];
    }

    protected void clearMarkersOn(IFile file) {
	try {
	    file.deleteMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
	} catch (CoreException e) {
	}
    }

    private boolean processResource(IResource resource) {
	if (resource instanceof IFile) {
	    IFile file= (IFile) resource;

	    if (isSourceFile(file) && file.exists()) {
		clearMarkersOn(file);
		compile(file);
	    }
	    return false;
	} else if (isOutputFolder(resource))
	    return false;
	return true;
    }

    private void fullBuild(IProgressMonitor monitor) throws CoreException {
        getProject().accept(new IResourceVisitor() {
            public boolean visit(IResource resource) throws CoreException {
        	return processResource(resource);
            }
        });
    }

    private void incrementalBuild(IResourceDelta delta, IProgressMonitor monitor) throws CoreException {
        delta.accept(new IResourceDeltaVisitor() {
            public boolean visit(IResourceDelta delta) {
        	return processResource(delta.getResource());
            }
        });
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
