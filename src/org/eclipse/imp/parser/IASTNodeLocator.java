/**
 * 
 */
package org.eclipse.uide.parser;

public interface IASTNodeLocator
{
    Object findNode(Object ast, int offset);

    Object findNode(Object ast, int startOffset, int endOffset);
    
    // SMS 23 Jun 2006
    // The following three methods are new, to avoid the need to refer
    // to a more language-specific entity in order to get these values
    
    int getStartOffset(Object node);
    
    int getEndOffset(Object node);
    
    int getLength(Object  node);
    
}