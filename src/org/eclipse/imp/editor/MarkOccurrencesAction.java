package org.eclipse.imp.editor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.imp.parser.IASTNodeLocator;
import org.eclipse.imp.parser.IParseController;
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
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.texteditor.IDocumentProvider;

public class MarkOccurrencesAction implements IWorkbenchWindowActionDelegate {
    private static final String OCCURRENCE_ANNOTATION= RuntimePlugin.IMP_RUNTIME + ".occurrenceAnnotation";

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

    /**
     * Listens to selection changes and forces a recomputation of the annotations.
     * The analysis is performed once each time the compilation unit in the active
     * editor changes, and the results are reused each time the selection changes
     * in order to produce the annotations corresponding to the selection.
     */
    private final class SelectionListener implements ISelectionChangedListener {
	private final IDocument fDocument;

	private SelectionListener(IDocument document) {
	    fDocument= document;
	}

	public void selectionChanged(SelectionChangedEvent event) {
	    ISelection selection= event.getSelection();

	    if (selection instanceof ITextSelection) {
		ITextSelection textSel= (ITextSelection) selection;
		int offset= textSel.getOffset();
		int length= textSel.getLength();

		recomputeAnnotationsForSelection(offset, length, fDocument);
	    }
	}
    }

    private AbstractTextEditor fActiveEditor;

    private IParseController fParseController;

    private IDocumentProvider fDocumentProvider;

    /**
     * The AST for the compilation unit in the active editor.
     */
    private Object fCompilationUnit;

    private IOccurrenceMarker fOccurrenceMarker;

    private Annotation[] fOccurrenceAnnotations;

    private ISelectionChangedListener fSelectionListener;

    private IDocumentListener fDocumentListener;

    private boolean fInstalled= false;

    public MarkOccurrencesAction() { }

    public void run(IAction action) {
	getActiveEditor();

	IDocument doc= getDocumentProvider().getDocument(getEditorInput());

	if (!fInstalled) {
	    registerListeners(doc);
	    fInstalled= true;
	} else {
	    unregisterListeners(doc);
	    fInstalled= false;
	}
    }

    private void registerListeners(IDocument document) {
	maybeCreateListeners(document);
	fActiveEditor.getSelectionProvider().addSelectionChangedListener(fSelectionListener);
	document.addDocumentListener(fDocumentListener);
    }

    private void maybeCreateListeners(IDocument document) {
	if (fSelectionListener == null)
	    fSelectionListener= new SelectionListener(document);
	if (fDocumentListener == null)
	    fDocumentListener= new DocumentListener();
    }

    private void unregisterListeners(IDocument document) {
	fActiveEditor.getSelectionProvider().removeSelectionChangedListener(fSelectionListener);
	document.removeDocumentListener(fDocumentListener);
    }

    private void recomputeAnnotationsForSelection(int offset, int length, IDocument document) {
	IAnnotationModel annotationModel= fDocumentProvider.getAnnotationModel(getEditorInput());
	Object root= getCompilationUnit();
	Object selectedNode= fParseController.getNodeLocator().findNode(root, offset, offset+length+1);
	List<Object> occurrences= fOccurrenceMarker.getOccurrencesOf(fParseController, selectedNode);
	Position[] positions= convertRefNodesToPositions(occurrences);

	placeAnnotations(convertPositionsToAnnotationMap(positions, document), annotationModel);
    }

    private Map<Annotation,Position> convertPositionsToAnnotationMap(Position[] positions, IDocument document) {
	Map<Annotation,Position> annotationMap= new HashMap<Annotation,Position>(positions.length);

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
	getDocumentProvider();

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
	IASTNodeLocator locator= fParseController.getNodeLocator();

	for(Iterator iter= refs.iterator(); iter.hasNext(); i++) {
	    Object node= iter.next();

	    positions[i]= new Position(locator.getStartOffset(node), locator.getLength(node));
	    // System.out.println("Annotation at " + positions[i].offset + ":" + positions[i].length);
	}
	return positions;
    }

    private Object getCompilationUnit() {
	if (fCompilationUnit == null) {
	    fCompilationUnit= fParseController.getCurrentAst();
	}
	return fCompilationUnit;
    }

    private IEditorInput getEditorInput() {
	return fActiveEditor.getEditorInput();
    }

    private IDocumentProvider getDocumentProvider() {
	fDocumentProvider= fActiveEditor.getDocumentProvider();

	return fDocumentProvider;
    }

    private void getActiveEditor() {
	fActiveEditor= (AbstractTextEditor) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();

	UniversalEditor ue= (UniversalEditor) fActiveEditor;
	fParseController= ue.getParseController();
	fOccurrenceMarker= ue.getOccurrenceMarker();
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
    }

    public void init(IWorkbenchWindow window) {
    }
}
