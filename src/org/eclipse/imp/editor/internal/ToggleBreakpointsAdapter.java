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
import org.eclipse.core.runtime.CoreException;
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

public class ToggleBreakpointsAdapter implements IToggleBreakpointsTarget {
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
	    IFile x10File= fileInput.getFile();
	    IProject project= x10File.getProject();
	    String x10FileName= x10File.getName();
	    IFile javaFile= javaFileForRootSourceFile(x10File, project);

	    if (!javaFile.exists())
		return; // Can't do anything; this file didn't produce a Java file

	    String typeName= x10FileName.substring(0, x10FileName.lastIndexOf('.'));
	    Map lineMap= getLineMap(x10File);
	    Integer x10LineNumber= new Integer(textSel.getStartLine() + 1);
	    int javaLineNum;

	    if (lineMap.containsKey(x10LineNumber)) // smapi.mapLine(javaFile, textSel.getStartLine());
		javaLineNum= ((Integer) lineMap.get(x10LineNumber)).intValue();
	    else
		javaLineNum= 1;

	    //	    System.out.println("Breakpoint toggle request @ line " + javaLineNum + " of file "
	    //		    + javaFile.getProjectRelativePath());

	    IJavaLineBreakpoint existingBreakpoint= JDIDebugModel.lineBreakpointExists(javaFile, typeName, javaLineNum);

	    // TODO Enable the breakpoint if there is already one that's disabled, rather than just blindly removing it.

	    if (existingBreakpoint != null) {
		DebugPlugin.getDefault().getBreakpointManager().removeBreakpoint(existingBreakpoint, true);
		return;
	    }

	    // At this point, we know there is no existing breakpoint at this line #.

	    Map bkptAttributes= new HashMap();

	    bkptAttributes.put("org.eclipse.jdt.debug.core.sourceName", typeName);

	    IBreakpoint bkpt= JDIDebugModel.createLineBreakpoint(javaFile, typeName, javaLineNum, -1, -1, 0, true, bkptAttributes);

	    // At this point, the Debug framework has been told there's a line breakpoint,
	    // and there's a marker in the *Java* source, but not in the X10 source.
	    // So create another marker that has basically all the same attributes as
	    // the Java marker, but is on the X10 source file instead at the corresponding line #.
	    IMarker javaMarker= bkpt.getMarker();
	    IMarker x10Marker= x10File.createMarker(IBreakpoint.LINE_BREAKPOINT_MARKER);

	    // Copy attributes from the Java breakpoint marker to the X10 marker
	    Map javaMarkerAttrs= javaMarker.getAttributes();
	    for(Iterator iter= javaMarkerAttrs.keySet().iterator(); iter.hasNext(); ) {
		String key= (String) iter.next();
		Object value= javaMarkerAttrs.get(key);

		// TODO filter the attributes to make sure that none that
		// really belong to the Java file get placed on the X10 file.
		x10Marker.setAttribute(key, value);
	    }
	    x10Marker.setAttribute(IMarker.LINE_NUMBER, x10LineNumber);
//	    bkptMarker.setAttribute(IMarker.MESSAGE, "foo");
	    bkpt.setMarker(x10Marker);
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
