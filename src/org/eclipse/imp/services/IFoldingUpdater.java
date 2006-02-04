package org.eclipse.uide.editor;

import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;
import org.eclipse.uide.core.ILanguageService;
import org.eclipse.uide.parser.IParseController;

public interface IFoldingUpdater extends ILanguageService {
    void updateFoldingStructure(IParseController parseController, ProjectionAnnotationModel annotationModel);
}
