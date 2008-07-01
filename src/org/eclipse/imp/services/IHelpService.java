package org.eclipse.imp.services;

import org.eclipse.imp.language.ILanguageService;
import org.eclipse.imp.parser.IParseController;

public interface IHelpService extends ILanguageService {
    /**
     * @param target an AST node, ISourceEntity, or other program entity
     * @return the help text for the given entity, possibly HTML formatted
     */
    String getHelp(Object target, IParseController parseController);
}
