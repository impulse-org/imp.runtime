/**
 * 
 */
package org.eclipse.uide.parser;


public interface IASTNodeLocator {
	Ast findNode(Ast ast, int offset);
	Ast findNode(Ast ast, int startOffset, int endOffset);
}