/**
 * 
 */
package org.eclipse.uide.core;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.eclipse.core.resources.IProject;

/**
 * Tracks dependencies among compilation units in a given project.
 * @author rfuhrer
 */
public class DependencyInfo {
    protected final Map/*<String unitPath, Set<String unitPath>>*/ fDependsUpon= new HashMap();
    protected final Map/*<String unitPath, Set<String unitPath>>*/ fIsDependedUponBy= new HashMap();
    protected final IProject fProject;
    protected final String fWorkspacePath;

    public DependencyInfo(IProject project) {
	fProject= project;
	fWorkspacePath= fProject.getProject().getWorkspace().getRoot().getLocation().toString();
    }

    protected Set/*<String unitPath>*/ getEntry(Map/*<String unitPath, Set<String unitPath>>*/ map, String unitPath) {
        Set/*<String path>*/ result;

        if (!map.containsKey(unitPath))
            result= Collections.EMPTY_SET;
        else
            result= (Set) map.get(unitPath);
        return result;
    }

    protected Set getOrCreateEntry(Map map, String unitPath) {
	Set/*<String path>*/ result;

        if (!map.containsKey(unitPath))
            map.put(unitPath, result= new HashSet());
        else
            result= (Set) map.get(unitPath);
        return result;
    }

    /**
     * @param fromPath a compilation unit path; should be project-relative
     * @param uponPath a compilation unit path; should be project-relative
     */
    public void addDependency(String fromPath, String uponPath) {
	Set/*<String path>*/ fwdEntry= getOrCreateEntry(fDependsUpon, fromPath);

        fwdEntry.add(uponPath);

        Set/*<String path>*/ bkwdEntry= getOrCreateEntry(fIsDependedUponBy, uponPath);

        bkwdEntry.add(fromPath);
    }

    public void clearAllDependencies() {
	fDependsUpon.clear();
	fIsDependedUponBy.clear();
    }

    /**
     * @param unitPath should be project-relative
     */
    public void clearDependenciesOf(String unitPath) {
        Set/*<String path>*/ entry= getEntry(fDependsUpon, unitPath);

        fDependsUpon.put(unitPath, new HashSet());
        for(Iterator iter= entry.iterator(); iter.hasNext(); ) {
            String uponPath= (String) iter.next();
            Set/*<String path>*/ uponSet= getEntry(fIsDependedUponBy, uponPath);

            uponSet.remove(unitPath);
        }
    }

    /**
     * @return a Map from project-relative unit paths to Sets of dependent
     * project-relative unit paths
     */
    public Map/*<String path,Set<String path>>*/ getDependencies() {
        return Collections.unmodifiableMap(fDependsUpon);
    }

    /**
     * @param unitPath should be project-relative
     * @return a Set of dependent project-relative unit paths
     */
    public Set/*<String path>*/ getDependentsOf(String unitPath) {
        return (Set) fIsDependedUponBy.get(unitPath);
    }

    public void dump() {
        System.out.println("*** Dependencies ***:");
        for(Iterator iter= fDependsUpon.keySet().iterator(); iter.hasNext(); ) {
            String unit= (String) iter.next();
            Set/*<String path>*/ dependents= (Set) fDependsUpon.get(unit);
    
            System.out.println("Unit " + unit + ": ");
            for(Iterator iterator= dependents.iterator(); iterator.hasNext(); ) {
        	String uponUnit= (String) iterator.next();
        	System.out.print("  ");
        	System.out.print(uponUnit);
        	if (iterator.hasNext()) System.out.print(", ");
            }
            System.out.println();
        }
    }
}
