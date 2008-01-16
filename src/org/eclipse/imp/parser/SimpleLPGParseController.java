/*
 * (C) Copyright IBM Corporation 2007
 * 
 * This file is part of the Eclipse IMP.
 */
package org.eclipse.imp.parser;

import java.util.Iterator;

import lpg.runtime.IToken;
import lpg.runtime.Monitor;
import lpg.runtime.PrsStream;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.imp.language.IAnnotationTypeInfo;
import org.eclipse.imp.model.ISourceProject;
import org.eclipse.jface.text.IRegion;

/**
 * Base class for an IParseController implementation that encapsulates a simple LPG-based
 * scanner and parser.  Implements IParseController because methods defined there are
 * used here.
 * 
 * @author rfuhrer
 */
public abstract class SimpleLPGParseController implements IParseController
{
    protected ISourceProject fProject;

    protected IPath fFilePath;

    protected IMessageHandler handler;
    
    protected Object fCurrentAst;

    private char fKeywords[][];

    private boolean fIsKeyword[];

    private final SimpleAnnotationTypeInfo fSimpleAnnotationTypeInfo= new SimpleAnnotationTypeInfo();

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

    /*
     * Defined in the IParseController interface.  The implementation here serves
     * as a super method to support initialization of lexer and parser in a concrete
     * subtype where the concrete lexer and parser types are known.
     * 
     * The handler parameter is required by the IParseController interface and is
     * used in a concrete subtype along with a concrete parser type.
     */
    public void initialize(IPath filePath, ISourceProject project, IMessageHandler handler) {
		this.fProject= project;
		this.fFilePath= filePath;	
		this.handler = handler;
    }

    public abstract IParser getParser();

    public abstract ILexer getLexer();

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

    public Iterator getTokenIterator(IRegion region) {
        final int offset= region.getOffset();
        final int length= region.getLength();

        return new Iterator() {
            final PrsStream stream= SimpleLPGParseController.this.getParser().getParseStream();
            final int firstTokIdx= getTokenIndexAtCharacter(offset);
            final int lastTokIdx= getTokenIndexAtCharacter(offset + length - 1);
            int curTokIdx= Math.max(1, firstTokIdx); // skip bogus initial token
            IToken[] adjuncts;
            int adjunctIdx= -1;

            {
                loadPrecedingAdjuncts();
            }

            private int getTokenIndexAtCharacter(int offset) {
                int result= stream.getTokenIndexAtCharacter(offset);
                // getTokenIndexAtCharacter() answers the negative of the
                // index of the preceding token if the given offset is not
                // actually within a token.
                if (result < 0) {
                    result= -result + 1;
                }
                return result;
            }
            private void loadPrecedingAdjuncts() {
                adjuncts= stream.getPrecedingAdjuncts(curTokIdx);
                for(adjunctIdx=0; adjunctIdx < adjuncts.length && adjuncts[adjunctIdx].getEndOffset() < offset; adjunctIdx++)
                    ;
                if (adjunctIdx >= adjuncts.length)
                    adjuncts= null;
            }
            private void loadFollowingAdjuncts() {
                adjuncts= stream.getFollowingAdjuncts(curTokIdx);
                if (adjuncts != null && (adjuncts.length == 0 || adjuncts[0].getStartOffset() >= offset + length))
                    adjuncts= null;
            }
            public boolean hasNext() {
                return curTokIdx < lastTokIdx - 1 || (curTokIdx == lastTokIdx - 1 && adjunctIdx >= 0);
            }
            public Object next() {
                if (adjunctIdx >= 0) {
                    if (adjuncts != null && adjunctIdx < adjuncts.length && adjuncts[adjunctIdx].getStartOffset() < offset + length && adjuncts[adjunctIdx].getEndOffset() > offset)
                        return adjuncts[adjunctIdx++];
                    adjunctIdx= -1;
                }
                Object o= stream.getIToken(curTokIdx++);
                if (curTokIdx == stream.getSize() - 1)
                    loadFollowingAdjuncts();
                else
                    loadPrecedingAdjuncts();
                return o;
            }
            public void remove() {
                throw new IllegalArgumentException("Unimplemented");
            }
        };
    }

    public IAnnotationTypeInfo getAnnotationTypeInfo() {
        return fSimpleAnnotationTypeInfo;
    }

    public boolean isKeyword(int kind) {
	return kind < getParser().numTokenKinds() && fIsKeyword[kind];
    }

    protected void cacheKeywordsOnce() {
        if (fKeywords == null) {
            // SMS 25 Jun 2007
            // Added try-catch block in case parser is null
            try {
                String tokenKindNames[]= getParser().orderedTerminalSymbols();
                this.fIsKeyword= new boolean[tokenKindNames.length];
                this.fKeywords= new char[tokenKindNames.length][];
                int[] keywordKinds= getLexer().getKeywordKinds();
                for(int i= 1; i < keywordKinds.length; i++) {
                    int index= getParser().getParseStream().mapKind(keywordKinds[i]);
                    fIsKeyword[index]= true;
                    fKeywords[index]= getParser().orderedTerminalSymbols()[index].toCharArray();
                }
            } catch (NullPointerException e) {
                System.err.println("SimpleLPGParseController.cacheKeywordsOnce():  NullPointerException; trapped and discarded");
            }
        }
    }
}
