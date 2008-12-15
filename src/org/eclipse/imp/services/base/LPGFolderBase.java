package org.eclipse.imp.services.base;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import lpg.runtime.Adjunct;
import lpg.runtime.ILexStream;
import lpg.runtime.IPrsStream;
import lpg.runtime.IToken;

import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;

public abstract class LPGFolderBase extends FolderBase {
    protected IPrsStream prsStream;

    protected void makeAnnotationWithOffsets(int first_offset, int last_offset) {
        super.makeAnnotation(first_offset, last_offset - first_offset + 1);
    }
    
    //
    // Use this version of makeAnnotation when you have a range of 
    // tokens to fold.
    //
    protected void makeAnnotation(IToken first_token, IToken last_token) {
        if (last_token.getEndLine() > first_token.getLine()) {
            IToken next_token = prsStream.getIToken(prsStream.getNext(last_token.getTokenIndex()));
            IToken[] adjuncts = next_token.getPrecedingAdjuncts();
            IToken gate_token = adjuncts.length == 0 ? next_token : adjuncts[0];
            makeAnnotationWithOffsets(first_token.getStartOffset(), last_token.getEndOffset());
            // SMS 29 Sep 2009:  modified the above to extend the foldable region just to the end
            // of the last token rather than to the beginning of the next token (as commented out
            // below), since that seems to simplify some aspects of annotation management (and is
            // also consistent with the JDT Java editor).
//                                      gate_token.getLine() > last_token.getEndLine()
//                                          ? prsStream.getLexStream().getLineOffset(gate_token.getLine() - 1)
//                                          : last_token.getEndOffset());
        }
    }

    protected void makeAdjunctAnnotations() {
        ILexStream lexStream = prsStream.getLexStream();
        if (lexStream == null)
            return;
        ArrayList adjuncts = (ArrayList) prsStream.getAdjuncts();
        for (int i = 0; i < adjuncts.size();)
        {
            Adjunct adjunct = (Adjunct) adjuncts.get(i);

            IToken previous_token = prsStream.getIToken(adjunct.getTokenIndex()),
                   next_token = prsStream.getIToken(prsStream.getNext(previous_token.getTokenIndex())),
                   comments[] = previous_token.getFollowingAdjuncts();

            for (int k = 0; k < comments.length; k++)
            {
                Adjunct comment = (Adjunct) comments[k];
                if (comment.getEndLine() > comment.getLine())
                {
                    IToken gate_token = k + 1 < comments.length ? comments[k + 1] : next_token;
                    makeAnnotationWithOffsets(comment.getStartOffset(),
                                              gate_token.getLine() > comment.getEndLine()
                                                  ? lexStream.getLineOffset(gate_token.getLine() - 1)
                                                  : comment.getEndOffset());
                }
            }

            i += comments.length;
        }    
    }
}
