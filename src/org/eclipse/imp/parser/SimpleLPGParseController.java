package org.eclipse.uide.parser;

import java.util.ArrayList;
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

    // Handler is required by the IParseController interface, but it is expected to
    // be used only in relation to types of parser defined in concrete subclasses
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
    	// SMS 25 Jun 2007
    	// Added try-catch block in case parser is null
    	//	int index= getParser().getParseStream().getTokenIndexAtCharacter(offset);
    	//	return (index < 0 ? -index : index);
    	try {
    		int index= getParser().getParseStream().getTokenIndexAtCharacter(offset);
    		return (index < 0 ? -index : index);
    	} catch (NullPointerException e) {
    		System.err.println("SimpleLPGParseController.getTokenIndexAtCharacter(offset):  NullPointerException; returning 0");
    	}
    	return 0;
    }

    public IToken getTokenAtCharacter(int offset) {
    	// SMS 25 Jun 2007
    	// Added try-catch block in case parser is null
    	try {
    		return getParser().getParseStream().getTokenAtCharacter(offset);
    	} catch (NullPointerException e) {
    		System.err.println("SimpleLPGParseController.getTokenAtCharacter(offset):  NullPointerException; returning null");
    	}
    	return null;
    }

    public boolean hasErrors() {
	return fCurrentAst == null;
    }

    public List getErrors() {
	return Collections.singletonList(new ParseError("parse error", null));
    }

    public String getSingleLineCommentPrefix() { return ""; }
    
    protected void cacheKeywordsOnce() {
		if (fKeywords == null) {
			// SMS 25 Jun 2007
			// Added try-catch block in case parser is null
			try {
			    String tokenKindNames[]= getParser().getParseStream().orderedTerminalSymbols();
			    this.fIsKeyword= new boolean[tokenKindNames.length];
			    this.fKeywords= new char[tokenKindNames.length][];
			    int[] keywordKinds= getLexer().getKeywordKinds();
			    for(int i= 1; i < keywordKinds.length; i++) {
					int index= getParser().getParseStream().mapKind(keywordKinds[i]);
					fIsKeyword[index]= true;
					fKeywords[index]= getParser().getParseStream().orderedTerminalSymbols()[index].toCharArray();
			    }
			}catch (NullPointerException e) {
	    		System.err.println("SimpleLPGParseController.cacheKeywordsOnce():  NullPointerException; trapped and discarded");
		    }
		}
    }

    
    
    /*
     * For the management of associated problem-marker types
     */
    
    private static List problemMarkerTypes = new ArrayList();
    
    public List getProblemMarkerTypes() {
    	return problemMarkerTypes;
    }
    
    public void addProblemMarkerType(String problemMarkerType) {
    	problemMarkerTypes.add(problemMarkerType);
    }
    
	public void removeProblemMarkerType(String problemMarkerType) {
		problemMarkerTypes.remove(problemMarkerType);
	}
}
