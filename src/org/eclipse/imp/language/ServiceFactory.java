package org.eclipse.imp.language;

import java.util.HashSet;
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
import org.eclipse.imp.utils.ExtensionFactory;

/**
 * This class stores language services. IMP services are configured with
 * language specific extension points. This registry provides implementations
 * for them. It finds the implementations by looking for Eclipse extensions for
 * IMP's extension points.
 * 
 * If IMP is extended with a new kind of language service, this class must be
 * extended.
 * 
 * The getter methods of this class return 'null' when a service does not exist
 * (i.e. an extension has not been provided yet)
 * 
 * The getter methods of this class will throw unchecked exceptions when the
 * extension implementations are not well formed.
 * 
 * The getter methods only load the extension implementations the first time
 * somebody asks for them. After that they are cached in the registry. This lazy
 * behavior is necessary to optimize the startup time of Eclipse.
 * 
 * @author jurgenv
 * 
 */
public class ServiceFactory {
    private static ServiceFactory sInstance;

    String OUTLINER_SERVICE = "outliner";

    String CONTENT_PROPOSER_SERVICE = "contentProposer";

    String HOVER_HELPER_SERVICE = "hoverHelper";

    String TOKEN_COLORER_SERVICE = "tokenColorer";

    String INDEX_CONTRIBUTOR_SERVICE = "indexContributor";

    String PARSER_SERVICE = "parser";

    String MODEL_BUILDER_SERVICE = "modelTreeBuilder";

    String LISTENER_SERVICE = "modelListener";

    String AUTO_EDIT_SERVICE = "autoEditStrategy";

    String FOLDING_SERVICE = "foldingUpdater";

    String ANNOTATION_HOVER_SERVICE = "annotationHover";

    String FORMATTER_SERVICE = "formatter";

    String HYPERLINK_SERVICE = "hyperLink";

    String LABEL_PROVIDER_SERVICE = "labelProvider";

    String IMAGE_DECORATOR_SERVICE = "imageDecorator";

    String OUTLINE_CONTENT_PROVIDER_SERVICE = "outlineContentProvider";

    String REFACTORING_CONTRIBUTIONS_SERVICE = "refactoringContributions";

    String REFERENCE_RESOLVER_SERVICE = "referenceResolvers";

    String EDITOR_ACTION_SERVICE = "editorActionContributions";

    String PREFERENCES_SERVICE = "preferencesDialog";

    String PREFERENCES_SPECIFICATION = "preferencesSpecification";

    String DOCUMENTATION_PROVIDER_SERVICE = "documentationProvider";

    String VIEWER_FILTER_SERVICE = "viewerFilter";

    String OCCURRENCE_MARKER = "markOccurrences";

    String SYNTAX_PROPS = "syntaxProps";

    protected ServiceFactory() {
    }

    /**
     * Returns the {@link ServiceFactory}. IMP services are configured with
     * language specific extension points. This registry provides the
     * implementations for them. This class finds these implementations via
     * Eclipse's extension point mechanism.
     * 
     * @return
     */
    public static ServiceFactory getInstance() {
        if (sInstance == null) {
            sInstance = new ServiceFactory();
        }
        return sInstance;
    }

    public IContentProposer getContentProposer(Language lang)
            throws ServiceException {
        try {
            return (IContentProposer) loadService(lang,
                    CONTENT_PROPOSER_SERVICE);
        } catch (ClassCastException e) {
            throw new ServiceException(
                    "Alleged implementation of CONTENT_PROPOSER_SERVICE does not implement IContentProposer",
                    e);
        }
    }

    public IHoverHelper getHoverHelper(Language lang) throws ServiceException {
        try {
            return (IHoverHelper) loadService(lang, HOVER_HELPER_SERVICE);
        } catch (ClassCastException e) {
            throw new ServiceException(
                    "Alleged implementation of HOVER_HELPER_SERVICE does not implement IHoverHelper",
                    e);
        }
    }

    public ITokenColorer getTokenColorer(Language lang) throws ServiceException {
        try {
            return (ITokenColorer) loadService(lang, TOKEN_COLORER_SERVICE);
        } catch (ClassCastException e) {
            throw new ServiceException(
                    "Alleged implementation of TOKEN_COLORER_SERVICE does not implement ITokenColorer",
                    e);
        }
    }

