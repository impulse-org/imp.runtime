package org.eclipse.uide.internal.editor;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;
import org.eclipse.uide.editor.IFoldingUpdater;
import org.eclipse.uide.parser.IModelListener;
import org.eclipse.uide.parser.IParseController;

public class FoldingController implements IModelListener {
    private final ProjectionAnnotationModel fAnnotationModel;
    private final IFoldingUpdater fFoldingUpdater;

    public FoldingController(ProjectionAnnotationModel annotationModel, IFoldingUpdater foldingUpdater) {
	super();
	this.fAnnotationModel= annotationModel;
	this.fFoldingUpdater= foldingUpdater;
    }

    public AnalysisRequired getAnalysisRequired() {
        return AnalysisRequired.SYNTACTIC_ANALYSIS;
    }

    public void update(IParseController parseController, IProgressMonitor monitor) {
	if (fAnnotationModel != null) // can be null if file is outside workspace
	    fFoldingUpdater.updateFoldingStructure(parseController, fAnnotationModel);
    }
}
