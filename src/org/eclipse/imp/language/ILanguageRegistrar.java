/*******************************************************************************
* Copyright (c) 2010 CWI Corporation.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*    Jurgen Vinju (jurgen@vinju.org) - initial API and implementation
*******************************************************************************/
package org.eclipse.imp.language;

/**
 * Classes registered via the languageRegistrar extension point implement this interface.
 * Each instance will be called when the {#link LanguageRegistry} is done loading the language definitions
 * from plugin.xml files. Then these language registrars are called to add some more language
 * definitions "dynamically".
 * 
 * Implementors of this class should use {#link LanguageRegistry} for registering languages.
 */
public interface ILanguageRegistrar {
  public void registerLanguages();
}
