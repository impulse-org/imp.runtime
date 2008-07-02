package org.eclipse.imp.services;

import org.eclipse.imp.language.ILanguageService;
import org.eclipse.imp.parser.IParseController;
import org.eclipse.jface.text.IRegion;

public interface IHelpService extends ILanguageService {
    /**
     * @param target an AST node, ISourceEntity, or other program entity
     * @return the help text for the given entity, possibly HTML formatted
     */
    String getHelp(Object target, IParseController parseController);

    /**
     * @param target a selected text region for which help was requested
     * @return the help text for the given entity, possibly HTML formatted
     */
    String getHelp(IRegion target, IParseController parseController);
}
