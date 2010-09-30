/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Robert Fuhrer (rfuhrer@watson.ibm.com) - initial API and implementation
 *******************************************************************************/

package org.eclipse.imp.editor;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.imp.runtime.RuntimePlugin;
import org.eclipse.imp.utils.StreamUtils;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IURIEditorInput;
import org.eclipse.ui.editors.text.StorageDocumentProvider;

class ZipDocumentProvider extends StorageDocumentProvider {
    public static boolean canHandle(IEditorInput editorInput) {
        if (editorInput instanceof IURIEditorInput) {
            IURIEditorInput uriEditorInput = (IURIEditorInput) editorInput;
            URI uri= uriEditorInput.getURI();
            String path= uri.getPath();

            return path.contains(".jar:") || path.contains(".zip:");
        }
        return false;
    }

    @Override
    protected IAnnotationModel createAnnotationModel(Object element) throws CoreException {
        // If we don't do this, the resulting editor won't permit source folding
        return new AnnotationModel();
    }

    @Override
    protected ElementInfo createElementInfo(Object element) throws CoreException {
        ElementInfo ei= super.createElementInfo(element);
        ei.fDocument= new Document(getZipEntryContents((IURIEditorInput) element));
        return ei;
    }

    @Override
    protected boolean setDocumentContent(IDocument document, IEditorInput editorInput) throws CoreException {
        if (!(editorInput instanceof IURIEditorInput)) {
            throw new IllegalArgumentException("Inappropriate type of IEditorInput passed to ZipDocumentProvider: " + editorInput.getClass());
        }
        IURIEditorInput uriEditorInput = (IURIEditorInput) editorInput;
        String contents = getZipEntryContents(uriEditorInput);

        document.set(contents);
        return true;
    }

    private String getZipEntryContents(IURIEditorInput uriEditorInput) {
        String contents= "";
        try {
            URI uri= uriEditorInput.getURI();
            String path= uri.getPath();
            int lastColonIdx = path.lastIndexOf(':');
			String jarPath= path.substring(0, lastColonIdx);
            String entryPath= path.substring(lastColonIdx + 1);

            ZipFile zipFile= new ZipFile(new File(jarPath));
            ZipEntry entry= zipFile.getEntry(entryPath);
            InputStream is= zipFile.getInputStream(entry);
            contents= StreamUtils.readStreamContents(is);
        } catch (Exception e) {
            RuntimePlugin.getInstance().logException("Exception caught while obtaining contents of zip file entry", e);
        }
        return contents;
    }
}

