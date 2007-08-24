/*
 * (C) Copyright IBM Corporation 2007
 * 
 * This file is part of the Eclipse IMP.
 */
package org.eclipse.imp.indexing;

import java.io.FileWriter;
import java.io.IOException;

import org.eclipse.core.resources.IFile;

public class DefinitionIndexEntry extends IndexEntry {
    public final static char DEFINITION_TYPE= 'D';

    private int fModifiers;

    public DefinitionIndexEntry() { }

    public DefinitionIndexEntry(int type, String name, int modifiers, IFile file, int start, int end) {
        super(type, name, file, start, end);
    }

    public void saveToStream(FileWriter writer) throws IOException {
        super.saveToStream(writer);
        writer.write(':');
        writer.write(Integer.toString(fModifiers));
    }

    public void readExtraFields(String[] fields) {
        fModifiers= Integer.parseInt(fields[6]);
    }

    public char getEntryKind() {
        return DEFINITION_TYPE;
    }

    public Object findASTNode(Object ast) {
        return null;
    }
}
