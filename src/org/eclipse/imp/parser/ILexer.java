package org.eclipse.uide.parser;

import com.ibm.lpg.IToken;
import com.ibm.lpg.LexStream;
import com.ibm.lpg.PrsStream;

public interface ILexer {
	public int[] getKeywordKinds();

	public LexStream getLexStream();

	public void initialize(char[] contents, String filename);

	public void lexer(PrsStream prsStream);
}
