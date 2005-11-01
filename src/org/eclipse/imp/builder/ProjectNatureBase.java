/*
 * Created on Nov 1, 2005
 */
package org.eclipse.uide.core;

import java.util.HashMap;

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
     * Return the ID of the builder on which this nature's builder depends to process
     * its output files. If there is no such dependency, returns null.
     */
    // TODO this should be a property of the builder itself...
    protected abstract String getDownstreamBuilderID();

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

	int where= 0;
	String downstreamBuilderID= getDownstreamBuilderID();

	if (downstreamBuilderID != null) {
	    // Since this builder depends on another one, it needs to run *before* that one.
	    // So, find the right spot (in front of that builder) to put this builder.
	    for(; where < cmds.length; where++) {
		if (cmds[where].getBuilderName().equals(downstreamBuilderID))
		    break; // found it
	    }
	}

	ICommand compilerCmd= desc.newCommand();

	compilerCmd.setBuilderName(builderID);
	compilerCmd.setArguments(new HashMap());

	ICommand[] newCmds= new ICommand[cmds.length+1];

	System.arraycopy(cmds, 0, newCmds, 0, where);
	newCmds[where] = compilerCmd;
	System.arraycopy(cmds, where, newCmds, where+1, cmds.length-where);
	desc.setBuildSpec(newCmds);
	getProject().setDescription(desc, null);
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
