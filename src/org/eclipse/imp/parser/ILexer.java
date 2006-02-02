package org.eclipse.uide.parser;

import lpg.lpgjavaruntime.LexStream;
import lpg.lpgjavaruntime.Monitor;
import lpg.lpgjavaruntime.PrsStream;

public interface ILexer {
    public int[] getKeywordKinds();

    public LexStream getLexStream();

    public void initialize(char[] contents, String filename);

    public void lexer(Monitor monitor, PrsStream prsStream);
}
