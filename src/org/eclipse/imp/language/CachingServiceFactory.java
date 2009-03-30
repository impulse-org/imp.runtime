/*******************************************************************************
* Copyright (c) 2007 IBM Corporation.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*    Robert Fuhrer (rfuhrer@watson.ibm.com) - initial API and implementation

*******************************************************************************/

package org.eclipse.imp.language;

import java.util.Set;

import org.eclipse.imp.editor.OutlineContentProviderBase;
import org.eclipse.imp.editor.OutlineLabelProvider.IElementImageProvider;
import org.eclipse.imp.indexing.IndexContributorBase;
import org.eclipse.imp.parser.IModelListener;
import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.services.IASTAdapter;
import org.eclipse.imp.services.IASTMatchAdapter;
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
    private IASTAdapter astAdapter;

    public CachingServiceFactory() {
    }

    public IContentProposer getContentProposer(Language lang)
            {
        if (contentProposer != null) {
            contentProposer = super.getContentProposer(lang);
        }

        return contentProposer;
    }

    public IHoverHelper getHoverHelper(Language lang) {
        if (hoverHelper != null) {
            hoverHelper = super.getHoverHelper(lang);
        }

        return hoverHelper;
    }

    public ITokenColorer getTokenColorer(Language lang) {
        if (tokenColorer != null) {
            tokenColorer = super.getTokenColorer(lang);
        }

        return tokenColorer;
    }

    public IndexContributorBase getIndexContributor(Language lang)
            {
        if (indexContributor != null) {
            indexContributor = super.getIndexContributor(lang);
        }

        return indexContributor;
    }

    public IParseController getParseController(Language lang)
            {
        if (parseController != null) {
            parseController = super.getParseController(lang);
        }

        return parseController;
    }

    public TreeModelBuilderBase getTreeModelBuilder(Language lang)
            {
        if (treeModelBuilder != null) {
            treeModelBuilder = super.getTreeModelBuilder(lang);
        }

        return treeModelBuilder;
    }

    public IModelListener getModelListener(Language lang)
            {
        if (modelListener != null) {
            modelListener = super.getModelListener(lang);
        }

        return modelListener;
    }

    public IAutoEditStrategy getAutoEditStrategy(Language lang)
            {
        if (autoEditStrategy != null) {
            autoEditStrategy = super.getAutoEditStrategy(lang);
        }

        return autoEditStrategy;
    }

    public IFoldingUpdater getFoldingUpdater(Language lang)
            {
        if (foldingUpdater != null) {
            foldingUpdater = super.getFoldingUpdater(lang);
        }

        return foldingUpdater;
    }

    public IAnnotationHover getAnnotationHover(Language lang)
            {
        if (annotationHover != null) {
            annotationHover = super.getAnnotationHover(lang);
        }

        return annotationHover;
    }

    public ISourceFormatter getSourceFormatter(Language lang)
            {
        if (sourceFormatter != null) {
            sourceFormatter = super.getSourceFormatter(lang);
        }

        return sourceFormatter;
    }

    public ISourceHyperlinkDetector getSourceHyperlinkDetector(Language lang)
            {
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
            {
        if (elementImageProvider != null) {
            elementImageProvider = super.getElementImageProvider(lang);
        }

        return elementImageProvider;
    }

    public IOutliner getOutliner(Language lang) {
        if (outliner != null) {
            outliner = super.getOutliner(lang);
        }

        return outliner;
    }

    public IASTAdapter getASTAdapter(Language lang) {
        if (astAdapter != null) {
            astAdapter = super.getASTAdapter(lang);
        }

        return astAdapter;
    }
}
