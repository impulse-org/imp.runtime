package org.eclipse.imp.editor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.imp.core.ErrorHandler;
import org.eclipse.imp.language.LanguageRegistry;
import org.eclipse.imp.model.ISourceProject;
import org.eclipse.imp.model.ModelFactory;
import org.eclipse.imp.model.ModelFactory.ModelException;
import org.eclipse.imp.parser.IMessageHandler;
import org.eclipse.imp.parser.IModelListener;
import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.preferences.PreferenceCache;
import org.eclipse.imp.runtime.RuntimePlugin;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.IDocumentProvider;

/**
 * Parsing may take a long time, and is not done inside the UI thread. Therefore, we create a job that is executed in a
 * background thread by the platform's job service.
 */
// TODO Perhaps this should be driven off of the "IReconcilingStrategy" mechanism?
public class ParserScheduler extends Job {
    private final IParseController fParseController;

    private final IEditorPart fEditorPart;

    private final IDocumentProvider fDocumentProvider;

    private final IMessageHandler fMsgHandler;

    private final List<IModelListener> fAstListeners= new ArrayList<IModelListener>();

    public ParserScheduler(IParseController parseController, IEditorPart editorPart,
            IDocumentProvider docProvider, IMessageHandler msgHandler) {
    	super(LanguageRegistry.findLanguage(EditorInputUtils.getPath(editorPart.getEditorInput()), null) + " ParserScheduler");
        setSystem(true); // do not show this job in the Progress view
        fParseController= parseController;
        fEditorPart= editorPart;
        fDocumentProvider= docProvider;
        fMsgHandler= msgHandler;

        // rmf 7/1/2008 - N.B. The parse controller is now initialized before it gets handed to us here,
        // since some other services may actually depend on that.
    }

    public IStatus run(IProgressMonitor monitor) {
        if (fParseController == null || fDocumentProvider == null) {
            /* Editor was closed, or no parse controller */
            return Status.OK_STATUS;
        }

        IEditorInput editorInput= fEditorPart.getEditorInput();
        try {
            IDocument document= fDocumentProvider.getDocument(editorInput);

            if (PreferenceCache.emitMessages)
                RuntimePlugin.getInstance().writeInfoMsg(
                        "Parsing language " + fParseController.getLanguage().getName() + " for input " + editorInput.getName());

            // Don't need to retrieve the AST; we don't need it.
            // Just make sure the document contents gets parsed once (and only once).
            fMsgHandler.clearMessages();
            fParseController.parse(document.get(), false, monitor);
            if (!monitor.isCanceled())
                notifyAstListeners(monitor);
        } catch (Exception e) {
            ErrorHandler.reportError("Error running parser for language " + fParseController.getLanguage().getName() + " and input " + editorInput.getName() + ":", e);
            // RMF 8/2/2006 - Notify the AST listeners even on an exception - the compiler front end
            // may have failed at some phase, but there may be enough info to drive IDE services.
            notifyAstListeners(monitor);
        }
        return Status.OK_STATUS;
    }

    public void addModelListener(IModelListener listener) {
        fAstListeners.add(listener);
    }

    public void notifyAstListeners(IProgressMonitor monitor) {
        // Suppress the notification if there's no AST (e.g. due to a parse error)
        if (fParseController != null) {
            if (PreferenceCache.emitMessages)
                RuntimePlugin.getInstance().writeInfoMsg(
                        "Notifying AST listeners of change in " + fParseController.getPath().toPortableString());
            for(int n= fAstListeners.size() - 1; n >= 0 && !monitor.isCanceled(); n--) {
                IModelListener listener= fAstListeners.get(n);
                // Pretend to get through the highest level of analysis so all services execute (for now)
                int analysisLevel= IModelListener.AnalysisRequired.POINTER_ANALYSIS.level();

                if (fParseController.getCurrentAst() == null)
                    analysisLevel= IModelListener.AnalysisRequired.LEXICAL_ANALYSIS.level();
                // TODO How to tell how far we got with the source analysis? The IAnalysisController should tell us!
                // TODO Rename IParseController to IAnalysisController
                // TODO Compute the minimum amount of analysis sufficient for all current listeners, and pass that to
                // the IAnalysisController.
                if (listener.getAnalysisRequired().level() <= analysisLevel)
                    listener.update(fParseController, monitor);
            }
        } else if (PreferenceCache.emitMessages)
            RuntimePlugin.getInstance().writeInfoMsg("No AST; bypassing listener notification.");
    }
}
