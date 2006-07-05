package org.eclipse.uide.editor;

import org.eclipse.uide.parser.IParseController;


/**
 * Specifies methods to support the construction of hyperlinks between
 * AST nodes.
 * 
 * @author sutton
 *
 */


public interface IReferenceResolver {
	
	/**
	 * Returns true or false according to whether the given node has
	 * a type that may serve as the source of a hyperlink (or at least
	 * for which a target node can reasonably be requested).
	 * 
	 * The purpose of this method is to allow unsuitble nodes to be
	 * screened out before an attempt is made to identify their target
	 * nodes.
	 * 
	 * Note:  If the getLinkText(..) method is robust and efficient, then
	 * the check for a suitable source type may be bypassed (or the
	 * implementation of this method may always return true), and a null
	 * target can simply be returned for nodes that cannot serve as link
	 * sources because of their type.
	 * 
	 * @param node	A candidate source node
	 * @return		True or false according to whether the given
	 * 				node has a type that can serve as a source
	 * 				for hyperlinks (or at least that getLinkTarget(..)
	 * 				can accept as input)
	 */
	public boolean hasSuitableLinkSourceType(Object node);

	
	/**
	 * Returns the AST node that represents the target of a hyperlink that
	 * has the given AST node as a source and that is interpreted in the
	 * context of the AST represented by the given parse controller.
	 * 
	 * Normally the source and target nodes should both occur in the AST
	 * represented by the given parse controller.
	 * 
	 * @param node				The AST node to be considered as the source
	 * 							of a hyperlink
	 * @param parseController	The parse controller that contains the AST with
	 * 							respect to which the link is defined
	 * @return					The AST node that is the target of the hyperlink,
	 * 							if found, or null if no target node has been found
	 */
	public Object getLinkTarget(Object node, IParseController parseController);
	
	
	/**
	 * Returns a text string to be associated with a hyperlink.
	 *
	 * Historical note:  So far in practice we have obtained the link
	 * text from the source node.  This method may also be used with the
	 * target node.  If a need arises, we can add another method that
	 * would take two nodes.  SMS 13 Jun 2006.
	 * 
	 * @param node		The node with respect to which the link text is
	 * 					to be determined
	 * @return			The link text determined with respect to the
	 * 					given node.
	 */
	public String getLinkText(Object node);	

}
