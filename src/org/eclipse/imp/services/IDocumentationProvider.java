/*
 * Created on Mar 8, 2007
 */
package org.eclipse.imp.services;

import org.eclipse.imp.parser.IParseController;

public interface IDocumentationProvider {
    String getDocumentation(Object target, IParseController parseController);
}
