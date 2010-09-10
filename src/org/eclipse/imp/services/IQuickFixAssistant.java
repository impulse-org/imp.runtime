package org.eclipse.imp.services;

import java.util.Collection;

import org.eclipse.imp.editor.hover.ProblemLocation;
import org.eclipse.imp.language.ILanguageService;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.source.Annotation;

public interface IQuickFixAssistant extends ILanguageService {
	public boolean canFix(Annotation annotation);

	public boolean canAssist(IQuickFixInvocationContext invocationContext);

	public String[] getSupportedMarkerTypes();

	public void addProposals(IQuickFixInvocationContext context,
			ProblemLocation problem, Collection<ICompletionProposal> proposals);
}
