package org.eclipse.uide.editor;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 1998, 2004  All Rights Reserved
 */

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import lpg.lpgjavaruntime.IToken;
import lpg.lpgjavaruntime.PrsStream;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTarget;
import org.eclipse.jdt.internal.ui.text.HTMLTextPresenter;
import org.eclipse.jdt.ui.actions.IJavaEditorActionDefinitionIds;
import org.eclipse.jdt.ui.text.IJavaPartitions;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.text.*;
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
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;
import org.eclipse.jface.text.source.projection.ProjectionSupport;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPageLayout;
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
import org.eclipse.uide.core.ErrorHandler;
import org.eclipse.uide.core.Language;
import org.eclipse.uide.core.LanguageRegistry;
import org.eclipse.uide.defaults.DefaultAnnotationHover;
import org.eclipse.uide.internal.editor.FoldingController;
import org.eclipse.uide.internal.editor.FormattingController;
import org.eclipse.uide.internal.editor.OutlineController;
import org.eclipse.uide.internal.editor.PresentationController;
import org.eclipse.uide.internal.editor.SourceHyperlinkController;
import org.eclipse.uide.parser.IModelListener;
import org.eclipse.uide.parser.IParseController;
import org.eclipse.uide.preferences.PreferenceConstants;
import org.eclipse.uide.preferences.SAFARIPreferenceCache;
import org.eclipse.uide.runtime.RuntimePlugin;
import org.eclipse.uide.utils.ExtensionPointFactory;

/**
 * An Eclipse editor. This editor is not enhanced using API. Instead, we publish extension points for outline, content assist, hover help, etc.
 * 
 * Credits go to Martin Kersten and Bob Foster for guiding the good parts of this design. Sole responsiblity for the bad parts rest with Chris Laffra.
 * 
 * @author Chris Laffra
 * @author Robert M. Fuhrer
 */
public class UniversalEditor extends TextEditor implements IASTFindReplaceTarget {
    public static final String TOGGLE_COMMENT_COMMAND= "org.eclipse.uide.runtime.toggleComment";

    public static final String SHOW_OUTLINE_COMMAND= "org.eclipse.uide.runtime.showOutlineCommand";

    public static final String MESSAGE_BUNDLE= "org.eclipse.uide.editor.messages";

    public static final String EDITOR_ID= RuntimePlugin.UIDE_RUNTIME + ".safariEditor";

    public static final String PARSE_ANNOTATION_TYPE= "org.eclipse.uide.editor.parseAnnotation";

    protected Language fLanguage;

    protected ParserScheduler fParserScheduler;

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

    private static final String BUNDLE_FOR_CONSTRUCTED_KEYS= MESSAGE_BUNDLE;//$NON-NLS-1$

    static ResourceBundle fgBundleForConstructedKeys= ResourceBundle.getBundle(BUNDLE_FOR_CONSTRUCTED_KEYS);

    public UniversalEditor() {
	if (SAFARIPreferenceCache.emitMessages)
	    RuntimePlugin.getInstance().writeInfoMsg("Creating UniversalEditor instance");
	setPreferenceStore(RuntimePlugin.getInstance().getPreferenceStore());
	setSourceViewerConfiguration(new StructuredSourceViewerConfiguration());
	configureInsertMode(SMART_INSERT, true);
	setInsertMode(SMART_INSERT);
    }

