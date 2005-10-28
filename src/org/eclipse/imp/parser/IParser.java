package org.eclipse.uide.parser;

import org.eclipse.uide.core.ILanguageService;

import com.ibm.lpg.LexStream;
import com.ibm.lpg.Monitor;
import com.ibm.lpg.PrsStream;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 1998, 2004  All Rights Reserved
 */


/**
 * @author rfuhrer, pcharles
 */
public interface IParser extends ILanguageService {
    /**
     * Run the parser to create a model.
     * @param monitor stop scanning/parsing when monitor.isCanceled() is true.
     * @return
     */
    public Object parser(Monitor monitor, int error_repair_count);

    public PrsStream getParseStream();

    /**
     * @return the token kind for the EOF token
     */
    public int getEOFTokenKind();
	
    /**
     * Inform the parser what language it is parsing.
     * @param language the name of the language
     */
    //public void setLanguage(String language);
}
