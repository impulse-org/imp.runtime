/*******************************************************************************
* Copyright (c) 2007 IBM Corporation.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*    Robert Fuhrer (rfuhrer@watson.ibm.com) - initial API and implementation
*    										- refinement and hardening
*    Stan Sutton (suttons@us.ibm.com)       - refinement and hardening
*
*******************************************************************************/

package org.eclipse.imp.editor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.imp.core.ErrorHandler;
import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.parser.ISourcePositionLocator;
import org.eclipse.imp.runtime.RuntimePlugin;
import org.eclipse.imp.services.IOccurrenceMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.ISynchronizable;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelExtension;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

public class MarkOccurrencesAction implements IWorkbenchWindowActionDelegate {
	
    public static final String OCCURRENCE_ANNOTATION= RuntimePlugin.IMP_RUNTIME + ".occurrenceAnnotation";
    
    private boolean fMarkingEnabled = false;
    
    private ITextEditor fActiveEditor;

    private IParseController fParseController;

    private IDocumentProvider fDocumentProvider;

    private Object fCompilationUnit;	// AST for compilation unit in active editor

    private IOccurrenceMarker fOccurrenceMarker;

    private Annotation[] fOccurrenceAnnotations;

    private ISelectionChangedListener fSelectionListener;

    private IDocumentListener fDocumentListener;

    /**
     * Listens to document changes and invalidates the AST cache to force a re-parsing.
     */
    private final class DocumentListener implements IDocumentListener {
		public void documentAboutToBeChanged(DocumentEvent event) {
		}
	
		public void documentChanged(DocumentEvent event) {
		    fCompilationUnit= null;
		}
    }
    
	
	/*
	 * Notes on the interaction of text folding and occurrence marking:
	 * 
	 * When you click on a text-folding widget, you can generate two events:  one signaling a
	 * change to the project model, the other signaling a change to the text selection (even
	 * though you have not directly clicked in text).  The selection-changed event is propagated
	 * to the listener posted by MarkOccurrencesAction, just as selection-changed events that
	 * actually correspond to changes of selections within the text.  Only selection-changed
	 * events originating within the text are of interest here.  More to the point, selection-
	 * changed events from the folding widgets should be ignored because they may cause the
	 * selection (and dependent markings) to be changed in unanticipated and undesirable ways.
	 * In general, we do not expect the text selection and dependent markings to change just
	 * because the text has been folded (or unfolded).
	 * 
	 * Unfortunately, while we can listen for updates to the projection annotation model,
	 * there appears to be no definite way to correlate changes in that model to changes in
	 * the annotation model that contains the selection annotations.  The projection annotation
	 * events seem to be generated after the corresponding selection-changed events, and they
	 * lack a text position or timestamp that would allow them to be correlated with the
	 * selection-changed events.
	 * 
	 * The one potentially useful characteristic of the selection-changed events that come
	 * from changes to the projection annotation model is that they seem to have a length of 0.
	 * On that basis, the selection listener implemented below filters out events of 0 length.
	 * This approach assumes that such events can be ignored.  In practice this does prevent
	 * changes to the projection annotation model from causing inappropriate updates to the
	 * text selection.  Some genuine selections of length 0 may be missed by this approach,
	 * but perhaps most text selections of interest will have length greater than 0.  If not,
	 * then we can revisit this issue.
	 */
	
	
    
    /**
     * Listens to selection changes and forces a recomputation of the annotations.
     * The analysis is performed once each time the compilation unit in the active
     * editor changes, and the results are reused each time the selection changes
     * in order to produce the annotations corresponding to the selection.
     */
    private final class SelectionListener implements ISelectionChangedListener {
		private final IDocument document;
		private ISelection previousSelection = null;
		
		private SelectionListener(IDocument document) {
		    this.document = document;
		}
	
		
		public void selectionChanged(SelectionChangedEvent event) {
		    ISelection selection= event.getSelection();
		    if (selection instanceof ITextSelection) {
		    	if (previousSelection != null && previousSelection.equals(selection)) {
		    		return;
		    	}
				
		    	previousSelection = selection;
				ITextSelection textSel= (ITextSelection) selection;
				int offset= textSel.getOffset();
				int length= textSel.getLength();
				
				if (length == 0)
					return;
				
				recomputeAnnotationsForSelection(offset, length, document);
		    }
		}
    }
    
    
    public MarkOccurrencesAction() { }

    
    public void run(IAction action) {
    	fMarkingEnabled = action.isChecked();
		if (fMarkingEnabled) {
			setUpActiveEditor(
				(ITextEditor) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor()
				);
		} else {
			unregisterListeners();
			removeExistingOccurrenceAnnotations();
		}
    }

    private void registerListeners() {
    	// getDocumentFromEditor() can return null, but register listeners
    	// should only be called when there is an active editor that can
    	// be presumed to have a document provider that has document
    	IDocument document = getDocumentFromEditor();
    	if (document == null)
    		return;
    	
    	createListeners(document);
    	fActiveEditor.getSelectionProvider().addSelectionChangedListener(fSelectionListener);
    	document.addDocumentListener(fDocumentListener);
    }

