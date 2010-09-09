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

package org.eclipse.imp.services.base;

import java.util.List;

import org.eclipse.imp.language.ILanguageService;
import org.eclipse.imp.utils.AnnotationUtils;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.ISourceViewer;

public class DefaultAnnotationHover implements IAnnotationHover, ILanguageService {
	/**
	 * @see IVerticalRulerHover#getHoverInfo(ISourceViewer, int)
	 */
	public String getHoverInfo(ISourceViewer sourceViewer, int lineNumber) {
		List<Annotation> annotations = AnnotationUtils.getAnnotationsForLine(sourceViewer, lineNumber);

		return AnnotationUtils.formatAnnotationList(annotations);
	}
}
