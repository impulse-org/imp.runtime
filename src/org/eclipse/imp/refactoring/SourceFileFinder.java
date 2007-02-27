package org.eclipse.uide.refactoring;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.uide.core.ILanguageService;
import org.eclipse.uide.core.Language;
import org.eclipse.uide.parser.IParseController;
import org.eclipse.uide.runtime.RuntimePlugin;
import org.eclipse.uide.utils.ExtensionPointFactory;

public abstract class SourceFileFinder implements IResourceVisitor {
    private final TextFileDocumentProvider fProvider;

    protected final IProject fProject;

    protected final IFileVisitor fVisitor;

    protected final Language fLanguage;

    private final Set<String> fFileNameExtensions= new HashSet<String>();

    public SourceFileFinder(TextFileDocumentProvider provider, IProject project, IFileVisitor visitor, Language language) {
        super();
        fProvider= provider;
        fProject= project;
        fVisitor= visitor;
        fLanguage= language;

        Language lang= fLanguage;

        while (lang != null) {
            String[] extens= lang.getFilenameExtensions();
            for(int i= 0; i < extens.length; i++) {
                fFileNameExtensions.add(extens[i]);
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
        IParseController parseCtrlr= (IParseController) ExtensionPointFactory.createExtensionPoint(fLanguage, RuntimePlugin.UIDE_RUNTIME, ILanguageService.PARSER_SERVICE);
        String declFilePath= file.getLocation().toOSString().substring(fProject.getLocation().toOSString().length() + 1);
        IFileEditorInput fileInput= new FileEditorInput(file);

        parseCtrlr.initialize(declFilePath, fProject, null);
        try {
            fProvider.connect(fileInput);
        } catch (CoreException e) {
            e.printStackTrace();
            return;
        }

        IDocument document= fProvider.getDocument(fileInput);
        Object astRoot= parseCtrlr.parse(document.get(), false, null);

        fVisitor.enterFile(file);
        doVisit(file, document, astRoot);
        fVisitor.leaveFile(file);
    }

    public abstract void doVisit(IFile file, IDocument doc, Object astRoot);
}
