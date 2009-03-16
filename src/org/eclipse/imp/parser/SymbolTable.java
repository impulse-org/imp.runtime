package org.eclipse.imp.parser;

import java.util.HashMap;

/**
 * Trivial symbol table class mapping Strings to declarations (whatever kind
 * the parser/resolver produces). Parent/child relationship is intended to
 * represent lexical nesting of scopes.
 */
public class SymbolTable<T> extends HashMap<String,Object> {
    private final SymbolTable<T> parent;

    public SymbolTable(SymbolTable<T> parent) { this.parent = parent; }

    public T findDeclaration(String name) {
        T decl = (T) get(name);
        return decl != null ? decl : (parent != null ? parent.findDeclaration(name) : null);
    }

    public SymbolTable<T> getParent() { return parent; }
}
