package org.eclipse.uide.editor;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 1998, 2004  All Rights Reserved
 */

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.IUndoManager;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.jface.text.formatter.IContentFormatter;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.IHyperlinkPresenter;
import org.eclipse.jface.text.information.IInformationPresenter;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.IPresentationRepairer;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.ContentAssistAction;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.eclipse.uide.core.ErrorHandler;
import org.eclipse.uide.core.Language;
import org.eclipse.uide.core.LanguageRegistry;
import org.eclipse.uide.internal.editor.OutlineController;
import org.eclipse.uide.internal.editor.PresentationController;
import org.eclipse.uide.internal.util.ExtensionPointFactory;
import org.eclipse.uide.parser.IModelListener;
import org.eclipse.uide.parser.IParseController;
import org.eclipse.uide.runtime.RuntimePlugin;

import com.ibm.lpg.IToken;
import com.ibm.lpg.PrsStream;

/**
 * An Eclipse editor. This editor is not enhanced using API. Instead, we publish extension points for outline, content assist, hover help, etc.
 * 
 * Credits go to Martin Kersten and Bob Foster for guiding the good parts of this design. Sole responsiblity for the bad parts rest with Chris Laffra.
 * 
 * @author Chris Laffra
 */
public class UniversalEditor extends TextEditor {
    public static final String EDITOR_ID= RuntimePlugin.UIDE_RUNTIME + ".universalEditor";

    protected Language language;
    protected ParserScheduler parserScheduler;
    protected HoverHelpController hoverHelpController;
    protected OutlineController outlineController;
    protected PresentationController presentationController;
    protected CompletionProcessor completionProcessor;
    protected IHyperlinkDetector fHyperLinkDetector;

    public UniversalEditor() {
	setSourceViewerConfiguration(new Configuration());
    }

    public Object getAdapter(Class required) {
	if (IContentOutlinePage.class.equals(required)) {
	    return outlineController;
	}
	return super.getAdapter(required);
    }

