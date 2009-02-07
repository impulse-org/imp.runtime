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

package org.eclipse.imp.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.imp.editor.internal.ToggleBreakpointsAdapter;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.internal.debug.core.breakpoints.JavaLineBreakpoint;

public class BreakpointUtils {

    public static void resetJavaBreakpoints(IFile origSrcFile){
    	IFile javaFile = ToggleBreakpointsAdapter.javaFileForRootSourceFile(origSrcFile);
    	//first record which lines in original source file need to have a breakpoint
    	//this is needed because when we remove the breakpoints from the java file,
    	//the markers in the original file get deleted.
    	
    	Set lineNumbers = new HashSet();
    	try {
    		IMarker[] markers = origSrcFile.findMarkers(IBreakpoint.LINE_BREAKPOINT_MARKER, /*false*/true, IResource.DEPTH_INFINITE);
    		for (int i = 0; i < markers.length; i++) {
    			Integer num = (Integer) markers[i].getAttribute(IMarker.LINE_NUMBER);
    			if (ToggleBreakpointsAdapter.validateLineNumber(origSrcFile,num))
    				lineNumbers.add(num);
    		}
    	} catch (CoreException e){
    		System.err.println(e);
    	}
    	
    	//remove all breakpoints from java file, this also removes the original markers
    	IBreakpoint[] breakpoints = DebugPlugin.getDefault().getBreakpointManager().getBreakpoints();
    	for (int i = 0; i < breakpoints.length ; i++){
    		IResource res = breakpoints[i].getMarker().getResource();
    		if (res.equals((IResource)/*javaFile*/origSrcFile)){
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
    			String typeName = ToggleBreakpointsAdapter.getTypeName(origSrcFile);
    			Map bkptAttributes= new HashMap();
//            bkptAttributes.put("org.eclipse.jdt.debug.core.sourceName", typeName);
//            final IBreakpoint bkpt= JDIDebugModel.createLineBreakpoint(javaFile, typeName, origSrcLineNumber.intValue(), -1, -1, 0, true,
//                  bkptAttributes);
//    			bkptAttributes.put("org.eclipse.jdt.debug.core.sourceName", origSrcFile.getFullPath().toString());
    			final IBreakpoint bkpt= JDIDebugModel.createStratumBreakpoint(origSrcFile , "x10", origSrcFile.getName(), /*origSrcFile.getFullPath().toString()*/null, null, origSrcLineNumber.intValue(), -1, -1, 0, true, bkptAttributes);
//            final IMarker javaMarker= bkpt.getMarker();
    
// mmk 7/29/08: removal of breakpoint doesn't appear to remove original source marker.  So we shouldn't "re"-create it -- yields duplicates on recompile
//            // create the marker
//            IMarker origSrcMarker= origSrcFile.createMarker(IBreakpoint.LINE_BREAKPOINT_MARKER);
//            
//    
//            Map javaMarkerAttrs= javaMarker.getAttributes();
//            for(Iterator iter= javaMarkerAttrs.keySet().iterator(); iter.hasNext();) {
//                String key= (String) iter.next();
//                Object value= javaMarkerAttrs.get(key);
//                if (key.equals(IMarker.LINE_NUMBER)) {
//                    value= origSrcLineNumber;
//                }
//                if (key.equals(IMarker.CHAR_END) || key.equals(IMarker.CHAR_START))
//                    continue;
//                origSrcMarker.setAttribute(key, value);
//                
//                
//            }
//            origSrcMarker.setAttribute(IMarker.LINE_NUMBER, origSrcLineNumber);
//            
    		}
    	} catch (CoreException e) {
    		// TODO Auto-generated catch block
    		e.printStackTrace();
    	}
    	
    	//finally recreate the markers
    	
    
    }

    // mmk: slightly modified from JDIDebugModel to support stratum line breakpoints
	/**
	 * Returns a Java line breakpoint that is already registered with the breakpoint
	 * manager for a type with the given name at the given line number in the given resource.
	 * 
	 * @param resource the resource
	 * @param typeName fully qualified type name
	 * @param lineNumber line number
	 * @return a Java line breakpoint that is already registered with the breakpoint
	 *  manager for a type with the given name at the given line number or <code>null</code>
	 * if no such breakpoint is registered
	 * @exception CoreException if unable to retrieve the associated marker
	 * 	attributes (line number).
	 * @since 3.1
	 */
	public static IJavaLineBreakpoint lineBreakpointExists(IResource resource, String typeName, int lineNumber) throws CoreException {
		String modelId= JDIDebugModel.getPluginIdentifier();
		String markerType= /*JavaLineBreakpoint.getMarkerType()*/"org.eclipse.jdt.debug.javaStratumLineBreakpointMarker";
		IBreakpointManager manager= DebugPlugin.getDefault().getBreakpointManager();
		IBreakpoint[] breakpoints= manager.getBreakpoints(modelId);
		for (int i = 0; i < breakpoints.length; i++) {
			if (!(breakpoints[i] instanceof IJavaLineBreakpoint)) {
				continue;
			}
			IJavaLineBreakpoint breakpoint = (IJavaLineBreakpoint) breakpoints[i];
			IMarker marker = breakpoint.getMarker();
			if (marker != null && marker.exists() && marker.getType().equals(markerType)) {
				String breakpointTypeName = breakpoint.getTypeName();
				if (
//					(breakpointTypeName.equals(typeName) || breakpointTypeName.startsWith(typeName + '$')) &&
					breakpoint.getLineNumber() == lineNumber &&
					resource.equals(marker.getResource())) {
						return breakpoint;
				}
			}
		}
		return null;
	}	
		

}
