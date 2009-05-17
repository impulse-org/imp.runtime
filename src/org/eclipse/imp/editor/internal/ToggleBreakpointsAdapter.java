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

package org.eclipse.imp.editor.internal;

import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointListener;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTarget;
import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.imp.runtime.RuntimePlugin;
import org.eclipse.imp.services.IToggleBreakpointsHandler;
import org.eclipse.imp.smapi.LineMapBuilder;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Stan Sutton, suttons@us.ibm.com:
 * 
 * Modified constructor to take a UniversalEditor (presumably the one creating
 * the adapter and to save that editor and the filename extension of the source
 * file opened in that editor (the "original" source file).
 * 
 * The original filename extension is useful for methods that may need
 * the extension but that don't have access to the original file or to an
 * editor (or other IWorkbenchPart) from which to obtain the extension.
 * 
 * The editor that created the adapter may serve as a convenient replacement
 * for the IWorkbenchParts that are passed into the methods that control toggling.
 * The interface IToggleBreakpointsTarget requires that each of these methods
 * be passed an IWorkbenchPart, so we can't eliminate those parameters from
 * the method signatures in which they occur.  Another consideration is whether
 * the IWorkbenchParts that are passed into these methods are guaranteed to be
 * the same as the editor that created the adapter.  I guess that in general
 * the adapters might be created by things other than the parts on which they
 * may operate (consider that a single adapter should be able to handle multiple
 * parts).  However, I also suspect that in our usage the editors that create
 * the adapters are likely to be the only parts that get passed into these
 * methods (at least so far).  In any case, the editor is availble here for
 * future implementors of these methods to use or not as they see fit.
 */
public class ToggleBreakpointsAdapter implements IToggleBreakpointsTarget, IBreakpointListener {
	private final IToggleBreakpointsHandler fHandler;

	public ToggleBreakpointsAdapter(UniversalEditor editor, IToggleBreakpointsHandler handler) {
		fHandler= handler;

		DebugPlugin.getDefault().getBreakpointManager().addBreakpointListener(this);
	}

    public void toggleLineBreakpoints(IWorkbenchPart part, ISelection selection) throws CoreException {
    	if (selection instanceof ITextSelection) {
            ITextSelection textSel= (ITextSelection) selection;

            IEditorPart editorPart= (IEditorPart) part.getAdapter(IEditorPart.class);
            IFileEditorInput fileInput= (IFileEditorInput) editorPart.getEditorInput();
            final IFile origSrcFile= fileInput.getFile();
            final int lineNumber = textSel.getStartLine()+1;

            IWorkspaceRunnable wr= new IWorkspaceRunnable() {
                public void run(IProgressMonitor monitor) throws CoreException {
                	IMarker marker = findBreakpointMarker(origSrcFile, lineNumber);

                	if (marker != null) {
                	    // The following will delete the associated marker
                	    fHandler.clearLineBreakpoint(origSrcFile, lineNumber);
                	} else {
                	    // The following will create a marker as a side-effect
                	    fHandler.setLineBreakpoint(origSrcFile, lineNumber);
                	}
                }
            };
            try {
                ResourcesPlugin.getWorkspace().run(wr, null);
            } catch (CoreException e) {
                throw new DebugException(e.getStatus());
            }
        }
    }
    
    public static boolean validateLineNumber(IFile origSrcFile, Integer origSrcLineNumber) {
    	LineMapBuilder lmb= new LineMapBuilder(origSrcFile.getRawLocation().removeFileExtension().toString());
        Map lineMap= lmb.getLineMap();

        if (lineMap.containsKey(origSrcLineNumber))
        	return true;
        
        return false;
	}

	private IMarker findBreakpointMarker(IFile srcFile, int lineNumber) throws CoreException {
    	IMarker[] markers = srcFile.findMarkers(IBreakpoint.LINE_BREAKPOINT_MARKER, true, IResource.DEPTH_INFINITE);

    	for (int k = 0; k < markers.length; k++ ){
    		if (((Integer) markers[k].getAttribute(IMarker.LINE_NUMBER)).intValue() == lineNumber){
    			return markers[k];
    		}
    	}
    	return null;
    }

    public static  IFile javaFileForRootSourceFile(IFile rootSrcFile) {
    	IProject project = rootSrcFile.getProject();
    	String rootSrcName= rootSrcFile.getName();

        return project.getFile(rootSrcFile.getProjectRelativePath().removeLastSegments(1).append(
                rootSrcName.substring(0, rootSrcName.lastIndexOf('.')) + ".java"));
    }

    public boolean canToggleLineBreakpoints(IWorkbenchPart part, ISelection selection) {
        return true;
    }

    public void toggleMethodBreakpoints(IWorkbenchPart part, ISelection selection) throws CoreException {
    }

    public boolean canToggleMethodBreakpoints(IWorkbenchPart part, ISelection selection) {
        return false;
    }

    public void toggleWatchpoints(IWorkbenchPart part, ISelection selection) throws CoreException {
    }

    public boolean canToggleWatchpoints(IWorkbenchPart part, ISelection selection) {
        return false;
    }

	public void breakpointAdded(IBreakpoint breakpoint) { }

	public void breakpointRemoved(IBreakpoint breakpoint, IMarkerDelta delta) { }

	public void breakpointChanged(IBreakpoint breakpoint, IMarkerDelta delta) { }

	// MV -- This method is called from smapifier to reset the breakpoint in the
	// Java file when a new build has been done.
	// RMF 5/9/2009 - doesn't seem to be true (at least for LEG) - this method is
	// never called, and yet the breakpoint markers' line #'s get updated properly.
	public static String getTypeName(IFile origSrcFile) {
		IProject project = origSrcFile.getProject();
		IJavaProject javaProj= JavaCore.create(project);
        
        String pathPrefix = project.getWorkspace().getRoot().getRawLocation() + project.getFullPath().toString();
        IPath projPath= project.getFullPath();
        //MV Note: javaProj.getOutputLocation returns a workspace relative path
		boolean projectIsSrcBin;

		try {
			projectIsSrcBin = (javaProj.getOutputLocation().matchingFirstSegments(projPath) == projPath.segmentCount()) && 
									 (javaProj.getOutputLocation().segmentCount() == projPath.segmentCount());
			if (!projectIsSrcBin) {
				String temp = origSrcFile.getRawLocation().toString().substring(pathPrefix.length()).substring(1);
				pathPrefix = pathPrefix + "/" + temp.substring(0,temp.indexOf("/"));
			}
		} catch (JavaModelException e) {
		    RuntimePlugin.getInstance().logException("Error determining path of file " + origSrcFile.getFullPath(), e);
		}
		
        String temp = origSrcFile.getRawLocation().toString().substring(pathPrefix.length()).replaceAll("/", ".");
        return temp.substring(1,temp.lastIndexOf("."));
	}
}
