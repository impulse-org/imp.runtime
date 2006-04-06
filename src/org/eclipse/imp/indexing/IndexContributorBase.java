package org.eclipse.uide.indexing;

import org.eclipse.uide.core.ILanguageService;


/**
 * Base class for implementations of the indexContributor language service extension point.
 * @author Dr. Robert M. Fuhrer
 *
 */
public abstract class IndexContributorBase implements ILanguageService {
    public IndexContributorBase() {
        super();
    }

    /**
     * Create index entries for entities in the given AST and add them to the given Indexer,
     * by means of calls to Indexer.addEntry().
     */
    public abstract void contributeEntries(Object ast, Indexer indexer);

    /**
     * Parse the given entry string
     * @param s
     * @return
     */
    public IndexEntry parseEntry(String s) {
        char kind= s.charAt(0);
        IndexEntry entry;

        if (kind == DefinitionIndexEntry.DEFINITION_TYPE)
            entry= new DefinitionIndexEntry();
        else if (kind == ReferenceIndexEntry.REFERENCE_TYPE)
            entry= new ReferenceIndexEntry();
        else
            return null;

        entry.parseFromString(s);
        return entry;
    }
}
