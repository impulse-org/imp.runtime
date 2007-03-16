package org.eclipse.uide.core;


/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 1998, 2004  All Rights Reserved
 */


/**
 * This interface is to be implemented by language services used by the Universal Editor.
 */
public interface ILanguageService {
    /**
     * Sets the language for this service. Examples would be "Java", "C", "Pascal".
     * The language name has to be registered through the <tt>org.eclipse.uide.runtime.languageDescription</tt> extension point.
     * 
     * @param language the canonical name of the language this service is meant for
     */
    //public void setLanguage(String language);

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
    String LABEL_PROVIDER_SERVICE= "labelProvider";
    String IMAGE_DECORATOR_SERVICE= "imageDecorator";
    String OUTLINE_CONTENT_PROVIDER_SERVICE= "outlineContentProvider";
    String REFACTORING_CONTRIBUTIONS_SERVICE= "refactoringContributions";
    String REFERENCE_RESOLVER_SERVICE= "referenceResolvers";
    String EDITOR_ACTION_SERVICE= "editorActionContributions";
    String PREFERENCES_SERVICE= "preferencesDialog";

    String DOCUMENTATION_PROVIDER_SERVICE= "documentationProvider";
    String VIEWER_FILTER_SERVICE= "viewerFilter";
}
