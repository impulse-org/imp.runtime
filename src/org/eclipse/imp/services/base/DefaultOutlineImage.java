package org.eclipse.uide.defaults;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.uide.editor.IOutlineImage;



public class DefaultOutlineImage implements IOutlineImage
{
	private DefaultOutlineImage() { }
	
	private static IOutlineImage image = null;
	
	public static IOutlineImage getDefaultOutlineImage() {
		if (image == null) {
			image = new DefaultOutlineImage();
		}
		return image;
	}
	

	private static final String IMAGE_ROOT= "icons";

	private static ImageDescriptor OUTLINE_ITEM_DESC= AbstractUIPlugin.imageDescriptorFromPlugin("org.eclipse.uide.runtime", IMAGE_ROOT + "/outline_item.gif");

																	private static Image OUTLINE_ITEM_IMAGE= OUTLINE_ITEM_DESC.createImage();

	
	public String getImageRoot() {
		return IMAGE_ROOT;
	}

	public ImageDescriptor getOutlineItemDesc() {
		return OUTLINE_ITEM_DESC;
	}

	public Image getOutlineItemImage() {
		return OUTLINE_ITEM_IMAGE;
	}

}

