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

package org.eclipse.imp.parser;

import java.util.Iterator;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.imp.language.ILanguageService;
import org.eclipse.imp.model.ISourceProject;
import org.eclipse.imp.services.IAnnotationTypeInfo;
import org.eclipse.imp.services.ILanguageSyntaxProperties;
import org.eclipse.jface.text.IRegion;

public interface IParseController extends ILanguageService {
    /**
     * Initialize the parse controller to parse source text corresponding to
     * a compilation unit in the given ISourceProject at the given path.
     * @param filePath either a project-relative path, if project is non-null,
     * or an absolute path
     * @param project the source project to which the compilation unit is
     * considered to belong
     * @param handler the message handler to which error/warning/info messages
     * should be directed
     */
    void initialize(IPath filePath, ISourceProject project, IMessageHandler handler);

    /**
     * @return the ISourceProject that contains the source text that this
     * parse controller will process
     */
    ISourceProject getProject();

    /**
     * @return either a project-relative path, if getProject() is non-null, or an absolute path.
     */
    IPath getPath();

    /**
     * @return the AST corresponding to the most recently-parsed source text,
     * if an AST was successfully produced. In general, there may be an AST
     * even when parse errors were detected (e.g., if error recovery was
     * performed).
     */
    Object getCurrentAst();

    /**
     * @return an ISourcePositionLocator that can be used to correlate
     * program entities (AST nodes, tokens, etc.) to source positions
     */
    ISourcePositionLocator getNodeLocator();

    /**
     * @return an Iterator that iterates over the tokens contained within
     * the given region, including any tokens that are only partially
     * contained
     */
    Iterator getTokenIterator(IRegion region);

    Object parse(String input, boolean scanOnly, IProgressMonitor monitor);

    ILanguageSyntaxProperties getSyntaxProperties();

    IAnnotationTypeInfo getAnnotationTypeInfo();
}
