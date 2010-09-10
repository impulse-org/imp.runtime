/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.imp.editor.internal;

import java.util.Iterator;
import java.util.ResourceBundle;

import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.imp.preferences.PreferenceConstants;
import org.eclipse.imp.runtime.RuntimePlugin;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationAccessExtension;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.AbstractMarkerAnnotationModel;
import org.eclipse.ui.texteditor.AnnotationPreference;
import org.eclipse.ui.texteditor.AnnotationPreferenceLookup;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.ITextEditorExtension;
import org.eclipse.ui.texteditor.SelectMarkerRulerAction;

/**
 * Action which gets triggered when selecting (annotations) in the vertical ruler.
 *
 * <p>
 * Was originally called >code>JavaSelectMarkerRulerAction</code>.
 * </p>
 */
@SuppressWarnings({"unchecked", "unused"})
public class SelectAnnotationRulerAction extends SelectMarkerRulerAction {

	private ITextEditor fTextEditor;
	private Position fPosition;
	private Annotation fAnnotation;
	private AnnotationPreferenceLookup fAnnotationPreferenceLookup;
	private IPreferenceStore fStore;
	private boolean fHasCorrection;
	private ResourceBundle fBundle;

	public SelectAnnotationRulerAction(ResourceBundle bundle, String prefix, ITextEditor editor, IVerticalRulerInfo ruler) {
		super(bundle, prefix, editor, ruler);
		fBundle= bundle;
		fTextEditor= editor;

		fAnnotationPreferenceLookup= EditorsUI.getAnnotationPreferenceLookup();
		fStore= RuntimePlugin.getInstance().getPreferenceStore();

//		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IJavaHelpContextIds.JAVA_SELECT_MARKER_RULER_ACTION);
	}

	public void run() {
		if (fStore.getBoolean(PreferenceConstants.EDITOR_ANNOTATION_ROLL_OVER))
			return;

		runWithEvent(null);
	}

	/*
	 * @see org.eclipse.jface.action.IAction#runWithEvent(org.eclipse.swt.widgets.Event)
	 * @since 3.2
	 */
	public void runWithEvent(Event event) {
//		if (fAnnotation instanceof OverrideIndicatorManager.OverrideIndicator) {
//			((OverrideIndicatorManager.OverrideIndicator)fAnnotation).open();
//			return;
//		}

		if (fHasCorrection) {
			ITextOperationTarget operation= (ITextOperationTarget) fTextEditor.getAdapter(ITextOperationTarget.class);
			final int opCode= ISourceViewer.QUICK_ASSIST;
			if (operation != null && operation.canDoOperation(opCode)) {
				fTextEditor.selectAndReveal(fPosition.getOffset(), fPosition.getLength());
				operation.doOperation(opCode);
			}
			return;
		}

		super.run();
	}

	public void update() {
		findAnnotation();
		setEnabled(true); // super.update() might change this later

//		if (fAnnotation instanceof OverrideIndicatorManager.OverrideIndicator) {
//			initialize(fBundle, "JavaSelectAnnotationRulerAction.OpenSuperImplementation."); //$NON-NLS-1$
//			return;
//		}
		if (fHasCorrection) {
//			if (fAnnotation instanceof AssistAnnotation)
//				initialize(fBundle, "JavaSelectAnnotationRulerAction.QuickAssist."); //$NON-NLS-1$
//			else
				initialize(fBundle, "SelectAnnotationRulerAction.QuickFix."); //$NON-NLS-1$
			return;
		}

		initialize(fBundle, "SelectAnnotationRulerAction.GotoAnnotation."); //$NON-NLS-1$;
		super.update();
	}

	private void findAnnotation() {
		fPosition= null;
		fAnnotation= null;
		fHasCorrection= false;
		
		if(!(fTextEditor instanceof UniversalEditor))
		{
			return;
		}
		
		AbstractMarkerAnnotationModel model= getAnnotationModel();
		IAnnotationAccessExtension annotationAccess= getAnnotationAccessExtension();

		IDocument document= getDocument();
		if (model == null)
			return ;

		boolean hasAssistLightbulb= fStore.getBoolean(PreferenceConstants.EDITOR_QUICKASSIST_LIGHTBULB);

		Iterator iter= model.getAnnotationIterator();
		int layer= Integer.MIN_VALUE;

		while (iter.hasNext()) {
			Annotation annotation= (Annotation) iter.next();
			if (annotation.isMarkedDeleted())
				continue;

			int annotationLayer= layer;
			if (annotationAccess != null) {
				annotationLayer= annotationAccess.getLayer(annotation);
				if (annotationLayer < layer)
					continue;
			}

			Position position= model.getPosition(annotation);
			if (!includesRulerLine(position, document))
				continue;

			boolean isReadOnly= fTextEditor instanceof ITextEditorExtension && ((ITextEditorExtension)fTextEditor).isEditorInputReadOnly();
			QuickFixController qac = new QuickFixController((UniversalEditor)fTextEditor);
			
			if (!isReadOnly
					&& (
//						((hasAssistLightbulb && annotation instanceof AssistAnnotation)
//						||
						qac.canFix(annotation)
						))
							{
				fPosition= position;
				fAnnotation= annotation;
				fHasCorrection= true;
				layer= annotationLayer;
				continue;
			} else if (!fHasCorrection) {
				AnnotationPreference preference= fAnnotationPreferenceLookup.getAnnotationPreference(annotation);
				if (preference == null)
					continue;

				String key= preference.getVerticalRulerPreferenceKey();
				if (key == null)
					continue;

				if (fStore.getBoolean(key)) {
					fPosition= position;
					fAnnotation= annotation;
					layer= annotationLayer;
				}
			}
		}
	}
}

