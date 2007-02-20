package org.eclipse.uide.defaults;

import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.uide.core.ErrorHandler;
import org.eclipse.uide.editor.IOutliner;
import org.eclipse.uide.editor.UniversalEditor;
import org.eclipse.uide.parser.Ast;
import org.eclipse.uide.parser.IParseController;
import org.eclipse.uide.parser.ParseError;

import lpg.runtime.IToken;

/**
 * @author CLaffra
 */
public class DefaultOutliner implements IOutliner {
    protected Tree tree;

    protected UniversalEditor editor;

    protected static String message;

    private static final String MESSAGE= "This is the default outliner. Add your own using the UIDE wizard and see class 'org.eclipse.uide.defaults.DefaultOutliner'";

    public void setLanguage(String language) {
	ErrorHandler.reportError("No Outliner defined for \"" + language + "\"");
    }

    public void setEditor(UniversalEditor editor) {
	this.editor= editor;
    }

    public void setTree(Tree tree) {
	this.tree= tree;
    }

    public void createOutlinePresentation(IParseController controller, int offset) {
	try {
	    if (controller != null && tree != null) {
		if (controller.hasErrors()) {
		    tree.setRedraw(false);
		    tree.removeAll();
		    new TreeItem(tree, SWT.NONE).setText(MESSAGE);
		    List errors= controller.getErrors();
		    new TreeItem(tree, SWT.NONE).setText("Found " + errors.size() + " syntax error(s) in input: ");
		    for(Iterator error= errors.iterator(); error.hasNext(); ) {
			ParseError pe= (ParseError) error.next();
			new TreeItem(tree, SWT.NONE).setText("  " + pe.description);
		    }
		    int count= controller.getParser().getParseStream().getSize();
		    if (count > 1) {
			new TreeItem(tree, SWT.NONE).setText("Tokens:");
			for(int n= 1; n < 100 && n < count; n++) {
			    IToken token= controller.getParser().getParseStream().getTokenAt(n);
			    String label= n + ": "
				    + controller.getParser().getParseStream().orderedTerminalSymbols()[token.getKind()] + " = "
				    + token.getValue(controller.getLexer().getLexStream().getInputChars());
			    new TreeItem(tree, SWT.NONE).setText(label);
			}
			if (count >= 100)
			    new TreeItem(tree, SWT.NONE).setText("rest of outline truncated...");
		    }
		    return;
		} else {
		    tree.setRedraw(false);
		    tree.removeAll();
		    Object ast= controller.getCurrentAst();
		    if (ast instanceof Ast) {
			Ast program= (Ast) ast;
			new TreeItem(tree, SWT.NONE).setText(MESSAGE);
			for(int n= 0; n < program.children.size(); n++) {
			    Ast node= program.getChild(n);
			    addToOutlineView(node, controller, tree, null);
			}
		    }
		}
		tree.setSelection(new TreeItem[] { tree.getItem(new Point(0, 0)) });
	    }
	    //selectTreeItemAtTextOffset(offset);
	} catch (Throwable e) {
	    ErrorHandler.reportError("Could not generate outline", e);
	} finally {
	    if (tree != null)
		tree.setRedraw(true);
	}
    }

    protected void selectTreeItemAtTextOffset(int offset) {
	TreeItem items[]= tree.getItems();
	if (items.length == 0)
	    return;
	for(int n= 0; n < items.length; n++) {
	    Ast node= (Ast) items[n].getData();
	    if (node != null && node.getStartOffset() > offset) {
		if (n > 0)
		    tree.setSelection(new TreeItem[] { items[n - 1] });
		return;
	    }
	}
    }

    /**
     * Visit a node and its children to determine what to add to the 
     * outline tree. Subclasses can override and do the actual adding.
     * They may decide to recursively visit their children. 
     * @param scanner
     * 
     * @param tree the tree to add the item to
     * @param item the treeitem to add the item to, if it is not null
     */
    protected void addToOutlineView(Ast node, IParseController controller, Tree tree, TreeItem item) {
	createOutlineItem(node, tree, item, /* REPLACE controller.getString(node) by: */node.getRuleName());
    }

    /**
     * Add a new outline item to the existing outline tree
     * @param tree the tree to add the item to
     * @param item the treeitem to add the item to, if it is not null
     * @param label the label for the item
     */
    protected TreeItem createOutlineItem(Ast node, Tree tree, TreeItem item, String label) {
	TreeItem subItem= null;
	if (item == null)
	    subItem= new TreeItem(tree, SWT.NONE);
	else
	    subItem= new TreeItem(item, SWT.NONE);
	subItem.setText(label);
	subItem.setData(node);
	return subItem;
    }
}
