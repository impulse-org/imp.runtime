/*
 * (C) Copyright IBM Corporation 2007
 * 
 * This file is part of the Eclipse IMP.
 */
package org.eclipse.imp.editor;

import org.eclipse.imp.editor.UniversalEditor.StructuredSourceViewerConfiguration;
import org.eclipse.imp.parser.IParseController;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentRewriteSession;
import org.eclipse.jface.text.DocumentRewriteSessionType;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension4;
import org.eclipse.jface.text.information.IInformationPresenter;
import org.eclipse.jface.text.source.IOverviewRuler;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;

public class StructuredSourceViewer extends ProjectionViewer {
    /**
     * Text operation code for requesting the outline for the current input.
     */
    public static final int SHOW_OUTLINE= 51;

    /**
     * Text operation code for requesting the outline for the element at the current position.
     */
    public static final int OPEN_STRUCTURE= 52;

    /**
     * Text operation code for requesting the hierarchy for the current input.
     */
    public static final int SHOW_HIERARCHY= 53;

    /**
     * Text operation code for toggling the commenting of a selected range of text, or the current line.
     */
    public static final int TOGGLE_COMMENT= 54;

    /**
     * Text operation code for toggling the display of "occurrences" of the
     * current selection, whatever that means to the current language.
     */
    public static final int MARK_OCCURRENCES= 55;

    private IInformationPresenter fOutlinePresenter;

    private IInformationPresenter fStructurePresenter;

    private IInformationPresenter fHierarchyPresenter;

    // SMS 29 May 2007
    private IParseController fParseController;
    
    
    /**
     * Is this source viewer configured?
     *
     * @since 3.0
     */
    private boolean fIsConfigured;

    public StructuredSourceViewer(Composite parent, IVerticalRuler verticalRuler, IOverviewRuler overviewRuler, boolean showAnnotationsOverview, int styles) {
	super(parent, verticalRuler, overviewRuler, showAnnotationsOverview, styles);
    }

    /*
     * @see ITextOperationTarget#doOperation(int)
     */
    public void doOperation(int operation) {
	if (getTextWidget() == null)
	    return;
	switch (operation) {
	case SHOW_OUTLINE:
	    if (fOutlinePresenter != null)
		fOutlinePresenter.showInformation();
	    return;
	case OPEN_STRUCTURE:
	    if (fStructurePresenter != null)
		fStructurePresenter.showInformation();
	    return;
	case SHOW_HIERARCHY:
	    if (fHierarchyPresenter != null)
		fHierarchyPresenter.showInformation();
	    return;
	case TOGGLE_COMMENT:
	    doToggleComment();
	}
	super.doOperation(operation);
    }
    
    // SMS 29 May 2007 (see doToggleComment())
    public void setParseController(IParseController parseController) {
    	fParseController = parseController;
    }

    private void doToggleComment() {
	IDocument doc= this.getDocument();
	DocumentRewriteSession rewriteSession= null;
	Point p= this.getSelectedRange();
	//final String lineCommentStart= "//"; // RMF this needs to be language-specific
	// SMS 29 May 2007
	// Short term solution to getting language-specific single-line
	// comment prefix (i.e., from parse controller)
	final String lineCommentStart;
	if (fParseController != null)
		lineCommentStart = fParseController.getSingleLineCommentPrefix();
	else
		lineCommentStart = "";

	if (doc instanceof IDocumentExtension4) {
	    IDocumentExtension4 extension= (IDocumentExtension4) doc;
	    rewriteSession= extension.startRewriteSession(DocumentRewriteSessionType.SEQUENTIAL);
	}

	try {
	    final int selStart= p.x;
	    final int selLen= p.y;
	    final int selEnd= selStart + selLen;
	    final int startLine= doc.getLineOfOffset(selStart);
	    int endLine= doc.getLineOfOffset(selEnd);

	    if (selLen > 0 && doc.getChar(selEnd-1) == '\n')
		endLine--;
	    for(int line= startLine; line <= endLine; line++) {
		int lineStartOffset= doc.getLineOffset(line);
		int offset= lineStartOffset;

		while (Character.isWhitespace(doc.getChar(offset)) && offset < doc.getLength())
		    offset++;
		if (doc.get(offset, lineCommentStart.length()).equals(lineCommentStart)) {
		    int len= lineCommentStart.length();

		    while (Character.isWhitespace(doc.getChar(offset+len)))
			len++;
		    doc.replace(offset, len, "");
		} else
		    doc.replace(offset, 0, lineCommentStart + " ");
	    }
	} catch (BadLocationException e) {
	    e.printStackTrace();
	} finally {
	    if (doc instanceof IDocumentExtension4) {
		IDocumentExtension4 extension= (IDocumentExtension4) doc;
		extension.stopRewriteSession(rewriteSession);
	    }
	    restoreSelection();
	}
    }

