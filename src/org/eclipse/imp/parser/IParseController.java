package org.eclipse.uide.parser;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.uide.core.ILanguageService;

import lpg.lpgjavaruntime.IToken;

public interface IParseController extends ILanguageService
{
    IParser getParser();

    ILexer getLexer();

    Object getCurrentAst();

    boolean isKeyword(int kind);

    char [][] getKeywords();

    public int getTokenIndexAtCharacter(int offset);

    public IToken getTokenAtCharacter(int offset);

    IASTNodeLocator getNodeLocator();

    boolean hasErrors();

    List getErrors();

    Object parse(String input, boolean scanOnly, IProgressMonitor monitor);
}