/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Robert Fuhrer (rfuhrer@watson.ibm.com) - initial API and implementation
 *******************************************************************************/

package org.eclipse.imp.utils;

import java.io.PrintStream;

import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

public class ConsoleUtil {
    public static MessageConsole findConsole(String name) {
        final IConsoleManager consoleManager= ConsolePlugin.getDefault().getConsoleManager();
        IConsole[] consoles= consoleManager.getConsoles();

        for(int i= 0; i < consoles.length; i++) {
            IConsole console= consoles[i];
            if (console.getName().equals(name))
                return (MessageConsole) console;
        }
        MessageConsole myConsole= new MessageConsole(name, null);

        consoleManager.addConsoles(new IConsole[] { myConsole });

        return myConsole;
    }

    public static MessageConsole findAndShowConsole(String name) {
        MessageConsole console= findConsole(name);

        showConsole(console);
        return console;
    }

    public static PrintStream getPrintStreamFor(MessageConsole console) {
        MessageConsoleStream consStream= console.newMessageStream();
        PrintStream ps= new PrintStream(consStream);

        return ps;
    }

    public static PrintStream findConsoleStream(String name) {
        MessageConsole myConsole= findConsole(name);

        return getPrintStreamFor(myConsole);
    }

    public static void showConsole(MessageConsole console) {
        final IConsoleManager consoleManager = ConsolePlugin.getDefault().getConsoleManager();

        consoleManager.showConsoleView(console);
    }
}
