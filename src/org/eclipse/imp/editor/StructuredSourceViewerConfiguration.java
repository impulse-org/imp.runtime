/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Robert Fuhrer (rfuhrer@watson.ibm.com) - initial API and implementation
 *******************************************************************************/

package org.eclipse.imp.editor;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.imp.editor.internal.QuickFixController;
import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.parser.ISourcePositionLocator;
import org.eclipse.imp.preferences.IPreferencesService;
import org.eclipse.imp.preferences.PreferenceCache;
import org.eclipse.imp.preferences.PreferenceConstants;
import org.eclipse.imp.runtime.RuntimePlugin;
import org.eclipse.imp.services.IDocumentationProvider;
import org.eclipse.imp.services.base.DefaultAnnotationHover;
import org.eclipse.imp.services.base.TreeModelBuilderBase;
import org.eclipse.imp.ui.textPresentation.HTMLTextPresenter;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.preference.IPreferenceStore;
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
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.quickassist.IQuickAssistAssistant;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;

public class StructuredSourceViewerConfiguration extends TextSourceViewerConfiguration {
    protected final UniversalEditor fEditor;
    private ServiceControllerManager fServiceControllerManager;
    private LanguageServiceManager fLanguageServiceManager;
    private IPreferencesService fLangSpecificPrefs;

    public StructuredSourceViewerConfiguration(IPreferenceStore prefStore, UniversalEditor editor) {
        super(prefStore);
        fEditor= editor;
        // Can't cache the ServiceControllerManager, LangaugeServiceManager, or the IPreferencesService
        // yet, b/c they haven't been set up by the editor yet. Retrieve them lazily.
    }

    protected ServiceControllerManager getServiceControllerManager() {
        if (fServiceControllerManager == null) {
            fServiceControllerManager= fEditor.fServiceControllerManager;
        }
        return fServiceControllerManager;
    }

    protected LanguageServiceManager getLanguageServiceManager() {
        if (fLanguageServiceManager == null) {
            fLanguageServiceManager= fEditor.getLanguageServiceManager();
        }
        return fLanguageServiceManager;
    }

    protected IPreferencesService getLangSpecificPrefs() {
        if (fLangSpecificPrefs == null) {
            fLangSpecificPrefs= fEditor.getLanguageSpecificPreferences();
        }
        return fLangSpecificPrefs;
    }

    public int getTabWidth(ISourceViewer sourceViewer) {
        boolean langSpecificSetting= getLangSpecificPrefs() != null && getLangSpecificPrefs().isDefined(PreferenceConstants.P_TAB_WIDTH);

        return langSpecificSetting ? getLangSpecificPrefs().getIntPreference(PreferenceConstants.P_TAB_WIDTH) : PreferenceCache.tabWidth;
    }

    public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
        if (getServiceControllerManager() == null || getLanguageServiceManager().getTokenColorer() == null) {
            return super.getPresentationReconciler(sourceViewer);
        }
        // BUG Perhaps we shouldn't use a PresentationReconciler; its JavaDoc says it runs in the UI thread!
        PresentationReconciler reconciler= new PresentationReconciler();

