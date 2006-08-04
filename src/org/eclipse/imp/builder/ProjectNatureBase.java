/*
 * Created on Nov 1, 2005
 */
package org.eclipse.uide.core;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.uide.runtime.IPluginLog;

public abstract class ProjectNatureBase implements IProjectNature {
    private IProject fProject;

    public ProjectNatureBase() {}

    public abstract String getNatureID();

    public abstract String getBuilderID();

    public abstract IPluginLog getLog();

    /**
     * Refresh the preferences, to make sure the project settings are up to date.
     * Derived classes must implement.
     */
    protected abstract void refreshPrefs();

    /**
     * Returns the ID of the builder that processes the artifacts that this
     * nature's builder produces. If there is no such dependency, returns null.
     */
    // TODO this should be a property of the builder itself...
    protected String getDownstreamBuilderID() {
	return null; // by default, no such dependency
    }

    /**
     * Returns the ID of the builder that produces artifacts that this nature's
     * builder consumes. If there is no such dependency, returns null.
     */
    // TODO this should be a property of the builder itself...
    protected String getUpstreamBuilderID() {
	return null; // by default, no such dependency
    }

    public void addToProject(IProject project) {
	String natureID= getNatureID();

	refreshPrefs();
	getLog().maybeWriteInfoMsg("Attempting to add nature" + natureID);

	try {
	    IProjectDescription	description= project.getDescription();
	    String[] natures= description.getNatureIds();
	    String[] newNatures= new String[natures.length + 1];

	    System.arraycopy(natures, 0, newNatures, 0, natures.length);
	    newNatures[natures.length]= natureID;

	    description.setNatureIds(newNatures);
	    project.setDescription(description, null);

	    getLog().maybeWriteInfoMsg("Added nature " + natureID);
	} catch (CoreException e) {
	    // Something went wrong
	    getLog().writeErrorMsg("Failed to add nature " + natureID + ": " + e.getMessage());
	}
    }

    public void configure() throws CoreException {
	IProjectDescription desc= getProject().getDescription();
	String builderID= getBuilderID();
	ICommand[] cmds= desc.getBuildSpec();

	// Check: is the builder already in this project?
	for(int i=0; i < cmds.length; i++) {
	    if (cmds[i].getBuilderName().equals(builderID))
		return; // relevant command is already in there...
	}

	int beforeWhere= cmds.length;
	String downstreamBuilderID= getDownstreamBuilderID();

	if (downstreamBuilderID != null) {
	    // Since this builder produces artifacts that another one will
	    // post-process, it needs to run *before* that one.
	    // So, find the right spot (in front of that builder) to put this one.
	    for(beforeWhere--; beforeWhere >= 0; beforeWhere--) {
		if (cmds[beforeWhere].getBuilderName().equals(downstreamBuilderID))
		    break; // found it
	    }
	    if (beforeWhere < 0)
		getLog().writeErrorMsg("Unable to find downstream builder '" + downstreamBuilderID + "' for builder '" + builderID + "'.");
	}

	int afterWhere= -1;
	String upstreamBuilderID= getUpstreamBuilderID();

	if (upstreamBuilderID != null) {
	    // This builder consumes artifacts that another one will produce,
	    // so it needs to run *after* that one.
	    // So, find the right spot (after that builder) to put this one.
	    for(afterWhere= 0; afterWhere < cmds.length; afterWhere++) {
		if (cmds[afterWhere].getBuilderName().equals(upstreamBuilderID))
		    break; // found it
	    }
	    if (afterWhere == cmds.length)
		getLog().writeErrorMsg("Unable to find upstream builder '" + upstreamBuilderID + "' for builder '" + builderID + "'.");
	}

	if (beforeWhere <= afterWhere)
	    getLog().writeErrorMsg("Error: builder '" + builderID + "' needs to be before downstream builder '" + downstreamBuilderID + "' but after builder " + upstreamBuilderID + ", but " + downstreamBuilderID + " comes after " + upstreamBuilderID + "!");
	if (beforeWhere == cmds.length && afterWhere >= 0)
	    beforeWhere= afterWhere + 1;

	ICommand compilerCmd= desc.newCommand();

	compilerCmd.setBuilderName(builderID);
	compilerCmd.setArguments(getBuilderArguments());

	ICommand[] newCmds= new ICommand[cmds.length+1];

	System.arraycopy(cmds, 0, newCmds, 0, beforeWhere);
	newCmds[beforeWhere] = compilerCmd;
	System.arraycopy(cmds, beforeWhere, newCmds, beforeWhere+1, cmds.length-beforeWhere);
	desc.setBuildSpec(newCmds);
	getProject().setDescription(desc, null);
    }

    protected Map getBuilderArguments() {
	return new HashMap();
    }

    public void deconfigure() throws CoreException {
	IProjectDescription desc= getProject().getDescription();
	String builderID= getBuilderID();
	ICommand[] cmds= desc.getBuildSpec();

	for(int i=0; i < cmds.length; ++i) {
	    if (cmds[i].getBuilderName().equals(builderID)) {
		ICommand[] newCmds= new ICommand[cmds.length - 1];

		System.arraycopy(cmds, 0, newCmds, 0, i);
		System.arraycopy(cmds, i + 1, newCmds, i, cmds.length - i - 1);
		desc.setBuildSpec(newCmds);
		getProject().setDescription(desc, null);
		return;
	    }
	}
    }

    public IProject getProject() {
	return fProject;
    }

    public void setProject(IProject project) {
	fProject= project;
    }
}
