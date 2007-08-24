package org.eclipse.imp.services;

import java.util.List;

import org.eclipse.imp.language.ILanguageService;
import org.eclipse.imp.parser.IParseController;

public interface IOccurrenceMarker extends ILanguageService {
    /**
     * @return the user-readable name of the kind of occurrence.
     */
    public String getKindName();

    /**
     * @param entity the AST node representing the given entity
     * @return the list of AST nodes representing occurrences of the given entity
     * in the same file as the entity.
     */
    public List<Object> getOccurrencesOf(IParseController parseCtrlr, Object entity);
}
