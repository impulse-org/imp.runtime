package org.eclipse.uide.editor;

import org.eclipse.jdt.ui.text.JavaSourceViewerConfiguration;
import org.eclipse.jface.text.information.IInformationPresenter;
import org.eclipse.jface.text.source.IOverviewRuler;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.uide.editor.UniversalEditor.StructuredSourceViewerConfiguration;

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

    private IInformationPresenter fOutlinePresenter;

    private IInformationPresenter fStructurePresenter;

    private IInformationPresenter fHierarchyPresenter;

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
	}
	super.doOperation(operation);
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
