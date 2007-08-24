/*
 * (C) Copyright IBM Corporation 2007
 * 
 * This file is part of the Eclipse IMP.
 */
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
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.imp.editor.internal.ToggleBreakpointsAdapter;
import org.eclipse.jdt.debug.core.JDIDebugModel;

public class BreakpointUtils {

    public static void resetJavaBreakpoints(IFile origSrcFile){
    	IFile javaFile = ToggleBreakpointsAdapter.javaFileForRootSourceFile(origSrcFile);
    	//first record which lines in original source file need to have a breakpoint
    	//this is needed because when we remove the breakpoints from the java file,
    	//the markers in the original file get deleted.
    	
    	Set lineNumbers = new HashSet();
    	try {
    		IMarker[] markers = origSrcFile.findMarkers(IBreakpoint.LINE_BREAKPOINT_MARKER, false, IResource.DEPTH_INFINITE);
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
    			String typeName = ToggleBreakpointsAdapter.getTypeName(origSrcFile);
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

}
