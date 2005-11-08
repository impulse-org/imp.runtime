/**
 * 
 */
package org.eclipse.uide.parser;

public interface IASTNodeLocator {
    Object findNode(Object ast, int offset);

    Object findNode(Object ast, int startOffset, int endOffset);
}