        reconciler.setRepairer(fEditor.new PresentationRepairer(), IDocument.DEFAULT_CONTENT_TYPE);
        reconciler.setDamager(fEditor.new PresentationDamager(), IDocument.DEFAULT_CONTENT_TYPE);
        return reconciler;
    }

    public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
        if (getServiceControllerManager() == null) {
            return super.getContentAssistant(sourceViewer);
        }
        ContentAssistant ca= new ContentAssistant();
        ca.setContentAssistProcessor(getServiceControllerManager().getCompletionProcessor(), IDocument.DEFAULT_CONTENT_TYPE);
        ca.setInformationControlCreator(getInformationControlCreator(sourceViewer));
        return ca;
    }

    public IAnnotationHover getAnnotationHover(ISourceViewer sourceViewer) {
        if (getLanguageServiceManager() == null) {
            return super.getAnnotationHover(sourceViewer);
        }
        IAnnotationHover hover= getLanguageServiceManager().getAnnotationHover();
        if (hover == null)
            hover= new DefaultAnnotationHover();
        return hover;
    }

    public IAutoEditStrategy[] getAutoEditStrategies(ISourceViewer sourceViewer, String contentType) {
        if (getLanguageServiceManager() == null) {
            return super.getAutoEditStrategies(sourceViewer, contentType);
        }
        Set<org.eclipse.imp.services.IAutoEditStrategy> autoEdits= getLanguageServiceManager().getAutoEditStrategies();

        if (autoEdits == null || autoEdits.size() == 0) {
            return super.getAutoEditStrategies(sourceViewer, contentType);
        }

        return autoEdits.toArray(new IAutoEditStrategy[autoEdits.size()]);
    }

    public IContentFormatter getContentFormatter(ISourceViewer sourceViewer) {
        // Disable the content formatter if no language-specific implementation exists.
        // N.B.: This will probably always be null, since this method gets called before
        // the formatting controller has been instantiated (which happens in
        // instantiateServiceControllers()).
        if (getServiceControllerManager() == null || getServiceControllerManager().getFormattingController() == null)
            return null;

        // For now, assumes only one content type (i.e. one kind of partition)
        ContentFormatter formatter= new ContentFormatter();

        formatter.setFormattingStrategy(getServiceControllerManager().getFormattingController(), IDocument.DEFAULT_CONTENT_TYPE);
        return formatter;
    }

    public String[] getDefaultPrefixes(ISourceViewer sourceViewer, String contentType) {
        return super.getDefaultPrefixes(sourceViewer, contentType);
    }

    public ITextDoubleClickStrategy getDoubleClickStrategy(ISourceViewer sourceViewer, String contentType) {
        return new DoubleClickStrategy(getLanguageServiceManager().getParseController());
    }

    public IHyperlinkDetector[] getHyperlinkDetectors(ISourceViewer sourceViewer) {
        if (getServiceControllerManager() != null && getServiceControllerManager().getHyperLinkController() != null)
            return new IHyperlinkDetector[] { getServiceControllerManager().getHyperLinkController() };
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
                // int shellStyle= SWT.RESIZE | SWT.TOOL;
                // int style= SWT.NONE; // SWT.V_SCROLL | SWT.H_SCROLL;

                // if (BrowserInformationControl.isAvailable(parent))
                // return new BrowserInformationControl(parent, SWT.TOOL | SWT.NO_TRIM, SWT.NONE,
                // EditorsUI.getTooltipAffordanceString());
                // else
                // return new OutlineInformationControl(parent, shellStyle, style, new HTMLTextPresenter(false));
                return new DefaultInformationControl(parent, "Press 'F2' for focus", new HTMLTextPresenter(true));
            }
        };
    }

    private InformationPresenter fInfoPresenter;

    public IInformationPresenter getInformationPresenter(ISourceViewer sourceViewer) {
        if (getLanguageServiceManager() == null) {
            return super.getInformationPresenter(sourceViewer);
        }
        if (fInfoPresenter == null) {
            fInfoPresenter= new InformationPresenter(getInformationControlCreator(sourceViewer));
            fInfoPresenter.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));
            fInfoPresenter.setAnchor(AbstractInformationControlManager.ANCHOR_GLOBAL);

            IInformationProvider provider= new IInformationProvider() { // this should be language-specific
            	private IAnnotationModel annModel= fEditor.getDocumentProvider().getAnnotationModel(fEditor.getEditorInput());

            	public IRegion getSubject(ITextViewer textViewer, int offset) {
                	List<Annotation> parserAnnsAtOffset = getParserAnnotationsAtOffset(offset);
                	if (parserAnnsAtOffset.size() > 0) {
                		Annotation theAnn= parserAnnsAtOffset.get(0);
                		Position pos= annModel.getPosition(theAnn);
                		return new Region(pos.offset, pos.length);
                	}
                    IParseController pc= getLanguageServiceManager().getParseController();
                    ISourcePositionLocator locator= pc.getSourcePositionLocator();
                    if (locator == null) {
                        return new Region(offset, 0);
                    }
                    Object selNode= locator.findNode(pc.getCurrentAst(), offset);
                    return new Region(locator.getStartOffset(selNode), locator.getLength(selNode));
                }

				private List<Annotation> getParserAnnotationsAtOffset(int offset) {
					List<Annotation> result= new LinkedList<Annotation>();
                	for(Iterator<Annotation> iter= annModel.getAnnotationIterator(); iter.hasNext(); ) {
                		Annotation ann= iter.next();

                		if (annModel.getPosition(ann).includes(offset) && UniversalEditor.isParseAnnotation(ann)) {
                			result.add(ann);
                		}
                	}
					return result;
				}

                public String getInformation(ITextViewer textViewer, IRegion subject) {
                	List<Annotation> parserAnnsAtOffset = getParserAnnotationsAtOffset(subject.getOffset());
                	if (parserAnnsAtOffset.size() > 0) {
                		Annotation theAnn= parserAnnsAtOffset.get(0);
                		return theAnn.getText();
                	}
                    IParseController pc= getLanguageServiceManager().getParseController();
                    ISourcePositionLocator locator= pc.getSourcePositionLocator();
                    if (locator == null) {
                        return "";
                    }
                    IDocumentationProvider docProvider= getLanguageServiceManager().getDocProvider();
                    Object selNode= locator.findNode(pc.getCurrentAst(), subject.getOffset());
                    return (docProvider != null) ? docProvider.getDocumentation(selNode, pc) : "No documentation available on the selected entity.";
                }
            };
            fInfoPresenter.setInformationProvider(provider, IDocument.DEFAULT_CONTENT_TYPE);
            fInfoPresenter.setSizeConstraints(60, 10, true, false);
            fInfoPresenter.setRestoreInformationControlBounds(getSettings("outline_presenter_bounds"), true, true); //$NON-NLS-1$
        }
        return fInfoPresenter;
    }

    public ITextHover getTextHover(ISourceViewer sourceViewer, String contentType) {
        if (getServiceControllerManager() == null) {
            return super.getTextHover(sourceViewer, contentType);
        }
        return getServiceControllerManager().getHoverHelpController();
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

    private class OutlineInformationProvider implements IInformationProvider, IInformationProviderExtension {
        private TreeModelBuilderBase fBuilder;

        public IRegion getSubject(ITextViewer textViewer, int offset) {
            return new Region(offset, 0); // Could be anything, since it's ignored below in getInformation2()...
        }

        public String getInformation(ITextViewer textViewer, IRegion subject) {
            return "never called?!?"; // shouldn't be called, given IInformationProviderExtension???
        }

        public Object getInformation2(ITextViewer textViewer, IRegion subject) {
            if (fBuilder == null) {
                fBuilder= getLanguageServiceManager().getModelBuilder();
            }
            return fBuilder.buildTree(getLanguageServiceManager().getParseController().getCurrentAst());
        }
    }

    private IInformationProvider fOutlineElementProvider= new OutlineInformationProvider();

    public IInformationPresenter getOutlinePresenter(ISourceViewer sourceViewer) {
        if (getLanguageServiceManager() == null) {
            return null;
        }
        if (getLanguageServiceManager().getModelBuilder() == null) {
            return null;
        }

        InformationPresenter presenter;

        presenter= new InformationPresenter(getOutlinePresenterControlCreator(sourceViewer, IEditorActionDefinitionIds.SHOW_OUTLINE));
        presenter.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));
        presenter.setAnchor(AbstractInformationControlManager.ANCHOR_GLOBAL);

        IInformationProvider provider= fOutlineElementProvider;

        presenter.setInformationProvider(provider, IDocument.DEFAULT_CONTENT_TYPE);
        // TODO Should associate all other partition types with this provider, too
        presenter.setSizeConstraints(50, 20, true, false);
        presenter.setRestoreInformationControlBounds(getSettings("outline_presenter_bounds"), true, true); //$NON-NLS-1$
        return presenter;
    }

    /**
     * Returns the outline presenter control creator. The creator is a factory creating outline presenter controls for
     * the given source viewer. This implementation always returns a creator for <code>OutlineInformationControl</code>
     * instances.
     * 
     * @param sourceViewer
     *            the source viewer to be configured by this configuration
     * @param commandId
     *            the ID of the command that opens this control
     * @return an information control creator
     */
    private IInformationControlCreator getOutlinePresenterControlCreator(ISourceViewer sourceViewer, final String commandId) {
        return new IInformationControlCreator() {
            public IInformationControl createInformationControl(Shell parent) {
                int shellStyle= SWT.RESIZE;
                int treeStyle= SWT.V_SCROLL | SWT.H_SCROLL;

                return new OutlineInformationControl(parent, shellStyle, treeStyle, commandId, fEditor.getLanguage());
            }
        };
    }

    /**
     * Returns the hierarchy presenter which will determine and shown type hierarchy information requested for the
     * current cursor position.
     * 
     * @param sourceViewer
     *            the source viewer to be configured by this configuration
     * @param doCodeResolve
     *            a boolean which specifies whether code resolve should be used to compute the program element
     * @return an information presenter
     */
    public IInformationPresenter getHierarchyPresenter(ISourceViewer sourceViewer, boolean doCodeResolve) {
        InformationPresenter presenter= new InformationPresenter(getHierarchyPresenterControlCreator(sourceViewer));
        presenter.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));
        presenter.setAnchor(AbstractInformationControlManager.ANCHOR_GLOBAL);
        IInformationProvider provider= null; // TODO RMF new HierarchyInformationProvider(this);
        presenter.setInformationProvider(provider, IDocument.DEFAULT_CONTENT_TYPE);
        // presenter.setInformationProvider(provider, IJavaPartitions.JAVA_DOC);
        // presenter.setInformationProvider(provider, IJavaPartitions.JAVA_MULTI_LINE_COMMENT);
        // presenter.setInformationProvider(provider, IJavaPartitions.JAVA_SINGLE_LINE_COMMENT);
        // presenter.setInformationProvider(provider, IJavaPartitions.JAVA_STRING);
        // presenter.setInformationProvider(provider, IJavaPartitions.JAVA_CHARACTER);
        presenter.setSizeConstraints(50, 20, true, false);
        presenter.setRestoreInformationControlBounds(getSettings("hierarchy_presenter_bounds"), true, true); //$NON-NLS-1$
        return presenter;
    }

    private IInformationControlCreator getHierarchyPresenterControlCreator(ISourceViewer sourceViewer) {
        return new IInformationControlCreator() {
            public IInformationControl createInformationControl(Shell parent) {
                // int shellStyle= SWT.RESIZE;
                // int treeStyle= SWT.V_SCROLL | SWT.H_SCROLL;

                return new DefaultInformationControl(parent); // HierarchyInformationControl(parent, shellStyle,
                                                              // treeStyle);
            }
        };
    }

    /**
     * Returns the settings for the given section.
     * 
     * @param sectionName
     *            the section name
     * @return the settings
     * @since 3.0
     */
    private IDialogSettings getSettings(String sectionName) {
        IDialogSettings settings= RuntimePlugin.getInstance().getDialogSettings().getSection(sectionName);
        if (settings == null)
            settings= RuntimePlugin.getInstance().getDialogSettings().addNewSection(sectionName);
        return settings;
    }

    @Override
    public IQuickAssistAssistant getQuickAssistAssistant(ISourceViewer sourceViewer) {
        return new QuickFixController(fEditor);
    }
}