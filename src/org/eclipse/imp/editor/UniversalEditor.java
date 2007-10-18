/*
 * (C) Copyright IBM Corporation 2007
 * 
 * This file is part of the Eclipse IMP.
 */
package org.eclipse.imp.editor;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 1998, 2004  All Rights Reserved
 */

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import lpg.runtime.IPrsStream;
import lpg.runtime.IToken;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTarget;
import org.eclipse.imp.core.ErrorHandler;
import org.eclipse.imp.editor.internal.AnnotationCreator;
import org.eclipse.imp.editor.internal.CompletionProcessor;
import org.eclipse.imp.editor.internal.EditorErrorTickUpdater;
import org.eclipse.imp.editor.internal.FoldingController;
import org.eclipse.imp.editor.internal.FormattingController;
import org.eclipse.imp.editor.internal.HoverHelpController;
import org.eclipse.imp.editor.internal.HyperlinkDetector;
import org.eclipse.imp.editor.internal.OutlineController;
import org.eclipse.imp.editor.internal.PresentationController;
import org.eclipse.imp.editor.internal.ProblemMarkerManager;
import org.eclipse.imp.editor.internal.SourceHyperlinkController;
import org.eclipse.imp.editor.internal.ToggleBreakpointsAdapter;
import org.eclipse.imp.language.ILanguageService;
import org.eclipse.imp.language.Language;
import org.eclipse.imp.language.LanguageRegistry;
import org.eclipse.imp.model.ISourceProject;
import org.eclipse.imp.model.ModelFactory;
import org.eclipse.imp.parser.IModelListener;
import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.preferences.PreferenceCache;
import org.eclipse.imp.preferences.PreferenceConstants;
import org.eclipse.imp.runtime.RuntimePlugin;
import org.eclipse.imp.services.IASTFindReplaceTarget;
import org.eclipse.imp.services.IFoldingUpdater;
import org.eclipse.imp.services.IOccurrenceMarker;
import org.eclipse.imp.services.ISourceFormatter;
import org.eclipse.imp.services.ISourceHyperlinkDetector;
import org.eclipse.imp.services.base.DefaultAnnotationHover;
import org.eclipse.imp.utils.ExtensionPointFactory;
import org.eclipse.jdt.internal.ui.text.HTMLTextPresenter;
import org.eclipse.jdt.ui.actions.IJavaEditorActionDefinitionIds;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.text.AbstractInformationControlManager;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.IUndoManager;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.formatter.ContentFormatter;
import org.eclipse.jface.text.formatter.IContentFormatter;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.IHyperlinkPresenter;
import org.eclipse.jface.text.information.IInformationPresenter;
import org.eclipse.jface.text.information.IInformationProvider;
import org.eclipse.jface.text.information.IInformationProviderExtension;
import org.eclipse.jface.text.information.InformationPresenter;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.IPresentationRepairer;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelListener;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;
import org.eclipse.jface.text.source.projection.ProjectionSupport;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;
import org.eclipse.ui.texteditor.ContentAssistAction;
import org.eclipse.ui.texteditor.IEditorStatusLine;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.texteditor.MarkerAnnotation;
import org.eclipse.ui.texteditor.TextOperationAction;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

/**
 * An Eclipse editor. This editor is not enhanced using API. Instead, we publish extension points for outline, content assist, hover help, etc.
 * 
 * Credits go to Martin Kersten and Bob Foster for guiding the good parts of this design. Sole responsiblity for the bad parts rest with Chris Laffra.
 * 
 * @author Chris Laffra
 * @author Robert M. Fuhrer
 */
public class UniversalEditor extends TextEditor implements IASTFindReplaceTarget {
    public static final String TOGGLE_COMMENT_COMMAND= RuntimePlugin.IMP_RUNTIME + ".toggleComment";

    public static final String SHOW_OUTLINE_COMMAND= RuntimePlugin.IMP_RUNTIME + ".showOutlineCommand";

    public static final String MESSAGE_BUNDLE= "org.eclipse.imp.editor.messages";

    public static final String EDITOR_ID= RuntimePlugin.IMP_RUNTIME + ".impEditor";

    public static final String PARSE_ANNOTATION_TYPE= "org.eclipse.imp.editor.parseAnnotation";

    private static final String ERROR_ANNOTATION_TYPE= "org.eclipse.ui.workbench.texteditor.error";

    private static final String WARNING_ANNOTATION_TYPE= "org.eclipse.ui.workbench.texteditor.warning";

    private static final String INFO_ANNOTATION_TYPE= "org.eclipse.ui.workbench.texteditor.info";

    private static final String DEBUG_ANNOTATION_TYPE= "org.eclipse.debug.core.breakpoint";

    public Language fLanguage;

    public ParserScheduler fParserScheduler;

    protected HoverHelpController fHoverHelpController;

    protected OutlineController fOutlineController;

    protected PresentationController fPresentationController;

    protected CompletionProcessor fCompletionProcessor;

    protected SourceHyperlinkController fHyperLinkController;

    protected ISourceHyperlinkDetector fHyperLinkDetector;

    protected IAutoEditStrategy fAutoEditStrategy;

    private IFoldingUpdater fFoldingUpdater;

    private ProjectionAnnotationModel fAnnotationModel;

    public ISourceFormatter fFormattingStrategy;

    private FormattingController fFormattingController;

    private ProblemMarkerManager fProblemMarkerManager;

    private IOccurrenceMarker fOccurrenceMarker;

    private static final String BUNDLE_FOR_CONSTRUCTED_KEYS= MESSAGE_BUNDLE;//$NON-NLS-1$

    static ResourceBundle fgBundleForConstructedKeys= ResourceBundle.getBundle(BUNDLE_FOR_CONSTRUCTED_KEYS);

    public UniversalEditor() {
	if (PreferenceCache.emitMessages)
	    RuntimePlugin.getInstance().writeInfoMsg("Creating UniversalEditor instance");
	// SMS 4 Apr 2007
	// Do not set preference store with store obtained from plugin; one is
	// already defined for the parent text editor and populated with relevant
	// preferences
	//setPreferenceStore(RuntimePlugin.getInstance().getPreferenceStore());
	setSourceViewerConfiguration(new StructuredSourceViewerConfiguration());
	configureInsertMode(SMART_INSERT, true);
	setInsertMode(SMART_INSERT);
        fProblemMarkerManager= new ProblemMarkerManager();
    }

    public Object getAdapter(Class required) {
	if (IContentOutlinePage.class.equals(required)) {
	    return fOutlineController;
	}
	if (IToggleBreakpointsTarget.class.equals(required)) {
		// SMS 14 MAR 2007  added "this" parameter
		// to make use of new constructor
	    return new ToggleBreakpointsAdapter(this);
	}
	return super.getAdapter(required);
    }

    protected void createActions() {
	super.createActions();

        final ResourceBundle bundle= ResourceBundle.getBundle(MESSAGE_BUNDLE);
	Action action= new ContentAssistAction(bundle, "ContentAssistProposal.", this);
	action.setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS);
	setAction("ContentAssistProposal", action);
	markAsStateDependentAction("ContentAssistProposal", true);