    protected void createActions() {
	super.createActions();
	Action action= new ContentAssistAction(ResourceBundle.getBundle("org.eclipse.uide.editor.messages"), "ContentAssistProposal.", this);
	action.setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS);
	setAction("ContentAssistProposal", action);
	markAsStateDependentAction("ContentAssistProposal", true);
    }

    public void createPartControl(Composite parent) {
	language= LanguageRegistry.findLanguage(getEditorInput());

        if (language != null)
            fHyperLinkDetector= (IHyperlinkDetector) createExtensionPoint("hyperlink");

        super.createPartControl(parent);

        if (language != null) {
	    try {
		outlineController= new OutlineController(this);
		presentationController= new PresentationController(getSourceViewer());
		presentationController.damage(0, getSourceViewer().getDocument().getLength());

		outlineController.setLanguage(language);
		presentationController.setLanguage(language);
		completionProcessor.setLanguage(language);
		hoverHelpController.setLanguage(language);

                parserScheduler= new ParserScheduler("Universal Editor Parser");
		parserScheduler.addModelListener(outlineController);
		parserScheduler.addModelListener(presentationController);
		parserScheduler.addModelListener(completionProcessor);
		parserScheduler.addModelListener(hoverHelpController);
		parserScheduler.run(new NullProgressMonitor());
	    } catch (Exception e) {
		ErrorHandler.reportError("Could not create part", e);
	    }
	}
    }

    private Object createExtensionPoint(String extensionPoint) {
	return ExtensionPointFactory.createExtensionPoint(language, RuntimePlugin.UIDE_RUNTIME, extensionPoint);
    }

    /**
     * Add a Model listener to this editor. Anytime the underlying AST is recomputed, the listener is notified.
     * 
     * @param listener the listener to notify of Model changes
     */
    public void addModelListener(IModelListener listener) {
	parserScheduler.addModelListener(listener);
    }

    class Configuration extends SourceViewerConfiguration {
	public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
	    PresentationReconciler reconciler= new PresentationReconciler();
	    reconciler.setRepairer(new PresentationRepairer(), IDocument.DEFAULT_CONTENT_TYPE);
	    return reconciler;
	}

	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
	    ContentAssistant ca= new ContentAssistant();
	    completionProcessor= new CompletionProcessor();
	    ca.setContentAssistProcessor(completionProcessor, IDocument.DEFAULT_CONTENT_TYPE);
	    ca.setInformationControlCreator(getInformationControlCreator(sourceViewer));
	    return ca;
	}

	public ITextHover getTextHover(ISourceViewer sourceViewer, String contentType) {
	    return hoverHelpController= new HoverHelpController();
	}

	public IAnnotationHover getAnnotationHover(ISourceViewer sourceViewer) {
	    return (IAnnotationHover) createExtensionPoint("annotationHover");
	}

	public IAutoEditStrategy[] getAutoEditStrategies(ISourceViewer sourceViewer, String contentType) {
	    return super.getAutoEditStrategies(sourceViewer, contentType);
	}

	public IContentFormatter getContentFormatter(ISourceViewer sourceViewer) {
	    return super.getContentFormatter(sourceViewer);
	}

	public String[] getDefaultPrefixes(ISourceViewer sourceViewer, String contentType) {
	    return super.getDefaultPrefixes(sourceViewer, contentType);
	}

	public ITextDoubleClickStrategy getDoubleClickStrategy(ISourceViewer sourceViewer, String contentType) {
	    return super.getDoubleClickStrategy(sourceViewer, contentType);
	}

	public IHyperlinkDetector[] getHyperlinkDetectors(ISourceViewer sourceViewer) {
            if (fHyperLinkDetector != null)
                return new IHyperlinkDetector[] { fHyperLinkDetector };
	    return super.getHyperlinkDetectors(sourceViewer);
	}

	public IHyperlinkPresenter getHyperlinkPresenter(ISourceViewer sourceViewer) {
	    return super.getHyperlinkPresenter(sourceViewer);
	}

	public String[] getIndentPrefixes(ISourceViewer sourceViewer, String contentType) {
	    return super.getIndentPrefixes(sourceViewer, contentType);
	}

	public IInformationControlCreator getInformationControlCreator(ISourceViewer sourceViewer) {
	    return super.getInformationControlCreator(sourceViewer);
	}

	public IInformationPresenter getInformationPresenter(ISourceViewer sourceViewer) {
	    return super.getInformationPresenter(sourceViewer);
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
    }

    class PresentationRepairer implements IPresentationRepairer {
	IDocument document;

	public void createPresentation(TextPresentation presentation, ITypedRegion damage) {
	    try {

		if (presentationController != null) {
			PrsStream parseStream = parserScheduler.parseController.getParser().getParseStream();
			int damagedToken= parserScheduler.parseController.getTokenIndexAtCharacter(damage.getOffset());
			IToken[] adjuncts= parseStream.getFollowingAdjuncts(damagedToken);
			int endOffset= (adjuncts.length == 0) ? parseStream.getEndOffset(damagedToken) : adjuncts[adjuncts.length-1].getEndOffset();
			int length = endOffset - damage.getOffset();
		    presentationController.damage(damage.getOffset(), (length > damage.getLength() ? length : damage.getLength()));
		}
		if (parserScheduler != null) {
		    parserScheduler.cancel();
		    parserScheduler.schedule();
		}
	    } catch (Exception e) {
		ErrorHandler.reportError("Could not repair damage ", e);
	    }
	}

	public void setDocument(IDocument document) {
	    this.document= document;
	}
    }

    class CompletionProcessor implements IContentAssistProcessor, IModelListener {
	private final IContextInformation[] NO_CONTEXTS= new IContextInformation[0];
	private ICompletionProposal[] NO_COMPLETIONS= new ICompletionProposal[0];
	private IParseController parseController;
	private IContentProposer contentProposer;

	public CompletionProcessor() {
	}

	public void setLanguage(Language language) {
	    contentProposer= (IContentProposer) ExtensionPointFactory.createExtensionPoint(language, RuntimePlugin.UIDE_RUNTIME, "contentProposer");
	}

	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {
	    try {
		if (parseController != null) {
		    return contentProposer.getContentProposals(parseController, offset);
		}
	    } catch (Throwable e) {
		ErrorHandler.reportError("Universal Editor Error", e);
	    }
	    return NO_COMPLETIONS;
	}

	public IContextInformation[] computeContextInformation(ITextViewer viewer, int offset) {
	    return NO_CONTEXTS;
	}

	public char[] getCompletionProposalAutoActivationCharacters() {
	    return null;
	}

	public char[] getContextInformationAutoActivationCharacters() {
	    return null;
	}

	public IContextInformationValidator getContextInformationValidator() {
	    return null;
	}

	public String getErrorMessage() {
	    return null;
	}

	public void update(IParseController parseResult, IProgressMonitor monitor) {
	    this.parseController= parseResult;
	}
    }

    /*
         * Parsing may take a long time, and is not done inside the UI thread. Therefore, we create a job that is executed in a background thread by the
         * platform's job service.
         */
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
		IDocument document= getDocumentProvider().getDocument(getEditorInput());
		// Don't need to retrieve the AST; we don't need it.
		// Just make sure the document contents gets parsed once (and only once).
		parseController.parse(document.get(), false, monitor);
		if (!monitor.isCanceled())
			notifyAstListeners(parseController, monitor);
//		else
//			System.out.println("Bypassed AST listeners (cancelled).");
	    } catch (Exception e) {
		ErrorHandler.reportError("Error running parser for " + language, e);
	    }
	    return Status.OK_STATUS;
	}

	public void addModelListener(IModelListener listener) {
	    astListeners.add(listener);
	}

	public void notifyAstListeners(IParseController parseController, IProgressMonitor monitor) {
	    if (parseController != null)
		for(int n= astListeners.size() - 1; n >= 0; n--)
		    ((IModelListener) astListeners.get(n)).update(parseController, monitor);
	}
    };

    class HoverHelpController implements ITextHover, IModelListener {
	private IParseController controller;
	private IHoverHelper hoverHelper;

	public IRegion getHoverRegion(ITextViewer textViewer, int offset) {
	    return new Region(offset, 0);
	}

	public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion) {
	    try {
		if (controller != null && hoverHelper != null)
		    return hoverHelper.getHoverHelpAt(controller, hoverRegion.getOffset());
	    } catch (Throwable e) {
		ErrorHandler.reportError("Universal Editor Error", e);
	    }
	    return null;
	}

	public void update(IParseController controller, IProgressMonitor monitor) {
	    this.controller= controller;
	}

	public void setLanguage(Language language) {
	    hoverHelper= (IHoverHelper) ExtensionPointFactory.createExtensionPoint(language, RuntimePlugin.UIDE_RUNTIME, "hoverHelper");
	}
    }
}
