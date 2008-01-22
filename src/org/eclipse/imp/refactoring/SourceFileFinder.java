/*
 * (C) Copyright IBM Corporation 2007
 * 
 * This file is part of the Eclipse IMP.
 */
package org.eclipse.imp.refactoring;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.imp.language.ILanguageService;
import org.eclipse.imp.language.Language;
import org.eclipse.imp.model.ISourceProject;
import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.utils.ExtensionPointFactory;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;
import org.eclipse.ui.part.FileEditorInput;

public abstract class SourceFileFinder implements IResourceVisitor {
    private final TextFileDocumentProvider fProvider;

    protected final ISourceProject fProject;

    protected final IFileVisitor fVisitor;

    protected final Language fLanguage;

    private final Set<String> fFileNameExtensions= new HashSet<String>();

    public SourceFileFinder(TextFileDocumentProvider provider, ISourceProject project, IFileVisitor visitor, Language language) {
        super();
        fProvider= provider;
        fProject= project;
        fVisitor= visitor;
        fLanguage= language;

        Language lang= fLanguage;

        // Do we really want to include base language source files? I don't think so...
        while (lang != null) {
            for (String ext : lang.getFilenameExtensions()) {
                fFileNameExtensions.add(ext);
            }
            lang= lang.getBaseLanguage();
        }
    }

    public boolean visit(IResource resource) throws CoreException {
        if (resource instanceof IFile) {
            IFile file= (IFile) resource;
            if (fFileNameExtensions.contains(file.getFileExtension())) {
                visitFile(file);
            }
            return false;
        }
        return true;
    }

    private void visitFile(IFile file) {
        System.out.println("Visiting file " + file.getName() + ".");
        IParseController parseCtrlr= (IParseController) ExtensionPointFactory.createExtensionPoint(fLanguage, ILanguageService.PARSER_SERVICE);
        IPath declFilePath= file.getLocation().removeFirstSegments(fProject.getRawProject().getLocation().segmentCount());
        IFileEditorInput fileInput= new FileEditorInput(file);

        parseCtrlr.initialize(declFilePath, fProject, null);
        try {
            fProvider.connect(fileInput);
        } catch (CoreException e) {
            e.printStackTrace();
            return;
        }

        IDocument document= fProvider.getDocument(fileInput);
        Object astRoot= parseCtrlr.parse(document.get(), false, new NullProgressMonitor()); // TODO get a real monitor from somewhere...

        fVisitor.enterFile(file);
        doVisit(file, document, astRoot);
        fVisitor.leaveFile(file);
    }

    public abstract void doVisit(IFile file, IDocument doc, Object astRoot);
}