    /*
     * @see ITextOperationTarget#canDoOperation(int)
     */
    public boolean canDoOperation(int operation) {
	if (operation == SHOW_OUTLINE)
	    return fOutlinePresenter != null;
	if (operation == OPEN_STRUCTURE)
	    return fStructurePresenter != null;
	if (operation == SHOW_HIERARCHY)
	    return fHierarchyPresenter != null;
	if (operation == TOGGLE_COMMENT)
	    return true;
	return super.canDoOperation(operation);
    }

    /*
     * @see ISourceViewer#configure(SourceViewerConfiguration)
     */
    public void configure(SourceViewerConfiguration configuration) {
	/*
	 * Prevent access to colors disposed in unconfigure(), see:
	 *   https://bugs.eclipse.org/bugs/show_bug.cgi?id=53641
	 *   https://bugs.eclipse.org/bugs/show_bug.cgi?id=86177
	 */
	StyledText textWidget= getTextWidget();
	if (textWidget != null && !textWidget.isDisposed()) {
	    Color foregroundColor= textWidget.getForeground();
	    if (foregroundColor != null && foregroundColor.isDisposed())
		textWidget.setForeground(null);
	    Color backgroundColor= textWidget.getBackground();
	    if (backgroundColor != null && backgroundColor.isDisposed())
		textWidget.setBackground(null);
	}
	super.configure(configuration);
	if (configuration instanceof StructuredSourceViewerConfiguration) {
	    StructuredSourceViewerConfiguration sSVConfiguration= (StructuredSourceViewerConfiguration) configuration;

	    fOutlinePresenter= sSVConfiguration.getOutlinePresenter(this);
	    if (fOutlinePresenter != null)
		fOutlinePresenter.install(this);
	    fStructurePresenter= sSVConfiguration.getOutlinePresenter(this);
	    if (fStructurePresenter != null)
		fStructurePresenter.install(this);
	    fHierarchyPresenter= sSVConfiguration.getHierarchyPresenter(this, true);
	    if (fHierarchyPresenter != null)
		fHierarchyPresenter.install(this);
	}
//	if (fPreferenceStore != null) {
//	    fPreferenceStore.addPropertyChangeListener(this);
//	    initializeViewerColors();
//	}
	fIsConfigured= true;
    }

    /*
     * @see org.eclipse.jface.text.source.ISourceViewerExtension2#unconfigure()
     * @since 3.0
     */
    public void unconfigure() {
	if (fOutlinePresenter != null) {
	    fOutlinePresenter.uninstall();
	    fOutlinePresenter= null;
	}
	if (fStructurePresenter != null) {
	    fStructurePresenter.uninstall();
	    fStructurePresenter= null;
	}
	if (fHierarchyPresenter != null) {
	    fHierarchyPresenter.uninstall();
	    fHierarchyPresenter= null;
	}
//	if (fForegroundColor != null) {
//	    fForegroundColor.dispose();
//	    fForegroundColor= null;
//	}
//	if (fBackgroundColor != null) {
//	    fBackgroundColor.dispose();
//	    fBackgroundColor= null;
//	}
//	if (fPreferenceStore != null)
//	    fPreferenceStore.removePropertyChangeListener(this);
	super.unconfigure();
	fIsConfigured= false;
    }
}