    private void createListeners(IDocument document) {
	    fSelectionListener= new SelectionListener(document);
	    fDocumentListener= new DocumentListener();
    }

    private void unregisterListeners() {
	    if (fActiveEditor == null)
	    	return;
	    if (fSelectionListener != null) {
	    	ISelectionProvider provider = fActiveEditor.getSelectionProvider();
	    	if (provider != null)
	    		fActiveEditor.getSelectionProvider().removeSelectionChangedListener(fSelectionListener);
	    }
	    if (fDocumentListener != null) {
	    	IDocument document = getDocumentFromEditor();
	    	if (document != null)
	    		getDocumentFromEditor().removeDocumentListener(fDocumentListener);
	    }
    }

	private IDocument getDocumentFromEditor() {
		IDocumentProvider provider = getDocumentProvider();
		if (provider != null)
			return provider.getDocument(getEditorInput());
		else
			return null;
	}

    private void recomputeAnnotationsForSelection(int offset, int length, IDocument document) {
		IAnnotationModel annotationModel= fDocumentProvider.getAnnotationModel(getEditorInput());
		Object root= getCompilationUnit();
		if (root == null) {
			// Get this when "selecting" an error message that is shown in the editor view
			// but is not part of the source file; just returning should leave previous
			// markings, if any, as they were (which is probably fine)
			// Also get this when the current AST is null, e.g., as in the event of
			// a parse error
//			System.err.println("MarkOccurrencesAction.recomputeAnnotationsForSelection(..):  root of current AST is null; returning");
			return;
		}
		Object selectedNode= fParseController.getSourcePositionLocator().findNode(root, offset, offset+length-1);
		if (fOccurrenceMarker == null) {
			// It might be possible to set the active editor at this point under
			// some circumstances, but attempting to do so under other circumstances
			// can lead to stack overflow, so just return.
			return;
		}
		try {
		    List<Object> occurrences= fOccurrenceMarker.getOccurrencesOf(fParseController, selectedNode);
		    if (occurrences != null) {
		        Position[] positions= convertRefNodesToPositions(occurrences);

		        placeAnnotations(convertPositionsToAnnotationMap(positions, document), annotationModel);
		    } else {
		        ErrorHandler.reportError("Occurrence marker returned a null list of occurrences.");
		    }
		} catch (Exception e) {
		    ErrorHandler.reportError("Error obtaining occurrences of selected node", e);
		}
    }

    private Map<Annotation, Position> convertPositionsToAnnotationMap(Position[] positions, IDocument document) {
        Map<Annotation, Position> annotationMap= new HashMap<Annotation, Position>(positions.length);

        for(int i= 0; i < positions.length; i++) {
            Position position= positions[i];
            try { // Create & add annotation
                String message= document.get(position.offset, position.length);

                annotationMap.put(new Annotation(OCCURRENCE_ANNOTATION, false, message), position);
            } catch (BadLocationException ex) {
                continue; // skip apparently bogus position
            }
        }
        return annotationMap;
    }

    private void placeAnnotations(Map<Annotation,Position> annotationMap, IAnnotationModel annotationModel) {
		Object lockObject= getLockObject(annotationModel);
	
		synchronized (lockObject) {
		    if (annotationModel instanceof IAnnotationModelExtension) {
			((IAnnotationModelExtension) annotationModel).replaceAnnotations(fOccurrenceAnnotations, annotationMap);
		    } else {
				removeExistingOccurrenceAnnotations();
				Iterator<Map.Entry<Annotation,Position>> iter= annotationMap.entrySet().iterator();
				while (iter.hasNext()) {
				    Map.Entry<Annotation,Position> mapEntry= iter.next();
				    annotationModel.addAnnotation((Annotation) mapEntry.getKey(), (Position) mapEntry.getValue());
				}
		    }
		    fOccurrenceAnnotations= (Annotation[]) annotationMap.keySet().toArray(new Annotation[annotationMap.keySet().size()]);
		}
    }

