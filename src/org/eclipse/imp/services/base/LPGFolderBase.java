package org.eclipse.imp.services.base;

import java.util.List;

import lpg.runtime.Adjunct;
import lpg.runtime.IAst;
import lpg.runtime.ILexStream;
import lpg.runtime.IPrsStream;
import lpg.runtime.IToken;

public abstract class LPGFolderBase extends FolderBase {
    protected IPrsStream prsStream;

    /**
     * Folding helper method that takes a start and last offset, inclusive, unlike
     * the base class method makeAnnotation(int, int), which takes a start offset
     * and a length.
     */
    protected void makeFoldableByOffsets(int first_offset, int last_offset) {
        if (fDebugMode) {
            ILexStream lexStream = prsStream.getILexStream();
            int first_line= lexStream.getLineNumberOfCharAt(first_offset);
            int first_col= lexStream.getColumnOfCharAt(first_offset);
            int last_line= lexStream.getLineNumberOfCharAt(last_offset);
            int last_col= lexStream.getColumnOfCharAt(last_offset);

            System.out.println("Folding <line " + first_line + ", col " + first_col + " '" + lexStream.getCharValue(first_offset) + "'> -> "
                    + "<line " + last_line + ", col " + last_col + " '" + lexStream.getCharValue(last_offset) + "'>");
        }
        super.makeAnnotation(first_offset, last_offset - first_offset + 1);
    }
    
    /**
     * Use this version of makeAnnotation when you have a range of 
     * tokens to fold.
     */
    protected void makeFoldable(IToken first_token, IToken last_token) {
        if (last_token.getEndLine() > first_token.getLine()) {
            ILexStream lexStream = prsStream.getILexStream();
            int start = first_token.getStartOffset();
            int end = last_token.getEndOffset();

            // Following may be necessary if one edits an empty source file; there
            // may be an AST with an empty textual extent, which causes Position()
            // a heartache.
            if (end <= start) {
                return;
            }

            while (start > 0 && isWhiteNotLineTerm(lexStream.getCharValue(start))) {
                start--;
            }
            while (end < lexStream.getStreamLength() && isWhiteNotLineTerm(lexStream.getCharValue(end))) {
                end++;
            }
            if (end < lexStream.getStreamLength()-1) {
                char endChar= lexStream.getCharValue(end+1);

                if (endChar == '\n' || endChar == '\r') {
                    end++;
                }
            }

            makeFoldableByOffsets(start, end);
        }
    }

    /**
     * @return true, if the given character is a whitespace (according to
     * Character.isWhitespace()), but is not a line terminating character.
     */
    protected boolean isWhiteNotLineTerm(char ch) {
        return Character.isWhitespace(ch) && ch != '\n' && ch != '\r' && ch != '\u000C';
    }

    /**
     * Marks the given AST node foldable.
     */
    protected void makeFoldable(IAst n) {
        if (fDebugMode) {
            System.out.println("Folding node of type " + n.getClass());
        }
        makeFoldable(n.getLeftIToken(), n.getRightIToken());
    }

    /**
     * Makes all of the multi-line adjuncts (i.e. comments) foldable.
     */
    protected void makeAdjunctsFoldable() {
        ILexStream lexStream = prsStream.getILexStream();

        if (lexStream == null)
            return;

        List<IToken> adjuncts = prsStream.getAdjuncts();

        for (int i = 0; i < adjuncts.size(); ) {
            Adjunct adjunct = (Adjunct) adjuncts.get(i);

            IToken previous_token = prsStream.getIToken(adjunct.getTokenIndex()),
                   next_token = prsStream.getIToken(prsStream.getNext(previous_token.getTokenIndex())),
                   comments[] = previous_token.getFollowingAdjuncts();

            for (int k = 0; k < comments.length; k++) {
                Adjunct comment = (Adjunct) comments[k];
                if (comment.getEndLine() > comment.getLine()) {
                    IToken gate_token = k + 1 < comments.length ? comments[k + 1] : next_token;
                    makeFoldableByOffsets(comment.getStartOffset(),
                                              gate_token.getLine() > comment.getEndLine()
                                                  ? lexStream.getLineOffset(gate_token.getLine() - 1)
                                                  : comment.getEndOffset());
                }
            }

            i += comments.length;
        }    
    }
}
