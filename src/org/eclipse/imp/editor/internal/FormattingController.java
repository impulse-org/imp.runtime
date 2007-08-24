package org.eclipse.imp.editor.internal;

import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.services.ISourceFormatter;
import org.eclipse.jface.text.formatter.IFormattingStrategy;

/**
 * A controller interposed between the Eclipse text framework's IFormattingStrategy
 * and the language-specific formatter, so that the latter can get its hands on an
 * IParseController (and therefore an AST), to drive the formatting decisions.
 * @author Dr. Robert M. Fuhrer
 */
public class FormattingController implements IFormattingStrategy {
    private IParseController fParseController;

    private final ISourceFormatter fFormattingStrategy;

    public FormattingController(ISourceFormatter formattingStrategy) {
        super();
        fFormattingStrategy= formattingStrategy;
    }

    public void formatterStarts(String initialIndentation) {
        fFormattingStrategy.formatterStarts(initialIndentation);
    }

    public String format(String content, boolean isLineStart, String indentation, int[] positions) {
        return fFormattingStrategy.format(fParseController, content, isLineStart, indentation, positions);
    }

    public void formatterStops() {
        fFormattingStrategy.formatterStops();
    }

    public void setParseController(IParseController parseController) {
        fParseController= parseController;
    }
}