        action= new TextOperationAction(bundle, "Format.", this, ISourceViewer.FORMAT); //$NON-NLS-1$
        action.setActionDefinitionId(IJavaEditorActionDefinitionIds.FORMAT);
        setAction("Format", action); //$NON-NLS-1$
        markAsStateDependentAction("Format", true); //$NON-NLS-1$
        markAsSelectionDependentAction("Format", true); //$NON-NLS-1$
//      PlatformUI.getWorkbench().getHelpSystem().setHelp(action, IJavaHelpContextIds.FORMAT_ACTION);

        action= new TextOperationAction(bundle, "ShowOutline.", this, StructuredSourceViewer.SHOW_OUTLINE); //$NON-NLS-1$
        action.setActionDefinitionId(SHOW_OUTLINE_COMMAND);
        setAction(SHOW_OUTLINE_COMMAND, action); //$NON-NLS-1$
//      PlatformUI.getWorkbench().getHelpSystem().setHelp(action, IJavaHelpContextIds.SHOW_OUTLINE_ACTION);

	action= new TextOperationAction(bundle, "ToggleComment.", this, StructuredSourceViewer.TOGGLE_COMMENT); //$NON-NLS-1$
	action.setActionDefinitionId(TOGGLE_COMMENT_COMMAND);
	setAction(TOGGLE_COMMENT_COMMAND, action); //$NON-NLS-1$
//	PlatformUI.getWorkbench().getHelpSystem().setHelp(action, IJavaHelpContextIds.TOGGLE_COMMENT_ACTION);
    }

    public interface IRefactoringContributor extends ILanguageService {
	public IAction[] getEditorRefactoringActions(UniversalEditor editor);
    }

    public interface ILanguageActionsContributor extends ILanguageService {
	public IAction[] getEditorActions(UniversalEditor editor);
    }

    protected void editorContextMenuAboutToShow(IMenuManager menu) {
        super.editorContextMenuAboutToShow(menu);

        Set<ILanguageService> contributors= getExtensions(ILanguageService.REFACTORING_CONTRIBUTIONS_SERVICE);

        if (!contributors.isEmpty()) {
            List<IAction> editorActions= new ArrayList<IAction>();

            for(Iterator iter= contributors.iterator(); iter.hasNext(); ) {
		IRefactoringContributor con= (IRefactoringContributor) iter.next();

		try {
		    IAction[] conActions= con.getEditorRefactoringActions(this);

		    for(int i=0; i < conActions.length; i++)
			editorActions.add(conActions[i]);
		} catch(Exception e) {
		    RuntimePlugin.getInstance().logException("Unable to create refactoring actions for contributor " + con, e);
		}
	    }
            Separator refGroup= new Separator("group.refactor");
            IMenuManager refMenu= new MenuManager("Refac&tor");

            menu.add(refGroup);
            menu.appendToGroup("group.refactor", refMenu);

            for(Iterator<IAction> actionIter= editorActions.iterator(); actionIter.hasNext(); ) {
                refMenu.add(actionIter.next());
	    }
        }

        Set<ILanguageService> actionContributors= getExtensions(ILanguageService.EDITOR_ACTION_SERVICE);

        if (!actionContributors.isEmpty()) {
            List<IAction> editorActions= new ArrayList<IAction>();

            for(Iterator iter= actionContributors.iterator(); iter.hasNext(); ) {
		ILanguageActionsContributor con= (ILanguageActionsContributor) iter.next();

		try {
		    IAction[] conActions= con.getEditorActions(this);

		    for(int i=0; i < conActions.length; i++)
			editorActions.add(conActions[i]);
		} catch(Exception e) {
		    RuntimePlugin.getInstance().logException("Unable to create editor actions for contributor " + con, e);
		}
	    }
            Separator refGroup= new Separator("group.languageActions");
            IMenuManager refMenu= new MenuManager(fLanguage.getName());

            menu.add(refGroup);
            menu.appendToGroup("group.languageActions", refMenu);

            for(Iterator<IAction> actionIter= editorActions.iterator(); actionIter.hasNext(); ) {
                refMenu.add(actionIter.next());
	    }
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.texteditor.AbstractDecoratedTextEditor#isOverviewRulerVisible()
     */
    protected boolean isOverviewRulerVisible() {
        return true;
    }

    /**
     * Sets the given message as error message to this editor's status line.
     *
     * @param msg message to be set
     */
    protected void setStatusLineErrorMessage(String msg) {
	IEditorStatusLine statusLine= (IEditorStatusLine) getAdapter(IEditorStatusLine.class);
	if (statusLine != null)
	    statusLine.setMessage(true, msg, null);
    }

    /**
     * Sets the given message as message to this editor's status line.
     *
     * @param msg message to be set
     * @since 3.0
     */
    protected void setStatusLineMessage(String msg) {
	IEditorStatusLine statusLine= (IEditorStatusLine) getAdapter(IEditorStatusLine.class);
	if (statusLine != null)
	    statusLine.setMessage(false, msg, null);
    }

    public ProblemMarkerManager getProblemMarkerManager() {
        return fProblemMarkerManager;
    }

    public void updatedTitleImage(Image image) {
        setTitleImage(image);
    }

    
    // SMS 24 Jan 2007:  Restoring gotoAnnotation (which had been briefly
    // commented out) because it is called by the recently added class
    // GotoAnnotationAction.  Also made return void (since nothing is
    // returned and no return seems expected).
    // This change may be specific to Eclipse 3.1 and the method may
    // be removed again in versions of the Editor intended for Eclipse 3.2.
    // 
    // SMS 28 Jun 2007:  Yes, indeed, the void return type doesn't work
    // in Eclipse 3.2.  Converted return type back to Annotation and adapted
    // procedure to return an annotation in any case.
    
    /**
     * Jumps to the next enabled annotation according to the given direction.
     * An annotation type is enabled if it is configured to be in the
     * Next/Previous tool bar drop down menu and if it is checked.
     *
     * @param forward <code>true</code> if search direction is forward, <code>false</code> if backward
     */
    public Annotation /*void*/ gotoAnnotation(boolean forward) {
	ITextSelection selection= (ITextSelection) getSelectionProvider().getSelection();
	Position position= new Position(0, 0);

	// SMS 28 Jun 2007:  declared here for something to return from both
	// branches
	 Annotation annotation = null;
	
	if (false /* delayed - see bug 18316 */) {
	    annotation = getNextAnnotation(selection.getOffset(), selection.getLength(), forward, position);
	    selectAndReveal(position.getOffset(), position.getLength());
	} else /* no delay - see bug 18316 */{
	    /*Annotation*/ annotation= getNextAnnotation(selection.getOffset(), selection.getLength(), forward, position);

	    setStatusLineErrorMessage(null);
	    setStatusLineMessage(null);
	    if (annotation != null) {
		updateAnnotationViews(annotation);
		selectAndReveal(position.getOffset(), position.getLength());
		setStatusLineMessage(annotation.getText());
	    }
	}
	return annotation;
    }
    
    
    /**
     * Returns the annotation closest to the given range respecting the given
     * direction. If an annotation is found, the annotations current position
     * is copied into the provided annotation position.
     *
     * @param offset the region offset	
     * @param length the region length
     * @param forward <code>true</code> for forwards, <code>false</code> for backward
     * @param annotationPosition the position of the found annotation
     * @return the found annotation
     */
    private Annotation getNextAnnotation(final int offset, final int length, boolean forward, Position annotationPosition) {
	Annotation nextAnnotation= null;
	Position nextAnnotationPosition= null;
	Annotation containingAnnotation= null;
	Position containingAnnotationPosition= null;
	boolean currentAnnotation= false;

	IDocument document= getDocumentProvider().getDocument(getEditorInput());
	int endOfDocument= document.getLength();
	int distance= Integer.MAX_VALUE;

	IAnnotationModel model= getDocumentProvider().getAnnotationModel(getEditorInput());

	for(Iterator e= model.getAnnotationIterator(); e.hasNext(); ) {
	    Annotation a= (Annotation) e.next();
	    //	    if ((a instanceof IJavaAnnotation) && ((IJavaAnnotation) a).hasOverlay() || !isNavigationTarget(a))
	    //		continue;
	    // TODO RMF 4/19/2006 - Need more accurate logic here for filtering annotations
	    if (!(a instanceof MarkerAnnotation) && !a.getType().equals(PARSE_ANNOTATION_TYPE))
		continue;

	    Position p= model.getPosition(a);
	    if (p == null)
		continue;

	    if (forward && p.offset == offset || !forward && p.offset + p.getLength() == offset + length) {// || p.includes(offset)) {
		if (containingAnnotation == null
			|| (forward && p.length >= containingAnnotationPosition.length || !forward
				&& p.length >= containingAnnotationPosition.length)) {
		    containingAnnotation= a;
		    containingAnnotationPosition= p;
		    currentAnnotation= p.length == length;
		}
	    } else {
		int currentDistance= 0;

		if (forward) {
		    currentDistance= p.getOffset() - offset;
		    if (currentDistance < 0)
			currentDistance= endOfDocument + currentDistance;

		    if (currentDistance < distance || currentDistance == distance && p.length < nextAnnotationPosition.length) {
			distance= currentDistance;
			nextAnnotation= a;
			nextAnnotationPosition= p;
		    }
		} else {
		    currentDistance= offset + length - (p.getOffset() + p.length);
		    if (currentDistance < 0)
			currentDistance= endOfDocument + currentDistance;

		    if (currentDistance < distance || currentDistance == distance && p.length < nextAnnotationPosition.length) {
			distance= currentDistance;
			nextAnnotation= a;
			nextAnnotationPosition= p;
		    }
		}
	    }
	}
	if (containingAnnotationPosition != null && (!currentAnnotation || nextAnnotation == null)) {
	    annotationPosition.setOffset(containingAnnotationPosition.getOffset());
	    annotationPosition.setLength(containingAnnotationPosition.getLength());
	    return containingAnnotation;
	}
	if (nextAnnotationPosition != null) {
	    annotationPosition.setOffset(nextAnnotationPosition.getOffset());
	    annotationPosition.setLength(nextAnnotationPosition.getLength());
	}

	return nextAnnotation;
    }

    /**
     * Updates the annotation views that show the given annotation.
     *
     * @param annotation the annotation
     */
    private void updateAnnotationViews(Annotation annotation) {
	IMarker marker= null;
	if (annotation instanceof MarkerAnnotation)
	    marker= ((MarkerAnnotation) annotation).getMarker();
	else
	//        if (annotation instanceof IJavaAnnotation) {
	//	    Iterator e= ((IJavaAnnotation) annotation).getOverlaidIterator();
	//	    if (e != null) {
	//		while (e.hasNext()) {
	//		    Object o= e.next();
	//		    if (o instanceof MarkerAnnotation) {
	//			marker= ((MarkerAnnotation) o).getMarker();
	//			break;
	//		    }
	//		}
	//	    }
	//	}

	if (marker != null /*&& !marker.equals(fLastMarkerTarget)*/) {
	    try {
		boolean isProblem= marker.isSubtypeOf(IMarker.PROBLEM);
		IWorkbenchPage page= getSite().getPage();
		IViewPart view= page.findView(isProblem ? IPageLayout.ID_PROBLEM_VIEW : IPageLayout.ID_TASK_LIST); //$NON-NLS-1$  //$NON-NLS-2$
		if (view != null) {
		    Method method= view.getClass().getMethod(
			    "setSelection", new Class[] { IStructuredSelection.class, boolean.class }); //$NON-NLS-1$
		    method.invoke(view, new Object[] { new StructuredSelection(marker), Boolean.TRUE });
		}
	    } catch (CoreException x) {
	    } catch (NoSuchMethodException x) {
	    } catch (IllegalAccessException x) {
	    } catch (InvocationTargetException x) {
	    }
	    // ignore exceptions, don't update any of the lists, just set status line
	}
    }

    public void createPartControl(Composite parent) {
	if (PreferenceCache.emitMessages)
	    RuntimePlugin.getInstance().writeInfoMsg("Determining editor input source language");
	fLanguage= LanguageRegistry.findLanguage(getEditorInput());

	// Create language service extensions now, for any services that could
	// get invoked via super.createPartControl().
	if (fLanguage != null) {
	    if (PreferenceCache.emitMessages)
		RuntimePlugin.getInstance().writeInfoMsg("Creating hyperlink, folding, and formatting language service extensions for " + fLanguage.getName());
	    fHyperLinkDetector= (ISourceHyperlinkDetector) createExtensionPoint(ILanguageService.HYPERLINK_SERVICE);
	    if (fHyperLinkDetector == null)
		fHyperLinkDetector= new HyperlinkDetector(fLanguage);
	    if (fHyperLinkDetector != null)
	    	fHyperLinkController= new SourceHyperlinkController(fHyperLinkDetector, this);
	    fFoldingUpdater= (IFoldingUpdater) createExtensionPoint(ILanguageService.FOLDING_SERVICE);
	    fFormattingStrategy= (ISourceFormatter) createExtensionPoint(ILanguageService.FORMATTER_SERVICE);
	    fFormattingController= new FormattingController(fFormattingStrategy);
	    fProblemMarkerManager.addListener(new EditorErrorTickUpdater(this));
	}

	super.createPartControl(parent);

	// SMS 4 Apr 2007:  Call no longer needed because preferences for the
	// overview ruler are now obtained from appropriate preference store directly
        //setupOverviewRulerAnnotations();

	// SMS 4 Apr 2007:  Also should not need this, since we're not using
	// the plugin's store (for this purpose)
        //AbstractDecoratedTextEditorPreferenceConstants.initializeDefaultValues(RuntimePlugin.getInstance().getPreferenceStore());

        {
            ILabelProvider lp= (ILabelProvider) ExtensionPointFactory.createExtensionPoint(fLanguage, ILanguageService.LABEL_PROVIDER_SERVICE);

            // Only set the editor's title bar icon if the language has a label provider
            if (lp != null) {
        	IEditorInput editorInput= getEditorInput();
        	IFile file= null;

        	if (editorInput instanceof IFileEditorInput)
        	    setTitleImage(lp.getImage(((IFileEditorInput) editorInput).getFile()));
        	else if (editorInput instanceof IPathEditorInput) {
		    IPathEditorInput pathInput= (IPathEditorInput) editorInput;

		    file= ResourcesPlugin.getWorkspace().getRoot().getFile(pathInput.getPath());
        	}
            }
        }

        if (PreferenceCache.sourceFont != null)
	    getSourceViewer().getTextWidget().setFont(PreferenceCache.sourceFont);

	getPreferenceStore().addPropertyChangeListener(fPrefStoreListener);

	if (fLanguage != null) {
	    try {
		if (PreferenceCache.emitMessages)
		    RuntimePlugin.getInstance().writeInfoMsg("Creating remaining language service extensions for " + fLanguage.getName());
		fOutlineController= new OutlineController(this);
		fPresentationController= new PresentationController(getSourceViewer());
		fPresentationController.damage(0, getSourceViewer().getDocument().getLength());
		fParserScheduler= new ParserScheduler(fLanguage.getName() + " Parser");
		fFormattingController.setParseController(fParserScheduler.parseController);
		// SMS 29 May 2007 (to get viewer access to single-line comment prefix)
		ISourceViewer sourceViewer = getSourceViewer();
		if (sourceViewer instanceof StructuredSourceViewer) {
			((StructuredSourceViewer)sourceViewer).setParseController(getParseController());
		}
		if (fFoldingUpdater != null) {
		    if (PreferenceCache.emitMessages)
			RuntimePlugin.getInstance().writeInfoMsg("Enabling source folding for " + fLanguage.getName());
		    ProjectionViewer viewer= (ProjectionViewer) getSourceViewer();
		    ProjectionSupport projectionSupport= new ProjectionSupport(viewer, getAnnotationAccess(), getSharedColors());

		    projectionSupport.install();    
		    
		    viewer.doOperation(ProjectionViewer.TOGGLE);
		    fAnnotationModel= viewer.getProjectionAnnotationModel();
		    fParserScheduler.addModelListener(new FoldingController(fAnnotationModel, fFoldingUpdater));
		}

		fOutlineController.setLanguage(fLanguage);
		fPresentationController.setLanguage(fLanguage);
		fCompletionProcessor.setLanguage(fLanguage);
		fHoverHelpController.setLanguage(fLanguage);

		fParserScheduler.addModelListener(fOutlineController);
		fParserScheduler.addModelListener(fPresentationController);
		fParserScheduler.addModelListener(fCompletionProcessor);
		fParserScheduler.addModelListener(fHoverHelpController);
		
		// TODO RMF 8/6/2007 - Disable "Mark Occurrences" if no occurrence marker exists for this language
		// The following doesn't work b/c getAction() doesn't find the Mark Occurrences action (why?)
//		if (this.fOccurrenceMarker == null)
//		    getAction("org.eclipse.imp.runtime.actions.markOccurrencesAction").setEnabled(false);

		if (fHyperLinkController != null)
		    fParserScheduler.addModelListener(fHyperLinkController);
		fParserScheduler.run(new NullProgressMonitor());
	    } catch (Exception e) {
		ErrorHandler.reportError("Could not create part", e);
	    }
	}
    }  
    
    // SMS 4 Apr 2007
    // No longer necessary since UniversalEditor is now using the preference store
    // provided by the parent text editor rather the one obtained from the local plugin
    // (the former has preferences for annotations that are based on the preference pages
    // and adopted automatically, the latter does not).
//    private void setupOverviewRulerAnnotations() {
//        final IOverviewRuler overviewRuler= getOverviewRuler();
//
//        // Get these values from the preferences store
//        
//        overviewRuler.addAnnotationType(PARSE_ANNOTATION_TYPE);
//        overviewRuler.setAnnotationTypeColor(PARSE_ANNOTATION_TYPE, getSharedColors().getColor(new RGB(0,255,0)));
//        overviewRuler.setAnnotationTypeLayer(PARSE_ANNOTATION_TYPE, 0);
//        overviewRuler.addAnnotationType(ERROR_ANNOTATION_TYPE);
//        overviewRuler.setAnnotationTypeColor(ERROR_ANNOTATION_TYPE, getSharedColors().getColor(new RGB(255, 0, 0)));
//        overviewRuler.setAnnotationTypeLayer(ERROR_ANNOTATION_TYPE, 0);
//        overviewRuler.addAnnotationType(WARNING_ANNOTATION_TYPE);
//        overviewRuler.setAnnotationTypeColor(WARNING_ANNOTATION_TYPE, getSharedColors().getColor(new RGB(255, 215, 0)));
//        overviewRuler.setAnnotationTypeLayer(WARNING_ANNOTATION_TYPE, 0);
//        overviewRuler.addAnnotationType(INFO_ANNOTATION_TYPE);
//        overviewRuler.setAnnotationTypeColor(INFO_ANNOTATION_TYPE, getSharedColors().getColor(new RGB(0, 255, 0)));
//        overviewRuler.setAnnotationTypeLayer(INFO_ANNOTATION_TYPE, 0);
//        overviewRuler.addAnnotationType(DEBUG_ANNOTATION_TYPE);
//        overviewRuler.setAnnotationTypeColor(DEBUG_ANNOTATION_TYPE, getSharedColors().getColor(new RGB(0, 0, 255)));
//        overviewRuler.setAnnotationTypeLayer(DEBUG_ANNOTATION_TYPE, 0);
//    }

    public void dispose() {
	// Remove the pref store listener *before* calling super; super nulls out the pref store.
        getPreferenceStore().removePropertyChangeListener(fPrefStoreListener);
        super.dispose();
    }

    /**
     * Override creation of the normal source viewer with one that supports source folding.
     */
    protected ISourceViewer createSourceViewer(Composite parent, IVerticalRuler ruler, int styles) {
//	if (fFoldingUpdater == null)
//	    return super.createSourceViewer(parent, ruler, styles);

	fAnnotationAccess= createAnnotationAccess();
	fOverviewRuler= createOverviewRuler(getSharedColors());

	ISourceViewer viewer= new StructuredSourceViewer(parent, ruler, getOverviewRuler(), isOverviewRulerVisible(), styles);
	// ensure decoration support has been created and configured.
	getSourceViewerDecorationSupport(viewer);
	
	return viewer;
    }

    
	public final String PARSE_ANNOTATION = "Parse_Annotation";
	
	private HashMap<IMarker, Annotation> markerParseAnnotations = new HashMap();
	private HashMap<IMarker, MarkerAnnotation> markerMarkerAnnotations = new HashMap();
    
	
	/**
	 * Refresh the marker annotations on the input document by removing any
	 * that do not map to current parse annotations.  Do this for problem
	 * markers, specifically; ignore other types of markers.
	 * 
	 * SMS 25 Apr 2007
	 */
    public void refreshMarkerAnnotations(String problemMarkerType)
    {
    	// Get current marker annotations
		IAnnotationModel model = getDocumentProvider().getAnnotationModel(getEditorInput());
		Iterator annIter = model.getAnnotationIterator();
		List markerAnnotations = new ArrayList();
		while (annIter.hasNext()) {
			Object ann = annIter.next();
			if (ann instanceof MarkerAnnotation) {
				markerAnnotations.add(ann);
			} 
		}

		// For the current marker annotations, if any lacks a corresponding
		// parse annotation, delete the marker annotation from the document's
		// annotation model (but leave the marker on the underlying resource,
		// which presumably hasn't been changed, despite changes to the document)
		for (int i = 0; i < markerAnnotations.size(); i++) {
			IMarker marker = ((MarkerAnnotation)markerAnnotations.get(i)).getMarker();
			try {
				String markerType = marker.getType();
				if (!markerType.endsWith(problemMarkerType))
					continue;
			} catch (CoreException e) {
				// If we get a core exception here, probably something is wrong with the
				// marker, and we probably don't want to keep any annotation that may be
				// associated with it (I don't think)
				model.removeAnnotation((MarkerAnnotation)markerAnnotations.get(i));
				continue;
			}
			if (markerParseAnnotations.get(marker) != null) {
				continue;
			} else {
				model.removeAnnotation((MarkerAnnotation)markerAnnotations.get(i));
			}	
		}
    }
    
    
    /**
     * This is a type of listener whose purpose is to monitor changes to a document
     * annotation model and to maintain at a mapping from markers on the underlying
     * resource to parse annotations on the document.
     * 
     * The association of markers to annotations is determined by a subroutine that
     * may be more or less sophisticated in how it identifies associations.  The
     * accuracy of the map depends on the implementation of this routine.  (The
     * current implementation of the method simply compares text ranges of annotations
     * and markers.)
     * 
     * The motivating purpose of the mapping is to enable the identification of marker
     * annotations that are (or are not) associated with a current parse annotation.
     * Then, for instance, marker annotations that are not associated with current parse 
     * annotations might be removed from the document.
     * 
     * No assumptions are made here about the type (or types) of marker annotation of
     * interest; all types of marker annotation are considered.
     * 
     * SMS 25 Apr 2007
     */
    protected class InputAnnotationModelListener implements IAnnotationModelListener
    {
    	public void modelChanged(IAnnotationModel model)
    	{
    		List<Annotation> currentParseAnnotations = new ArrayList();
    		List<IMarker> currentMarkers = new ArrayList();
    		markerParseAnnotations = new HashMap();
    		markerMarkerAnnotations = new HashMap();
    		
    		// Collect the current set of markers and parse annotations;
    		// also maintain a map of markers to marker annotations (as	
    		// there doesn't seem to be a way to get from a marker to the
    		// annotations that may represent it)
    		Iterator annotations = model.getAnnotationIterator();
    		while (annotations.hasNext()) {
    			Object ann = annotations.next();
    			if (ann instanceof MarkerAnnotation) {
    				IMarker marker = ((MarkerAnnotation)ann).getMarker();
    				currentMarkers.add(marker);
    				markerMarkerAnnotations.put(marker, (MarkerAnnotation) ann);
    			} else if (ann instanceof Annotation) {
    				Annotation annotation = (Annotation) ann;
    				if (annotation.getType().equals(PARSE_ANNOTATION_TYPE)) {
    					currentParseAnnotations.add(annotation);
    				}
    			}
    		}

    		// Create a mapping between current markers and parse annotations
    		for (int i = 0; i < currentMarkers.size(); i++) {
    			IMarker marker = (IMarker) currentMarkers.get(i);
				Annotation annotation = findParseAnnotationForMarker(model, marker, currentParseAnnotations);
				if (annotation != null) {
					markerParseAnnotations.put(marker, annotation);
				}
    		}
    	}
    	
    	
    	public Annotation findParseAnnotationForMarker(IAnnotationModel model, IMarker marker, List parseAnnotations) {
    		Integer markerStartAttr = null;
    		Integer markerEndAttr = null;
			try {
				// SMS 22 May 2007:  With markers created through the editor the CHAR_START
				// and CHAR_END attributes are null, giving rise to NPEs here.  Not sure
				// why this happens, but it seems to help down the line to trap the NPE.
				markerStartAttr = ((Integer) marker.getAttribute(IMarker.CHAR_START));
				markerEndAttr = ((Integer) marker.getAttribute(IMarker.CHAR_END));
				if (markerStartAttr == null || markerEndAttr == null) {
					return null;
				}
			} catch (CoreException e) {
				System.err.println("UniversalEditor.findParseAnnotationForMarker:  CoreException geting marker start and end attributes");
				return null;
			} catch (NullPointerException e) {
				System.err.println("UniversalEditor.findParseAnnotationForMarker:  NullPointerException geting marker start and end attributes");
				return null;
			}
			
   			int markerStart = markerStartAttr.intValue();
			int markerEnd = markerEndAttr.intValue();
			int markerLength = markerEnd - markerStart;

			for (int j = 0; j < parseAnnotations.size(); j++) {
				Annotation parseAnnotation = (Annotation) parseAnnotations.get(j);
				Position pos = model.getPosition(parseAnnotation);
				if (pos == null)
					// And this would be why?
					continue;
				int annotationStart = pos.offset;
				int annotationLength = pos.length;
				//System.out.println("\tfindPareseAnnotationForMarker:  Checking annotation offset and length = " + annotationStart + ", " + annotationLength);
				
				if (markerStart == annotationStart && markerLength == annotationLength) {
					//System.out.println("\tfindPareseAnnotationForMarker:  Returning annotation at offset = " + markerStart);
					return parseAnnotation;
				} else {
  					//System.out.println("\tfindPareseAnnotationForMarker:  Not returning annotation at offset = " + markerStart);
				}
			}
			
			//System.out.println("  findPareseAnnotationForMarker:  No corresponding annotation found; returning null");
			return null;
    	}   	
    }
    
    
    
    protected void doSetInput(IEditorInput input) throws CoreException {
    	// SMS 22 May 2007:  added try/catch around doSetInput(..)
    	try {
    		super.doSetInput(input);
    	} catch (NullPointerException e) {
    		return;
    	}
		setInsertMode(SMART_INSERT);
	
		// SMS 25 Apr 2007
		// Added for maintenance of associations between marker annotations
		// and parse annotations	
		IAnnotationModel annotationModel = getDocumentProvider().getAnnotationModel(input);
                // RMF 6 Jun 2007 - Not sure why annotationModel is null for files outside the
                // workspace, but they are, so make sure we don't cause an NPE here.
                if (annotationModel != null)
                    annotationModel.addAnnotationModelListener(new InputAnnotationModelListener());
	
    }

    /**
     * Convenience method to create language extensions whose extension point is
     * defined by this plugin.
     * @param extensionPoint the extension point ID of the language service
     * @return the extension implementation
     */
    private Object createExtensionPoint(String extensionPointID) {
	return ExtensionPointFactory.createExtensionPoint(fLanguage, extensionPointID);
    }

    /**
     * Convenience method to create language extensions whose extension point is
     * defined by this plugin.
     * @param extensionPoint the extension point ID of the language service
     * @return the extension implementation
     */
    private Set<ILanguageService> getExtensions(String extensionPointID) {
	return ExtensionPointFactory.createExtensions(fLanguage, extensionPointID);
    }

    /**
     * Add a Model listener to this editor. Anytime the underlying AST is recomputed, the listener is notified.
     * 
     * @param listener the listener to notify of Model changes
     */
    public void addModelListener(IModelListener listener) {
	fParserScheduler.addModelListener(listener);
    }

    class StructuredSourceViewerConfiguration extends TextSourceViewerConfiguration {
	public int getTabWidth(ISourceViewer sourceViewer) {
	    return PreferenceCache.tabWidth;
	}

	public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
	    // BUG Perhaps we shouldn't use a PresentationReconciler; its JavaDoc says it runs in the UI thread!
	    PresentationReconciler reconciler= new PresentationReconciler();
	    reconciler.setRepairer(new PresentationRepairer(), IDocument.DEFAULT_CONTENT_TYPE);
	    return reconciler;
	}

	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
	    ContentAssistant ca= new ContentAssistant();
	    fCompletionProcessor= new CompletionProcessor();
	    ca.setContentAssistProcessor(fCompletionProcessor, IDocument.DEFAULT_CONTENT_TYPE);
	    ca.setInformationControlCreator(getInformationControlCreator(sourceViewer));
	    return ca;
	}

	public IAnnotationHover getAnnotationHover(ISourceViewer sourceViewer) {
	    IAnnotationHover hover= null;

	    if (fLanguage != null)
		hover= (IAnnotationHover) createExtensionPoint(ILanguageService.ANNOTATION_HOVER_SERVICE);
	    if (hover == null)
		hover= new DefaultAnnotationHover();
	    return hover;
	}

	public IAutoEditStrategy[] getAutoEditStrategies(ISourceViewer sourceViewer, String contentType) {
	    if (fLanguage != null)
		fAutoEditStrategy= (IAutoEditStrategy) createExtensionPoint(ILanguageService.AUTO_EDIT_SERVICE);

	    if (fAutoEditStrategy == null)
		fAutoEditStrategy= super.getAutoEditStrategies(sourceViewer, contentType)[0];

	    return new IAutoEditStrategy[] { fAutoEditStrategy };
	}

	public IContentFormatter getContentFormatter(ISourceViewer sourceViewer) {
	    // Disable the content formatter if no language-specific implementation exists.
	    if (fFormattingStrategy == null)
		return null;

	    // BUG For now, assumes only one content type (i.e. one kind of partition)
	    ContentFormatter formatter= new ContentFormatter();

//	    formatter.setDocumentPartitioning("foo");
	    formatter.setFormattingStrategy(fFormattingController, IDocument.DEFAULT_CONTENT_TYPE);
	    return formatter;
	}

	public String[] getDefaultPrefixes(ISourceViewer sourceViewer, String contentType) {
	    return super.getDefaultPrefixes(sourceViewer, contentType);
	}

	public ITextDoubleClickStrategy getDoubleClickStrategy(ISourceViewer sourceViewer, String contentType) {
	    return super.getDoubleClickStrategy(sourceViewer, contentType);
	}

	public IHyperlinkDetector[] getHyperlinkDetectors(ISourceViewer sourceViewer) {
	    if (fHyperLinkController != null)
		return new IHyperlinkDetector[] { fHyperLinkController };
	    return super.getHyperlinkDetectors(sourceViewer);
	}

	public IHyperlinkPresenter getHyperlinkPresenter(ISourceViewer sourceViewer) {
	    return super.getHyperlinkPresenter(sourceViewer);
	}

	public String[] getIndentPrefixes(ISourceViewer sourceViewer, String contentType) {
	    return super.getIndentPrefixes(sourceViewer, contentType);
	}

	public IInformationControlCreator getInformationControlCreator(ISourceViewer sourceViewer) {
	    return new IInformationControlCreator() {
		public IInformationControl createInformationControl(Shell parent) {
		    int shellStyle= SWT.RESIZE | SWT.TOOL;
		    int style= SWT.NONE; // SWT.V_SCROLL | SWT.H_SCROLL;

		    // return new OutlineInformationControl(parent, shellStyle, style, new HTMLTextPresenter(false));
		    return new DefaultInformationControl(parent, style, new HTMLTextPresenter(false), "Press 'F2' for focus");
		}
	    };
	}

	private InformationPresenter fInfoPresenter;

	public IInformationPresenter getInformationPresenter(ISourceViewer sourceViewer) {
	    if (fInfoPresenter == null) {
		fInfoPresenter= new InformationPresenter(getInformationControlCreator(sourceViewer));
		fInfoPresenter.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));
		fInfoPresenter.setAnchor(AbstractInformationControlManager.ANCHOR_GLOBAL);
		IInformationProvider provider= new IInformationProvider() { // this should be language-specific
		    public IRegion getSubject(ITextViewer textViewer, int offset) {
			return new Region(offset, 10);
		    }
		    public String getInformation(ITextViewer textViewer, IRegion subject) {
			return "Hi Mom!";
		    }
		};
		fInfoPresenter.setInformationProvider(provider, IDocument.DEFAULT_CONTENT_TYPE);
		fInfoPresenter.setSizeConstraints(60, 10, true, false);
		fInfoPresenter.setRestoreInformationControlBounds(getSettings("outline_presenter_bounds"), true, true); //$NON-NLS-1$
	    }
	    return fInfoPresenter;
	}

	public ITextHover getTextHover(ISourceViewer sourceViewer, String contentType) {
	    return fHoverHelpController= new HoverHelpController();
	}

	public ITextHover getTextHover(ISourceViewer sourceViewer, String contentType, int stateMask) {
	    return super.getTextHover(sourceViewer, contentType, stateMask);
	}

	public IUndoManager getUndoManager(ISourceViewer sourceViewer) {
	    return super.getUndoManager(sourceViewer);
	}

	public IAnnotationHover getOverviewRulerAnnotationHover(ISourceViewer sourceViewer) {
	    return super.getOverviewRulerAnnotationHover(sourceViewer);
	}

	private class LangInformationProvider implements IInformationProvider, IInformationProviderExtension {
	    public IRegion getSubject(ITextViewer textViewer, int offset) {
		return new Region(offset, 10);
	    }
	    public String getInformation(ITextViewer textViewer, IRegion subject) {
		return "never called?!?"; // shouldn't be called, given IInformationProviderExtension???
	    }
	    public Object getInformation2(ITextViewer textViewer, IRegion subject) {
		return fParserScheduler.parseController.getCurrentAst();
	    }
	}

	private IInformationProvider fSourceElementProvider= new LangInformationProvider();

	public IInformationPresenter getOutlinePresenter(ISourceViewer sourceViewer) {
	    if (!ExtensionPointFactory.languageServiceExists(RuntimePlugin.IMP_RUNTIME, OutlineInformationControl.OutlineContentProviderID, fLanguage))
		return null;

	    InformationPresenter presenter;

	    presenter= new InformationPresenter(getOutlinePresenterControlCreator(sourceViewer, IJavaEditorActionDefinitionIds.SHOW_OUTLINE));
	    presenter.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));
	    presenter.setAnchor(AbstractInformationControlManager.ANCHOR_GLOBAL);

	    IInformationProvider provider= fSourceElementProvider;

	    presenter.setInformationProvider(provider, IDocument.DEFAULT_CONTENT_TYPE);
	    // TODO Should associate all other partition types with this provider, too
	    presenter.setSizeConstraints(50, 20, true, false);
	    presenter.setRestoreInformationControlBounds(getSettings("outline_presenter_bounds"), true, true); //$NON-NLS-1$
	    return presenter;
	}

	/**
	 * Returns the outline presenter control creator. The creator is a factory creating outline
	 * presenter controls for the given source viewer. This implementation always returns a creator
	 * for <code>JavaOutlineInformationControl</code> instances.
	 *
	 * @param sourceViewer the source viewer to be configured by this configuration
	 * @param commandId the ID of the command that opens this control
	 * @return an information control creator
	 * @since 2.1
	 */
	private IInformationControlCreator getOutlinePresenterControlCreator(ISourceViewer sourceViewer, final String commandId) {
	    return new IInformationControlCreator() {
		public IInformationControl createInformationControl(Shell parent) {
		    int shellStyle= SWT.RESIZE;
		    int treeStyle= SWT.V_SCROLL | SWT.H_SCROLL;

		    return new OutlineInformationControl(parent, shellStyle, treeStyle, commandId, UniversalEditor.this.fLanguage);
		}
	    };
	}

	/**
	 * Returns the hierarchy presenter which will determine and shown type hierarchy
	 * information requested for the current cursor position.
	 *
	 * @param sourceViewer the source viewer to be configured by this configuration
	 * @param doCodeResolve a boolean which specifies whether code resolve should be used to compute the program element
	 * @return an information presenter
	 */
	public IInformationPresenter getHierarchyPresenter(ISourceViewer sourceViewer, boolean doCodeResolve) {
	    InformationPresenter presenter= new InformationPresenter(getHierarchyPresenterControlCreator(sourceViewer));
	    presenter.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));
	    presenter.setAnchor(AbstractInformationControlManager.ANCHOR_GLOBAL);
	    IInformationProvider provider= null; // TODO RMF new SourceElementProvider(this);
	    presenter.setInformationProvider(provider, IDocument.DEFAULT_CONTENT_TYPE);