    public IndexContributorBase getIndexContributor(Language lang)
            throws ServiceException {
        try {
            return (IndexContributorBase) loadService(lang,
                    INDEX_CONTRIBUTOR_SERVICE);
        } catch (ClassCastException e) {
            throw new ServiceException(
                    "Alleged implementation of INDEX_CONTRIBUTOR_SERVICE does not implement IndexContributorBase",
                    e);
        }
    }

    public IParseController getParseController(Language lang)
            throws ServiceException {
        try {
            return (IParseController) loadService(lang, PARSER_SERVICE);
        } catch (ClassCastException e) {
            throw new ServiceException(
                    "Alleged implementation of PARSER_SERVICE does not implement IParseController",
                    e);
        }
    }

    public TreeModelBuilderBase getTreeModelBuilder(Language lang)
            throws ServiceException {
        try {
            return (TreeModelBuilderBase) loadService(lang,
                    MODEL_BUILDER_SERVICE);
        } catch (ClassCastException e) {
            throw new ServiceException(
                    "Alleged implementation of MODEL_BUILDER_SERVICE does not implement TreeModelBuilderBase",
                    e);
        }
    }

    public IModelListener getModelListener(Language lang)
            throws ServiceException {
        try {
            return (IModelListener) loadService(lang, LISTENER_SERVICE);
        } catch (ClassCastException e) {
            throw new ServiceException(
                    "Alleged implementation of LISTENER_SERVICE does not implement IModelListener",
                    e);
        }
    }

    public IAutoEditStrategy getAutoEditStrategy(Language lang)
            throws ServiceException {
        try {
            return (IAutoEditStrategy) loadService(lang, AUTO_EDIT_SERVICE);
        } catch (ClassCastException e) {
            throw new ServiceException(
                    "Alleged implementation of AUTO_EDIT_SERVICE does not implement IAutoEditStrategy",
                    e);
        }
    }

    public IFoldingUpdater getFoldingUpdater(Language lang)
            throws ServiceException {
        try {
            return (IFoldingUpdater) loadService(lang, FOLDING_SERVICE);
        } catch (ClassCastException e) {
            throw new ServiceException(
                    "Alleged implementation of FOLDING_SERVICE does not implement IFoldingUpdater",
                    e);
        }
    }

    public IAnnotationHover getAnnotationHover(Language lang)
            throws ServiceException {
        try {
            return (IAnnotationHover) loadService(lang,
                    ANNOTATION_HOVER_SERVICE);
        } catch (ClassCastException e) {
            throw new ServiceException(
                    "Alleged implementation of ANNOTATION_HOVER_SERVICE does not implement IAnnotationHover",
                    e);
        }
    }

    public ISourceFormatter getSourceFormatter(Language lang)
            throws ServiceException {
        try {
            return (ISourceFormatter) loadService(lang, FORMATTER_SERVICE);
        } catch (ClassCastException e) {
            throw new ServiceException(
                    "Alleged implementation of FORMATTER_SERVICE does not implement ISourceFormatter",
                    e);
        }
    }

    public ISourceHyperlinkDetector getSourceHyperlinkDetector(Language lang)
            throws ServiceException {
        try {
            return (ISourceHyperlinkDetector) loadService(lang,
                    HYPERLINK_SERVICE);
        } catch (ClassCastException e) {
            throw new ServiceException(
                    "Alleged implementation of HYPERLINK_SERVICE does not implement ISourceHyperlinkDetector",
                    e);
        }
    }

    public ILabelProvider getLabelProvider(Language lang) {
        try {
            return (ILabelProvider) loadService(lang, LABEL_PROVIDER_SERVICE);
        } catch (ClassCastException e) {
            throw new ServiceException(
                    "Alleged implementation of LABEL_PROVIDER_SERVICE does not implement ILabelProvider",
                    e);
        }
    }

    public OutlineContentProviderBase getOutlineContentProvider(Language lang) {
        try {
            return (OutlineContentProviderBase) loadService(lang,
                    OUTLINE_CONTENT_PROVIDER_SERVICE);
        } catch (ClassCastException e) {
            throw new ServiceException(
                    "Alleged implementation of OUTLINE_CONTENT_PROVIDER_SERVICE does not implement OutlineContentProviderBase",
                    e);
        }
    }

