/*******************************************************************************
* Copyright (c) 2007 IBM Corporation.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*    Robert Fuhrer (rfuhrer@watson.ibm.com) - initial API and implementation

*******************************************************************************/

/*
 * Created on Nov 1, 2005
 */
package org.eclipse.imp.builder;

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
import org.eclipse.imp.runtime.PluginBase;
import org.eclipse.imp.runtime.RuntimePlugin;
import org.eclipse.imp.utils.UnimplementedError;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;

public abstract class BuilderBase extends IncrementalProjectBuilder {
    /**
     * @return the plugin associated with this builder instance
     */
    protected abstract PluginBase getPlugin();

    /**
     * @return the extension ID of this builder
     */
    public String getBuilderID() {
    	throw new UnimplementedError("Not implemented for builder for plug-in " + getPlugin().getID());
    }
    
    /**
     * @return true iff the given file is a source file that this builder should compile.
     */
    protected abstract boolean isSourceFile(IFile file);

    /**
     * @return true iff the given file is a source file that this builder should scan
     * for dependencies, but not compile as a top-level compilation unit.<br>
     * <code>isNonRootSourceFile()</code> and <code>isSourceFile()</code> should never
     * return true for the same file.
     */
    protected abstract boolean isNonRootSourceFile(IFile file);

    /**
     * @return true iff the given resource is an output folder
     */
    protected abstract boolean isOutputFolder(IResource resource);

    /**
     * Does whatever is necessary to "compile" the given "source file".
     * @param file the "source file" to compile
     * @param monitor used to indicate progress in the UI
     */
    protected abstract void compile(IFile file, IProgressMonitor monitor);

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

    private final Collection<IFile> fSourcesToCompile= new ArrayList<IFile>();

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
        for(Iterator<IFile> iter= fSourcesToCompile.iterator(); iter.hasNext(); ) {
            IFile srcFile= iter.next();

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
		// SMS 28 Mar 2007
		// Clear the markers for this builder only (and clear all of them)
		// (may be a simpler way to do this, given a more complex set up of
		// marker types)
	    //file.deleteMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
	    file.deleteMarkers(getErrorMarkerID(), true, IResource.DEPTH_INFINITE);
	    file.deleteMarkers(getWarningMarkerID(), true, IResource.DEPTH_INFINITE);
	    file.deleteMarkers(getInfoMarkerID(), true, IResource.DEPTH_INFINITE);
	} catch (CoreException e) {
	}
    }
    
    private void dumpSourceList(Collection<IFile> sourcesToCompile) {
        for(Iterator<IFile> iter= sourcesToCompile.iterator(); iter.hasNext(); ) {
            IFile srcFile= iter.next();

            System.out.println("  " + srcFile.getFullPath());
        }
    }

    /**
     * Clears the dependency information maintained for all files marked as
     * having changed (i.e. in <code>fSourcesToCompile</code>).
     */
    private void clearDependencyInfoForChangedFiles() {
        for(Iterator<IFile> iter= fSourcesToCompile.iterator(); iter.hasNext(); ) {
            IFile srcFile= iter.next();

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
        Collection<IFile> changeDependents= new ArrayList<IFile>();

        // TODO RMF 1/28/2008 - Should enable the following messages based on a debugging flag visible in a prefs page
//      System.out.println("Changed files:");
//      dumpSourceList(fSourcesToCompile);
        scanSourceList(fSourcesToCompile, changeDependents);
        fSourcesToCompile.addAll(changeDependents);
//      System.out.println("Changed files + dependents:");
//      dumpSourceList(fSourcesToCompile);
    }

    private void scanSourceList(Collection<IFile> srcList, Collection<IFile> changeDependents) {
	for(Iterator<IFile> iter= srcList.iterator(); iter.hasNext(); ) {
            IFile srcFile= iter.next();
            Set<String> fileDependents= fDependencyInfo.getDependentsOf(srcFile.getFullPath().toString());

            if (fileDependents != null) {
                for(Iterator<String> iterator= fileDependents.iterator(); iterator.hasNext(); ) {
                    String depPath= iterator.next();
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
     * @param charStart the offset of the first character with which the error is associated               
     * @param charEnd the offset of the last character with which the error is associated
     * @param message a human-readable text message to appear in the "Problems View"
     * @param severity the message severity, one of <code>IMarker.SEVERITY_*</code>
     */
    public void createMarker(IResource errorResource, int startLine, int charStart, int charEnd, String message, int severity) {
        try {
        	// TODO:  Address this situation properly after demo
        	// Issue is resources that are templates and not in user's workspace
        	if (!errorResource.exists())
        		return;
        	
            IMarker m = errorResource.createMarker(getMarkerIDFor(severity));

            String[] attributeNames = new String[] {IMarker.LINE_NUMBER, IMarker.MESSAGE, IMarker.PRIORITY, IMarker.SEVERITY};
            Object[] values = new Object[] {startLine, message, IMarker.PRIORITY_HIGH, severity};        
            m.setAttributes(attributeNames, values);
            
            if (charStart >= 0 && charEnd >= 0) {
            	attributeNames = new String[] {IMarker.CHAR_START, IMarker.CHAR_END};
            	values = new Object[] {charStart, charEnd};
                m.setAttributes(attributeNames, values);
            } else if (charStart >= 0) {
            	m.setAttribute(IMarker.CHAR_START, charStart);
            } else if (charEnd >= 0)
            	m.setAttribute(IMarker.CHAR_END, charEnd);
        } catch (CoreException e) {
            getPlugin().writeErrorMsg("Unable to create marker: " + e.getMessage());
        }
    }

    /**
     * Posts a dialog displaying the given message as soon as "conveniently possible".
     * This is not a synchronous call, since this method will get called from a
     * different thread than the UI thread, which is the only thread that can
     * post the dialog box.
     */
    protected void postMsgDialog(final String title, final String msg) {
        PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
            public void run() {
                Shell shell= RuntimePlugin.getInstance().getWorkbench().getActiveWorkbenchWindow().getShell();
    
                MessageDialog.openInformation(shell, title, msg);
            }
        });
    }

    /**
     * Posts a dialog displaying the given message as soon as "conveniently possible".
     * This is not a synchronous call, since this method will get called from a
     * different thread than the UI thread, which is the only thread that can
     * post the dialog box.
     */
    protected void postQuestionDialog(final String title, final String query, final Runnable runIfYes, final Runnable runIfNo) {
        PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
            public void run() {
                Shell shell= RuntimePlugin.getInstance().getWorkbench().getActiveWorkbenchWindow().getShell();
                boolean response= MessageDialog.openQuestion(shell, title, query);
    
                if (response)
                    runIfYes.run();
                else if (runIfNo != null)
                    runIfNo.run();
            }
        });
    }

    protected MessageConsole findConsole(String consoleName) {
        MessageConsole myConsole= null;
        final IConsoleManager consoleManager= ConsolePlugin.getDefault().getConsoleManager();
        IConsole[] consoles= consoleManager.getConsoles();
        for(int i= 0; i < consoles.length; i++) {
            IConsole console= consoles[i];
            if (console.getName().equals(consoleName))
                myConsole= (MessageConsole) console;
        }
        if (myConsole == null) {
            myConsole= new MessageConsole(consoleName, null);
            consoleManager.addConsoles(new IConsole[] { myConsole });
        }
        consoleManager.showConsoleView(myConsole);
        return myConsole;
    }
}
