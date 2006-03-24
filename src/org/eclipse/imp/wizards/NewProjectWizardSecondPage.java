/*
 * Created on Feb 6, 2006
 */
package org.eclipse.uide.wizards;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;
import org.eclipse.jdt.internal.ui.util.CoreUtility;
import org.eclipse.jdt.internal.ui.wizards.ClassPathDetector;
import org.eclipse.jdt.internal.ui.wizards.NewWizardMessages;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jdt.ui.wizards.JavaCapabilityConfigurationPage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.uide.core.ProjectNatureBase;

public abstract class NewProjectWizardSecondPage extends JavaCapabilityConfigurationPage {
    private final NewProjectWizardFirstPage fFirstPage;

    private IPath fCurrProjectLocation;

    private IProject fCurrProject;

    private Boolean fIsAutobuild= null;

    private static final String FILENAME_CLASSPATH= ".classpath"; //$NON-NLS-1$

    protected abstract ProjectNatureBase getProjectNature();

    protected abstract IPath getLanguageRuntimePath();

    public NewProjectWizardSecondPage(NewProjectWizardFirstPage firstPage) {
	super();
	fFirstPage= firstPage;
    }

    /**
     * Called from the wizard on finish.
     */
    public void performFinish(IProgressMonitor monitor) throws CoreException, InterruptedException {
	try {
	    monitor.beginTask(NewWizardMessages.JavaProjectWizardSecondPage_operation_create, 3);
	    if (fCurrProject == null) {
		updateProject(new SubProgressMonitor(monitor, 1));
	    }
	    configureJavaProject(new SubProgressMonitor(monitor, 2));
	    String compliance= fFirstPage.getJRECompliance();
	    if (compliance != null) {
		IJavaProject project= JavaCore.create(fCurrProject);
		Map options= project.getOptions(false);
		JavaModelUtil.setCompilanceOptions(options, compliance);
		project.setOptions(options);
	    }
	} finally {
	    monitor.done();
	    fCurrProject= null;
	    if (fIsAutobuild != null) {
		CoreUtility.enableAutoBuild(fIsAutobuild.booleanValue());
		fIsAutobuild= null;
	    }
	}
    }

    final void updateProject(IProgressMonitor monitor) throws CoreException, InterruptedException {

	fCurrProject= fFirstPage.getProjectHandle();
	fCurrProjectLocation= fFirstPage.getLocationPath();

	if (monitor == null) {
	    monitor= new NullProgressMonitor();
	}
	try {
	    monitor.beginTask(NewWizardMessages.JavaProjectWizardSecondPage_operation_initialize, 7);
	    if (monitor.isCanceled()) {
		throw new OperationCanceledException();
	    }

	    IPath realLocation= fCurrProjectLocation;
	    if (Platform.getLocation().equals(fCurrProjectLocation)) {
		realLocation= fCurrProjectLocation.append(fCurrProject.getName());
	    }

	    //	    rememberExistingFiles(realLocation);

	    createProject(fCurrProject, fCurrProjectLocation, new SubProgressMonitor(monitor, 2));

	    IClasspathEntry[] entries= null;
	    IPath outputLocation= null;

	    if (fFirstPage.getDetect()) {
		if (!fCurrProject.getFile(FILENAME_CLASSPATH).exists()) { //$NON-NLS-1$
		    final ClassPathDetector detector= new ClassPathDetector(fCurrProject, new SubProgressMonitor(monitor, 2));
		    entries= detector.getClasspath();
		    outputLocation= detector.getOutputLocation();
		} else {
		    monitor.worked(2);
		}
	    } else if (fFirstPage.isSrcBin()) {
		IPreferenceStore store= PreferenceConstants.getPreferenceStore();
		IPath srcPath= new Path(store.getString(PreferenceConstants.SRCBIN_SRCNAME));
		IPath binPath= new Path(store.getString(PreferenceConstants.SRCBIN_BINNAME));

		if (srcPath.segmentCount() > 0) {
		    IFolder folder= fCurrProject.getFolder(srcPath);
		    CoreUtility.createFolder(folder, true, true, new SubProgressMonitor(monitor, 1));
		} else {
		    monitor.worked(1);
		}

		if (binPath.segmentCount() > 0 && !binPath.equals(srcPath)) {
		    IFolder folder= fCurrProject.getFolder(binPath);
		    CoreUtility.createFolder(folder, true, true, new SubProgressMonitor(monitor, 1));
		} else {
		    monitor.worked(1);
		}

		final IPath projectPath= fCurrProject.getFullPath();

		// configure the classpath entries, including the default jre library.
		List cpEntries= new ArrayList();
		cpEntries.add(JavaCore.newSourceEntry(projectPath.append(srcPath)));
		cpEntries.addAll(Arrays.asList(getDefaultClasspathEntry()));
		entries= (IClasspathEntry[]) cpEntries.toArray(new IClasspathEntry[cpEntries.size()]);

		// configure the output location
		outputLocation= projectPath.append(binPath);
	    } else {
		IPath projectPath= fCurrProject.getFullPath();
		List cpEntries= new ArrayList();
		cpEntries.add(JavaCore.newSourceEntry(projectPath));
		cpEntries.addAll(Arrays.asList(getDefaultClasspathEntry()));
		entries= (IClasspathEntry[]) cpEntries.toArray(new IClasspathEntry[cpEntries.size()]);

		outputLocation= projectPath;
		monitor.worked(2);
	    }
	    if (monitor.isCanceled()) {
		throw new OperationCanceledException();
	    }

	    init(JavaCore.create(fCurrProject), outputLocation, entries, false);
	    configureJavaProject(new SubProgressMonitor(monitor, 3)); // config the Java project to use the source folder
	    getProjectNature().addToProject(fCurrProject);
	} finally {
	    monitor.done();
	}
    }

    private IClasspathEntry[] getDefaultClasspathEntry() {
	IClasspathEntry[] defaultJRELibrary= PreferenceConstants.getDefaultJRELibrary();
//	String compliance= fFirstPage.getJRECompliance();
//	IPath jreContainerPath= new Path(JavaRuntime.JRE_CONTAINER);
//	if (compliance == null || defaultJRELibrary.length > 1 || !jreContainerPath.isPrefixOf(defaultJRELibrary[0].getPath())) {
////	     use default
//	    return defaultJRELibrary;
//	}
//	// try to find a compatible JRE
//	IVMInstall inst= BuildPathSupport.findMatchingJREInstall(compliance);
//	if (inst != null) {
//	    IPath newPath= jreContainerPath.append(inst.getVMInstallType().getId()).append(inst.getName());
//	    return new IClasspathEntry[] { JavaCore.newContainerEntry(newPath) };
//	}
	IPath langRuntimePath= getLanguageRuntimePath();
	IClasspathEntry langRuntimeCPE= JavaCore.newVariableEntry(langRuntimePath, null, null);
	IClasspathEntry[] allEntries= new IClasspathEntry[defaultJRELibrary.length + 1];

	System.arraycopy(defaultJRELibrary, 0, allEntries, 0, defaultJRELibrary.length);
	allEntries[allEntries.length-1]= langRuntimeCPE;

	return allEntries;
    }
}
