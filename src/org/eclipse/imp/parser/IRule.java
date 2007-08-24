package org.eclipse.imp.parser;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 1998, 2004  All Rights Reserved
 */

/**
 * @author Claffra
 * 
 */
public interface IRule {
    public abstract String getLeftHandSide();

    public abstract int getSize();

    public abstract int getNumber();

    public abstract String getRightHandSide(int n);

    public abstract boolean isTerminal(int rhsNumber);
}