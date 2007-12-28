package org.eclipse.imp.model;

public interface ISourceContainer extends ISourceEntity {
    ISourceEntity[] getChildren();
}
