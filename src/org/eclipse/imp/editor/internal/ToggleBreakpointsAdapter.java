/*
 * Created on Mar 1, 2006
 */
package org.eclipse.uide.editor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.internal.resources.Workspace;
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
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPart;

import com.ibm.watson.smapi.LineElem;
import com.ibm.watson.smapi.LineMapBuilder;

public class ToggleBreakpointsAdapter implements IToggleBreakpointsTarget, IBreakpointListener {

    //private static Map /* IJavaLineBreakPoint -> IMarker */bkptToSrcMarkerMap= new HashMap();
  
	String origExten = "x10"; //MV -- TODO this is duplicated from SmapieBuilder
    
    public ToggleBreakpointsAdapter() {
        super();
        DebugPlugin.getDefault().getBreakpointManager().addBreakpointListener(this);
    }

    public void toggleLineBreakpoints(IWorkbenchPart part, ISelection selection) throws CoreException {
    	if (selection instanceof ITextSelection) {
            ITextSelection textSel= (ITextSelection) selection;
            IEditorPart editorPart= (IEditorPart) part.getAdapter(IEditorPart.class);
            IFileEditorInput fileInput= (IFileEditorInput) editorPart.getEditorInput();
            final IFile origSrcFile= fileInput.getFile();
            
           
            final String origSrcFileName= origSrcFile.getName();
            
            final String typeName = getTypeName(origSrcFile);
            
            
            final IFile javaFile= javaFileForRootSourceFile(origSrcFile);

            if (!javaFile.exists()) 
            	return; // Can't do anything; this file didn't produce a Java file

            int extenStart= origSrcFileName.lastIndexOf('.');
            String origExten= origSrcFileName.substring(extenStart+1);
            
         
            final Integer origSrcLineNumber= new Integer(textSel.getStartLine() + 1);
            

            if (! validateLineNumber(origSrcFile, origSrcLineNumber)) 
            	 return;
            
            
            // TODO Enable the breakpoint if there is already one that's disabled, rather than just blindly removing it.

        
           final IJavaLineBreakpoint existingBreakpoint= JDIDebugModel.lineBreakpointExists(javaFile, typeName, origSrcLineNumber.intValue());
           
            IWorkspaceRunnable wr= new IWorkspaceRunnable() {
                public void run(IProgressMonitor monitor) throws CoreException {

                    if (existingBreakpoint != null) {
                        //IMarker marker= (IMarker) bkptToSrcMarkerMap.get(existingBreakpoint);
                    	
                    	// find the marker first, then delete it
                    	IMarker marker = findMarker(origSrcFile, origSrcLineNumber.intValue());
                    	marker.delete();
                    	
                    	
                        //bkptToSrcMarkerMap.remove(existingBreakpoint);
                        DebugPlugin.getDefault().getBreakpointManager().removeBreakpoint(existingBreakpoint, true);
                        //System.out.println("******* deleting marker");

                        return;
                    }

                    // At this point, we know there is no existing breakpoint at this line #.

                    Map bkptAttributes= new HashMap();
                    bkptAttributes.put("org.eclipse.jdt.debug.core.sourceName", typeName);
                    final IBreakpoint bkpt= JDIDebugModel.createLineBreakpoint(javaFile, typeName, origSrcLineNumber.intValue(), -1, -1, 0, true,
                          bkptAttributes);
                    
                    
                   

                    // At this point, the Debug framework has been told there's a line breakpoint,
                    // and there's a marker in the *Java* source, but not in the original source.
                    // So create another marker that has basically all the same attributes as
                    // the Java marker, but is instead on the original source file at the
                    // corresponding line #.
                    final IMarker javaMarker= bkpt.getMarker();

                    // create the marker
                    IMarker origSrcMarker= origSrcFile.createMarker(IBreakpoint.LINE_BREAKPOINT_MARKER);
                    
              
                    Map javaMarkerAttrs= javaMarker.getAttributes();
                    for(Iterator iter= javaMarkerAttrs.keySet().iterator(); iter.hasNext();) {
                        String key= (String) iter.next();
                        Object value= javaMarkerAttrs.get(key);
                        if (key.equals(IMarker.LINE_NUMBER)) {
                            value= origSrcLineNumber;
                        }
                        if (key.equals(IMarker.CHAR_END) || key.equals(IMarker.CHAR_START))
                            continue;
                        origSrcMarker.setAttribute(key, value);
                        
                        //System.out.println("Attribute added for marker " + key + "-> " + value);
                    }
                    origSrcMarker.setAttribute(IMarker.LINE_NUMBER, origSrcLineNumber);
                    
                    //bkptToSrcMarkerMap.put(bkpt, origSrcMarker);

                    // bkptMarker.setAttribute(IMarker.MESSAGE, "foo");
                    //bkpt.setMarker(origSrcMarker);

                }

				
            };
            try {
                ResourcesPlugin.getWorkspace().run(wr, null);
            } catch (CoreException e) {
                throw new DebugException(e.getStatus());
            }

        }
    }
    
