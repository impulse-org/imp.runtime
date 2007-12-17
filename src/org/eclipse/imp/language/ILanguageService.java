/*
 * (C) Copyright IBM Corporation 2007
 * 
 * This file is part of the Eclipse IMP.
 */
package org.eclipse.imp.language;

import org.eclipse.imp.editor.OutlineContentProviderBase;
import org.eclipse.imp.indexing.IndexContributorBase;
import org.eclipse.imp.parser.IModelListener;
import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.services.IContentProposer;
import org.eclipse.imp.services.IFoldingUpdater;
import org.eclipse.imp.services.IHoverHelper;
import org.eclipse.imp.services.ILanguageActionsContributor;
import org.eclipse.imp.services.IOccurrenceMarker;
import org.eclipse.imp.services.IRefactoringContributor;
import org.eclipse.imp.services.IReferenceResolver;
import org.eclipse.imp.services.ISourceFormatter;
import org.eclipse.imp.services.ISourceHyperlinkDetector;
import org.eclipse.imp.services.base.TreeModelBuilderBase;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.viewers.ILabelProvider;


/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 1998, 2004  All Rights Reserved
 */


/**
 * This interface is to be implemented by language services used by the Universal Editor.
 */
public interface ILanguageService {
    // The constants below identify the various language services, and are defined
    // for use by clients that wish to instantiate a given language service.

    /**
     * Content proposer service extension point ID.
     * Extensions must implement {@link IContentProposer}.
     */
    String CONTENT_PROPOSER_SERVICE= "contentProposer";

    /**
     * Hover helper service extension point ID.
     * Extensions must implement {@link IHoverHelper}.
     */
    String HOVER_HELPER_SERVICE= "hoverHelper";

    /**
     * Token colorer service extension point ID.
     * Extensions must implement {@link IndexContributorBase}.
     */
    String TOKEN_COLORER_SERVICE= "tokenColorer";

    /**
     * Index contributor service extension point ID.
     * Extensions must implement {@link IndexContributorBase}.
     */
    String INDEX_CONTRIBUTOR_SERVICE= "indexContributor";

    /**
     * Parser service  extension point ID.
     * Extensions must implement interface {@link IParseController}.
     */
    String PARSER_SERVICE= "parser";

    /**
     * Model tree builder service extension point ID.
     * Extensions must extend {@link TreeModelBuilderBase}.
     */
    String MODEL_BUILDER_SERVICE= "modelTreeBuilder";

    /**
     * Model listener service extension point ID.
     * Extensions must implement {@link IModelListener}.
     */
    String LISTENER_SERVICE= "modelListener";

    /**
     * Auto edit service extension point ID.
     * Extensions must implement {@link IAutoEditStrategy}.
     */
    String AUTO_EDIT_SERVICE= "autoEditStrategy";

    /**
     * Source folding service extension point ID.
     * Extensions must implement {@link IFoldingUpdater}.
     */
    String FOLDING_SERVICE= "foldingUpdater";

    /**
     * Annotation hover service extension point ID.
     * Extensions must implement {@link IAnnotationHover}.
     */
    String ANNOTATION_HOVER_SERVICE= "annotationHover";

    /**
     * Source formatting service extension point ID.
     * Extensions must implement {@link ISourceFormatter}.
     */
    String FORMATTER_SERVICE= "formatter";

    /**
     * Hyperlink detector service extension point ID.
     * Extensions must implement {@link ISourceHyperlinkDetector}.
     */
    String HYPERLINK_SERVICE= "hyperLink";

    /**
     * Label provider service extension point ID.
     * Extensions must implement {@link ILabelProvider}.
     */
    String LABEL_PROVIDER_SERVICE= "labelProvider";

    String IMAGE_DECORATOR_SERVICE= "imageDecorator";

    /**
     * Outline content provider service extension point ID.
     * Extensions must extend {@link OutlineContentProviderBase}.
     */
    String OUTLINE_CONTENT_PROVIDER_SERVICE= "outlineContentProvider";

    /**
     * Refactoring contributions service extension point ID.
     * Extensions must implement {@link IRefactoringContributor}.
     */
    String REFACTORING_CONTRIBUTIONS_SERVICE= "refactoringContributions";

    /**
     * Reference resolver service extension point ID.
     * Extensions must implement {@link IReferenceResolver}.
     */
    String REFERENCE_RESOLVER_SERVICE= "referenceResolvers";

    /**
     * Editor action contributions service extension point ID.
     * Extensions must implement {@link ILanguageActionsContributor}.
     */
    String EDITOR_ACTION_SERVICE= "editorActionContributions";

    String PREFERENCES_SERVICE= "preferencesDialog";
    String PREFERENCES_SPECIFICATION= "preferencesSpecification";

    String DOCUMENTATION_PROVIDER_SERVICE= "documentationProvider";
    String VIEWER_FILTER_SERVICE= "viewerFilter";

    /**
     * Occurrence detector service extension point ID.
     * Extensions must implement {@link IOccurrenceMarker}.
     */
    String OCCURRENCE_MARKER= "markOccurrences";

    /**
     * Language syntax properties service extension point ID.
     * Extensions must implement {@link ILanguageSyntaxProperties}.
     */
    String SYNTAX_PROPS= "syntaxProps";
}
