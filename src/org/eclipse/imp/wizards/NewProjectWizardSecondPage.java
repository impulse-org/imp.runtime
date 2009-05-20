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

package org.eclipse.imp.wizards;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.imp.builder.ProjectNatureBase;
import org.eclipse.imp.runtime.RuntimePlugin;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.ui.wizards.ClassPathDetector;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstall2;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jdt.ui.wizards.JavaCapabilityConfigurationPage;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyDelegatingOperation;
import org.eclipse.ui.ide.IDE;

public abstract class NewProjectWizardSecondPage extends JavaCapabilityConfigurationPage {
    private static final String FILENAME_PROJECT= ".project"; //$NON-NLS-1$
    private static final String FILENAME_CLASSPATH= ".classpath"; //$NON-NLS-1$

    private final NewProjectWizardFirstPage fFirstPage;

    private URI fCurrProjectLocation; // null if location is platform location

    private IProject fCurrProject;

    private boolean fKeepContent;

    private File fDotProjectBackup;
    private File fDotClasspathBackup;

    private Boolean fIsAutobuild= null;

    /**
     * @return the IProjectNature to add to the newly-created project
     */
    protected abstract ProjectNatureBase getProjectNature();

    /**
     * Sub-classes may override if they need to contribute only one classpath entry to the
     * new project. In that case, they should not override createLanguageRuntimeEntries()
     * as well.
     * @return the IClasspathEntry for the language-specific runtime (jar, whatever) that
     * will be placed in the new project's classpath.
     * This used to return an IPath, but this class would always create a "variable"
     * classpath entry, which is not always appropriate. We now let the language-specific
     * implementation decide what kind of classpath entry to create.
     */
    protected IClasspathEntry createLanguageRuntimeEntry() {
        return null;
    }

    /**
     * Sub-classes may override if they need to contribute multiple classpath entries to the
     * new project. If they do, they shouldn't also override createLanguageRuntimeEntry().
     * The default implementation returns whatever createLanguageRuntimeEntry() returns.
     * @return the list of IClasspathEntry's for the language-specific runtime components
     * (jars, whatever) that will be placed in the new project's classpath.
     */
    protected List<IClasspathEntry> createLanguageRuntimeEntries() {
        IClasspathEntry uniqueEntry= createLanguageRuntimeEntry();

        if (uniqueEntry != null) {
            return Arrays.asList(uniqueEntry);
        }
        return Collections.emptyList();
    }

    public NewProjectWizardSecondPage(NewProjectWizardFirstPage firstPage) {
    	super();
    	fFirstPage= firstPage;
    }

    /**
     * Called from the wizard on finish.
     */
    public void performFinish(IProgressMonitor monitor) throws CoreException, InterruptedException {
        try {
            //monitor.beginTask(NewWizardMessages.JavaProjectWizardSecondPage_operation_create, 3);  // <= 3.3
            //monitor.beginTask(NewWizardMessages.NewJavaProjectWizardPageTwo_operation_create, 3);	 // >= 3.4
            monitor.beginTask("Creating project...", 3);
            if (fCurrProject == null) {
                updateProject(new SubProgressMonitor(monitor, 1));
            }
            configureJavaProject(new SubProgressMonitor(monitor, 2));
            String compliance= fFirstPage.getJRECompliance();
            if (compliance != null) {
                IJavaProject project= JavaCore.create(fCurrProject);
                Map<String, String> options= project.getOptions(false);

                setComplianceOptions(options, compliance);
                project.setOptions(options);
            }
        } finally {
            monitor.done();
            fCurrProject= null;
            if (fIsAutobuild != null) {
                enableAutoBuild(fIsAutobuild.booleanValue());
                fIsAutobuild= null;
            }
        }
    }

