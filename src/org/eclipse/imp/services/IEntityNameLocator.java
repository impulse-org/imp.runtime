package org.eclipse.imp.services;

import org.eclipse.imp.language.ILanguageService;

public interface IEntityNameLocator extends ILanguageService {
    Object getName(Object srcEntity);
}
