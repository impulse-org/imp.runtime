package org.eclipse.imp.ui.dialogs;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ISelectionValidator;
import org.eclipse.ui.dialogs.SelectionDialog;
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;
import org.eclipse.ui.internal.WorkbenchMessages;

/**
 * Copied and adapted from org.eclipse.ui.dialogs.ListSelectionDialog.
 *
 * From the original:
 *  
 * A standard dialog which solicits a list of selections from the user.
 * This class is configured with an arbitrary data model represented by content
 * and label provider objects. The <code>getResult</code> method returns the
 * selected elements.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * <p>
 * Example:
 * <pre>
 * ListSelectionDialog dlg =
 *   new ListSelectionDialog(
 *       getShell(),
 *       input,
 *       new BaseWorkbenchContentProvider(),
 *		 new WorkbenchLabelProvider(),
 *		 "Select the resources to save:");
 *	dlg.setInitialSelections(dirtyEditors);
 *	dlg.setTitle("Save Resources");
 *	dlg.open();
 * </pre>
 * </p>
 * 
 * Regarding the adaptation:
 * 
 * Adds facilities for filtering list contents and validating
 * list selections.  Filters and validators are set dynamically.
 * This dialog passes filters on to the CheckboxTableViewer that
 * represents the actual list in the dialog.  The filters are applied
 * in that viewer.  This dialog also creates an ICheckStateListener
 * to track changes to the CheckboxTableViewer (i.e., the checking and
 * unchecking of selections) and to trigger validation of selections
 * in response to those changes.
 * 
 * @author sutton (Stan Sutton, suttons@us.ibm.com)
 * @since	20071124
 * 
 */
public class ListSelectionDialog extends SelectionDialog
{
    // the root element to populate the viewer with
    private Object inputElement;

    // providers for populating this dialog
    private ILabelProvider labelProvider;

    private IStructuredContentProvider contentProvider;

    // the visual selection widget group
    CheckboxTableViewer listViewer;

    // sizing constants
    private final static int SIZING_SELECTION_WIDGET_HEIGHT = 250;
    private final static int SIZING_SELECTION_WIDGET_WIDTH = 300;
    
    // SMS:  Copied from SelectionDialog
	static String SELECT_ALL_TITLE = WorkbenchMessages.SelectionDialog_selectLabel;
	static String DESELECT_ALL_TITLE = WorkbenchMessages.SelectionDialog_deselectLabel;

    // SMS:  relating to validation and filtration
	List<ISelectionValidator> validators = new ArrayList();
	List<ViewerFilter> filters = new ArrayList();
	
    // SMS:  the status and error messages
    Label statusMessage;
    String errorMsg = null;
	
    /**
     * Creates a list selection dialog.
     *
     * @param parentShell the parent shell
     * @param input	the root element to populate this dialog with
     * @param contentProvider the content provider for navigating the model
     * @param labelProvider the label provider for displaying model elements
     * @param message the message to be displayed at the top of this dialog, or
     *    <code>null</code> to display a default message
     */
    public ListSelectionDialog(Shell parentShell, Object input,
            IStructuredContentProvider contentProvider,
            ILabelProvider labelProvider, String message) {
        super(parentShell);
        setTitle(WorkbenchMessages.ListSelection_title);
        inputElement = input;
        this.contentProvider = contentProvider;
        this.labelProvider = labelProvider;
        if (message != null) {
			setMessage(message);
		} else {
			setMessage(WorkbenchMessages.ListSelection_message);
		} 
    }

    /**
     * Add the selection and deselection buttons to the dialog.
     * @param composite org.eclipse.swt.widgets.Composite
     */
    private void addSelectionButtons(Composite composite) {
        Composite buttonComposite = new Composite(composite, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 0;
		layout.marginWidth = 0;
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
        buttonComposite.setLayout(layout);
        buttonComposite.setLayoutData(new GridData(SWT.END, SWT.TOP, true, false));

        Button selectButton = createButton(buttonComposite,
                IDialogConstants.SELECT_ALL_ID, SELECT_ALL_TITLE, false);

        SelectionListener listener = new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                listViewer.setAllChecked(true);
            }
        };
        selectButton.addSelectionListener(listener);

        Button deselectButton = createButton(buttonComposite,
                IDialogConstants.DESELECT_ALL_ID, DESELECT_ALL_TITLE, false);

