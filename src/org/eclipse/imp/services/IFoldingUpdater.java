/*
 * (C) Copyright IBM Corporation 2007
 * 
 * This file is part of the Eclipse IMP.
 */
package org.eclipse.imp.services;

import org.eclipse.imp.language.ILanguageService;
import org.eclipse.imp.parser.IParseController;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;

public interface IFoldingUpdater extends ILanguageService {
    void updateFoldingStructure(IParseController parseController, ProjectionAnnotationModel annotationModel);
}
