package org.eclipse.uide.parser;

import lpg.javaruntime.LexStream;
import lpg.javaruntime.Monitor;
import lpg.javaruntime.IPrsStream;

public interface ILexer {
    public int[] getKeywordKinds();

    public LexStream getLexStream();

    public void initialize(char[] contents, String filename);

    public void lexer(Monitor monitor, IPrsStream prsStream);
}
