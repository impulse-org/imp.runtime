package org.eclipse.imp.services;

import org.eclipse.imp.language.ILanguageService;

public interface ILanguageSyntaxProperties extends ILanguageService {
    /**
     * if null, the language has no single-line comment syntax
     * @return
     */
    public String getSingleLineCommentPrefix();

    public String getBlockCommentStart();

    public String getBlockCommentContinuation();

    public String getBlockCommentEnd();

    /**
     * If -1, there is no comment column
     */
//  public int getSingleLineCommentColumn();

    /**
     * If -1, there is no continuation column
     */
//  public int getLineContinuationColumn();

    /**
     * @return an array of arrays each containing a balanced pair
     * of fence Strings, e.g.:
     *    [ [ "[" "]" ] ["(" ")" ] [ "{" "}" ] [ "/." "./" ]
     */
    public String[][] getFences();

    public String getIdentifierConstituentChars();

    public int[] getIdentifierComponents(String ident);
}
