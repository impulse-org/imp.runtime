package org.eclipse.uide.editor;

import java.util.HashSet;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.source.AnnotationModelEvent;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelListener;
import org.eclipse.jface.text.source.IAnnotationModelListenerExtension;
import org.eclipse.jface.util.ListenerList;
import org.eclipse.swt.widgets.Display;
import org.eclipse.uide.runtime.RuntimePlugin;
import org.eclipse.uide.wizards.fields.SWTUtil;

/**
 * Listens to resource deltas and filters for marker changes of type IMarker.PROBLEM Viewers showing error ticks should
 * register as listener to this type. Based on class of the same name from JDT/UI.
 * 
 * @author Dr. Robert M. Fuhrer
 */
public class ProblemMarkerManager implements IResourceChangeListener, IAnnotationModelListener, IAnnotationModelListenerExtension {

    /**
     * Visitors used to look if the element change delta containes a marker change.
     */
    private static class ProjectErrorVisitor implements IResourceDeltaVisitor {
        private HashSet fChangedElements;

        public ProjectErrorVisitor(HashSet changedElements) {
            fChangedElements= changedElements;
        }

        public boolean visit(IResourceDelta delta) throws CoreException {
            IResource res= delta.getResource();
            if (res instanceof IProject && delta.getKind() == IResourceDelta.CHANGED) {
                IProject project= (IProject) res;
                if (!project.isAccessible()) {
                    // only track open Java projects
                    return false;
                }
            }
            checkInvalidate(delta, res);
            return true;
        }

        private void checkInvalidate(IResourceDelta delta, IResource resource) {
            int kind= delta.getKind();
            if (kind == IResourceDelta.REMOVED || kind == IResourceDelta.ADDED || (kind == IResourceDelta.CHANGED && isErrorDelta(delta))) {
                // invalidate the resource and all parents
                while (resource.getType() != IResource.ROOT && fChangedElements.add(resource)) {
                    resource= resource.getParent();
                }
            }
        }

        private boolean isErrorDelta(IResourceDelta delta) {
            if ((delta.getFlags() & IResourceDelta.MARKERS) != 0) {
                IMarkerDelta[] markerDeltas= delta.getMarkerDeltas();
                for(int i= 0; i < markerDeltas.length; i++) {
                    if (markerDeltas[i].isSubtypeOf(IMarker.PROBLEM)) {
                        int kind= markerDeltas[i].getKind();
                        if (kind == IResourceDelta.ADDED || kind == IResourceDelta.REMOVED)
                            return true;
                        int severity= markerDeltas[i].getAttribute(IMarker.SEVERITY, -1);
                        int newSeverity= markerDeltas[i].getMarker().getAttribute(IMarker.SEVERITY, -1);
                        if (newSeverity != severity)
                            return true;
                    }
                }
            }
            return false;
        }
    }

    private ListenerList fListeners;

    public ProblemMarkerManager() {
        fListeners= new ListenerList(10);
    }

    /*
     * @see IResourceChangeListener#resourceChanged
     */
    public void resourceChanged(IResourceChangeEvent event) {
        HashSet changedElements= new HashSet();

        try {
            IResourceDelta delta= event.getDelta();
            if (delta != null)
                delta.accept(new ProjectErrorVisitor(changedElements));
        } catch (CoreException e) {
            RuntimePlugin.getInstance().logException("", e);
        }

        if (!changedElements.isEmpty()) {
            IResource[] changes= (IResource[]) changedElements.toArray(new IResource[changedElements.size()]);
            fireChanges(changes, true);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see IAnnotationModelListener#modelChanged(IAnnotationModel)
     */
    public void modelChanged(IAnnotationModel model) {
        // no action
    }

    /*
     * (non-Javadoc)
     * 
     * @see IAnnotationModelListenerExtension#modelChanged(AnnotationModelEvent)
     */
    public void modelChanged(AnnotationModelEvent event) {
        // TODO Need to create the analogous bit of logic here... need more stuff in UniversalEditor and friends...
//        if (event instanceof CompilationUnitAnnotationModelEvent) {
//            CompilationUnitAnnotationModelEvent cuEvent= (CompilationUnitAnnotationModelEvent) event;
//            if (cuEvent.includesProblemMarkerAnnotationChanges()) {
//                IResource[] changes= new IResource[] { event.getUnderlyingResource() };
//
//                fireChanges(changes, false);
//            }
//        }
    }

    /**
     * Adds a listener for problem marker changes.
     */
    public void addListener(IProblemChangedListener listener) {
        if (fListeners.isEmpty()) {
            ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
            // TODO Need to create the analogous bit of logic here... need more stuff in UniversalEditor and friends...
//            RuntimePlugin.getInstance().getCompilationUnitDocumentProvider().addGlobalAnnotationModelListener(this);
        }
        fListeners.add(listener);
    }

    /**
     * Removes a <code>IProblemChangedListener</code>.
     */
    public void removeListener(IProblemChangedListener listener) {
        fListeners.remove(listener);
        if (fListeners.isEmpty()) {
            ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
            // TODO Need to create the analogous bit of logic here... need more stuff in UniversalEditor and friends...
//            RuntimePlugin.getInstance().getCompilationUnitDocumentProvider().removeGlobalAnnotationModelListener(this);
        }
    }

    private void fireChanges(final IResource[] changes, final boolean isMarkerChange) {
        Display display= SWTUtil.getStandardDisplay();

        if (display != null && !display.isDisposed()) {
            display.asyncExec(new Runnable() {
                public void run() {
                    Object[] listeners= fListeners.getListeners();

                    for(int i= 0; i < listeners.length; i++) {
                        IProblemChangedListener curr= (IProblemChangedListener) listeners[i];

                        curr.problemsChanged(changes, isMarkerChange);
                    }
                }
            });
        }
    }
}