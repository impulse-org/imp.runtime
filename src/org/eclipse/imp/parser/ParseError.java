package org.eclipse.imp.parser;

import lpg.runtime.IToken;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 2005  All Rights Reserved
 */

/**
 * @author Claffra
 *
 */
public class ParseError {
    public IToken token;
    public String description;

    public ParseError(String description, IToken token) {
        this.description = description;
        this.token = token;
    }
}
