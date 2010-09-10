/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Andre Soereng <andreis@fast.no> - [syntax highlighting] highlight numbers - https://bugs.eclipse.org/bugs/show_bug.cgi?id=63573
 *******************************************************************************/
package org.eclipse.imp.editor.internal;

import java.util.ResourceBundle;

import org.eclipse.osgi.util.NLS;

/**
 * Helper class to get NLSed messages.
 */
final class EditorMessages extends NLS {

	private static final String BUNDLE_FOR_CONSTRUCTED_KEYS= "org.eclipse.imp.editor.internal.ConstructedEditorMessages";//$NON-NLS-1$
	private static ResourceBundle fgBundleForConstructedKeys= ResourceBundle.getBundle(BUNDLE_FOR_CONSTRUCTED_KEYS);

	/**
	 * Returns the message bundle which contains constructed keys.
	 *
	 * @since 3.1
	 * @return the message bundle
	 */
	public static ResourceBundle getBundleForConstructedKeys() {
		return fgBundleForConstructedKeys;
	}

	private static final String BUNDLE_NAME= EditorMessages.class.getName();


	private EditorMessages() {
		// Do not instantiate
	}

	static {
		NLS.initializeMessages(BUNDLE_NAME, EditorMessages.class);
	}

}