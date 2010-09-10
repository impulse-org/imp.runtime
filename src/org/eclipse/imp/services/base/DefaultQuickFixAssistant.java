package org.eclipse.imp.services.base;

import java.util.Collection;

import org.eclipse.imp.editor.hover.ProblemLocation;
import org.eclipse.imp.services.IQuickFixAssistant;
import org.eclipse.imp.services.IQuickFixInvocationContext;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.source.Annotation;

public class DefaultQuickFixAssistant implements IQuickFixAssistant {

	public boolean canAssist(IQuickFixInvocationContext invocationContext) {
		return false;
	}

	public boolean canFix(Annotation annotation) {
		return false;
	}

	public String[] getSupportedMarkerTypes() {
		return new String[0];
	}

	public void addProposals(IQuickFixInvocationContext context,
			ProblemLocation problem, Collection<ICompletionProposal> proposals) {
	}
}
