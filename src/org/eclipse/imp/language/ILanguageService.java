/*
 * (C) Copyright IBM Corporation 2007
 * 
 * This file is part of the Eclipse IMP.
 */
package org.eclipse.imp.language;


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

    String CONTENT_PROPOSER_SERVICE= "contentProposer";
    String HOVER_HELPER_SERVICE= "hoverHelper";
    String TOKEN_COLORER_SERVICE= "tokenColorer";
    String INDEX_CONTRIBUTOR_SERVICE= "indexContributor";

    /**
     * Parser service implementation. Must implement interface IParseController.
     */
    String PARSER_SERVICE= "parser";
    String LISTENER_SERVICE= "modelListener";
    String AUTO_EDIT_SERVICE= "autoEditStrategy";
    String FOLDING_SERVICE= "foldingUpdater";
    String ANNOTATION_HOVER_SERVICE= "annotationHover";
    String FORMATTER_SERVICE= "formatter";
    String HYPERLINK_SERVICE= "hyperLink";
    String LABEL_PROVIDER_SERVICE= "labelProvider";
    String IMAGE_DECORATOR_SERVICE= "imageDecorator";
    String OUTLINE_CONTENT_PROVIDER_SERVICE= "outlineContentProvider";
    String REFACTORING_CONTRIBUTIONS_SERVICE= "refactoringContributions";
    String REFERENCE_RESOLVER_SERVICE= "referenceResolvers";
    String EDITOR_ACTION_SERVICE= "editorActionContributions";
    String PREFERENCES_SERVICE= "preferencesDialog";
    String PREFERENCES_SPECIFICATION= "preferencesSpecification";

    String DOCUMENTATION_PROVIDER_SERVICE= "documentationProvider";
    String VIEWER_FILTER_SERVICE= "viewerFilter";
    String OCCURRENCE_MARKER= "markOccurrences";
}
