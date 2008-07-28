package org.eclipse.imp.utils;

import java.io.PrintStream;

import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

public class ConsoleUtil {
    public static MessageConsole findConsole(String name) {
        MessageConsole myConsole= null;
        final IConsoleManager consoleManager= ConsolePlugin.getDefault().getConsoleManager();
        IConsole[] consoles= consoleManager.getConsoles();
        for(int i= 0; i < consoles.length; i++) {
            IConsole console= consoles[i];
            if (console.getName().equals(name))
                myConsole= (MessageConsole) console;
        }
        if (myConsole == null) {
            myConsole= new MessageConsole(name, null);
            consoleManager.addConsoles(new IConsole[] { myConsole });
        }
        consoleManager.showConsoleView(myConsole);
        return myConsole;
    }

    public static PrintStream findConsoleStream(String name) {
        MessageConsole myConsole= findConsole(name);
        MessageConsoleStream consStream= myConsole.newMessageStream();
        PrintStream ps= new PrintStream(consStream);

        return ps;
    }
}