    public Set<IRefactoringContributor> getRefactoringContributors(Language lang) {
        try {
            Set<ILanguageService> services = loadServices(lang,
                    REFACTORING_CONTRIBUTIONS_SERVICE);
            Set<IRefactoringContributor> refactoringContribs = new HashSet<IRefactoringContributor>();

            for (ILanguageService s : services) {
                refactoringContribs.add((IRefactoringContributor) s);
            }

            return refactoringContribs;
        } catch (ClassCastException e) {
            throw new ServiceException(
                    "Alleged implementation of SERVICE does not implement Set<ILanguageSerivice>",
                    e);
        }
    }

    public IReferenceResolver getReferenceResolver(Language lang) {
        try {
            return (IReferenceResolver) loadService(lang,
                    REFERENCE_RESOLVER_SERVICE);
        } catch (ClassCastException e) {
            throw new ServiceException(
                    "Alleged implementation of REFERENCE_RESOLVER_SERVICE does not implement IReferenceResolver",
                    e);
        }
    }

    public Set<ILanguageActionsContributor> getLanguageActionsContributors(
            Language lang) {
        try {
            Set<ILanguageService> services = loadServices(lang,
                    EDITOR_ACTION_SERVICE);

            Set<ILanguageActionsContributor> actionContributors = new HashSet<ILanguageActionsContributor>();

            for (ILanguageService s : services) {
                actionContributors.add((ILanguageActionsContributor) s);
            }

            return actionContributors;
        } catch (ClassCastException e) {
            throw new ServiceException(
                    "Alleged implementation of EDITOR_ACTION_SERVICE does not implement ILanguageActionConstributor",
                    e);
        }
    }

    public IDocumentationProvider getDocumentationProvider(Language lang) {
        try {
            return (IDocumentationProvider) loadService(lang,
                    DOCUMENTATION_PROVIDER_SERVICE);
        } catch (ClassCastException e) {
            throw new ServiceException(
                    "Alleged implementation of DOCUMENTATION_PROVIDER_SERVICE does not implement IDocumentationProvider",
                    e);
        }
    }

    public IOccurrenceMarker getOccurrenceMarker(Language lang) {
        try {
            return (IOccurrenceMarker) loadService(lang, OCCURRENCE_MARKER);
        } catch (ClassCastException e) {
            throw new ServiceException(
                    "Alleged implementation of OCCURRENCE_MARKER does not implement IOccurrenceMarker",
                    e);
        }
    }

    public ILanguageSyntaxProperties getSyntaxProperties(Language lang) {
        try {
            return (ILanguageSyntaxProperties) loadService(lang, SYNTAX_PROPS);
        } catch (ClassCastException e) {
            throw new ServiceException(
                    "Alleged implementation of SYNTAX_PROPS does not implement ILanguageSyntaxProperties",
                    e);
        }
    }

    public IElementImageProvider getElementImageProvider(Language lang)
            throws ServiceException {
        try {
            return (IElementImageProvider) loadService(lang,
                    IMAGE_DECORATOR_SERVICE);
        } catch (ClassCastException e) {
            throw new ServiceException(
                    "Alleged implementation of IMAGE_DECORATOR_SERVICE does not implement IElementImageProvider",
                    e);
        }
    }

    public IOutliner getOutliner(Language lang) throws ServiceException {
        try {
            return (IOutliner) loadService(lang, OUTLINER_SERVICE);
        } catch (ClassCastException e) {
            throw new ServiceException(
                    "Alleged implementation of OLD_OUTLINER_SERVICE does not implement IOutliner",
                    e);
        }
    }

    private ILanguageService createExtensionPoint(Language lang, String id)
            throws ServiceException {
        return ExtensionFactory.createServiceExtension(lang, id);
    }

    private Set<ILanguageService> createExtensionPoints(Language lang, String id)
            throws ServiceException {
        return ExtensionFactory.createServiceExtensionSet(lang, id);
    }

    private Set<ILanguageService> loadServices(Language lang, String serviceId)
            throws ServiceException {
        return createExtensionPoints(lang, serviceId);
    }

    private ILanguageService loadService(Language lang, String name)
            throws ServiceException {
        return createExtensionPoint(lang, name);
    }

}
