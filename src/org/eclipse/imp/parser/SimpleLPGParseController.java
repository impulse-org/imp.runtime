package org.eclipse.uide.parser;

import java.util.Collections;
import java.util.List;
import lpg.runtime.IMessageHandler;
import lpg.runtime.IToken;
import lpg.runtime.Monitor;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.uide.model.ISourceProject;

/**
 * Base class for an IParseController implementation that encapsulates a simple LPG-based
 * scanner and parser.
 * @author rfuhrer
 */
public abstract class SimpleLPGParseController implements IParseController {
    protected ISourceProject fProject;

    protected IPath fFilePath;

    protected Object fCurrentAst;

    private char fKeywords[][];

    private boolean fIsKeyword[];

    /**
     * An adapter from an Eclipse IProgressMonitor to an LPG Monitor
     */
    protected class PMMonitor implements Monitor {
	private IProgressMonitor monitor;

	private boolean wasCancelled= false;

	public PMMonitor(IProgressMonitor monitor) {
	    this.monitor= monitor;
	}

	public boolean isCancelled() {
	    if (!wasCancelled)
		wasCancelled= monitor.isCanceled();
	    return wasCancelled;
	}
    }

    public SimpleLPGParseController() {}

    public void initialize(IPath filePath, ISourceProject project, IMessageHandler handler) {
	this.fProject= project;
	this.fFilePath= filePath;
    }

    public ISourceProject getProject() {
	return fProject;
    }

    public IPath getPath() {
	return fFilePath;
    }

    public Object getCurrentAst() {
	return fCurrentAst;
    }

    public char[][] getKeywords() {
	return fKeywords;
    }

    public boolean isKeyword(int kind) {
	return fIsKeyword[kind];
    }

    public int getTokenIndexAtCharacter(int offset) {
	int index= getParser().getParseStream().getTokenIndexAtCharacter(offset);
	return (index < 0 ? -index : index);
    }

    public IToken getTokenAtCharacter(int offset) {
	return getParser().getParseStream().getTokenAtCharacter(offset);
    }

    public boolean hasErrors() {
	return fCurrentAst == null;
    }

    public List getErrors() {
	return Collections.singletonList(new ParseError("parse error", null));
    }

    protected void cacheKeywordsOnce() {
	if (fKeywords == null) {
	    String tokenKindNames[]= getParser().getParseStream().orderedTerminalSymbols();
	    this.fIsKeyword= new boolean[tokenKindNames.length];
	    this.fKeywords= new char[tokenKindNames.length][];
	    int[] keywordKinds= getLexer().getKeywordKinds();
	    for(int i= 1; i < keywordKinds.length; i++) {
		int index= getParser().getParseStream().mapKind(keywordKinds[i]);
		fIsKeyword[index]= true;
		fKeywords[index]= getParser().getParseStream().orderedTerminalSymbols()[index].toCharArray();
	    }
	}
    }
}
