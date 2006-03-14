/*
 * Created on Mar 1, 2006
 */
package org.eclipse.uide.editor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTarget;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPart;

import com.ibm.watson.smapi.LineElem;
import com.ibm.watson.smapi.LineMapBuilder;

public class ToggleBreakpointsAdapter implements IToggleBreakpointsTarget {
    
	private static Map /* IJavaLineBreakPoint -> IMarker */ x10MarkerMap = new HashMap(); 
	
	public ToggleBreakpointsAdapter() {
	super();
    }

    private Map/*<Integer,Integer>*/ getLineMap(IFile x10File) { // Will come from SMAPI
	Map lineMap= new HashMap();

	lineMap.put(new Integer(1), new Integer(121));
	lineMap.put(new Integer(2), new Integer(8));
	lineMap.put(new Integer(3), new Integer(22));
	lineMap.put(new Integer(4), new Integer(26));
	lineMap.put(new Integer(5), new Integer(36));
	lineMap.put(new Integer(6), new Integer(43));
	lineMap.put(new Integer(7), new Integer(62));
	lineMap.put(new Integer(9), new Integer(78));
	lineMap.put(new Integer(10), new Integer(82));
	lineMap.put(new Integer(11), new Integer(112));

	return lineMap;
    }

    public void toggleLineBreakpoints(IWorkbenchPart part, ISelection selection) throws CoreException {
	if (selection instanceof ITextSelection) {
	    ITextSelection textSel= (ITextSelection) selection;
	    IEditorPart editorPart= (IEditorPart) part.getAdapter(IEditorPart.class);
	    IFileEditorInput fileInput= (IFileEditorInput) editorPart.getEditorInput();
	    final IFile x10File= fileInput.getFile();
	    IProject project= x10File.getProject();
	    final String x10FileName= x10File.getName();
	    final IFile javaFile= javaFileForRootSourceFile(x10File, project);

	    if (!javaFile.exists())
	    	return; // Can't do anything; this file didn't produce a Java file

	    final String typeName= x10FileName.substring(0, x10FileName.lastIndexOf('.'));
	    LineMapBuilder lmb = new LineMapBuilder(x10File.getRawLocation().removeFileExtension().toString());
	    Map lineMap= lmb.getLineMap();
	    final Integer x10LineNumber= new Integer(textSel.getStartLine() + 1);
	    final int javaLineNum;

	    if (lineMap.containsKey(x10LineNumber)) {
	    	javaLineNum= ((LineElem) lineMap.get(x10LineNumber)).getStart();
	    } else {
	    	javaLineNum= 1;
	    	System.out.println("Warning: breakpoint ignored because no corresponding line in Java file!");
	    	return;
	    }
	    
	    System.out.println("******** The breakpoint is at line: " + x10LineNumber + " in x10 and " + javaLineNum + "in Java");

	    //	    System.out.println("Breakpoint toggle request @ line " + javaLineNum + " of file "
	    //		    + javaFile.getProjectRelativePath());

	    
	    // TODO Enable the breakpoint if there is already one that's disabled, rather than just blindly removing it.

	    final IJavaLineBreakpoint existingBreakpoint= JDIDebugModel.lineBreakpointExists(javaFile, typeName, x10LineNumber.intValue());

	    IWorkspaceRunnable wr= new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				
				if (existingBreakpoint != null) {
					IMarker marker = (IMarker) x10MarkerMap.get(existingBreakpoint);
			    	marker.delete();
			    	x10MarkerMap.remove(existingBreakpoint);
					DebugPlugin.getDefault().getBreakpointManager().removeBreakpoint(existingBreakpoint, true);
			    	System.out.println("******* deleting marker");
			    	
			    	return;
			    }

			    // At this point, we know there is no existing breakpoint at this line #.

			    Map bkptAttributes= new HashMap();
			    bkptAttributes.put("org.eclipse.jdt.debug.core.sourceName", typeName);
			    final IBreakpoint bkpt= JDIDebugModel.createLineBreakpoint(javaFile, typeName, x10LineNumber.intValue(), -1, -1, 0, true, bkptAttributes);

			    // At this point, the Debug framework has been told there's a line breakpoint,
			    // and there's a marker in the *Java* source, but not in the X10 source.
			    // So create another marker that has basically all the same attributes as
			    // the Java marker, but is on the X10 source file instead at the corresponding line #.
			    final IMarker javaMarker= bkpt.getMarker();
			   
			    
				// create the marker
				IMarker x10Marker= x10File.createMarker(IBreakpoint.LINE_BREAKPOINT_MARKER);
				Map javaMarkerAttrs= javaMarker.getAttributes();
			    for(Iterator iter= javaMarkerAttrs.keySet().iterator(); iter.hasNext(); ) {
			    	String key= (String) iter.next();
			    	Object value= javaMarkerAttrs.get(key);
			    	if (key.equals(IMarker.LINE_NUMBER)){
			    		value = x10LineNumber;
			    	}
			    	if (key.equals(IMarker.CHAR_END) || key.equals(IMarker.CHAR_START))
			    		continue;
			    	x10Marker.setAttribute(key, value);
			    	System.out.println("Attribute added for marker " + key + "-> " + value);
			    }
			    x10Marker.setAttribute(IMarker.LINE_NUMBER, x10LineNumber);
			    x10MarkerMap.put(bkpt, x10Marker);
			    
			   
			    
			    //bkptMarker.setAttribute(IMarker.MESSAGE, "foo");
			    //bkpt.setMarker(x10Marker);
			   
			}
	    };
	    try {
    		ResourcesPlugin.getWorkspace().run(wr, null);
    	} catch (CoreException e) {
    		throw new DebugException(e.getStatus());
    	}			
	    	   
	}
    }

    private IFile javaFileForRootSourceFile(IFile rootSrcFile, IProject project) {
	String rootSrcName= rootSrcFile.getName();

	return project.getFile(rootSrcFile.getProjectRelativePath().removeLastSegments(1).append(
		rootSrcName.substring(0, rootSrcName.lastIndexOf('.')) + ".java"));
    }

    public boolean canToggleLineBreakpoints(IWorkbenchPart part, ISelection selection) {
	return true;
    }

    public void toggleMethodBreakpoints(IWorkbenchPart part, ISelection selection) throws CoreException {}

    public boolean canToggleMethodBreakpoints(IWorkbenchPart part, ISelection selection) {
	return false;
    }

    public void toggleWatchpoints(IWorkbenchPart part, ISelection selection) throws CoreException {}

    public boolean canToggleWatchpoints(IWorkbenchPart part, ISelection selection) {
	return false;
    }
}
