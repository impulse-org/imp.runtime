package org.eclipse.imp.help;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.help.HelpSystem;
import org.eclipse.help.IContext;
import org.eclipse.help.IContext2;
import org.eclipse.help.IHelpResource;
import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.services.IHelpService;
import org.eclipse.ui.PlatformUI;

public class IMPHelpContext implements IContext2 {
    private final String fHelpText;
    private final String fTitle;

    public static void displayHelp(IHelpService helpSrvc, IParseController parseController, String contextId, Object[] selected) throws CoreException {
        IContext context= HelpSystem.getContext(contextId);

        if (context != null) {
            if (selected != null && selected.length > 0) {
                String helpText= IMPHelpContextProvider.buildHelpString(selected, helpSrvc, parseController);

                context= new IMPHelpContext(context, helpText);
            }
            PlatformUI.getWorkbench().getHelpSystem().displayHelp(context);
        }
    }

    public IMPHelpContext(IContext context, String helpText) {
        fHelpText= helpText;
        fTitle= (context instanceof IContext2) ? ((IContext2) context).getTitle() : "";
    }

    public IHelpResource[] getRelatedTopics() {
        return new IHelpResource[0];
    }

    public String getText() {
        return fHelpText;
    }

    public String getCategory(IHelpResource topic) {
        // TODO Auto-generated method stub
        return null;
    }

    public String getStyledText() {
        return fHelpText;
    }

    public String getTitle() {
        return fTitle;
    }
}
