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
    //private static Map /* IJavaLineBreakPoint -> IMarker */bkptToSrcMarkerMap= new HashMap();

	private final String origExten;

	private final UniversalEditor fEditor; // Editor that created this adapter

	private final IToggleBreakpointsHandler fHandler;

    // SMS 14 Mar 2007
    // New constructor to take a UniversalEditor as a parameter.  This allows
	// the field origExten to be initialized with the proper extension for the	
	// original source file.  We can also save the editor in a new field, in case
	// it may be useful (see class 	header comments)
	public ToggleBreakpointsAdapter(UniversalEditor editor, IToggleBreakpointsHandler handler) {
		fEditor = editor;
		fHandler = handler;
		// SMS 19 Apr 2007:
		// fLanguage may be null, especially if language is no longer
		// available in workspace, so check for that	
		if (editor.fLanguage != null) {
		    final String name= editor.getEditorInput().getName();
		    origExten = name.substring(name.lastIndexOf('.'));
//			origExten = editor.fLanguage.getFilenameExtensions().iterator().next();
		} else {
			// And, if null, then the "original extension" should
			// be meaningless (since that would only be defined and
			// used with respect to a language).  Setting it to null
			// (which it may already be) doesn't seem to cause any
			// problems.
			origExten = null;
		}
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
                		try {
                			fHandler.clearLineBreakpoint(origSrcFile, lineNumber, marker);
                			marker.delete();
                		} catch (Exception e) {
                			RuntimePlugin.getInstance().logException(e.getMessage(), e);
                		}
                	} else {
                		try {
                			marker = origSrcFile.createMarker(IBreakpoint.LINE_BREAKPOINT_MARKER);
                			marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
                			marker.setAttribute(IBreakpoint.ENABLED, true);
                			fHandler.setLineBreakpoint(origSrcFile, lineNumber, marker);
                		} catch (Exception e) {
                			RuntimePlugin.getInstance().logException(e.getMessage(), e);
                			marker.delete();
                		}
                	}

//                    // At this point, we know there is no existing breakpoint at this line #.
//
//                    Map<String,String> bkptAttributes= new HashMap<String, String>();
//                    bkptAttributes.put("org.eclipse.jdt.debug.core.sourceName", typeName);
//                    final IBreakpoint bkpt= JDIDebugModel.createLineBreakpoint(javaFile, typeName, origSrcLineNumber.intValue(), -1, -1, 0, true,
//                          bkptAttributes);
//                    bkptAttributes.put("org.eclipse.jdt.debug.core.sourceName", origSrcFileName);
//                    final IBreakpoint bkpt= JDIDebugModel.createStratumBreakpoint(origSrcFile , "x10", origSrcFile.getName(), /*origSrcFile.getFullPath().toString()*/null, null, origSrcLineNumber.intValue(), -1, -1, 0, true, bkptAttributes);
//                    
//                    // At this point, the Debug framework has been told there's a line breakpoint,
//                    // and there's a marker in the *Java* source, but not in the original source.
//                    // So create another marker that has basically all the same attributes as
//                    // the Java marker, but is instead on the original source file at the
//                    // corresponding line #.
//                    final IMarker javaMarker= bkpt.getMarker();
//
//                    // create the marker
//                    IMarker origSrcMarker= origSrcFile.createMarker(IBreakpoint.LINE_BREAKPOINT_MARKER);
//              
//                    Map javaMarkerAttrs= javaMarker.getAttributes();
//                    for(Iterator iter= javaMarkerAttrs.keySet().iterator(); iter.hasNext();) {
//                        String key= (String) iter.next();
//                        Object value= javaMarkerAttrs.get(key);
//                        if (key.equals(IMarker.LINE_NUMBER)) {
//                            value= origSrcLineNumber;
//                        }
//                        if (key.equals(IMarker.CHAR_END) || key.equals(IMarker.CHAR_START))
//                            continue;
//                        origSrcMarker.setAttribute(key, value);
//                    }
//                    origSrcMarker.setAttribute(IMarker.LINE_NUMBER, origSrcLineNumber);
//                    
//                    //bkptToSrcMarkerMap.put(bkpt, origSrcMarker);
//                    //bkpt.setMarker(origSrcMarker);
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

	private IMarker findBreakpointMarker(IFile origSrcFile, int lineNumber) throws CoreException {
    	IMarker[] markers = origSrcFile.findMarkers(IBreakpoint.LINE_BREAKPOINT_MARKER, true, IResource.DEPTH_INFINITE);
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

	public void breakpointRemoved(IBreakpoint breakpoint, IMarkerDelta delta) {
		IPath path = breakpoint.getMarker().getResource().getRawLocation();
		String fileName = path.lastSegment();
		fileName = fileName.substring(0, fileName.indexOf(".") + 1) + origExten; 
		path = path.removeLastSegments(1).append(fileName);

		// SMS 20 Apr 2007
		// Changed following call to avoid discouraged access to type Workspace
		//IFile origSrcFile = ((Workspace)breakpoint.getMarker().getResource().getProject().getWorkspace()).getFileSystemManager().fileForLocation(path);
		IFile origSrcFile = breakpoint.getMarker().getResource().getProject().getWorkspace().getRoot().getFileForLocation(path);
		
		try {
			IMarker marker = findBreakpointMarker(origSrcFile, ((Integer)breakpoint.getMarker().getAttribute(IMarker.LINE_NUMBER)).intValue());
			//System.out.println("deleting marker " + origSrcFile.getFullPath() + " at line " + ((Integer)breakpoint.getMarker().getAttribute(IMarker.LINE_NUMBER)).intValue());
			if (marker != null)
				marker.delete();
		} catch(CoreException e){
			System.err.println(e);
		}
	}

	public void breakpointChanged(IBreakpoint breakpoint, IMarkerDelta delta) { }
	
	//MV -- This method is called from smapifier to reset the breakpoint in the
	//Java file when a new build has been done.
	
	public static String getTypeName(IFile origSrcFile){
		IProject project = origSrcFile.getProject();
		IJavaProject javaProj= JavaCore.create(project);
        final String origSrcFileName= origSrcFile.getName();
        
        String pathPrefix = project.getWorkspace().getRoot().getRawLocation() + project.getFullPath().toString();
        IPath projPath= project.getFullPath();
        //MV Note: javaProj.getOutputLocation returns a workspace relative path
		boolean projectIsSrcBin;
		try {
			projectIsSrcBin = (javaProj.getOutputLocation().matchingFirstSegments(projPath) == projPath.segmentCount()) && 
									 (javaProj.getOutputLocation().segmentCount() == projPath.segmentCount());

			if (!projectIsSrcBin){
				String temp = origSrcFile.getRawLocation().toString().substring(pathPrefix.length()).substring(1);
				pathPrefix = pathPrefix + "/" + temp.substring(0,temp.indexOf("/"));
			}
		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
        String temp = origSrcFile.getRawLocation().toString().substring(pathPrefix.length()).replaceAll("/", ".");
        return temp.substring(1,temp.lastIndexOf("."));
	}
}