    public static void setComplianceOptions(Map<String,String> map, String compliance) {
        if (JavaCore.VERSION_1_6.equals(compliance)) {
            map.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_6);
            map.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_6);
            map.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_6);
            map.put(JavaCore.COMPILER_PB_ASSERT_IDENTIFIER, JavaCore.ERROR);
            map.put(JavaCore.COMPILER_PB_ENUM_IDENTIFIER, JavaCore.ERROR);
        } else if (JavaCore.VERSION_1_5.equals(compliance)) {
            map.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);
            map.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_5);
            map.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_5);
            map.put(JavaCore.COMPILER_PB_ASSERT_IDENTIFIER, JavaCore.ERROR);
            map.put(JavaCore.COMPILER_PB_ENUM_IDENTIFIER, JavaCore.ERROR);
        } else if (JavaCore.VERSION_1_4.equals(compliance)) {
            map.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_4);
            map.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_3);
            map.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_2);
            map.put(JavaCore.COMPILER_PB_ASSERT_IDENTIFIER, JavaCore.WARNING);
            map.put(JavaCore.COMPILER_PB_ENUM_IDENTIFIER, JavaCore.WARNING);
        } else if (JavaCore.VERSION_1_3.equals(compliance)) {
            map.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_3);
            map.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_3);
            map.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_1);
            map.put(JavaCore.COMPILER_PB_ASSERT_IDENTIFIER, JavaCore.IGNORE);
            map.put(JavaCore.COMPILER_PB_ENUM_IDENTIFIER, JavaCore.IGNORE);
        } else {
            throw new IllegalArgumentException("Unsupported compliance: " + compliance); //$NON-NLS-1$
        }
    }
    
    /**
     * Set the autobuild to the value of the parameter and
     * return the old one.
     * 
     * @param state the value to be set for autobuilding.
     * @return the old value of the autobuild state
     */
    public static boolean enableAutoBuild(boolean state) throws CoreException {
        IWorkspace workspace= ResourcesPlugin.getWorkspace();
        IWorkspaceDescription desc= workspace.getDescription();
        boolean isAutoBuilding= desc.isAutoBuilding();
        if (isAutoBuilding != state) {
            desc.setAutoBuilding(state);
            workspace.setDescription(desc);
        }
        return isAutoBuilding;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.IDialogPage#setVisible(boolean)
     */
    public void setVisible(boolean visible) {
        if (visible) {
            IStatus status= changeToNewProject();
            if (status != null && !status.isOK()) {
                ErrorDialog.openError(getShell(),
                //NewWizardMessages.JavaProjectWizardSecondPage_error_title,
                        "New Java Project", null, status);
            }
        } else {
            removeProject();
        }
        super.setVisible(visible);
        if (visible) {

            setFocus();
        }
    }

    private IStatus changeToNewProject() {
        fKeepContent= fFirstPage.getDetect();

        class UpdateRunnable implements IRunnableWithProgress {
            public IStatus infoStatus= Status.OK_STATUS;

            public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                try {
                    if (fIsAutobuild == null) {
                        fIsAutobuild= Boolean.valueOf(enableAutoBuild(false));
                    }
                    infoStatus= updateProject(monitor);
                } catch (CoreException e) {
                    throw new InvocationTargetException(e);
                } catch (OperationCanceledException e) {
                    throw new InterruptedException();
                } finally {
                    monitor.done();
                }
            }
        }
        UpdateRunnable op= new UpdateRunnable();
        try {
            getContainer().run(true, false, new WorkspaceModifyDelegatingOperation(op));
            return op.infoStatus;
        } catch (InvocationTargetException e) {
            final String title= "New Java Project"; //NewWizardMessages.JavaProjectWizardSecondPage_error_title; 
            final String message= "An error occurred while creating project. Check log for details."; //NewWizardMessages.JavaProjectWizardSecondPage_error_message; 
            perform(e, getShell(), title, message);
        } catch (InterruptedException e) {
            // cancel pressed
        }
        return null;
    }

    protected void perform(CoreException e, Shell shell, String title, String message) {
        RuntimePlugin.getInstance().logException(message, e);
        IStatus status= e.getStatus();
        if (status != null) {
            ErrorDialog.openError(shell, title, message, status);
        } else {
            displayMessageDialog(e, e.getMessage(), shell, title, message);
        }
    }

    protected void perform(InvocationTargetException e, Shell shell, String title, String message) {
        Throwable target= e.getTargetException();
        if (target instanceof CoreException) {
            perform((CoreException)target, shell, title, message);
        } else {
            RuntimePlugin.getInstance().logException(message, e);
            if (e.getMessage() != null && e.getMessage().length() > 0) {
                displayMessageDialog(e, e.getMessage(), shell, title, message);
            } else {
                displayMessageDialog(e, target.getMessage(), shell, title, message);
            }
        }
    }
    private void displayMessageDialog(Throwable t, String exceptionMessage, Shell shell, String title, String message) {
        StringWriter msg= new StringWriter();
        if (message != null) {
            msg.write(message);
            msg.write("\n\n"); //$NON-NLS-1$
        }
        if (exceptionMessage == null || exceptionMessage.length() == 0)
            msg.write("See the Error Log for more details.");
        else
            msg.write(exceptionMessage);
        MessageDialog.openError(shell, title, msg.toString());          
    }   

    final IStatus updateProject(IProgressMonitor monitor) throws CoreException, InterruptedException {
        IStatus result= new Status(IStatus.OK, RuntimePlugin.IMP_RUNTIME, "Ok");

        fCurrProject= fFirstPage.getProjectHandle();
        fCurrProjectLocation= getProjectLocationURI();

        if (monitor == null) {
            monitor= new NullProgressMonitor();
        }
        try {
            monitor.beginTask("Initializing project...", 7);
            if (monitor.isCanceled()) {
                throw new OperationCanceledException();
            }

            URI realLocation= fCurrProjectLocation;
            if (fCurrProjectLocation == null) { // inside workspace
                try {
                    URI rootLocation= ResourcesPlugin.getWorkspace().getRoot().getLocationURI();
                    realLocation= new URI(rootLocation.getScheme(), null, Path.fromPortableString(rootLocation.getPath()).append(fCurrProject.getName())
                            .toString(), null);
                } catch (URISyntaxException e) {
                    Assert.isTrue(false, "Can't happen"); //$NON-NLS-1$
                }
            }

            rememberExistingFiles(realLocation);

            try {
                createProject(fCurrProject, fCurrProjectLocation, new SubProgressMonitor(monitor, 2));
            } catch (CoreException e) {
                if (e.getStatus().getCode() == IResourceStatus.FAILED_READ_METADATA) {
                    result= new Status(IStatus.INFO, RuntimePlugin.IMP_RUNTIME,
                            MessageFormat.format("A problem occurred while creating the project from existing source:\n\n''{0}''\n\nThe corrupt project file will be replaced by a valid one.",
                                            e.getLocalizedMessage()));

                    deleteProjectFile(realLocation);
                    if (fCurrProject.exists())
                        fCurrProject.delete(true, null);

                    createProject(fCurrProject, fCurrProjectLocation, null);
                } else {
                    throw e;
                }
            }

            IClasspathEntry[] entries= null;
            IPath outputLocation= null;

            if (fFirstPage.getDetect()) {
                if (!fCurrProject.getFile(FILENAME_CLASSPATH).exists()) {
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
                    createFolder(folder, true, true, new SubProgressMonitor(monitor, 1));
                } else {
                    monitor.worked(1);
                }

                if (binPath.segmentCount() > 0 && !binPath.equals(srcPath)) {
                    IFolder folder= fCurrProject.getFolder(binPath);
                    createDerivedFolder(folder, true, true, new SubProgressMonitor(monitor, 1));
                } else {
                    monitor.worked(1);
                }

                final IPath projectPath= fCurrProject.getFullPath();

                // configure the classpath entries, including the default jre library.
                List<IClasspathEntry> cpEntries= new ArrayList<IClasspathEntry>();
                cpEntries.add(JavaCore.newSourceEntry(projectPath.append(srcPath)));
                cpEntries.addAll(Arrays.asList(getDefaultClasspathEntry()));
                entries= (IClasspathEntry[]) cpEntries.toArray(new IClasspathEntry[cpEntries.size()]);

                // configure the output location
                outputLocation= projectPath.append(binPath);
            } else {
                IPath projectPath= fCurrProject.getFullPath();
                List<IClasspathEntry> cpEntries= new ArrayList<IClasspathEntry>();
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
            configureJavaProject(new SubProgressMonitor(monitor, 3)); // create the Java project to allow the use of the new source folder page
            getProjectNature().addToProject(fCurrProject);
        } finally {
            monitor.done();
        }
        return result;
    }

    /**
     * Creates a folder and all parent folders if not existing.
     * Project must exist.
     * <code> org.eclipse.ui.dialogs.ContainerGenerator</code> is too heavy
     * (creates a runnable)
     */
    public static void createFolder(IFolder folder, boolean force, boolean local, IProgressMonitor monitor) throws CoreException {
        if (!folder.exists()) {
            IContainer parent= folder.getParent();
            if (parent instanceof IFolder) {
                createFolder((IFolder)parent, force, local, null);
            }
            folder.create(force, local, monitor);
        }
    }
    
    public static void createDerivedFolder(IFolder folder, boolean force, boolean local, IProgressMonitor monitor) throws CoreException {
        if (!folder.exists()) {
            IContainer parent= folder.getParent();
            if (parent instanceof IFolder) {
                createDerivedFolder((IFolder)parent, force, local, null);
            }
            folder.create(force ? (IResource.FORCE | IResource.DERIVED) : IResource.DERIVED, local, monitor);
        }
    }

    private URI getProjectLocationURI() throws CoreException {
        if (fFirstPage.isInWorkspace()) {
            return null;
        }
        return URIUtil.toURI(fFirstPage.getLocationPath());
    }

    private void deleteProjectFile(URI projectLocation) throws CoreException {
        IFileStore file= EFS.getStore(projectLocation);
        if (file.fetchInfo().exists()) {
            IFileStore projectFile= file.getChild(FILENAME_PROJECT);
            if (projectFile.fetchInfo().exists()) {
                projectFile.delete(EFS.NONE, null);
            }
        }
    }

    private void rememberExistingFiles(URI projectLocation) throws CoreException {
        fDotProjectBackup= null;
        fDotClasspathBackup= null;

        IFileStore file= EFS.getStore(projectLocation);
        if (file.fetchInfo().exists()) {
            IFileStore projectFile= file.getChild(FILENAME_PROJECT);
            if (projectFile.fetchInfo().exists()) {
                fDotProjectBackup= createBackup(projectFile, "project-desc"); //$NON-NLS-1$ 
            }
            IFileStore classpathFile= file.getChild(FILENAME_CLASSPATH);
            if (classpathFile.fetchInfo().exists()) {
                fDotClasspathBackup= createBackup(classpathFile, "classpath-desc"); //$NON-NLS-1$ 
            }
        }
    }

    private void restoreExistingFiles(URI projectLocation, IProgressMonitor monitor) throws CoreException {
        int ticks= ((fDotProjectBackup != null ? 1 : 0) + (fDotClasspathBackup != null ? 1 : 0)) * 2;
        monitor.beginTask("", ticks); //$NON-NLS-1$
        try {
            IFileStore projectFile= EFS.getStore(projectLocation).getChild(FILENAME_PROJECT);
            projectFile.delete(EFS.NONE, new SubProgressMonitor(monitor, 1));
            if (fDotProjectBackup != null) {
                copyFile(fDotProjectBackup, projectFile, new SubProgressMonitor(monitor, 1));
            }
        } catch (IOException e) {
            IStatus status= new Status(IStatus.ERROR, JavaUI.ID_PLUGIN, IStatus.ERROR,
            //NewWizardMessages.JavaProjectWizardSecondPage_problem_restore_project,
                    "Problem while restoring backup for .project", e);
            throw new CoreException(status);
        }
        try {
            IFileStore classpathFile= EFS.getStore(projectLocation).getChild(FILENAME_CLASSPATH);
            classpathFile.delete(EFS.NONE, new SubProgressMonitor(monitor, 1));
            if (fDotClasspathBackup != null) {
                copyFile(fDotClasspathBackup, classpathFile, new SubProgressMonitor(monitor, 1));
            }
        } catch (IOException e) {
            IStatus status= new Status(IStatus.ERROR, JavaUI.ID_PLUGIN, IStatus.ERROR,
            //NewWizardMessages.JavaProjectWizardSecondPage_problem_restore_classpath,
                    "Problem while restoring backup for .classpath", e);
            throw new CoreException(status);
        }
    }

    private File createBackup(IFileStore source, String name) throws CoreException {
        try {
            File bak= File.createTempFile("eclipse-" + name, ".bak"); //$NON-NLS-1$//$NON-NLS-2$
            copyFile(source, bak);
            return bak;
        } catch (IOException e) {
            IStatus status= new Status(IStatus.ERROR, JavaUI.ID_PLUGIN, IStatus.ERROR, MessageFormat.format(
            //NewWizardMessages.JavaProjectWizardSecondPage_problem_backup,
                    "Problem while creating backup for ''{0}''", name), e);
            throw new CoreException(status);
        }
    }

    private void copyFile(IFileStore source, File target) throws IOException, CoreException {
        InputStream is= source.openInputStream(EFS.NONE, null);
        FileOutputStream os= new FileOutputStream(target);
        copyFile(is, os);
    }

    private void copyFile(File source, IFileStore target, IProgressMonitor monitor) throws IOException, CoreException {
        FileInputStream is= new FileInputStream(source);
        OutputStream os= target.openOutputStream(EFS.NONE, monitor);
        copyFile(is, os);
    }

    private void copyFile(InputStream is, OutputStream os) throws IOException {
        try {
            byte[] buffer= new byte[8192];
            while (true) {
                int bytesRead= is.read(buffer);
                if (bytesRead == -1)
                    break;

                os.write(buffer, 0, bytesRead);
            }
        } finally {
            try {
                is.close();
            } finally {
                os.close();
            }
        }
    }

    private IClasspathEntry[] getDefaultClasspathEntry() {
        // First create a classpath entry for the language-specific runtime
        List<IClasspathEntry> cpEntries= new ArrayList<IClasspathEntry>();
        List<IClasspathEntry> langRuntimeCPEs= createLanguageRuntimeEntries();

        if (langRuntimeCPEs != null) {
            cpEntries.addAll(langRuntimeCPEs);
        }

        // Now try to find a compatible JRE
        String compliance= fFirstPage.getJRECompliance();
        IVMInstall inst= findMatchingJREInstall(compliance);
        IPath jreContainerPath= new Path(JavaRuntime.JRE_CONTAINER);

        if (inst != null) {
            IPath newPath= jreContainerPath.append(inst.getVMInstallType().getId()).append(inst.getName());
            IClasspathEntry jreCPE= JavaCore.newContainerEntry(newPath);
            cpEntries.add(jreCPE);
        } else {
            // Didn't find a compatible JRE; use the default
            IClasspathEntry[] defaultJRELibrary= PreferenceConstants.getDefaultJRELibrary();

            for(int i=0; i < defaultJRELibrary.length; i++) {
                cpEntries.add(defaultJRELibrary[i]);
            }
        }
        return cpEntries.toArray(new IClasspathEntry[cpEntries.size()]);
    }

    private IVMInstall findMatchingJREInstall(String compliance) {
        IVMInstallType vmInstallType= JavaRuntime.getVMInstallTypes()[0];
        IVMInstall[] vmInstalls= vmInstallType.getVMInstalls();

        for(int i= 0; i < vmInstalls.length; i++) {
            if (vmInstalls[i] instanceof IVMInstall2) {
                IVMInstall2 vmInstall2= (IVMInstall2) vmInstalls[i];
                String vmVers= vmInstall2.getJavaVersion();

                if (vmVers.startsWith(compliance)) {
                    return vmInstalls[i];
                }
            }
        }
        return null;
    }

    private void removeProject() {
        if (fCurrProject == null || !fCurrProject.exists()) {
            return;
        }

        IRunnableWithProgress op= new IRunnableWithProgress() {
            public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                doRemoveProject(monitor);
            }
        };

        try {
            getContainer().run(true, true, new WorkspaceModifyDelegatingOperation(op));
        } catch (InvocationTargetException e) {
            final String title= //NewWizardMessages.JavaProjectWizardSecondPage_error_remove_title; 
            "Error Creating Java Project";
            final String message= //NewWizardMessages.JavaProjectWizardSecondPage_error_remove_message; 
            "An error occurred while removing a temporary project.";
            perform(e, getShell(), title, message);
        } catch (InterruptedException e) {
            // cancel pressed
        }
    }

    final void doRemoveProject(IProgressMonitor monitor) throws InvocationTargetException {
        final boolean noProgressMonitor= (fCurrProjectLocation == null); // inside workspace
        if (monitor == null || noProgressMonitor) {
            monitor= new NullProgressMonitor();
        }
        monitor.beginTask( //NewWizardMessages.JavaProjectWizardSecondPage_operation_remove,
                "Removing project...", 3);
        try {
            try {
                URI projLoc= fCurrProject.getLocationURI();

                boolean removeContent= !fKeepContent && fCurrProject.isSynchronized(IResource.DEPTH_INFINITE);
                fCurrProject.delete(removeContent, false, new SubProgressMonitor(monitor, 2));

                restoreExistingFiles(projLoc, new SubProgressMonitor(monitor, 1));
            } finally {
                enableAutoBuild(fIsAutobuild.booleanValue()); // fIsAutobuild must be set
                fIsAutobuild= null;
            }
        } catch (CoreException e) {
            throw new InvocationTargetException(e);
        } finally {
            monitor.done();
            fCurrProject= null;
            fKeepContent= false;
        }
    }

    protected void openResource(final IFile resource) {
        Display.getDefault().asyncExec(new Runnable() {
            public void run() {
                final IWorkbenchPage activePage= PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                if (activePage != null) {
                    final Display display= getShell().getDisplay();
                    if (display != null) {
                        display.asyncExec(new Runnable() {
                            public void run() {
                                try {
                                    IDE.openEditor(activePage, resource, true);
                                } catch (PartInitException e) {
                                    RuntimePlugin.getInstance().getLog().log(new Status(IStatus.ERROR, RuntimePlugin.IMP_RUNTIME, "Error opening editor on newly-created source file", e));
                                }
                            }
                        });
                    }
                }
            }
        });
    }
}
