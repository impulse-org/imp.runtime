package org.eclipse.uide.defaults;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;
import org.eclipse.uide.core.ErrorHandler;
import org.eclipse.uide.editor.IFoldingUpdater;
import org.eclipse.uide.parser.IASTNodeLocator;
import org.eclipse.uide.parser.IParseController;


/**
 * FolderBase is an abstract base type for a source-text folding service.
 * It is intended to support extensions for language-specific folders.
 * The class is abstract only with respect to a method that sends a
 * visitor to an AST, as both the visitor and AST node types are language
 * specific.
 * 
 * @author 	suttons@us.ibm.com
 */
public abstract class FolderBase implements IFoldingUpdater
{	
	// For recording annotations
	
	// Maps new annotations to positions
    protected HashMap newAnnotations = new HashMap();
    // Lists the new annotations, which are the keys for newAnnotations
    protected List annotations = new ArrayList();

    protected IParseController parseController = null;
    
    // Methods to make annotations will be called by visitor methods
    // in the language-specific concrete subtype    
    
    /**
     * Make a folding annotation that corresponds to the extent of text
     * represented by a given AST node.
     * 
     * @param n		an Object that will be taken to represent an AST node
     */
    public void makeAnnotation(Object n)
    {	
		// Use the parse controller to get a node locator
    	// (assume that parse controller will have been set)
		IASTNodeLocator nodeLocator = parseController.getNodeLocator();
		
		// Use the node locator to get the starting and ending offsets of the node
		int startOffset = 0;
		int endOffset = 0;
		try {
			// The methods of nodeLocator typically take an object and cast it
			// to an ASTNode type without any concern for what it actually is,
			// so try that here and catch any exception that is thrown
			startOffset = nodeLocator.getStartOffset(n);
			endOffset = nodeLocator.getEndOffset(n);
		} catch (ClassCastException x) {
			System.err.println("FolderBase.makeAnnotation(Object):  " +
				"ClassCastException trying to treat event data as AST node type");
			return;
		}

		// Create an annotation corresponding to the node and add it to the list
		// of annotations that go into the document annotation model
		ProjectionAnnotation annotation= new ProjectionAnnotation();
		newAnnotations.put(annotation, new Position(startOffset, endOffset-startOffset+1));
		annotations.add(annotation);
    }    


    /**
     * Make a folding annotation that corresponds to a given range of text,
     * without any necessary correspondence to an AST node
     * 
     * @param start		The starting offset of the text range
     * @param len		The ending offset of the text range
     */
    public void makeAnnotation(int start, int len) {
		ProjectionAnnotation annotation= new ProjectionAnnotation();
		newAnnotations.put(annotation, new Position(start, len));
		annotations.add(annotation);
    }
	
	
    
