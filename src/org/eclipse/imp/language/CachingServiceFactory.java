package org.eclipse.imp.language;

import java.util.Set;

import org.eclipse.imp.editor.OutlineContentProviderBase;
import org.eclipse.imp.editor.OutlineLabelProvider.IElementImageProvider;
import org.eclipse.imp.indexing.IndexContributorBase;
import org.eclipse.imp.parser.IModelListener;
import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.services.IAnnotationHover;
import org.eclipse.imp.services.IAutoEditStrategy;
import org.eclipse.imp.services.IContentProposer;
import org.eclipse.imp.services.IDocumentationProvider;
import org.eclipse.imp.services.IFoldingUpdater;
import org.eclipse.imp.services.IHoverHelper;
import org.eclipse.imp.services.ILabelProvider;
import org.eclipse.imp.services.ILanguageActionsContributor;
import org.eclipse.imp.services.ILanguageSyntaxProperties;
import org.eclipse.imp.services.IOccurrenceMarker;
import org.eclipse.imp.services.IOutliner;
import org.eclipse.imp.services.IRefactoringContributor;
import org.eclipse.imp.services.IReferenceResolver;
import org.eclipse.imp.services.ISourceFormatter;
import org.eclipse.imp.services.ISourceHyperlinkDetector;
import org.eclipse.imp.services.ITokenColorer;
import org.eclipse.imp.services.base.TreeModelBuilderBase;

public class CachingServiceFactory extends ServiceFactory {

    private IContentProposer contentProposer;
    private IOutliner outliner;
    private IElementImageProvider elementImageProvider;
    private ILanguageSyntaxProperties syntaxProperties;
    private IOccurrenceMarker occurrenceMarker;
    private IDocumentationProvider documentationProvider;
    private Set<ILanguageActionsContributor> languageActionsContributors;
    private IReferenceResolver referenceResolver;
    private Set<IRefactoringContributor> refactoringContributors;
    private OutlineContentProviderBase outlineContentProvider;
    private ILabelProvider labelProvider;
    private ISourceHyperlinkDetector sourceHyperlinkDetector;
    private ISourceFormatter sourceFormatter;
    private IAnnotationHover annotationHover;
    private IFoldingUpdater foldingUpdater;
    private IAutoEditStrategy autoEditStrategy;
    private IModelListener modelListener;
    private TreeModelBuilderBase treeModelBuilder;
    private IParseController parseController;
    private IndexContributorBase indexContributor;
    private ITokenColorer tokenColorer;
    private IHoverHelper hoverHelper;

    public CachingServiceFactory() {
    }

    public IContentProposer getContentProposer(Language lang)
            throws ServiceException {
        if (contentProposer != null) {
            contentProposer = super.getContentProposer(lang);
        }

        return contentProposer;
    }

    public IHoverHelper getHoverHelper(Language lang) throws ServiceException {
        if (hoverHelper != null) {
            hoverHelper = super.getHoverHelper(lang);
        }

        return hoverHelper;
    }

    public ITokenColorer getTokenColorer(Language lang) throws ServiceException {
        if (tokenColorer != null) {
            tokenColorer = super.getTokenColorer(lang);
        }

        return tokenColorer;
    }

    public IndexContributorBase getIndexContributor(Language lang)
            throws ServiceException {
        if (indexContributor != null) {
            indexContributor = super.getIndexContributor(lang);
        }

        return indexContributor;
    }

    public IParseController getParseController(Language lang)
            throws ServiceException {
        if (parseController != null) {
            parseController = super.getParseController(lang);
        }

        return parseController;
    }

    public TreeModelBuilderBase getTreeModelBuilder(Language lang)
            throws ServiceException {
        if (treeModelBuilder != null) {
            treeModelBuilder = super.getTreeModelBuilder(lang);
        }

        return treeModelBuilder;
    }

    public IModelListener getModelListener(Language lang)
            throws ServiceException {
        if (modelListener != null) {
            modelListener = super.getModelListener(lang);
        }

        return modelListener;
    }

    public IAutoEditStrategy getAutoEditStrategy(Language lang)
            throws ServiceException {
        if (autoEditStrategy != null) {
            autoEditStrategy = super.getAutoEditStrategy(lang);
        }

        return autoEditStrategy;
    }

    public IFoldingUpdater getFoldingUpdater(Language lang)
            throws ServiceException {
        if (foldingUpdater != null) {
            foldingUpdater = super.getFoldingUpdater(lang);
        }

        return foldingUpdater;
    }

    public IAnnotationHover getAnnotationHover(Language lang)
            throws ServiceException {
        if (annotationHover != null) {
            annotationHover = super.getAnnotationHover(lang);
        }

        return annotationHover;
    }

    public ISourceFormatter getSourceFormatter(Language lang)
            throws ServiceException {
        if (sourceFormatter != null) {
            sourceFormatter = super.getSourceFormatter(lang);
        }

        return sourceFormatter;
    }

    public ISourceHyperlinkDetector getSourceHyperlinkDetector(Language lang)
            throws ServiceException {
        if (sourceHyperlinkDetector != null) {
            sourceHyperlinkDetector = super.getSourceHyperlinkDetector(lang);
        }

        return sourceHyperlinkDetector;
    }

    public ILabelProvider getLabelProvider(Language lang) {
        if (labelProvider != null) {
            labelProvider = super.getLabelProvider(lang);
        }

        return labelProvider;
    }

    public OutlineContentProviderBase getOutlineContentProvider(Language lang) {
        if (outlineContentProvider != null) {
            outlineContentProvider = super.getOutlineContentProvider(lang);
        }

        return outlineContentProvider;
    }

    public Set<IRefactoringContributor> getRefactoringContributors(Language lang) {
        if (refactoringContributors != null) {
            refactoringContributors = super.getRefactoringContributors(lang);
        }

        return refactoringContributors;
    }

    public IReferenceResolver getReferenceResolver(Language lang) {
        if (referenceResolver != null) {
            referenceResolver = super.getReferenceResolver(lang);
        }

        return referenceResolver;
    }

    @SuppressWarnings("unchecked")
    public Set<ILanguageActionsContributor> getLanguageActionsContributors(
            Language lang) {
        if (languageActionsContributors != null) {
            languageActionsContributors = super
                    .getLanguageActionsContributors(lang);
        }

        return languageActionsContributors;
    }

    public IDocumentationProvider getDocumentationProvider(Language lang) {
        if (documentationProvider != null) {
            documentationProvider = super.getDocumentationProvider(lang);
        }

        return documentationProvider;
    }

    public IOccurrenceMarker getOccurrenceMarker(Language lang) {
        if (occurrenceMarker != null) {
            occurrenceMarker = super.getOccurrenceMarker(lang);
        }

        return occurrenceMarker;
    }

    public ILanguageSyntaxProperties getSyntaxProperties(Language lang) {
        if (syntaxProperties != null) {
            syntaxProperties = super.getSyntaxProperties(lang);
        }

        return syntaxProperties;
    }

    public IElementImageProvider getElementImageProvider(Language lang)
            throws ServiceException {
        if (elementImageProvider != null) {
            elementImageProvider = super.getElementImageProvider(lang);
        }

        return elementImageProvider;
    }

    public IOutliner getOldOutliner(Language lang) throws ServiceException {
        if (outliner != null) {
            outliner = super.getOutliner(lang);
        }

        return outliner;
    }
}
