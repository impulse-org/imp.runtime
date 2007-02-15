package org.eclipse.uide.editor;


import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
//import org.eclipse.ui.plugin.AbstractUIPlugin;

public interface IOutlineImage {

	public String getImageRoot();
	//public static final String IMAGE_ROOT= "icons";

	public ImageDescriptor getOutlineItemDesc();
	//public static ImageDescriptor OUTLINE_ITEM_DESC= AbstractUIPlugin.imageDescriptorFromPlugin("leg", IMAGE_ROOT + "/outline_item.gif");

	public Image getOutlineItemImage();
	//public static Image OUTLINE_ITEM_IMAGE= OUTLINE_ITEM_DESC.createImage();
	
}
