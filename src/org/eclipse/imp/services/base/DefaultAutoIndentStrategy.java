package org.eclipse.imp.services.base;

import org.eclipse.imp.services.IAutoEditStrategy;
import org.eclipse.jface.text.DefaultIndentLineAutoEditStrategy;


/**
 * Essentially copied from org.eclipse.jface.text.DefaultAutoIndentStrategy
 * but implementing an interface that is not deprecated.
 * 
 * Provides a default implementation that always copies the indentation
 * of the previous line.
 * 
 * @author sutton
 *
 */

public class DefaultAutoIndentStrategy
	extends DefaultIndentLineAutoEditStrategy implements IAutoEditStrategy
{

	/**
	 * Creates a new default auto indent strategy.
	 */
	public DefaultAutoIndentStrategy() {
	}
	
	
}
