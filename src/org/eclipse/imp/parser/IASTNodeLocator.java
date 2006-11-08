/**
 * 
 */
package org.eclipse.uide.parser;

public interface IASTNodeLocator
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
     * @param node the AST node
     * @return the offset, in characters, of the beginning of the textual extent spanned by the given AST node
     */
    int getStartOffset(Object node);

    /**
     * @return the offset, in characters, of the end of the textual extent spanned by the given AST node
     */
    int getEndOffset(Object node);

    /**
     * @return the length, in characters, of the textual extent spanned by the given AST node
     */
    int getLength(Object node);

    /**
     * @return the path to the compilation unit (source or compiled, if no source) that contains the given AST node
     */
    String getPath(Object node);
}
