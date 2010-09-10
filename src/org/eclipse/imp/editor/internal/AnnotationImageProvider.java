/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.imp.editor.internal;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.imp.editor.quickfix.IAnnotation;
import org.eclipse.imp.preferences.PreferenceConstants;
import org.eclipse.imp.runtime.PluginImages;
import org.eclipse.imp.runtime.RuntimePlugin;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IMarkerHelpRegistry;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.IAnnotationImageProvider;
import org.eclipse.ui.texteditor.SimpleMarkerAnnotation;

/**
 * Image provider for annotations based on problem markers.
 * 
 * @since 3.0
 */
public class AnnotationImageProvider implements IAnnotationImageProvider {

	private final static int NO_IMAGE = 0;
	private final static int GRAY_IMAGE = 1;
	// private final static int OVERLAY_IMAGE= 2;
	private final static int QUICKFIX_IMAGE = 3;
	private final static int QUICKFIX_ERROR_IMAGE = 4;

	private static Image fgQuickFixImage;
	private static Image fgQuickFixErrorImage;
	private static ImageRegistry fgImageRegistry;

	private boolean fShowQuickFixIcon;
	private int fCachedImageType;
	private Image fCachedImage;

	public AnnotationImageProvider() {
		fShowQuickFixIcon = RuntimePlugin.getInstance().getPreferenceStore()
				.getBoolean(PreferenceConstants.EDITOR_CORRECTION_INDICATION);
	}

	/*
	 * @see
	 * org.eclipse.jface.text.source.IAnnotationImageProvider#getManagedImage
	 * (org.eclipse.jface.text.source.Annotation)
	 */
	public Image getManagedImage(Annotation annotation) {
		if (annotation instanceof IAnnotation) {
			int imageType = getImageType(annotation);
			return getImage(annotation, imageType, Display.getCurrent());
		}
		return null;
	}

	/*
	 * @see
	 * org.eclipse.jface.text.source.IAnnotationImageProvider#getImageDescriptorId
	 * (org.eclipse.jface.text.source.Annotation)
	 */
	public String getImageDescriptorId(Annotation annotation) {
		// unmanaged images are not supported
		return null;
	}

	/*
	 * @see
	 * org.eclipse.jface.text.source.IAnnotationImageProvider#getImageDescriptor
	 * (.lang.String)
	 */
	public ImageDescriptor getImageDescriptor(String symbolicName) {
		// unmanaged images are not supported
		return null;
	}

	private boolean showQuickFix(Annotation annotation) {
		return fShowQuickFixIcon && hasCorrections(annotation);
	}

	private Image getQuickFixImage() {
		if (fgQuickFixImage == null)
			fgQuickFixImage = PluginImages
					.get(PluginImages.IMG_OBJS_FIXABLE_PROBLEM);
		return fgQuickFixImage;
	}

	private Image getQuickFixErrorImage() {
		if (fgQuickFixErrorImage == null)
			fgQuickFixErrorImage = PluginImages
					.get(PluginImages.IMG_OBJS_FIXABLE_ERROR);
		return fgQuickFixErrorImage;
	}

	private ImageRegistry getImageRegistry(Display display) {
		if (fgImageRegistry == null)
			fgImageRegistry = new ImageRegistry(display);
		return fgImageRegistry;
	}

	private int getImageType(Annotation annotation) {
		int imageType = NO_IMAGE;
		// if (annotation.hasOverlay())
		// imageType= OVERLAY_IMAGE;
		// else
		if (!annotation.isMarkedDeleted()) {
			if (showQuickFix(annotation)) {
				imageType = IStatus.ERROR == ((IAnnotation) annotation)
						.getSeverity() ? QUICKFIX_ERROR_IMAGE : QUICKFIX_IMAGE;
			}
		} else {
			imageType = GRAY_IMAGE;
		}
		return imageType;
	}

	private Image getImage(Annotation annotation, int imageType, Display display) {
		if ((imageType == QUICKFIX_IMAGE || imageType == QUICKFIX_ERROR_IMAGE)
				&& fCachedImageType == imageType)
			return fCachedImage;

		Image image = null;
		switch (imageType) {
		// case OVERLAY_IMAGE:
		// IAnnotation overlay= annotation.getOverlay();
		// image= getManagedImage((Annotation) overlay);
		// fCachedImageType= -1;
		// break;
		case QUICKFIX_IMAGE:
			image = getQuickFixImage();
			fCachedImageType = imageType;
			fCachedImage = image;
			break;
		case QUICKFIX_ERROR_IMAGE:
			image = getQuickFixErrorImage();
			fCachedImageType = imageType;
			fCachedImage = image;
			break;
		case GRAY_IMAGE: {
			ISharedImages sharedImages = PlatformUI.getWorkbench()
					.getSharedImages();
			String annotationType = annotation.getType();
			if ("error".equals(annotationType)) {
				image = sharedImages.getImage(ISharedImages.IMG_OBJS_ERROR_TSK);
			} else if ("warning".equals(annotationType)) {
				image = sharedImages.getImage(ISharedImages.IMG_OBJS_WARN_TSK);
			} else if ("info".equals(annotationType)) {
				image = sharedImages.getImage(ISharedImages.IMG_OBJS_INFO_TSK);
			}
			if (image != null) {
				ImageRegistry registry = getImageRegistry(display);
				String key = Integer.toString(image.hashCode());
				Image grayImage = registry.get(key);
				if (grayImage == null) {
					grayImage = new Image(display, image, SWT.IMAGE_GRAY);
					registry.put(key, grayImage);
				}
				image = grayImage;
			}
			fCachedImageType = -1;
			break;
		}
		}

		return image;
	}

	public static boolean hasCorrections(Annotation annotation) {
		if (annotation instanceof IAnnotation) {
			IAnnotation iAnnotation = (IAnnotation) annotation;
			int problemId = iAnnotation.getId();
			if (problemId != -1) {
				IEditorPart editor = PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow().getActivePage()
						.getActiveEditor();
				if (editor instanceof UniversalEditor) {
					QuickFixController qfc = new QuickFixController(
							(UniversalEditor) editor);
					return qfc.canFix(annotation);
				}
			}
		}
		if (annotation instanceof SimpleMarkerAnnotation) {
			return hasCorrections(((SimpleMarkerAnnotation) annotation)
					.getMarker());
		}
		return false;
	}

	private static boolean hasCorrections(IMarker marker) {
		if (marker == null || !marker.exists())
			return false;

		IMarkerHelpRegistry registry = IDE.getMarkerHelpRegistry();
		return registry != null && registry.hasResolutions(marker);
	}
}
