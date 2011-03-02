package org.eclipse.imp.parser;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.imp.language.Language;
import org.eclipse.imp.language.LanguageRegistry;
import org.eclipse.imp.model.ISourceProject;
import org.eclipse.jface.text.IDocument;

/**
 * Base class for implementations of IParseController that takes care of maintaining the
 * language, project, path, message handler, and the current AST. This implementation is
 * entirely language- and parser-agnostic (i.e. it can be used with any type of parser).
 * @author rfuhrer@watson.ibm.com
 */
public abstract class ParseControllerBase implements IParseController {
    /**
     * The language of the source being parsed by this IParseController.
     */
    protected Language fLanguage;

    /**
     * The project containing the source being parsed by this IParseController. May be null
     * if the source isn't actually part of an Eclipse project (e.g., a random bit of source
     * text living outside the workspace).
     */
	protected ISourceProject fProject;

	/**
	 * The path to the file containing the source being parsed by this {@link IParseController}.
	 */
	protected IPath fFilePath;

	/**
	 * The {@link IMessageHandler} to which parser/compiler messages are directed.
	 */
	protected IMessageHandler handler;

	/**
	 * The current AST (if any) produced by the most recent successful parse.<br>
	 * N.B.: "Successful" may mean that there were syntax errors, but the parser managed
	 * to perform error recovery and still produce an AST.
	 */
	protected Object fCurrentAst;

	/**
	 * The most-recently parsed source document. May be null if this parse controller
	 * has never parsed an IDocument before.
	 */
	protected IDocument fDocument;

	/**
	 * Constructor that does not set up the fLanguage field.<br>
	 * Only intended for use of parse controllers outside IMP.
	 */
	public ParseControllerBase() { }

	public ParseControllerBase(String languageID) {
		fLanguage= LanguageRegistry.findLanguage(languageID);
	}

	public void initialize(IPath filePath, ISourceProject project, IMessageHandler handler) {
		this.fProject= project;
		this.fFilePath= filePath;
		this.handler= handler;
	}

	public Object parse(IDocument doc, IProgressMonitor monitor) {
	    fDocument= doc;

	    return parse(fDocument.get(), monitor);
	}

	public Language getLanguage() {
		return fLanguage;
	}

	public ISourceProject getProject() {
		return fProject;
	}

	public IPath getPath() {
		return fFilePath;
	}

	public IMessageHandler getHandler() {
		return handler;
	}

	public Object getCurrentAst() {
		return fCurrentAst;
	}

	public IDocument getDocument() {
	    return fDocument;
	}
}
