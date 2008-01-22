package org.eclipse.imp.ui.explorer;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.imp.core.IMPMessages;
import org.eclipse.imp.editor.EditorUtility;
import org.eclipse.imp.language.ILanguageService;
import org.eclipse.imp.language.Language;
import org.eclipse.imp.language.LanguageRegistry;
import org.eclipse.imp.model.ICompilationUnit;
import org.eclipse.imp.model.ISourceEntity;
import org.eclipse.imp.model.ISourceFolder;
import org.eclipse.imp.model.ISourceProject;
import org.eclipse.imp.model.IWorkspaceModel;
import org.eclipse.imp.model.ModelFactory;
import org.eclipse.imp.model.ModelFactory.ModelException;
import org.eclipse.imp.preferences.PreferenceConstants;
import org.eclipse.imp.runtime.PluginImages;
import org.eclipse.imp.runtime.RuntimePlugin;
import org.eclipse.imp.utils.ExtensionPointFactory;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.ui.viewsupport.AppearanceAwareLabelProvider;
import org.eclipse.jdt.internal.ui.workingsets.WorkingSetModel;
import org.eclipse.jdt.ui.IContextMenuConstants;
import org.eclipse.jdt.ui.JavaElementLabels;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.util.DelegatingDragAdapter;
import org.eclipse.jface.util.DelegatingDropAdapter;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.util.TransferDragSourceListener;
import org.eclipse.jface.util.TransferDropTargetListener;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.actions.ActionContext;
import org.eclipse.ui.navigator.ICommonMenuConstants;
import org.eclipse.ui.part.ISetSelectionTarget;
import org.eclipse.ui.part.IShowInSource;
import org.eclipse.ui.part.IShowInTarget;
import org.eclipse.ui.part.IShowInTargetList;
import org.eclipse.ui.part.ResourceTransfer;
import org.eclipse.ui.part.ShowInContext;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.views.framelist.Frame;
import org.eclipse.ui.views.framelist.FrameList;
import org.eclipse.ui.views.framelist.TreeFrame;
import org.eclipse.ui.views.navigator.LocalSelectionTransfer;

public class ProjectExplorerPart extends ViewPart implements ISetSelectionTarget, IMenuListener, IShowInTarget, IPropertyChangeListener {
    private TreeViewer fViewer;

    private Set<Language> fLanguages= new HashSet<Language>();

    private ITreeContentProvider fContentProvider;

    private ILabelProvider fLabelProvider;

    private ILabelDecorator fLabelDecorator;

    private FilterUpdater fFilterUpdater;

    private Map<Language, ILabelProvider> fLabelProviderMap= new HashMap<Language, ILabelProvider>();

    private Map<Language, ILabelDecorator> fLabelDecoratorMap= new HashMap<Language, ILabelDecorator>();

    private boolean fIsCurrentLayoutFlat= false;

    private int fRootMode;

    private Menu fContextMenu;

    private ProjectExplorerActionGroup fActionSet;

    private IMemento fMemento;

    private ISelection fLastOpenSelection;

    private ISelectionChangedListener fPostSelectionListener;

