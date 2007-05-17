package org.eclipse.uide.refactoring;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;

/**
 * Represents a set of <code>SourceRange</code>s in the file corresponding to an <code>IFile</code>.
 */
public class SourceRangeGroup {
    public IFile fFile;
    public List<SourceRange> fRanges= new ArrayList<SourceRange>();

    public SourceRangeGroup(IFile file) {
        fFile= file;
    }

    public void addReference(SourceRange range) {
        fRanges.add(range);
    }
    
    /**
     * @return Returns the file.
     */
    public IFile getFile() {
        return fFile;
    }

    public List<SourceRange> getRanges() {
        return fRanges;
    }
}
