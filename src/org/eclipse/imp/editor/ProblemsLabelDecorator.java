package org.eclipse.uide.editor;

import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;

/**
 * Decorates an image of a program element to indicate any related errors/warnings.
 * See org.eclipse.jdt.ui.ProblemsLabelDecorator for inspiration.
 * @author rfuhrer
 */
public class ProblemsLabelDecorator implements ILabelDecorator {
    public ProblemsLabelDecorator() {
	super();
    }

    public Image decorateImage(Image image, Object element) {
	return image; // do nothing for now...
    }

    public String decorateText(String text, Object element) {
	return text; // do nothing for now...
    }

    public void addListener(ILabelProviderListener listener) {
    // TODO Auto-generated method stub
    }

    public void dispose() {
    // TODO Auto-generated method stub
    }

    public boolean isLabelProperty(Object element, String property) {
	// TODO Auto-generated method stub
	return false;
    }

    public void removeListener(ILabelProviderListener listener) {
    // TODO Auto-generated method stub
    }
}
