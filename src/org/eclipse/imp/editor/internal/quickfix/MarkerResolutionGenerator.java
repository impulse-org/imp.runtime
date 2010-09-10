package org.eclipse.imp.editor.internal.quickfix;

import java.util.ArrayList;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.imp.editor.EditorUtility;
import org.eclipse.imp.editor.hover.ProblemLocation;
import org.eclipse.imp.editor.internal.QuickFixController;
import org.eclipse.imp.model.ICompilationUnit;
import org.eclipse.imp.services.IQuickFixInvocationContext;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.quickassist.IQuickAssistInvocationContext;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolution2;
import org.eclipse.ui.IMarkerResolutionGenerator;
import org.eclipse.ui.IMarkerResolutionGenerator2;
import org.eclipse.ui.texteditor.ITextEditor;

public class MarkerResolutionGenerator implements IMarkerResolutionGenerator,
		IMarkerResolutionGenerator2 {

	private static final IMarkerResolution[] NO_RESOLUTIONS = new IMarkerResolution[0];

	private static class CorrectionMarkerResolution implements
			IMarkerResolution, IMarkerResolution2 {

		private int fOffset;
		private int fLength;
		private ICompletionProposal fProposal;
		private final IDocument fDocument;

		public CorrectionMarkerResolution(ICompilationUnit cu, int offset,
				int length, ICompletionProposal proposal, IMarker marker,
				IDocument document) {
			fOffset = offset;
			fLength = length;
			fProposal = proposal;
			fDocument = document;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see IMarkerResolution#getLabel()
		 */
		public String getLabel() {
			return fProposal.getDisplayString();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see IMarkerResolution#run(IMarker)
		 */
		public void run(IMarker marker) {
			try {
				IEditorPart part = EditorUtility.openInEditor(marker
						.getResource());

				if (part instanceof ITextEditor) {
					((ITextEditor) part).selectAndReveal(fOffset, fLength);
				}

				if (fDocument != null) {
					fProposal.apply(fDocument);
				}
			} catch (CoreException e) {
				// JavaPlugin.log(e);
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.IMarkerResolution2#getDescription()
		 */
		public String getDescription() {
			return fProposal.getAdditionalProposalInfo();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.IMarkerResolution2#getImage()
		 */
		public Image getImage() {
			return fProposal.getImage();
		}
	}

	public IMarkerResolution[] getResolutions(final IMarker marker) {
		if (!hasResolutions(marker)) {
			return NO_RESOLUTIONS;
		}

		try {
			QuickFixController qac = new QuickFixController(marker);
			IQuickAssistInvocationContext quickAssistContext = new IQuickAssistInvocationContext() {
				public ISourceViewer getSourceViewer() {
					return null;
				}

				public int getOffset() {
					return marker.getAttribute(IMarker.CHAR_START, 0);
				}

				public int getLength() {
					return marker.getAttribute(IMarker.CHAR_END, 0)
							- getOffset();
				}
			};
			IQuickFixInvocationContext context = qac
					.getContext(quickAssistContext);

			ArrayList<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
			ProblemLocation loc = new ProblemLocation(marker);

			IDocument doc = EditorUtility.getDocument(marker.getResource());
			qac.collectCorrections(context, new ProblemLocation[] { loc },
					proposals);

			IMarkerResolution[] resolutions = new IMarkerResolution[proposals
					.size()];
			int i = 0;
			for (ICompletionProposal proposal : proposals) {
				resolutions[i++] = new CorrectionMarkerResolution(context
						.getModel(), context.getOffset(), context.getLength(),
						proposal, marker, doc);
			}
			return resolutions;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return NO_RESOLUTIONS;
	}

	public boolean hasResolutions(IMarker marker) {
		try {
			QuickFixController c = new QuickFixController(marker);
			return c.canFix(marker);
		} catch (CoreException e) {
			return false;
		}
	}
}