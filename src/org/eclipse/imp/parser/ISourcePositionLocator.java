/*
 * (C) Copyright IBM Corporation 2007
 * 
 * This file is part of the Eclipse IMP.
 */
/**
 * 
 */
package org.eclipse.imp.parser;

import org.eclipse.core.runtime.IPath;

public interface ISourcePositionLocator
{
    /**
     * @param ast the root of the AST
     * @param offset the textual offset, in characters
     * @return the innermost AST node whose textual extent contains the given text offset
     */
    Object findNode(Object ast, int offset);

    /**
     * @param ast the root of the AST
     * @param startOffset the beginning of the textual extent, in characters
     * @param endOffset the end of the textual extent, in characters
     * @return the innermost AST node whose textual extent completely contains the given text extent
     */
    Object findNode(Object ast, int startOffset, int endOffset);

    /**
     * @param entity the program entity, e.g. AST node, token, or some
     * kind of type system object
     * @return the offset, in characters, of the beginning of the textual extent
     * spanned by the given entity
     */
    int getStartOffset(Object entity);

    /**
     * @param entity the program entity, e.g. AST node, token, or some
     * kind of type system object
     * @return the offset, in characters, of the end of the textual extent
     * spanned by the given entity
     */
    int getEndOffset(Object node);

    /**
     * @param entity the program entity, e.g. AST node, token, or some
     * kind of type system object
     * @return the length, in characters, of the textual extent spanned by the given AST node
     */
    int getLength(Object node);

    /**
     * @return the workspace-relative or file-system absolute path to the compilation unit
     * (source or compiled, if no source) that contains the given AST node.
     * The path is in "portable" format, using the Eclipse convention '/' for the path
     * component separator.
     * @see org.eclipse.core.runtime.IPath#toPortableString()
     */
    IPath getPath(Object node);
}