    public Object getAdapter(Class required) {
	if (IContentOutlinePage.class.equals(required)) {
	    return fOutlineController;
	}
	if (IToggleBreakpointsTarget.class.equals(required)) {
	    return new ToggleBreakpointsAdapter();
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

    /**
     * Jumps to the next enabled annotation according to the given direction.
     * An annotation type is enabled if it is configured to be in the
     * Next/Previous tool bar drop down menu and if it is checked.
     *
     * @param forward <code>true</code> if search direction is forward, <code>false</code> if backward
     */
    public void gotoAnnotation(boolean forward) {
	ITextSelection selection= (ITextSelection) getSelectionProvider().getSelection();
	Position position= new Position(0, 0);

	if (false /* delayed - see bug 18316 */) {
	    getNextAnnotation(selection.getOffset(), selection.getLength(), forward, position);
	    selectAndReveal(position.getOffset(), position.getLength());
	} else /* no delay - see bug 18316 */{
	    Annotation annotation= getNextAnnotation(selection.getOffset(), selection.getLength(), forward, position);

	    setStatusLineErrorMessage(null);
	    setStatusLineMessage(null);
	    if (annotation != null) {
		updateAnnotationViews(annotation);
		selectAndReveal(position.getOffset(), position.getLength());
		setStatusLineMessage(annotation.getText());
	    }
	}
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
	if (SAFARIPreferenceCache.emitMessages)
	    RuntimePlugin.getInstance().writeInfoMsg("Determining editor input source language");
	fLanguage= LanguageRegistry.findLanguage(getEditorInput());

	// Create language service extensions now, for any services that could
	// get invoked via super.createPartControl().
	if (fLanguage != null) {
	    if (SAFARIPreferenceCache.emitMessages)
		RuntimePlugin.getInstance().writeInfoMsg("Creating hyperlink, folding, and formatting language service extensions for " + fLanguage.getName());
	    fHyperLinkDetector= (ISourceHyperlinkDetector) createExtensionPoint("hyperLink");
	    if (fHyperLinkDetector != null)
	    	fHyperLinkController= new SourceHyperlinkController(fHyperLinkDetector);
	    fFoldingUpdater= (IFoldingUpdater) createExtensionPoint("foldingUpdater");
	    fFormattingStrategy= (ISourceFormatter) createExtensionPoint("formatter");
	    fFormattingController= new FormattingController(fFormattingStrategy);
	}

	super.createPartControl(parent);

	if (SAFARIPreferenceCache.sourceFont != null)
	    getSourceViewer().getTextWidget().setFont(SAFARIPreferenceCache.sourceFont);

	getPreferenceStore().addPropertyChangeListener(fPrefStoreListener);

	if (fLanguage != null) {
	    try {
		if (SAFARIPreferenceCache.emitMessages)
		    RuntimePlugin.getInstance().writeInfoMsg("Creating remaining language service extensions for " + fLanguage.getName());
		fOutlineController= new OutlineController(this);
		fPresentationController= new PresentationController(getSourceViewer());
		fPresentationController.damage(0, getSourceViewer().getDocument().getLength());
		fParserScheduler= new ParserScheduler("Universal Editor Parser");
		fFormattingController.setParseController(fParserScheduler.parseController);

		if (fFoldingUpdater != null) {
		    if (SAFARIPreferenceCache.emitMessages)
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
		
		if (fHyperLinkController != null)
		    fParserScheduler.addModelListener(fHyperLinkController);
		fParserScheduler.run(new NullProgressMonitor());
	    } catch (Exception e) {
		ErrorHandler.reportError("Could not create part", e);
	    }
	}
    }

    public void dispose() {
        super.dispose();
        getPreferenceStore().removePropertyChangeListener(fPrefStoreListener);
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

    protected void doSetInput(IEditorInput input) throws CoreException {
	super.doSetInput(input);
	setInsertMode(SMART_INSERT);
    }

    /**
     * Convenience method to create language extensions whose extension point is
     * defined by this plugin.
     * @param extensionPoint the extension point ID of the language service
     * @return the extension implementation
     */
    private Object createExtensionPoint(String extensionPointID) {
	return ExtensionPointFactory.createExtensionPoint(fLanguage, RuntimePlugin.UIDE_RUNTIME, extensionPointID);
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
	    return SAFARIPreferenceCache.tabWidth;
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
		hover= (IAnnotationHover) createExtensionPoint("annotationHover");
	    if (hover == null)
		hover= new DefaultAnnotationHover();
	    return hover;
	}

	public IAutoEditStrategy[] getAutoEditStrategies(ISourceViewer sourceViewer, String contentType) {
	    if (fLanguage != null)
		fAutoEditStrategy= (IAutoEditStrategy) createExtensionPoint("autoEditStrategy");

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
		    int style= SWT.V_SCROLL | SWT.H_SCROLL;

		    // return new OutlineInformationControl(parent, shellStyle, style, new HTMLTextPresenter(false));
		    return new DefaultInformationControl(parent, shellStyle, style, new HTMLTextPresenter(false));
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
		if (fPresentationController != null) {
		    PrsStream parseStream= fParserScheduler.parseController.getParser().getParseStream();
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

			if (SAFARIPreferenceCache.emitMessages)
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
				if (SAFARIPreferenceCache.emitMessages)
				    RuntimePlugin.getInstance().writeInfoMsg(msg);
				else
				    ErrorHandler.reportError(msg, e);
			    return;
			}

		    int length= endOffset - damage.getOffset();

		    fPresentationController.damage(damage.getOffset(),
			    (length > damage.getLength() ? length : damage.getLength()));
		}
		if (fParserScheduler != null) {
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
    	    getSourceViewer().getTextWidget().setFont(SAFARIPreferenceCache.sourceFont);
    	} else if (event.getProperty().equals(PreferenceConstants.P_TAB_WIDTH)) {
    	    getSourceViewer().getTextWidget().setTabs(SAFARIPreferenceCache.tabWidth);
    	}
        }
    };

    /**
     * Parsing may take a long time, and is not done inside the UI thread.
     * Therefore, we create a job that is executed in a background thread
     * by the platform's job service.
     */
    // TODO Perhaps this should be driven off of the "IReconcilingStrategy" mechanism?
    class ParserScheduler extends Job {
	protected IParseController parseController;

	protected List astListeners= new ArrayList();

	ParserScheduler(String name) {
	    super(name);
	    setSystem(true); // do not show this job in the Progress view
	    parseController= (IParseController) createExtensionPoint("parser");
	}

	protected IStatus run(IProgressMonitor monitor) {
	    try {
		IFileEditorInput fileEditorInput= (IFileEditorInput) getEditorInput();
		IDocument document= getDocumentProvider().getDocument(fileEditorInput);
		String filePath= fileEditorInput.getFile().getProjectRelativePath().toString();

		if (SAFARIPreferenceCache.emitMessages)
		    RuntimePlugin.getInstance().writeInfoMsg("Parsing language " + fLanguage.getName() + " for input " + getEditorInput().getName());

		// Don't need to retrieve the AST; we don't need it.
		// Just make sure the document contents gets parsed once (and only once).
		fAnnotationCreator.removeParserAnnotations();
		parseController.initialize(filePath, fileEditorInput.getFile().getProject(), fAnnotationCreator);
		parseController.parse(document.get(), false, monitor);
		if (!monitor.isCanceled())
		    notifyAstListeners(parseController, monitor);
		// else
		//	System.out.println("Bypassed AST listeners (cancelled).");
	    } catch (Exception e) {
	    	ErrorHandler.reportError("Error running parser for " + fLanguage.getName(), e);
		if (SAFARIPreferenceCache.emitMessages)
		    RuntimePlugin.getInstance().writeInfoMsg("Parsing failed for language " + fLanguage.getName() + " and input " + getEditorInput().getName());
	    }
	    return Status.OK_STATUS;
	}

	public void addModelListener(IModelListener listener) {
	    astListeners.add(listener);
	}

	public void notifyAstListeners(IParseController parseController, IProgressMonitor monitor) {
	    // Suppress the notification if there's no AST (e.g. due to a parse error)
	    if (parseController != null) {
		if (SAFARIPreferenceCache.emitMessages)
		    RuntimePlugin.getInstance().writeInfoMsg("Notifying AST listeners of change in " + parseController.getParser().getParseStream().getFileName());
		for(int n= astListeners.size() - 1; n >= 0 && !monitor.isCanceled(); n--) {
		    IModelListener listener= (IModelListener) astListeners.get(n);

		    // HACK RMF 7/19/2006 - PresentationController only needs a token stream,
		    // so notify it even if the parser wasn't successful in producing an AST.
		    //
		    // In the long run, listeners should specify their prerequisite analysis
		    // results (tokenization, parsing, name resolution, etc.). We should
		    // probably have a SourceListener API that exposes this meta-info.
		    if (parseController.getCurrentAst() != null || listener instanceof PresentationController)
			listener.update(parseController, monitor);
		}
	    } else
		if (SAFARIPreferenceCache.emitMessages)
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
    
    // SMS 4 May 2006:
    // Added this as the only way I could think of (so far) to
    // remove parser annotations that I expect to be duplicated
    // if a save triggers a build that leads to the creation
    // of markers and another set of annotations.
	public void doSave(IProgressMonitor progressMonitor) {
		removeParserAnnotations();
		super.doSave(progressMonitor);
	}
	
    private void removeParserAnnotations() {
    	IAnnotationModel model= getDocumentProvider().getAnnotationModel(getEditorInput());

    	for(Iterator i= model.getAnnotationIterator(); i.hasNext(); ) {
    	    Annotation a= (Annotation) i.next();

    	    if (a.getType().equals(PARSE_ANNOTATION_TYPE))
    		model.removeAnnotation(a);
    	}
    }
	
}
