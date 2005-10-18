package org.eclipse.uide.parser;

import com.ibm.lpg.IToken;
import com.ibm.lpg.LexStream;
import com.ibm.lpg.PrsStream;

public interface ILexer {

	boolean isKeyword(IToken token);

	int [] getKeywordKinds();

	boolean isKeywordStart(char c);

	public LexStream getLexStream();
    public void resetInput(char[] input_chars, String filename);
	public void lexer(PrsStream prsStream);
}
