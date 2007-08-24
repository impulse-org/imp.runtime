/*
 * Created on Nov 1, 2005
 */
package org.eclipse.imp.runtime;

public interface IPluginLog {
    public abstract void maybeWriteInfoMsg(String msg);

    public abstract void writeErrorMsg(String msg);
}