    public ProjectExplorerPart() {
        buildProviderMaps();
        fPostSelectionListener= new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
                handlePostSelectionChanged(event);
            }
        };
    }

    private void buildProviderMaps() {
        // Ask the LanguageRegistry for the set of languages, and instantiate
        // ILabelProviders for all languages that define one.
        Collection<Language> languages= LanguageRegistry.getLanguages();
        for(Language language : languages) {
            ILabelProvider labelProvider= (ILabelProvider) ExtensionPointFactory.createExtensionPoint(language, ILanguageService.LABEL_PROVIDER_SERVICE);
            if (labelProvider != null) {
                fLabelProviderMap.put(language, labelProvider);
            }
        }
        fLanguages.addAll(fLabelProviderMap.keySet());
        // TODO Do likewise for ILabelDecorators.
        // Instantiate ILabelDecorators for all languages that define one.
        for(Language language : languages) {
            // TODO Need an extension point for ILabelDecorator, or roll its API in with ILabelProvider
            ILabelDecorator labelDecorator= null; // (ILabelDecorator) ExtensionPointFactory.createExtensionPoint(language, ILanguageService.LABEL_DECORATOR_SERVICE);
            if (labelDecorator != null) {
                fLabelDecoratorMap.put(language, labelDecorator);
            }
        }
    }

    private IPartListener fPartListener= new IPartListener() {
        public void partActivated(IWorkbenchPart part) {
            if (part instanceof IEditorPart)
                editorActivated((IEditorPart) part);
        }

        public void partBroughtToTop(IWorkbenchPart part) {
        }

        public void partClosed(IWorkbenchPart part) {
        }

        public void partDeactivated(IWorkbenchPart part) {
        }

        public void partOpened(IWorkbenchPart part) {
        }
    };

    private ITreeViewerListener fExpansionListener= new ITreeViewerListener() {
        public void treeCollapsed(TreeExpansionEvent event) {
        }

        public void treeExpanded(TreeExpansionEvent event) {
//          Object element= event.getElement();
//          if (element instanceof ICompilationUnit || element instanceof IClassFile)
//            expandMainType(element);
        }
    };

    private Object fWorkingSetModel;

    /**
     * Handles post selection changed in viewer.
     * 
     * Links to editor (if option enabled).
     */
    private void handlePostSelectionChanged(SelectionChangedEvent event) {
        ISelection selection= event.getSelection();
        // If the selection is the same as the one that triggered the last
        // open event then do nothing. The editor already got revealed.
        if (isLinkingEnabled() && !selection.equals(fLastOpenSelection)) {
            linkToEditor((IStructuredSelection) selection);
        }
        fLastOpenSelection= null;
    }

    /**
     * Links to editor (if option enabled)
     */
    private void linkToEditor(IStructuredSelection selection) {
        // ignore selection changes if the package explorer is not the active part.
        // In this case the selection change isn't triggered by a user.
        if (!isActivePart())
            return;
        Object obj= selection.getFirstElement();
        if (selection.size() == 1) {
            IEditorPart part= EditorUtility.isOpenInEditor(obj);
            if (part != null) {
                IWorkbenchPage page= getSite().getPage();
                page.bringToTop(part);
                if (obj instanceof ISourceEntity)
                    EditorUtility.revealInEditor(part, (ISourceEntity) obj);
            }
        }
    }

    private boolean isActivePart() {
        return this == getSite().getPage().getActivePart();
    }

    /**
     * Dispatches to one of several {@link ILabelProvider}s, depending on the language nature of the owning project.
     */
    private class LabelProviderDispatcher implements ILabelProvider {
        private Set<ILabelProviderListener> fListeners= new HashSet<ILabelProviderListener>();

        private Set<ILabelProvider> getRelevantProvidersFor(IProject project) {
            Set<ILabelProvider> providers= new HashSet<ILabelProvider>();
            for(Language lang : fLanguages) {
                String natureId= lang.getNatureID();
                try {
                    if (project.hasNature(natureId) && fLabelProviderMap.containsKey(lang)) {
                        providers.add(fLabelProviderMap.get(lang));
                    }
                } catch (CoreException e) {
                    RuntimePlugin.getInstance().logException("Unable to query project natures of " + project.getName(), e);
                }
            }
            return providers;
        }

        public Image getImage(Object element) {
            // First try the language-specific label providers
            if (element instanceof IResource || element instanceof ISourceEntity) {
                IResource res= (element instanceof ISourceEntity) ? ((ISourceEntity) element).getResource() : (IResource) element;
                IProject project= res.getProject();
                Set<ILabelProvider> relevantProviders= getRelevantProvidersFor(project);

                for(ILabelProvider provider : relevantProviders) {
                    Image image= provider.getImage(element);
                    if (image != null)
                        return image;
                }
            }
            // No language-specific label provider provided a label; use the generic ones
            if (element instanceof ISourceProject) {
                return PluginImages.get(PluginImages.PROJECT_IMAGE);
            } else if (element instanceof ISourceFolder) {
                return PluginImages.get(PluginImages.FOLDER_IMAGE);
            } else if (element instanceof ICompilationUnit) {
                return PluginImages.get(PluginImages.FILE_IMAGE);
            }
            return null;
        }

        public String getText(Object element) {
            if (element instanceof ISourceProject) {
                ISourceProject srcProject= (ISourceProject) element;

                return srcProject.getRawProject().getName();
            } else if (element instanceof ISourceFolder) {
                ISourceFolder srcFolder= (ISourceFolder) element;

                return srcFolder.getName();
            } else if (element instanceof ICompilationUnit) {
                ICompilationUnit cu= (ICompilationUnit) element;

                return cu.getName();
            } else if (element instanceof IResource) {
                IResource res= (IResource) element;
                IProject project= res.getProject();
                Set<ILabelProvider> relevantProviders= getRelevantProvidersFor(project);
                for(ILabelProvider provider : relevantProviders) {
                    String text= provider.getText(element);
                    if (text != null)
                        return text;
                }
            }
            return null;
        }

        public void addListener(ILabelProviderListener listener) {
            fListeners.add(listener);
        }

        public void dispose() {
        }

        public boolean isLabelProperty(Object element, String property) {
            if (element instanceof IResource) {
                IResource res= (IResource) element;
                IProject project= res.getProject();
                Set<ILabelProvider> relevantProviders= getRelevantProvidersFor(project);
                for(ILabelProvider provider : relevantProviders) {
                    if (provider.isLabelProperty(element, property))
                        return true;
                }
            }
            return false;
        }

        public void removeListener(ILabelProviderListener listener) {
            fListeners.remove(listener);
        }
    }

    class FilterUpdater implements IResourceChangeListener {
        private StructuredViewer fViewer;

        public FilterUpdater(StructuredViewer viewer) {
            Assert.isNotNull(viewer);
            fViewer= viewer;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.core.resources.IResourceChangeListener#resourceChanged(org.eclipse.core.resources.IResourceChangeEvent)
         */
        public void resourceChanged(IResourceChangeEvent event) {
            IResourceDelta delta= event.getDelta();
            if (delta == null)
                return;
            IResourceDelta[] projDeltas= delta.getAffectedChildren(IResourceDelta.CHANGED);
            for(int i= 0; i < projDeltas.length; i++) {
                IResourceDelta pDelta= projDeltas[i];
                if ((pDelta.getFlags() & IResourceDelta.DESCRIPTION) != 0) {
                    final Control ctrl= fViewer.getControl();
                    if (ctrl != null && !ctrl.isDisposed()) {
                        // async is needed due to bug 33783
                        ctrl.getDisplay().asyncExec(new Runnable() {
                            public void run() {
                                if (!ctrl.isDisposed())
                                    fViewer.refresh(false);
                            }
                        });
                    }
                }
            }
        }
    }

    /**
     * Answer the property defined by key.
     */
    public Object getAdapter(Class key) {
        if (key.equals(ISelectionProvider.class))
            return fViewer;
        if (key == IShowInSource.class) {
            return getShowInSource();
        }
        if (key == IShowInTargetList.class) {
            return new IShowInTargetList() {
                public String[] getShowInTargetIds() {
                    return new String[] { IPageLayout.ID_RES_NAV };
                }
            };
        }
        // if (key == IContextProvider.class) {
        // return JavaUIHelp.getHelpContextProvider(this, IJavaHelpContextIds.PACKAGES_VIEW);
        // }
        return super.getAdapter(key);
    }

    /**
     * Returns the <code>IShowInSource</code> for this view.
     */
    protected IShowInSource getShowInSource() {
        return new IShowInSource() {
            public ShowInContext getShowInContext() {
                return new ShowInContext(getViewer().getInput(), getViewer().getSelection());
            }
        };
    }

    @Override
    public void createPartControl(Composite parent) {
        fViewer= new TreeViewer(parent);
        fViewer.setUseHashlookup(true);
        initDragAndDrop();
        setProviders();
        // TODO Listen to prefs stores for all language IDE plugins?
        RuntimePlugin.getInstance().getPreferenceStore().addPropertyChangeListener(this);
        MenuManager menuMgr= new MenuManager("#PopupMenu"); //$NON-NLS-1$
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(this);
        fContextMenu= menuMgr.createContextMenu(fViewer.getTree());
        fViewer.getTree().setMenu(fContextMenu);
        // Register viewer with site. This must be done before making the actions.
        IWorkbenchPartSite site= getSite();
        site.registerContextMenu(menuMgr, fViewer);
        site.setSelectionProvider(fViewer);
        site.getPage().addPartListener(fPartListener);
        if (fMemento != null) {
            restoreLinkingEnabled(fMemento);
        }
        makeActions(); // call before registering for selection changes
        // Set input after filter and sorter has been set. This avoids resorting and refiltering.
        restoreFilterAndSorter();
        fViewer.setInput(findInputElement());
        initFrameActions();
        initKeyListener();
        fViewer.addPostSelectionChangedListener(fPostSelectionListener);
        fViewer.addDoubleClickListener(new IDoubleClickListener() {
            public void doubleClick(DoubleClickEvent event) {
                fActionSet.handleDoubleClick(event);
            }
        });
        fViewer.addOpenListener(new IOpenListener() {
            public void open(OpenEvent event) {
                fActionSet.handleOpen(event);
                fLastOpenSelection= event.getSelection();
            }
        });
        IStatusLineManager slManager= getViewSite().getActionBars().getStatusLineManager();
        fViewer.addSelectionChangedListener(new StatusBarUpdater(slManager));
        fViewer.addTreeListener(fExpansionListener);
        if (fMemento != null)
            restoreUIState(fMemento);
        fMemento= null;
        // Set help for the view
        // JavaUIHelp.setHelp(fViewer, IJavaHelpContextIds.PACKAGES_VIEW);
        fillActionBars();
        updateTitle();
        fFilterUpdater= new FilterUpdater(fViewer);
        ResourcesPlugin.getWorkspace().addResourceChangeListener(fFilterUpdater);
    }

    /**
     * Answers whether this part shows the packages flat or hierarchical.
     * 
     * @since 2.1
     */
    public boolean isFlatLayout() {
        return fIsCurrentLayoutFlat;
    }

    void toggleLayout() {
        // Update current state and inform content and label providers
        fIsCurrentLayoutFlat= !fIsCurrentLayoutFlat;
        saveLayoutState(null);
        // fContentProvider.setIsFlatLayout(isFlatLayout());
        // fLabelProvider.setIsFlatLayout(isFlatLayout());
        // ((DecoratingJavaLabelProvider) fViewer.getLabelProvider()).setFlatPackageMode(isFlatLayout());
        fViewer.getControl().setRedraw(false);
        fViewer.refresh();
        fViewer.getControl().setRedraw(true);
    }

    public void rootModeChanged(int newMode) {
        fRootMode= newMode;
        if (showWorkingSets() && fWorkingSetModel == null) {
            // createWorkingSetModel();
            if (fActionSet != null) {
                // fActionSet.getWorkingSetActionGroup().setWorkingSetModel(fWorkingSetModel);
            }
        }
        IStructuredSelection selection= new StructuredSelection(((IStructuredSelection) fViewer.getSelection()).toArray());
        Object input= fViewer.getInput();
        boolean isRootInputChange= JavaCore.create(ResourcesPlugin.getWorkspace().getRoot()).equals(input)
                || (fWorkingSetModel != null && fWorkingSetModel.equals(input)) || input instanceof IWorkingSet;
        try {
            fViewer.getControl().setRedraw(false);
            if (isRootInputChange) {
                fViewer.setInput(null);
            }
            setProviders();
            setSorter();
            fActionSet.getWorkingSetActionGroup().fillFilters(fViewer);
            if (isRootInputChange) {
                fViewer.setInput(findInputElement());
            }
            fViewer.setSelection(selection, true);
        } finally {
            fViewer.getControl().setRedraw(true);
        }
        if (isRootInputChange && showWorkingSets() /* && fWorkingSetModel.needsConfiguration() */) {
            // ConfigureWorkingSetAction action= new ConfigureWorkingSetAction(getSite());
            // action.setWorkingSetModel(fWorkingSetModel);
            // action.run();
            // fWorkingSetModel.configured();
        }
        setTitleToolTip(getTitleToolTip());
    }

    public Object /* WorkingSetModel */getWorkingSetModel() {
        return fWorkingSetModel;
    }

    private void setProviders() {
        // content provider must be set before the label provider
        fContentProvider= createContentProvider();
        // fContentProvider.setIsFlatLayout(fIsCurrentLayoutFlat );
        // fViewer.setComparer(createElementComparer());
        fViewer.setContentProvider(fContentProvider);
        fLabelProvider= createLabelProvider();
        // fLabelProvider.setIsFlatLayout(fIsCurrentLayoutFlat);
        fViewer.setLabelProvider(new DecoratingLabelProvider(fLabelProvider, fLabelDecorator));
        // problem decoration provided by ProjectLabelProvider
    }

    private void makeActions() {
        fActionSet= new ProjectExplorerActionGroup(this);
        // if (fWorkingSetModel != null)
        // fActionSet.getWorkingSetActionGroup().setWorkingSetModel(fWorkingSetModel);
    }

    private void initFrameActions() {
        fActionSet.getUpAction().update();
        fActionSet.getBackAction().update();
        fActionSet.getForwardAction().update();
    }

    /**
     * Create the KeyListener for doing the refresh on the viewer.
     */
    private void initKeyListener() {
        fViewer.getControl().addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent event) {
                fActionSet.handleKeyEvent(event);
            }
        });
    }

    private Object findInputElement() {
        try {
            if (showWorkingSets()) {
                return null /* fWorkingSetModel */;
            } else {
                Object input= getSite().getPage().getInput();
                if (input instanceof IWorkspace) {
                    return JavaCore.create(((IWorkspace) input).getRoot());
                } else if (input instanceof IContainer) {
                    ISourceEntity element= ModelFactory.open((IContainer) input);
                    if (element != null /* && element.exists() */)
                        return element;
                    return input;
                }
                // 1GERPRT: ITPJUI:ALL - Packages View is empty when shown in Type Hierarchy Perspective
                // we can't handle the input
                // fall back to show the workspace
                return ModelFactory.open(ResourcesPlugin.getWorkspace().getRoot());
            }
        } catch (ModelException e) {
            RuntimePlugin.getInstance().logException("Error opening input element", e);
            return null;
        }
    }

    /**
     * Returns the name for the given element. Used as the name for the current frame.
     */
    String getFrameName(Object element) {
        if (element instanceof ISourceEntity) {
            return ((ISourceEntity) element).getName();
//         } else if (element instanceof WorkingSetModel) {
//         return ""; //$NON-NLS-1$
        } else {
            return fLabelProvider.getText(element);
        }
    }

    public int getRootMode() {
        return fRootMode;
    }

    /* package */ boolean showProjects() {
        return fRootMode == ViewActionGroup.SHOW_PROJECTS;
    }

    /* package */ boolean showWorkingSets() {
        return fRootMode == ViewActionGroup.SHOW_WORKING_SETS;
    }

    /**
     * Updates the title text and title tool tip. Called whenever the input of the viewer changes.
     */
    void updateTitle() {
        Object input= fViewer.getInput();
        if (input == null || (input instanceof IWorkspaceModel)) {
            setContentDescription(""); //$NON-NLS-1$
            setTitleToolTip(""); //$NON-NLS-1$
        } else {
            String inputText= JavaElementLabels.getTextLabel(input, AppearanceAwareLabelProvider.DEFAULT_TEXTFLAGS);
            setContentDescription(inputText);
            setTitleToolTip(getToolTipText(input));
        }
    }

    /**
     * Returns the tool tip text for the given element.
     */
    String getToolTipText(Object element) {
        String result;
        if (!(element instanceof IResource)) {
            if (element instanceof IWorkspaceModel) {
                result= ExplorerMessages.ProjectExplorerPart_workspace;
            } else if (element instanceof ISourceEntity) {
                result= JavaElementLabels.getTextLabel(element, JavaElementLabels.ALL_FULLY_QUALIFIED);
            } else if (element instanceof IWorkingSet) {
                result= ((IWorkingSet) element).getLabel();
            } else if (element instanceof WorkingSetModel) {
                result= ExplorerMessages.ProjectExplorerPart_workingSetModel;
            } else {
                result= fLabelProvider.getText(element);
            }
        } else {
            IPath path= ((IResource) element).getFullPath();
            if (path.isRoot()) {
                result= ExplorerMessages.ProjectExplorer_title;
            } else {
                result= path.makeRelative().toString();
            }
        }
        if (fRootMode == ViewActionGroup.SHOW_PROJECTS) {
            // if (fWorkingSetLabel == null)
            // return result;
            if (result.length() == 0)
                return IMPMessages.format(ExplorerMessages.ProjectExplorer_toolTip, new String[] { null /* fWorkingSetLabel */});
            return IMPMessages.format(ExplorerMessages.ProjectExplorer_toolTip2, new String[] { result, null /* fWorkingSetLabel */});
        } else { // Working set mode. During initialization element and action set can be null.
            if (element != null && !(element instanceof IWorkingSet) && !(element instanceof WorkingSetModel) && fActionSet != null) {
                FrameList frameList= fActionSet.getFrameList();
                int index= frameList.getCurrentIndex();
                IWorkingSet ws= null;
                while (index >= 0) {
                    Frame frame= frameList.getFrame(index);
                    if (frame instanceof TreeFrame) {
                        Object input= ((TreeFrame) frame).getInput();
                        if (input instanceof IWorkingSet) {
                            ws= (IWorkingSet) input;
                            break;
                        }
                    }
                    index--;
                }
                if (ws != null) {
                    return IMPMessages.format(ExplorerMessages.ProjectExplorer_toolTip3, new String[] { ws.getLabel(), result });
                } else {
                    return result;
                }
            } else {
                return result;
            }
        }
    }

    public String getTitleToolTip() {
        if (fViewer == null)
            return super.getTitleToolTip();
        return getToolTipText(fViewer.getInput());
    }

    /**
     * Returns the Viewer.
     */
    TreeViewer getViewer() {
        return fViewer;
    }

    /**
     * Returns the TreeViewer.
     */
    public TreeViewer getTreeViewer() {
        return fViewer;
    }

    private ILabelProvider createLabelProvider() {
        return new LabelProviderDispatcher();
    }

    private ITreeContentProvider createContentProvider() {
        return new ITreeContentProvider() {
            public Object[] getChildren(Object parentElement) {
                if (parentElement instanceof IWorkspaceModel) {
                    IWorkspaceModel ws= (IWorkspaceModel) parentElement;

                    return ws.getProjects();
                } else if (parentElement instanceof ISourceProject) {
                    ISourceProject srcProject= (ISourceProject) parentElement;

                    return srcProject.getChildren();
                } else if (parentElement instanceof ISourceFolder) {
                    ISourceFolder srcFolder= (ISourceFolder) parentElement;

                    return srcFolder.getChildren();
                }
                return null;
            }

            public Object getParent(Object element) {
                if (element instanceof IWorkspaceModel) {
                    return null;
                } else if (element instanceof ISourceProject) {
                    try {
                        return ModelFactory.open(ResourcesPlugin.getWorkspace().getRoot());
                    } catch (ModelException e) {
                        RuntimePlugin.getInstance().logException(e.getMessage(), e);
                    }
                } else if (element instanceof ISourceFolder) {
                    return ((ISourceFolder) element).getParent();
                } else if (element instanceof ICompilationUnit) {
                    ICompilationUnit cu= (ICompilationUnit) element;
                    IPath path= cu.getPath();
                    if (path.isAbsolute())
                        path= path.removeFirstSegments(1);
                    if (path.segmentCount() > 1) {
                        try {
                            return ModelFactory.open(cu.getFile().getProject().findMember(path.removeLastSegments(1)));
                        } catch (ModelException e) {
                            RuntimePlugin.getInstance().logException(e.getMessage(), e);
                        }
                    }
                    return cu.getProject();
                }
                return null;
            }

            public boolean hasChildren(Object element) {
                if (element instanceof IWorkspaceModel) {
                    IWorkspaceModel ws= (IWorkspaceModel) element;
                    return ws.getProjects().length > 0;
                } else if (element instanceof ISourceProject) {
                    ISourceProject sp= (ISourceProject) element;
                    return sp.getChildren().length > 0;
                } else if (element instanceof ISourceFolder) {
                    ISourceFolder sf= (ISourceFolder) element;
                    return sf.getChildren().length > 0;
                }
                return false;
            }

            public Object[] getElements(Object inputElement) {
                if (inputElement instanceof IWorkspaceModel) {
                    IWorkspaceModel ws= (IWorkspaceModel) inputElement;
                    return ws.getProjects();
                }
                return null;
            }

            public void dispose() {
                // TODO Auto-generated method stub
            }

            public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
                // TODO Auto-generated method stub
            }
        };
    }

    private void initDragAndDrop() {
        initDrag();
        initDrop();
    }

    private void initDrag() {
        int ops= DND.DROP_COPY | DND.DROP_MOVE | DND.DROP_LINK;
        Transfer[] transfers= new Transfer[] { LocalSelectionTransfer.getInstance(), ResourceTransfer.getInstance(), FileTransfer.getInstance() };
        TransferDragSourceListener[] dragListeners= new TransferDragSourceListener[] {
        // new SelectionTransferDragAdapter(fViewer),
        // new ResourceTransferDragAdapter(fViewer),
        // new FileTransferDragAdapter(fViewer)
        };
        fViewer.addDragSupport(ops, transfers, new DelegatingDragAdapter(/* fViewer, dragListeners */));
    }

    private void initDrop() {
        int ops= DND.DROP_COPY | DND.DROP_MOVE | DND.DROP_LINK | DND.DROP_DEFAULT;
        Transfer[] transfers= new Transfer[] { LocalSelectionTransfer.getInstance(), FileTransfer.getInstance() };
        TransferDropTargetListener[] dropListeners= new TransferDropTargetListener[] {
        // new SelectionTransferDropAdapter(fViewer),
        // new FileTransferDropAdapter(fViewer),
        // new WorkingSetDropAdapter(this)
        };
        fViewer.addDropSupport(ops, transfers, new DelegatingDropAdapter(/* dropListeners */));
    }

    private boolean fLinkingEnabled;

    public boolean isLinkingEnabled() {
        return fLinkingEnabled;
    }

    /**
     * An editor has been activated. Set the selection in this Project Viewer to be the editor's input, if linking is enabled.
     */
    void editorActivated(IEditorPart editor) {
        if (!isLinkingEnabled())
            return;
        Object input= getElementOfInput(editor.getEditorInput());
        if (input == null)
            return;
        if (!inputIsSelected(editor.getEditorInput()))
            showInput(input);
        else
            getTreeViewer().getTree().showSelection();
    }

    private void showInput(Object input) {
        // TODO Auto-generated method stub
    }

    public Object getViewPartInput() {
        if (fViewer != null) {
            return fViewer.getInput();
        }
        return null;
    }

    public void collapseAll() {
        try {
            fViewer.getControl().setRedraw(false);
            fViewer.collapseToLevel(getViewPartInput(), AbstractTreeViewer.ALL_LEVELS);
        } finally {
            fViewer.getControl().setRedraw(true);
        }
    }

    private boolean inputIsSelected(IEditorInput editorInput) {
        // TODO Auto-generated method stub
        return false;
    }

    private Object getElementOfInput(IEditorInput editorInput) {
        // TODO Determine what element in the Project Explorer corresponds to the given IEditorInput, if any
        if (editorInput instanceof IFileEditorInput) {
            IFileEditorInput fileInput= (IFileEditorInput) editorInput;
            try {
                return ModelFactory.open(fileInput.getFile());
            } catch (ModelException e) {
                RuntimePlugin.getInstance().logException("Error determining Project Explorer element corresponding to " + fileInput.getName(), e);
            }
        }
        return null;
    }

    private void fillActionBars() {
        IActionBars actionBars= getViewSite().getActionBars();
        fActionSet.fillActionBars(actionBars);
    }

    @Override
    public void setFocus() {
        fViewer.getTree().setFocus();
    }

    /**
     * Returns the current selection.
     */
    private ISelection getSelection() {
        return fViewer.getSelection();
    }

    public void selectReveal(ISelection selection) {
        // TODO Package Explorer converts the selection into IJavaElements first; do something similar?
        fViewer.setSelection(selection, true);
    }

    public void menuAboutToShow(IMenuManager menu) {
        if (!menu.isEmpty())
            return;

        menu.add(new Separator(IContextMenuConstants.GROUP_NEW));
        menu.add(new GroupMarker(IContextMenuConstants.GROUP_GOTO));
        menu.add(new Separator(IContextMenuConstants.GROUP_OPEN));
        menu.add(new GroupMarker(IContextMenuConstants.GROUP_SHOW));
        menu.add(new Separator(ICommonMenuConstants.GROUP_EDIT));
        menu.add(new Separator(IContextMenuConstants.GROUP_REORGANIZE));
        menu.add(new Separator(IContextMenuConstants.GROUP_GENERATE));
        menu.add(new Separator(IContextMenuConstants.GROUP_SEARCH));
        menu.add(new Separator(IContextMenuConstants.GROUP_BUILD));
        menu.add(new Separator(IContextMenuConstants.GROUP_ADDITIONS));
        menu.add(new Separator(IContextMenuConstants.GROUP_VIEWER_SETUP));
        menu.add(new Separator(IContextMenuConstants.GROUP_PROPERTIES));

        fActionSet.setContext(new ActionContext(getSelection()));
        fActionSet.fillContextMenu(menu);
        fActionSet.setContext(null);
    }

    public boolean show(ShowInContext context) {
        ISelection selection= context.getSelection();
        if (selection instanceof IStructuredSelection) {
            // fix for 64634 Navigate/Show in/Package Explorer doesn't work 
            IStructuredSelection structuredSelection= ((IStructuredSelection) selection);
            if (structuredSelection.size() == 1 && tryToReveal(structuredSelection.getFirstElement()))
                return true;
        }
        
        Object input= context.getInput();
        if (input instanceof IEditorInput) {
            Object elementOfInput= getElementOfInput((IEditorInput)context.getInput());
            return elementOfInput != null && tryToReveal(elementOfInput);
        }
        return false;
    }

    public boolean tryToReveal(Object element) {
        if (revealElementOrParent(element))
            return true;
        // TODO Package Explorer asks user whether to remove filters to make the element visible
        return false;
    }

    private boolean revealElementOrParent(Object element) {
        if (revealAndVerify(element))
            return true;
        element= getVisibleParent(element);
        if (element != null) {
            if (revealAndVerify(element))
                return true;
//            if (element instanceof IJavaElement) {
//                IResource resource= ((IJavaElement)element).getResource();
//                if (resource != null) {
//                    if (revealAndVerify(resource))
//                        return true;
//                }
//            }
        }
        return false;
    }

    private Object getVisibleParent(Object object) {
        if (object == null)
                return null;
        if (!(object instanceof ISourceEntity))
            return object;
        try {
            if (object instanceof ISourceProject) {
                return ModelFactory.open(ResourcesPlugin.getWorkspace().getRoot());
            } else if (object instanceof ISourceFolder) {
                return((ISourceFolder) object).getParent();
            } else if (object instanceof ICompilationUnit) {
                return ModelFactory.open(((ICompilationUnit) object).getFile().getParent());
            }
        } catch (ModelException e) {
            RuntimePlugin.getInstance().logException(e.getMessage(), e);
        }
        return null;
    }

    private boolean revealAndVerify(Object element) {
        if (element == null)
                return false;
        selectReveal(new StructuredSelection(element));
        return ! getSite().getSelectionProvider().getSelection().isEmpty();
    }

    public void propertyChange(PropertyChangeEvent event) {
        // TODO Auto-generated method stub
    }

    // Persistance tags.
    static final String TAG_SELECTION= "selection"; //$NON-NLS-1$

    static final String TAG_EXPANDED= "expanded"; //$NON-NLS-1$

    static final String TAG_ELEMENT= "element"; //$NON-NLS-1$

    static final String TAG_PATH= "path"; //$NON-NLS-1$

    static final String TAG_VERTICAL_POSITION= "verticalPosition"; //$NON-NLS-1$

    static final String TAG_HORIZONTAL_POSITION= "horizontalPosition"; //$NON-NLS-1$

    static final String TAG_FILTERS= "filters"; //$NON-NLS-1$

    static final String TAG_FILTER= "filter"; //$NON-NLS-1$

    static final String TAG_LAYOUT= "layout"; //$NON-NLS-1$

    static final String TAG_CURRENT_FRAME= "currentFramge"; //$NON-NLS-1$

    static final String TAG_ROOT_MODE= "rootMode"; //$NON-NLS-1$

    static final String SETTING_MEMENTO= "memento"; //$NON-NLS-1$

    private static final int HIERARCHICAL_LAYOUT= 0x1;

    private static final int FLAT_LAYOUT= 0x2;

    public void saveState(IMemento memento) {
        if (fViewer == null) {
            // part has not been created
            if (fMemento != null) // Keep the old state;
                memento.putMemento(fMemento);
            return;
        }
        memento.putInteger(TAG_ROOT_MODE, fRootMode);
        // if (fWorkingSetModel != null)
        // fWorkingSetModel.saveState(memento);
        // disable the persisting of state which can trigger expensive operations as
        // a side effect: see bug 52474 and 53958
        // saveCurrentFrame(memento);
        // saveExpansionState(memento);
        // saveSelectionState(memento);
        saveLayoutState(memento);
        saveLinkingEnabled(memento);
        // commented out because of http://bugs.eclipse.org/bugs/show_bug.cgi?id=4676
        // saveScrollState(memento, fViewer.getTree());
        // fActionSet.saveFilterAndSorterState(memento);
    }

    /*
     * private void saveCurrentFrame(IMemento memento) { FrameAction action = fActionSet.getUpAction(); FrameList frameList= action.getFrameList();
     * 
     * if (frameList.getCurrentIndex() > 0) { TreeFrame currentFrame = (TreeFrame) frameList.getCurrentFrame(); // don't persist the working set model as the
     * current frame if (currentFrame.getInput() instanceof WorkingSetModel) return; IMemento frameMemento = memento.createChild(TAG_CURRENT_FRAME);
     * currentFrame.saveState(frameMemento); } }
     */
    private void saveLinkingEnabled(IMemento memento) {
        memento.putInteger(PreferenceConstants.LINK_EXPLORER_TO_EDITOR, fLinkingEnabled ? 1 : 0);
    }

    private void saveLayoutState(IMemento memento) {
        if (memento != null) {
            memento.putInteger(TAG_LAYOUT, getLayoutAsInt());
        } else {
            // if memento is null save in preference store
            IPreferenceStore store= RuntimePlugin.getInstance().getPreferenceStore();
            store.setValue(TAG_LAYOUT, getLayoutAsInt());
        }
    }

    private int getLayoutAsInt() {
        if (fIsCurrentLayoutFlat)
            return FLAT_LAYOUT;
        else
            return HIERARCHICAL_LAYOUT;
    }

    protected void saveScrollState(IMemento memento, Tree tree) {
        ScrollBar bar= tree.getVerticalBar();
        int position= bar != null ? bar.getSelection() : 0;
        memento.putString(TAG_VERTICAL_POSITION, String.valueOf(position));
        // save horizontal position
        bar= tree.getHorizontalBar();
        position= bar != null ? bar.getSelection() : 0;
        memento.putString(TAG_HORIZONTAL_POSITION, String.valueOf(position));
    }

    protected void saveSelectionState(IMemento memento) {
        Object elements[]= ((IStructuredSelection) fViewer.getSelection()).toArray();
        if (elements.length > 0) {
            IMemento selectionMem= memento.createChild(TAG_SELECTION);
            for(int i= 0; i < elements.length; i++) {
                IMemento elementMem= selectionMem.createChild(TAG_ELEMENT);
                // we can only persist JavaElements for now
                Object o= elements[i];
                if (o instanceof ISourceEntity)
                    // TODO Need a persistent key like IJavaElement.toHandleIdentifier()
                    elementMem.putString(TAG_PATH, Integer.toString(((ISourceEntity) elements[i]).hashCode()));
            }
        }
    }

    protected void saveExpansionState(IMemento memento) {
        Object expandedElements[]= fViewer.getVisibleExpandedElements();
        if (expandedElements.length > 0) {
            IMemento expandedMem= memento.createChild(TAG_EXPANDED);
            for(int i= 0; i < expandedElements.length; i++) {
                IMemento elementMem= expandedMem.createChild(TAG_ELEMENT);
                // we can only persist JavaElements for now
                Object o= expandedElements[i];
                if (o instanceof ISourceEntity)
                    // TODO Need a persistent key like IJavaElement.toHandleIdentifier()
                    elementMem.putString(TAG_PATH, Integer.toString(((ISourceEntity) expandedElements[i]).hashCode()));
            }
        }
    }

    private void restoreFilterAndSorter() {
        fViewer.addFilter(new OutputFolderFilter());
        setSorter();
        if (fMemento != null)
            fActionSet.restoreFilterAndSorterState(fMemento);
    }

    private void setSorter() {
        if (showWorkingSets()) {
            // fViewer.setSorter(new WorkingSetAwareJavaElementSorter());
        } else {
            // fViewer.setSorter(new JavaElementSorter());
        }
    }

    private void restoreUIState(IMemento memento) {
        // see comment in save state
        // restoreCurrentFrame(memento);
        // restoreExpansionState(memento);
        // restoreSelectionState(memento);
        // commented out because of http://bugs.eclipse.org/bugs/show_bug.cgi?id=4676
        // restoreScrollState(memento, fViewer.getTree());
    }

    /*
     * private void restoreCurrentFrame(IMemento memento) { IMemento frameMemento = memento.getChild(TAG_CURRENT_FRAME);
     * 
     * if (frameMemento != null) { FrameAction action = fActionSet.getUpAction(); FrameList frameList= action.getFrameList(); TreeFrame frame = new
     * TreeFrame(fViewer); frame.restoreState(frameMemento); frame.setName(getFrameName(frame.getInput()));
     * frame.setToolTipText(getToolTipText(frame.getInput())); frameList.gotoFrame(frame); } }
     */
    private void restoreLinkingEnabled(IMemento memento) {
        Integer val= memento.getInteger(PreferenceConstants.LINK_EXPLORER_TO_EDITOR);
        if (val != null) {
            fLinkingEnabled= val.intValue() != 0;
        }
    }

    protected void restoreScrollState(IMemento memento, Tree tree) {
        ScrollBar bar= tree.getVerticalBar();
        if (bar != null) {
            try {
                String posStr= memento.getString(TAG_VERTICAL_POSITION);
                int position;
                position= new Integer(posStr).intValue();
                bar.setSelection(position);
            } catch (NumberFormatException e) {
                // ignore, don't set scrollposition
            }
        }
        bar= tree.getHorizontalBar();
        if (bar != null) {
            try {
                String posStr= memento.getString(TAG_HORIZONTAL_POSITION);
                int position;
                position= new Integer(posStr).intValue();
                bar.setSelection(position);
            } catch (NumberFormatException e) {
                // ignore don't set scroll position
            }
        }
    }

    protected void restoreSelectionState(IMemento memento) {
        IMemento childMem;
        childMem= memento.getChild(TAG_SELECTION);
        if (childMem != null) {
            ArrayList list= new ArrayList();
            IMemento[] elementMem= childMem.getChildren(TAG_ELEMENT);
            for(int i= 0; i < elementMem.length; i++) {
                Object element= JavaCore.create(elementMem[i].getString(TAG_PATH));
                if (element != null)
                    list.add(element);
            }
            fViewer.setSelection(new StructuredSelection(list));
        }
    }

    protected void restoreExpansionState(IMemento memento) {
        IMemento childMem= memento.getChild(TAG_EXPANDED);
        if (childMem != null) {
            ArrayList elements= new ArrayList();
            IMemento[] elementMem= childMem.getChildren(TAG_ELEMENT);
            for(int i= 0; i < elementMem.length; i++) {
                Object element= JavaCore.create(elementMem[i].getString(TAG_PATH));
                if (element != null)
                    elements.add(element);
            }
            fViewer.setExpandedElements(elements.toArray());
        }
    }

    @Override
    public void dispose() {
        if (fContextMenu != null && !fContextMenu.isDisposed())
            fContextMenu.dispose();
        getSite().getPage().removePartListener(fPartListener);
        RuntimePlugin.getInstance().getPreferenceStore().removePropertyChangeListener(this);
        if (fViewer != null) {
            fViewer.removeTreeListener(fExpansionListener);
            XMLMemento memento= XMLMemento.createWriteRoot("projectExplorer"); //$NON-NLS-1$
            saveState(memento);
            StringWriter writer= new StringWriter();
            try {
                memento.save(writer);
                String sectionName= getSectionName();
                IDialogSettings section= RuntimePlugin.getInstance().getDialogSettings().getSection(sectionName);
                if (section == null) {
                    section= RuntimePlugin.getInstance().getDialogSettings().addNewSection(sectionName);
                }
                section.put(SETTING_MEMENTO, writer.getBuffer().toString());
            } catch (IOException e) {
                // don't do anythiung. Simply don't store the settings
            }
        }
        // if (fActionSet != null)
        // fActionSet.dispose();
        if (fFilterUpdater != null)
            ResourcesPlugin.getWorkspace().removeResourceChangeListener(fFilterUpdater);
        // if (fWorkingSetModel != null)
        // fWorkingSetModel.dispose();
        super.dispose();
    }

    private String getSectionName() {
        return "org.eclipse.imp.ui.internal.projectExplorer"; //$NON-NLS-1$
    }
}
