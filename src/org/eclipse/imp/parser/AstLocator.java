package org.eclipse.uide.parser;

public class AstLocator implements IASTNodeLocator {
    public Ast findNode(Ast ast, int offset) {
        if (ast.token != null) {
            if (offset >= ast.token.getStartOffset() && offset <= ast.token.getEndOffset())
                return ast;
            else
                return null;
        }
        if (ast.children == null)
            return null;
        for(int i= 0; i < ast.children.size(); i++) {
            Ast maybe= findNode(ast.children.get(i), offset);
            if (maybe != null)
                return maybe;
        }
        return null;
    }

    public Ast findNode(Ast ast, int startOffset, int endOffset) {
        throw new UnsupportedOperationException();
    }
}