//	    presenter.setInformationProvider(provider, IJavaPartitions.JAVA_DOC);
//	    presenter.setInformationProvider(provider, IJavaPartitions.JAVA_MULTI_LINE_COMMENT);
//	    presenter.setInformationProvider(provider, IJavaPartitions.JAVA_SINGLE_LINE_COMMENT);
//	    presenter.setInformationProvider(provider, IJavaPartitions.JAVA_STRING);
//	    presenter.setInformationProvider(provider, IJavaPartitions.JAVA_CHARACTER);
	    presenter.setSizeConstraints(50, 20, true, false);
	    presenter.setRestoreInformationControlBounds(getSettings("hierarchy_presenter_bounds"), true, true); //$NON-NLS-1$
	    return presenter;
	}

	private IInformationControlCreator getHierarchyPresenterControlCreator(ISourceViewer sourceViewer) {
	    return new IInformationControlCreator() {
		public IInformationControl createInformationControl(Shell parent) {
		    int shellStyle= SWT.RESIZE;
		    int treeStyle= SWT.V_SCROLL | SWT.H_SCROLL;

		    return new DefaultInformationControl(parent); // HierarchyInformationControl(parent, shellStyle, treeStyle);
		}
	    };
	}

	/**
	 * Returns the settings for the given section.
	 *
	 * @param sectionName the section name
	 * @return the settings
	 * @since 3.0
	 */
	private IDialogSettings getSettings(String sectionName) {
	    IDialogSettings settings= RuntimePlugin.getInstance().getDialogSettings().getSection(sectionName);
	    if (settings == null)
		settings= RuntimePlugin.getInstance().getDialogSettings().addNewSection(sectionName);
	    return settings;
	}
    }

    class PresentationRepairer implements IPresentationRepairer {
	IDocument fDocument;

	public void createPresentation(TextPresentation presentation, ITypedRegion damage) {
	    // BUG Should we really just ignore the presentation passed in???
	    // JavaDoc says we're responsible for "merging" our changes in...
	    try {
		if (fPresentationController != null && fParserScheduler.parseController != null) {
		    IPrsStream parseStream= fParserScheduler.parseController.getParser().getParseStream();
		    int damagedToken= fParserScheduler.parseController.getTokenIndexAtCharacter(damage.getOffset());

		    // SMS 26 Apr 2006:
		    // (I'd rather see a simple message than a complete stack trace--less alarming for
		    // an occurrence that may not be all that exceptional, and the stack trace is not
		    // really informative, other than telling you that there was a problem here.)
		    // BUT this doesn't seem to catch all exceptions that are thrown in this method
		    // and I can't reliably reproduce the problem that this should catch.  I'm leaving
		    // this here in case it still might work sometimes and as a reminder that some
		    // alternative error handling might be appropriate here.
		    if (damagedToken < 0) {
			final String msg= "PresentationRepairer.createPresentation(): Could not repair damage @ " + damage.getOffset() + " (invalid damaged token) in " + parseStream.getFileName();

			if (PreferenceCache.emitMessages)
			    RuntimePlugin.getInstance().writeInfoMsg(msg);
			else
			    System.err.println(msg);
		    	return;
		    }
		    // SMS 22 JUN 2006:  Added try block around call
		    IToken[] adjuncts = null;
		    int endOffset = 0;
		    try {
		    	adjuncts= parseStream.getFollowingAdjuncts(damagedToken);
		    	endOffset= (adjuncts.length == 0) ? parseStream.getEndOffset(damagedToken)
		    			: adjuncts[adjuncts.length - 1].getEndOffset();
		    } catch (IndexOutOfBoundsException e) {
				final String msg= "PresentationRepairer.createPresentation(): Could not repair damage @ " + damage.getOffset() + " (size of parse stream = 0) in " + parseStream.getFileName();
				// SMS 17 Mar 2007
				// Problems occur frequently, and this block as originally coded emits a one-line message or,
				// if not that, prints a long and largely (now) unhelpful exception trace.  I'm removing the
				// exception trace but, by way of compensation, removing the condition on the message
				//if (PreferenceCache.emitMessages)
				    RuntimePlugin.getInstance().writeInfoMsg(msg);
				//else
				    //	ErrorHandler.reportError(msg, e);
			    return;
			// SMS 25 Jun 2007:  Added in case parseStream is null,
			// which may happen in some race conditions
		    } catch (NullPointerException e) {
				final String msg= "PresentationRepairer.createPresentation(): Could not repair damage @ " + damage.getOffset() + " (size of parse stream = 0)";   // in " + parseStream.getFileName();
				// SMS 17 Mar 2007
				// Problems occur frequently, and this block as originally coded emits a one-line message or,
				// if not that, prints a long and largely (now) unhelpful exception trace.  I'm removing the
				// exception trace but, by way of compensation, removing the condition on the message
				//if (PreferenceCache.emitMessages)
				    RuntimePlugin.getInstance().writeInfoMsg(msg);
				//else
				    //	ErrorHandler.reportError(msg, e);
			    return;
			}

		    int length= endOffset - damage.getOffset();

		    fPresentationController.damage(damage.getOffset(),
			    (length > damage.getLength() ? length : damage.getLength()));
		    fParserScheduler.cancel();
		    fParserScheduler.schedule();
		}
	    } catch (Exception e) {
		ErrorHandler.reportError("Could not repair damage ", e);
	    }
	}

	public void setDocument(IDocument document) {
	    fDocument= document;
	}
    }

    private AnnotationCreator fAnnotationCreator= new AnnotationCreator(this, PARSE_ANNOTATION_TYPE);

    private final IPropertyChangeListener fPrefStoreListener= new IPropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent event) {
    	if (event.getProperty().equals(PreferenceConstants.P_SOURCE_FONT)) {
    	    getSourceViewer().getTextWidget().setFont(PreferenceCache.sourceFont);
    	} else if (event.getProperty().equals(PreferenceConstants.P_TAB_WIDTH)) {
    	    getSourceViewer().getTextWidget().setTabs(PreferenceCache.tabWidth);
    	}
        }
    };

    /**
     * Parsing may take a long time, and is not done inside the UI thread.
     * Therefore, we create a job that is executed in a background thread
     * by the platform's job service.
     */
    // TODO Perhaps this should be driven off of the "IReconcilingStrategy" mechanism?
    public class ParserScheduler extends Job {
	public IParseController parseController;

	protected List astListeners= new ArrayList();

	ParserScheduler(String name) {
	    super(name);
	    setSystem(true); // do not show this job in the Progress view
	    parseController= (IParseController) createExtensionPoint(ILanguageService.PARSER_SERVICE);
	    if (parseController == null) {
		  ErrorHandler.reportError("Unable to instantiate parser for " + fLanguage.getName() + "; parser-related services disabled.");
	    }
	}

	protected IStatus run(IProgressMonitor monitor) {
	    if (parseController == null)
		return Status.OK_STATUS;
	    try {
		IEditorInput editorInput= getEditorInput();
		IFile file= null;
		IDocument document= null;
		IPath filePath= null;

		if (editorInput instanceof IFileEditorInput) {
		    IFileEditorInput fileEditorInput= (IFileEditorInput) getEditorInput();
		    document= getDocumentProvider().getDocument(fileEditorInput);
		    file= fileEditorInput.getFile();
		    filePath= fileEditorInput.getFile().getProjectRelativePath();
		} else if (editorInput instanceof IPathEditorInput) {
		    IPathEditorInput pathInput= (IPathEditorInput) editorInput;
		    file= null;
		    document= getDocumentProvider().getDocument(editorInput);
		    filePath= pathInput.getPath();
		} else if (editorInput instanceof IStorageEditorInput) {
		    IStorageEditorInput storageEditorInput= (IStorageEditorInput) editorInput;
		    file= null;
		    document= getDocumentProvider().getDocument(editorInput);
		    filePath= storageEditorInput.getStorage().getFullPath();
		}

		if (PreferenceCache.emitMessages)
		    RuntimePlugin.getInstance().writeInfoMsg("Parsing language " + fLanguage.getName() + " for input " + getEditorInput().getName());

		// Don't need to retrieve the AST; we don't need it.
		// Just make sure the document contents gets parsed once (and only once).
		fAnnotationCreator.removeAnnotations();
		ISourceProject srcProject= (file != null) ? ModelFactory.open(file.getProject()) : null;
		parseController.initialize(filePath, srcProject, fAnnotationCreator);
		parseController.parse(document.get(), false, monitor);
		if (!monitor.isCanceled())
		    notifyAstListeners(parseController, monitor);
		// else
		//	System.out.println("Bypassed AST listeners (cancelled).");
	    } catch (Exception e) {
	    	ErrorHandler.reportError("Error running parser for " + fLanguage.getName() + ":", e);
		if (PreferenceCache.emitMessages)
		    RuntimePlugin.getInstance().writeInfoMsg("Parsing failed for language " + fLanguage.getName() + " and input " + getEditorInput().getName());
                // RMF 8/2/2006 - Notify the AST listeners even on an exception - the compiler front end
                // may have failed at some phase, but there may be enough info to drive IDE services.
                notifyAstListeners(parseController, monitor);
	    }
	    // SMS 25 Apr 2007
	    // Since parsing has finished, check whether the marker annotations
	    // are up-to-date with the most recent parse annotations.
	    // Assuming that's often enough--i.e., don't refresh the marker
	    // annotations after every update to the document annotation model
	    // since there will be many of these, including possibly many that
	    // don't relate to problem markers.
	    List problemMarkerTypes = parseController.getProblemMarkerTypes();
	    for (int i = 0; i < problemMarkerTypes.size(); i++) {
	    	refreshMarkerAnnotations((String)problemMarkerTypes.get(i));
	    }
	    
	    return Status.OK_STATUS;
	}

	public void addModelListener(IModelListener listener) {
	    astListeners.add(listener);
	}

	public void notifyAstListeners(IParseController parseController, IProgressMonitor monitor) {
	    // Suppress the notification if there's no AST (e.g. due to a parse error)
	    if (parseController != null) {
		if (PreferenceCache.emitMessages)
		    RuntimePlugin.getInstance().writeInfoMsg("Notifying AST listeners of change in " + parseController.getParser().getParseStream().getFileName());
		for(int n= astListeners.size() - 1; n >= 0 && !monitor.isCanceled(); n--) {
		    IModelListener listener= (IModelListener) astListeners.get(n);
		    // Pretend to get through the highest level of analysis so all services execute (for now)
		    int analysisLevel= IModelListener.AnalysisRequired.POINTER_ANALYSIS.level();

		    if (parseController.getCurrentAst() == null)
			analysisLevel= IModelListener.AnalysisRequired.LEXICAL_ANALYSIS.level();
		    // TODO How to tell how far we got with the source analysis? The IAnalysisController should tell us!
		    // TODO Rename IParseController to IAnalysisController
		    // TODO Compute the minimum amount of analysis sufficient for all current listeners, and pass that to the IAnalysisController.
		    if (listener.getAnalysisRequired().level() <= analysisLevel)
			listener.update(parseController, monitor);
		}
	    } else
		if (PreferenceCache.emitMessages)
		    RuntimePlugin.getInstance().writeInfoMsg("No AST; bypassing listener notification.");
	}
    }

    public String getSelectionText() {
	Point sel= getSelection();
        IFileEditorInput fileEditorInput= (IFileEditorInput) getEditorInput();
        IDocument document= getDocumentProvider().getDocument(fileEditorInput);

        try {
	    return document.get(sel.x, sel.y);
	} catch (BadLocationException e) {
	    e.printStackTrace();
	    return "";
	}
    }

    public Point getSelection() {
	ISelection sel= this.getSelectionProvider().getSelection();
	ITextSelection textSel= (ITextSelection) sel;

	return new Point(textSel.getOffset(), textSel.getLength());
    }

    public boolean canPerformFind() {
	return true;
    }

    public IParseController getParseController() {
	return fParserScheduler.parseController;
    }
    
    public IOccurrenceMarker getOccurrenceMarker() {
        return fOccurrenceMarker;
    }

    // SMS 4 May 2006:
    // Added this as the only way I could think of (so far) to
    // remove parser annotations that I expect to be duplicated
    // if a save triggers a build that leads to the creation
    // of markers and another set of annotations.
	public void doSave(IProgressMonitor progressMonitor) {
		// SMS 25 Apr 2007:  Removing parser annotations here
		// may not hurt but also doesn't seem to be necessary
		//removeParserAnnotations();
		super.doSave(progressMonitor);
	}
	
    public void removeParserAnnotations() {
    	IAnnotationModel model= getDocumentProvider().getAnnotationModel(getEditorInput());

    	for(Iterator i= model.getAnnotationIterator(); i.hasNext(); ) {
    	    Annotation a= (Annotation) i.next();

    	    if (a.getType().equals(PARSE_ANNOTATION_TYPE))
    	    	model.removeAnnotation(a);
    	}
    }
}