    void removeExistingOccurrenceAnnotations() {
        // RMF 6/27/2008 - If we've come up in an empty workspace, there won't be an active editor
        if (fActiveEditor == null)
            return;
        // RMF 6/27/2008 - Apparently partActivated() gets called before the editor is initialized
        // (on MacOS?), and then we can't properly initialize this MarkOccurrencesAction instance.
        // When that happens, fDocumentProvider will be null. Initialization needs a fix for that,
        // rather than this simple-minded null guard.
        if (fDocumentProvider == null)
            return;
		IAnnotationModel annotationModel= fDocumentProvider.getAnnotationModel(getEditorInput());
		if (annotationModel == null || fOccurrenceAnnotations == null)
		    return;
	
		synchronized (getLockObject(annotationModel)) {
		    if (annotationModel instanceof IAnnotationModelExtension) {
			((IAnnotationModelExtension) annotationModel).replaceAnnotations(fOccurrenceAnnotations, null);
		    } else {
			for(int i= 0, length= fOccurrenceAnnotations.length; i < length; i++)
			    annotationModel.removeAnnotation(fOccurrenceAnnotations[i]);
		    }
		    fOccurrenceAnnotations= null;
		}
    }

    
    private Position[] convertRefNodesToPositions(List<Object> refs) {
        Position[] positions= new Position[refs.size()];
        int i= 0;
        ISourcePositionLocator locator= fParseController.getSourcePositionLocator();

        for(Iterator iter= refs.iterator(); iter.hasNext(); i++) {
            Object node= iter.next();
            positions[i]= new Position(locator.getStartOffset(node), locator.getLength(node)+1);
        }
        return positions;
    }

    
    private Object getCompilationUnit() {
    	// Do NOT compute fCompilationUnit conditionally based
    	// on the AST being null; that causes problems when switching
    	// between editor windows because the old value of the AST
    	// will be retained even after the new window comes up, until
    	// the text in the new window is parsed.  For now just
    	// get the current AST (but in the future do something more
    	// sophisticated to avoid needless recomputation but only
    	// when it is truly needless).
        fCompilationUnit= fParseController.getCurrentAst();
        return fCompilationUnit;
    }

    
    private IEditorInput getEditorInput() {
        return fActiveEditor.getEditorInput();
    }

    
    private IDocumentProvider getDocumentProvider() {
        fDocumentProvider= fActiveEditor.getDocumentProvider();
        
        return fDocumentProvider;
    }


    private void setUpActiveEditor(ITextEditor textEditor) {
    	unregisterListeners();
    	if (textEditor == null)
    		return;
		fActiveEditor = textEditor;
		LanguageServiceManager fLanguageServiceManager = LanguageServiceManager.getMyServiceManager(fActiveEditor);
		if (fLanguageServiceManager == null)
		    return;
		fParseController = fLanguageServiceManager.getParseController();

		if (fParseController == null) {
			return;
		}

		fOccurrenceMarker = fLanguageServiceManager.getOccurrenceMarker();
		registerListeners();
		
		ISelection selection = fActiveEditor.getSelectionProvider().getSelection();
		if (selection instanceof ITextSelection) {
			ITextSelection textSelection = (ITextSelection) selection;
			recomputeAnnotationsForSelection(textSelection.getOffset(), textSelection.getLength(), getDocumentFromEditor());
		}	
    }

    
    private Object getLockObject(IAnnotationModel annotationModel) {
        if (annotationModel instanceof ISynchronizable)
            return ((ISynchronizable) annotationModel).getLockObject();
        else
            return annotationModel;
    }

    
    public void selectionChanged(IAction action, ISelection selection) {
    }

    public void dispose() {
//		System.out.println("Dispose");
    }

    public void init(IWorkbenchWindow window) {
    	window.getActivePage().addPartListener(
    		new IPartListener() {

			public void partActivated(IWorkbenchPart part) {
//				System.out.println("partActivated");
				if (part instanceof ITextEditor) {
					setUpActiveEditor((ITextEditor)part);
					if (fDocumentProvider == null)
					    return;
					IAnnotationModel annotationModel= fDocumentProvider.getAnnotationModel(getEditorInput());
					// Need to initialize the set of pre-existing annotations in order
					// for them to be removed properly when new occurrences are marked
					if (annotationModel != null) {
					    Iterator<Annotation> annotationIterator = annotationModel.getAnnotationIterator();
					    List<Annotation> annotationList = new ArrayList<Annotation>();

					    while (annotationIterator.hasNext()) {
					    	// SMS 23 Jul 2008:  added test for annotation type
					    	Annotation ann = (Annotation)annotationIterator.next();
					    	if (ann.getType().indexOf(OCCURRENCE_ANNOTATION) > -1)
					    		annotationList.add(ann);
					    }
					    fOccurrenceAnnotations = annotationList.toArray(new Annotation[annotationList.size()]);
					}
				}
				if (!fMarkingEnabled) {
					unregisterListeners();
					removeExistingOccurrenceAnnotations();
				}
			}

			public void partBroughtToTop(IWorkbenchPart part) {
//				System.out.println("partBroughtToTop");
			}

			public void partClosed(IWorkbenchPart part) {
			    if (part == fActiveEditor) {
			        unregisterListeners();
			        fActiveEditor= null;
			        fCompilationUnit= null;
			        fDocumentProvider= null;
			        fParseController= null;
			        fOccurrenceMarker= null;
			        fOccurrenceAnnotations= null;
			    }
			}

			public void partDeactivated(IWorkbenchPart part) {
//				System.out.println("partDeactivated");
			}

			public void partOpened(IWorkbenchPart part) {
//				System.out.println("partOpened");
			}
    	});
    }
}
