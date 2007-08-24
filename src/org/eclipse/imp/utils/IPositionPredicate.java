/**
 * 
 */
package org.eclipse.imp.utils;

import org.eclipse.jface.text.Position;

/**
 * Interface that represents a single-argument predicate taking a textual Position.
 * Used by AnnotationUtils to detect annotations associated with a particular range
 * or location in source text.
 * @author rfuhrer
 */
public interface IPositionPredicate {
    boolean matchPosition(Position p);
}