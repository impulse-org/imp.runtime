package org.eclipse.uide.parser;

import java.util.List;

import lpg.runtime.IMessageHandler;
import lpg.runtime.IToken;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.uide.core.ILanguageService;
import org.eclipse.uide.model.ISourceProject;

public interface IParseController extends ILanguageService {
    void initialize(IPath projRelFilePath, ISourceProject project, IMessageHandler handler);

    ISourceProject getProject();

    /**
     * @return either a project-relative path, if getProject() is non-null, or an absolute path.
     */
    IPath getPath();

    IParser getParser();

    ILexer getLexer();

    Object getCurrentAst();

    boolean isKeyword(int kind);

    char [][] getKeywords();

    public int getTokenIndexAtCharacter(int offset);

    public IToken getTokenAtCharacter(int offset);

    IASTNodeLocator getNodeLocator();

    boolean hasErrors();

    /**
     * @return a List of ParseError
     */
    List getErrors();

    Object parse(String input, boolean scanOnly, IProgressMonitor monitor);
}
