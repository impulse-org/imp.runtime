package org.eclipse.uide.editor;

import org.eclipse.uide.parser.IParseController;

/**
 * Provides a default implementation for (most of) the the ILinnkMapper interface,
 * the purpose of which is to support the creation of hyperlinks between nodes
 * in an AST.
 * 
 * The principal methods remaining for the user to implement are
 * 		public Object getLinkTarget(Object node, IParseController parseController)
 * and
 * 		public String getLinkText(Object node)
 * which will currently return not-very-useful values.
 * 
 * @author sutton
 *
 */
public abstract class ReferenceResolver implements IReferenceResolver {

	protected Class[] fSourceTypes = null;
	
	public ReferenceResolver() { };
	
	public ReferenceResolver(Class[] sourceTypes)
	{
		fSourceTypes = sourceTypes;
	}
	

	protected boolean contains(Class[] types, Class type)
	{
		if (types == null || type == null) return false;
		for (int i = 0; i < types.length; i++) {
			if (types[i].equals(type)) return true;
		}
		return false;
	}
	
	
	public void setSourceTypes(Class[] sourceTypes) {
		fSourceTypes = sourceTypes;
	}
	
	
	public void addSourceType (Class type)
	{
		if (contains(fSourceTypes, type)) return;
		Class[] newSourceTypes = new Class[fSourceTypes.length + 1];
		for (int i = 0; i < fSourceTypes.length; i++) {
			newSourceTypes[i] = fSourceTypes[i];
		}
		newSourceTypes[newSourceTypes.length-1] = type;
		fSourceTypes = newSourceTypes;
	}

	
	public void removeSourceType(Class type)
	{
		if (!contains(fSourceTypes, type)) return;
		Class[] newSourceTypes = new Class[fSourceTypes.length - 1];
		int j = 0;
		for (int i = 0; i < fSourceTypes.length; i++) {
			if (fSourceTypes[i].equals(type)) continue;
			newSourceTypes[j] = fSourceTypes[i];
			j++;
		}
		fSourceTypes = newSourceTypes;
	}
	

	// If you don't wish to enumerate the source types for hyperlinks
	// then fSourceTypes can be ignored and this method can be 
	// overridden in the language-specific concrete subclass so as
	// to return an appropriate value in some other way
	public boolean hasSuitableLinkSourceType(Object node) {
		return contains(fSourceTypes, node.getClass());
	}

	
	public Object getLinkTarget(Object node, IParseController parseController) {
		// TODO Auto-generated method stub
		System.err.println("ReferenceResolver.getLinkTarget(..):  UNIMPLEMENTED; returning null");
		return null;
	}

	
	public String getLinkText(Object node) {
		// TODO Auto-generated method stub
		System.err.println("ReferenceResolver.getLinkTarget(..):  UNIMPLEMENTED; returning 'Text unspecified'");
		return "Text unspecified";
	}
	
}
