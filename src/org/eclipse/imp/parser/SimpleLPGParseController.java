/*******************************************************************************
* Copyright (c) 2007, 2008 IBM Corporation.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*    Robert Fuhrer (rfuhrer@watson.ibm.com) - initial API and implementation
*    Stan Sutton (suttons@us.ibm.com) - maintenance of iterator

*******************************************************************************/

package org.eclipse.imp.parser;

import java.util.Iterator;

import lpg.runtime.IToken;
import lpg.runtime.Monitor;
import lpg.runtime.PrsStream;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.imp.language.Language;
import org.eclipse.imp.language.LanguageRegistry;
import org.eclipse.imp.model.ISourceProject;
import org.eclipse.imp.services.IAnnotationTypeInfo;
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
    protected Language fLanguage;

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

    public void setMonitor(IProgressMonitor monitor) {
        this.monitor= monitor;
    }
    }

    public SimpleLPGParseController(String languageID) {
        fLanguage= LanguageRegistry.findLanguage(languageID);
    }

    public Language getLanguage() {
        return fLanguage;
    }

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

    public Iterator<IToken> getTokenIterator(IRegion region) {
        final int regionOffset= region.getOffset();
        final int regionLength= region.getLength();

        return new Iterator<IToken>() {   	
            final PrsStream stream= SimpleLPGParseController.this.getParser().getParseStream();
            final int firstTokIdx= getTokenIndexAtCharacter(regionOffset);
            
            // Compute the index of the last "proper" token in the region, that is,
            // not counting the EOF token, if present.
            // LPG puts an EOF token at the end of the token stream for a file,
            // so we can assume that if the EOF character occurs at the end of
            // the given character range then the EOF token will be the final
            // one in the token stream for the character range (in which case
            // we discount it)
            final int decrement;
            {
            	if (stream.getInputChars()[regionOffset + regionLength - 1] == IToken.EOF)
            		decrement = 1;
            	else
            		decrement = 0;
            }
            final int lastTokIdx= getTokenIndexAtCharacter(regionOffset + regionLength - 1) - decrement;

            
            int curTokIdx= Math.max(1, firstTokIdx); // skip bogus initial token

            private int getTokenIndexAtCharacter(int offset) {
            	
                int result= stream.getTokenIndexAtCharacter(offset);
                // getTokenIndexAtCharacter() answers the negative of the index of the
                // preceding token if the given offset is not actually within a token.
                if (result < 0) {
                    result= -result + 1;
                }
                return result;
            }


            // the preceding adjuncts for each token
            IToken[][] precedingAdjuncts = new IToken[lastTokIdx+1][];
            {
            	for (int i = 0; i < precedingAdjuncts.length; i++) {
            		precedingAdjuncts[i] = stream.getPrecedingAdjuncts(i);
            	}
            }
            // the current indices for each array of preceding adjuncts
            int[] nextPrecedingAdjunct = new int[lastTokIdx+1];
            {
            	for (int i = 0; i < nextPrecedingAdjunct.length; i++) {
            		nextPrecedingAdjunct[i] = 0;
            	}
            }
            
            // the following adjuncts (for the last token only)
            IToken[] followingAdjuncts = stream.getFollowingAdjuncts(lastTokIdx);
            // the current index for the array of following adjuncts
            int nextFollowingAdjunct = 0;
            
            // to support hasNext()
            private boolean finalTokenReturned = false;
            private boolean finalAdjunctsReturned = !(followingAdjuncts.length > 0);

            
            /**
             * Tests whether the iterator has any unreturned tokens.  These may
             * include "regular" tokens and "adjunct" tokens (e.g., representing comments).
             * 
             * @return	True if there is another token available, false otherwise
             */
            public boolean hasNext() {
               	return !(finalTokenReturned && finalAdjunctsReturned);
            }
            
            
            /**
             * Returns the next available token in the iterator (or null
             * if there is none)
             * 
             * The returned token may be a "regular" token (which will have a
             * corresponding AST node) or an "adjunct" token (which will represent
             * a comment).  The tokens are returned in the order in which they occur
             * in the text, regardless of their kind.
             * 
             */
            public IToken next()
            {	
            	int next = -1;	// for convenience
            	
            	// If we're not all the way through the tokens
            	if (curTokIdx <= lastTokIdx) {
	            	next = nextPrecedingAdjunct[curTokIdx];
	            	// If the current token has any unreturned preceding adjuncts
	            	if (next >= 0 && next < precedingAdjuncts[curTokIdx].length) {
	            		// Return the next preceding adjunct, incrementing the corresponding index
	            		return precedingAdjuncts[curTokIdx][nextPrecedingAdjunct[curTokIdx]++];
	            	}
            	}
            	
            	// If we're not all the way through the tokens
            	if (curTokIdx <= lastTokIdx) {
            		// Return the current token, flagging whether it's the final token
            		// and incrementing the current token index
            		finalTokenReturned = curTokIdx == lastTokIdx;
	            	return stream.getIToken(curTokIdx++);
            	}

            	// Here we must be on the final, following adjuncts, if any
            	next = nextFollowingAdjunct;
            	// If there are any unreturned following adjuncts
            	if (next >= 0 && next < followingAdjuncts.length) {
            		// Return the next of those, flagging whether it's the last and
            		// incrementing the final-adjunct index
            		finalAdjunctsReturned = ++next >= followingAdjuncts.length;
            		return followingAdjuncts[nextFollowingAdjunct++];
            	}
            	
            	return null;
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
