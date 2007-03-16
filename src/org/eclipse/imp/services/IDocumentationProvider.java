/*
 * Created on Mar 8, 2007
 */
package org.eclipse.uide.core;

import org.eclipse.uide.parser.IParseController;

public interface IDocumentationProvider {
    String getDocumentation(Object target, IParseController parseController);
}
