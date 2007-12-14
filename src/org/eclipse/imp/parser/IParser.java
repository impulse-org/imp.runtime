/*
 * (C) Copyright IBM Corporation 2007
 * 
 * This file is part of the Eclipse IMP.
 */
package org.eclipse.imp.parser;

import lpg.runtime.Monitor;
import lpg.runtime.PrsStream;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 1998, 2004  All Rights Reserved
 */


/**
 * @author rfuhrer, pcharles
 */
public interface IParser {
    /**
     * Run the parser to create a model.
     * @param monitor stop scanning/parsing when monitor.isCanceled() is true.
     * @return
     */
    public Object parser(Monitor monitor, int error_repair_count);

    public PrsStream getParseStream();

    /**
     * @return array of keywords in the order in which they are mapped to integers.
     */
    public String[] orderedTerminalSymbols();

    /**
     * @return array of keywords in the order in which they are mapped to integers.
     */
    public int numTokenKinds();

    /**
     * @return the token kind for the EOF token
     */
    public int getEOFTokenKind();
}