	// Used to support checking of whether annotations have
	// changed between invocations of updateFoldingStructure
	// (because, if they haven't, then it's probably best not
	// to update the folding structure)
	private ArrayList oldAnnotationsList = null;
  	private Annotation[] oldAnnotationsArray;

  	
	/**
	 * Update the folding structure for a source text, where the text and its
	 * AST are represented by a given parse controller and the folding structure
	 * is represented by annotations in a given annotation model.
	 * 
	 * This is the principal routine of the folding updater.
	 * 
	 * The implementation provided here makes use of a local class
	 * FoldingUpdateStrategy, to which the task of updating the folding
	 * structure is delegated.
	 * 
	 * updateFoldingStructure is synchronized because, at least on file opening,
	 * it can be called more than once before the first invocation has completed.
	 * This can lead to inconsistent calculations resulting in the absence of
	 * folding annotations in newly opened files.
	 * 
	 * @param parseController		A parse controller through which the AST for
	 *								the source text can be accessed
	 * @param annotationModel		A structure of projection annotations that
	 *								represent the foldable elements in the source
	 *								text
	 */
	public synchronized void updateFoldingStructure(
		IParseController parseController, ProjectionAnnotationModel annotationModel)
	{
		if (parseController != null)
			this.parseController = parseController;
		
		try {
			Object ast = parseController.getCurrentAst();
			
			if (ast == null) {
				// We can't create annotations without an AST
				return;
			}
		
			// But, since here we have the AST ...
			sendVisitorToAST(newAnnotations, annotations, ast);
	
			// Update the annotation model if there have been changes
			// but not otherwise (since update leads to redrawing of the	
			// source in the editor, which is likely to be unwelcome if
			// there haven't been any changes relevant to folding)
			boolean updateNeeded = false;
			if (oldAnnotationsList == null) {
				// Should just be the first time through
				updateNeeded = true;
			} else {
				// Check to see whether the current and previous annotations
				// differ in any significant way; if not, then there's no
				// reason to update the annotation model.
				// Note:  This test may be implemented in various ways that may
				// be more or less simple, efficient, correct, etc.  (The
				// default test provided below is simplistic although quick and
				// usually effective.)
				updateNeeded = differ(oldAnnotationsList, (ArrayList) annotations);	
			}
			if (updateNeeded) {
				// Save the current annotations to compare for changes the next time
				oldAnnotationsList = new ArrayList();
				for (int i = 0; i < annotations.size(); i++) {
					oldAnnotationsList.add(annotations.get(i));	
				}
			} else {
			}
		
			// Need to curtail calls to modifyAnnotations() because these lead to calls
			// to fireModelChanged(), which eventually lead to calls to updateFoldingStructure,
			// which lead back here, which would lead to another call to modifyAnnotations()
			// (unless those were curtailed)
			if (updateNeeded) {
				annotationModel.modifyAnnotations(oldAnnotationsArray, newAnnotations, null);
				// Capture the latest set of annotations in a form that can be used tne next
				// time that it is necessary to modify the annotations
				oldAnnotationsArray = (Annotation[]) annotations.toArray(new Annotation[annotations.size()]);
			} else {
			}

			newAnnotations.clear();
			annotations.clear();			
		} catch (Exception e) {
			ErrorHandler.reportError("FolderBase.updateFoldingStructure:  EXCEPTION", e);
		}
	}	


	/**
	 * A method to test whether there has been a significant change in the folding
	 * annotations for a source text.  The method works by comparing two lists of
	 * annotations, nominally the "old" and "new" annotations.  It returns true iff
	 * there is considered to be a "significant" difference in the two lists, where
	 * the meaning of "significant" is defined by the implementation of this method.
	 * 
	 * The default implementation provided here is a simplistic test of the difference
	 * between two lists, considering only their size.  This may work well enough much
	 * of the time as the comparisons between lists should be made very frequently,
	 * actually more frequently than the rate at which the typical human user will
	 * edit the program text so as to affect the AST so as to affect the lists.  Thus
	 * most changes of lists will entail some change in the number of elements at some
	 * point that will be observed here.  This will not work for certain very rapid
	 * edits of source text (e.g., rapid replacement of elements).
	 * 
	 * This method should be overridden in language-specific implementations of the
	 * folding updater where a more sophisticated test is desired.	
	 * 	
	 * @param list1		A list of annotations (nominally the "old" annotations)
	 * @param list2		A list of annotatoins (nominally the "new" annotations)
	 * @return			true iff there has been a "significant" difference in the
	 * 					two given lists of annotations
	 * 
	 */
	protected boolean differ(ArrayList list1, ArrayList list2) {
		
		if (list1.size() != list2.size()) {
			return true;
		}
		return false;
	}
	
	
	
	/**
	 * Send a visitor to an AST representing a program in order to construct the
	 * folding annotations.  Both the visitor type and the AST node type are language-
	 * dependent, so this method is abstract.
	 * 
	 * @param newAnnotations	A map of annotations to text positions
	 * @param annotations		A listing of the annotations in newAnnotations, that is,
	 * 							a listing of keys to the map of text positions
	 * @param ast				An Object that will be taken to represent an AST node
	 */
	protected abstract void sendVisitorToAST(HashMap newAnnotations, List annotations, Object ast);
	

	
    protected void dumpAnnotations(final List annotations, final HashMap newAnnotations)
	{
		for(int i= 0; i < annotations.size(); i++) {
		    Annotation a= (Annotation) annotations.get(i);
		    Position p= (Position) newAnnotations.get(a);
	
		    if (p == null) {
		    	System.out.println("Annotation position is null");
		    	continue;
		    }
		    
		    System.out.println("Annotation @ " + p.offset + ":" + p.length);
		}
	}
    
}
