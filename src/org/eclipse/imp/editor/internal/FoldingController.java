/*
 * (C) Copyright IBM Corporation 2007
 * 
 * This file is part of the Eclipse IMP.
 */
package org.eclipse.imp.editor.internal;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.imp.parser.IModelListener;
import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.services.IFoldingUpdater;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;

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
