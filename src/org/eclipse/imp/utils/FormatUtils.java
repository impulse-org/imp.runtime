package org.eclipse.imp.utils;

import org.eclipse.imp.editor.LanguageServiceManager;
import org.eclipse.imp.editor.internal.FormattingController;
import org.eclipse.imp.language.Language;
import org.eclipse.imp.services.ISourceFormatter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.formatter.ContentFormatter;

public class FormatUtils {
    private FormatUtils() { }

    public static void format(Language language, IDocument document, IRegion region) {
		LanguageServiceManager man = new LanguageServiceManager(language);
		ContentFormatter cf = new ContentFormatter();

		ISourceFormatter sf = man.getFormattingStrategy();
		if (sf == null) {
			return;
		}

		FormattingController fc = new FormattingController(sf);
		cf.setFormattingStrategy(fc, null);
		cf.format(document, region);
	}
}