    private static boolean validateLineNumber(IFile origSrcFile, Integer origSrcLineNumber) {
    	LineMapBuilder lmb= new LineMapBuilder(origSrcFile.getRawLocation().removeFileExtension().toString());
        Map lineMap= lmb.getLineMap();

        if (lineMap.containsKey(origSrcLineNumber))
        	return true;
        
        return false;
            
       
		
	}

	private IMarker findMarker(IFile origSrcFile, int lineNumber) throws CoreException {
    	IMarker[] markers = origSrcFile.findMarkers(IBreakpoint.LINE_BREAKPOINT_MARKER, false, IResource.DEPTH_INFINITE);
    	for (int k = 0; k < markers.length; k++ ){
    		if (((Integer)markers[k].getAttribute(IMarker.LINE_NUMBER)).intValue() == lineNumber){
    			return markers[k];
    		}
    	}
    	return null;
    }

    private static  IFile javaFileForRootSourceFile(IFile rootSrcFile) {
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

	public void breakpointAdded(IBreakpoint breakpoint) {
		
	}

	public void breakpointRemoved(IBreakpoint breakpoint, IMarkerDelta delta) {
		
		IPath path = breakpoint.getMarker().getResource().getRawLocation();
		String fileName = path.lastSegment();
		fileName = fileName.substring(0, fileName.indexOf(".") + 1) + origExten; 
		path = path.removeLastSegments(1).append(fileName);
		
		IFile origSrcFile = ((Workspace)breakpoint.getMarker().getResource().getProject().getWorkspace()).getFileSystemManager().fileForLocation(path);
		try {
			IMarker marker = findMarker(origSrcFile, ((Integer)breakpoint.getMarker().getAttribute(IMarker.LINE_NUMBER)).intValue());
			//System.out.println("deleting marker " + origSrcFile.getFullPath() + " at line " + ((Integer)breakpoint.getMarker().getAttribute(IMarker.LINE_NUMBER)).intValue());
			if (marker != null)
				marker.delete();
		} catch(CoreException e){
			System.err.println(e);
		}
		
	}

	public void breakpointChanged(IBreakpoint breakpoint, IMarkerDelta delta) {
		
		
	}
	
	//MV -- This method is called from smapifier to reset the breakpoint in the
	//Java file when a new build has been done.
	
	public static void resetJavaBreakpoints(IFile origSrcFile){
		IFile javaFile = javaFileForRootSourceFile(origSrcFile);
		//first record which lines in original source file need to have a breakpoint
		//this is needed because when we remove the breakpoints from the java file,
		//the markers in the original file get deleted.
		
		Set lineNumbers = new HashSet();
		try {
			IMarker[] markers = origSrcFile.findMarkers(IBreakpoint.LINE_BREAKPOINT_MARKER, false, IResource.DEPTH_INFINITE);
			for (int i = 0; i < markers.length; i++) {
				Integer num = (Integer) markers[i].getAttribute(IMarker.LINE_NUMBER);
				if (validateLineNumber(origSrcFile,num))
					lineNumbers.add(num);
			}
		} catch (CoreException e){
			System.err.println(e);
		}
		
		//remove all breakpoints from java file, this also removes the original markers
		IBreakpoint[] breakpoints = DebugPlugin.getDefault().getBreakpointManager().getBreakpoints();
		for (int i = 0; i < breakpoints.length ; i++){
			IResource res = breakpoints[i].getMarker().getResource();
			if (res.equals((IResource)javaFile)){
				try {
					DebugPlugin.getDefault().getBreakpointManager().removeBreakpoint(breakpoints[i], true);
				} catch (CoreException e) {
					e.printStackTrace();
				}
         
			}
		}
		
		//now add new breakpoints to the java file, and create corresponding marker in orig source file
		try {
			for(Iterator t = lineNumbers.iterator(); t.hasNext(); ) {
				Integer origSrcLineNumber = (Integer) t.next();
				String typeName = getTypeName(origSrcFile);
				Map bkptAttributes= new HashMap();
                bkptAttributes.put("org.eclipse.jdt.debug.core.sourceName", typeName);
                final IBreakpoint bkpt= JDIDebugModel.createLineBreakpoint(javaFile, typeName, origSrcLineNumber.intValue(), -1, -1, 0, true,
                      bkptAttributes);
                final IMarker javaMarker= bkpt.getMarker();

                // create the marker
                IMarker origSrcMarker= origSrcFile.createMarker(IBreakpoint.LINE_BREAKPOINT_MARKER);
                
          
                Map javaMarkerAttrs= javaMarker.getAttributes();
                for(Iterator iter= javaMarkerAttrs.keySet().iterator(); iter.hasNext();) {
                    String key= (String) iter.next();
                    Object value= javaMarkerAttrs.get(key);
                    if (key.equals(IMarker.LINE_NUMBER)) {
                        value= origSrcLineNumber;
                    }
                    if (key.equals(IMarker.CHAR_END) || key.equals(IMarker.CHAR_START))
                        continue;
                    origSrcMarker.setAttribute(key, value);
                    
                    
                }
                origSrcMarker.setAttribute(IMarker.LINE_NUMBER, origSrcLineNumber);
                
			}
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//finally recreate the markers
		
    	
	}
	
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