        listener = new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                listViewer.setAllChecked(false);
            }
        };
        deselectButton.addSelectionListener(listener);
    }

    
    /**
     * Visually checks the previously-specified elements in this dialog's list 
     * viewer.
     */
    private void checkInitialSelections() {
        Iterator itemsToCheck = getInitialElementSelections().iterator();

        while (itemsToCheck.hasNext()) {
			listViewer.setChecked(itemsToCheck.next(), true);
		}
    }

    /*
     *  (non-Javadoc)
     * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
     */
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(shell,
				IWorkbenchHelpContextIds.LIST_SELECTION_DIALOG);
    }

    /* (non-Javadoc)
     * Method declared on Dialog.
     */
    protected Control createDialogArea(Composite parent) {
        // page group
        Composite composite = (Composite) super.createDialogArea(parent);
        
        initializeDialogUnits(composite);
        
        createMessageArea(composite);

        listViewer = CheckboxTableViewer.newCheckList(composite, SWT.BORDER);
        GridData data = new GridData(GridData.FILL_BOTH);
        data.heightHint = SIZING_SELECTION_WIDGET_HEIGHT;
        data.widthHint = SIZING_SELECTION_WIDGET_WIDTH;
        listViewer.getTable().setLayoutData(data);

        listViewer.setLabelProvider(labelProvider);
        listViewer.setContentProvider(contentProvider);
        
        // SMS
        for (int i = 0; i < filters.size(); i++) {
        	listViewer.addFilter(filters.get(i));
        }

        addSelectionButtons(composite);

        initializeViewer();

        // initialize page
        if (!getInitialElementSelections().isEmpty()) {
			checkInitialSelections();
		}

        Dialog.applyDialogFont(composite);
        
        statusMessage = new Label(parent, SWT.NONE);
        statusMessage.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        statusMessage.setFont(parent.getFont());
        
        listViewer.addCheckStateListener( new ICheckStateListener() {
            public void checkStateChanged(CheckStateChangedEvent event) {
            	validate();
            }
        	
        });
        
        return composite;
    }

    /**
     * Returns the viewer used to show the list.
     * 
     * @return the viewer, or <code>null</code> if not yet created
     */
    protected CheckboxTableViewer getViewer() {
        return listViewer;
    }

    /**
     * Initializes this dialog's viewer after it has been laid out.
     */
    private void initializeViewer() {
        listViewer.setInput(inputElement);
    }

    /**
     * The <code>ListSelectionDialog</code> implementation of this 
     * <code>Dialog</code> method builds a list of the selected elements for later
     * retrieval by the client and closes this dialog.
     */
    protected void okPressed() {

        // Get the input children.
        Object[] children = contentProvider.getElements(inputElement);

        // Build a list of selected children.
        if (children != null) {
            ArrayList list = new ArrayList();
            for (int i = 0; i < children.length; ++i) {
                Object element = children[i];
                if (listViewer.getChecked(element)) {	
					list.add(element);
				}
            }
            setResult(list);
        }

        // SMS
        validate();
        
        super.okPressed();
        
    }
    
    
    /**
	 * Replaces the current list of validators with a new one.
	 * If the given validator is not null then it is added to
	 * the new list.
     * 
     * @param validator A selection validator (may be null)
     */
    public void setValidator(ISelectionValidator validator) {
    	validators = new ArrayList();
    	if (validator != null)
    		validators.add(validator);
    }
    
    
    /**
     * Adds a given validator to the current list of validators.
	 * Creates the list if it does not already exist.  If the
	 * given validator is null then no validator is added (but
	 * this is not an error).
     * 
     * @param validator		The validator to be added
     */
    public void addValdator(ISelectionValidator validator) {
    	if (validators == null) {
    		validators = new ArrayList();
    	}
    	validators.add(validator);
    }
    

    public void validate() {
    	String errorMsg = null;
    	boolean gotError = false;
    	Object[] selections = null;

    	// Even though we're in a ListSelectionDialog type that is
    	// derived from SelectionDialog, the results (selections)
    	// we want are not those maintained by the SelectionDialog
    	// but those maintained by the CheckboxTableViewer used in
    	// the ListSelectionDialog (this is important).
    	// Note:  getCheckedElements() does not return null.
    	selections = listViewer.getCheckedElements();

    	selections:  for (int j = 0; j < selections.length; j++) {
        	for (int i = 0; i < validators.size(); i++) {       		
        		errorMsg = validators.get(i).isValid(selections[j]);
        		if (errorMsg != null && errorMsg.length() > 0) {
        			gotError = true;
        			break selections;
        		}
        	}   		
    	}
    	
	    if (gotError) {
            statusMessage.setForeground(JFaceColors
                    .getErrorText(statusMessage.getDisplay()));
            statusMessage.setText(errorMsg);
            getOkButton().setEnabled(false);
	    } else {
            statusMessage.setText(""); //$NON-NLS-1$
            errorMsg = null;
            getOkButton().setEnabled(true);
	    }
    }
    
    
    public void addFilter(ViewerFilter filter) {
    	filters.add(filter);
    }
    
    
    
}
