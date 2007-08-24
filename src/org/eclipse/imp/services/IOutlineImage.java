/*
 * (C) Copyright IBM Corporation 2007
 * 
 * This file is part of the Eclipse IMP.
 */
package org.eclipse.imp.services;


import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

public interface IOutlineImage {

	public String getImageRoot();
	//public static final String IMAGE_ROOT= "icons";

	public ImageDescriptor getOutlineItemDesc();
	//public static ImageDescriptor OUTLINE_ITEM_DESC= AbstractUIPlugin.imageDescriptorFromPlugin("leg", IMAGE_ROOT + "/outline_item.gif");

	public Image getOutlineItemImage();
	//public static Image OUTLINE_ITEM_IMAGE= OUTLINE_ITEM_DESC.createImage();
	
}
