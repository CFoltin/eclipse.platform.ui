package org.eclipse.ui.dialogs;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.IPath;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.internal.WorkbenchMessages;
import java.util.Vector;
import java.util.List;
import java.util.Arrays;

/**
 * The common superclass for wizard import and export pages.
 * <p>
 * This class is not intended to be subclassed outside outside of the workbench.
 * </p>
 */
public abstract class WizardDataTransferPage extends WizardPage implements Listener, IOverwriteQuery {
	
	// constants
	protected static final int SIZING_TEXT_FIELD_WIDTH = 250;
	protected static final int COMBO_HISTORY_LENGTH = 5;
/**
 * Creates a new wizard page.
 *
 * @param pageName the name of the page
 */
protected WizardDataTransferPage(String pageName) {
	super(pageName);
}
/**
 * Adds an entry to a history, while taking care of duplicate history items
 * and excessively long histories.  The assumption is made that all histories
 * should be of length <code>WizardDataTransferPage.COMBO_HISTORY_LENGTH</code>.
 *
 * @param history the current history
 * @param newEntry the entry to add to the history
 */
protected String[] addToHistory(String[] history, String newEntry) {
	java.util.ArrayList l = new java.util.ArrayList(Arrays.asList(history));
	addToHistory(l,newEntry);
	String[] r = new String[l.size()];
	l.toArray(r);
	return r;
}
/**
 * Adds an entry to a history, while taking care of duplicate history items
 * and excessively long histories.  The assumption is made that all histories
 * should be of length <code>WizardDataTransferPage.COMBO_HISTORY_LENGTH</code>.
 *
 * @param history the current history
 * @param newEntry the entry to add to the history
 */
protected void addToHistory(List history, String newEntry) {
	history.remove(newEntry);
	history.add(0,newEntry);

	// since only one new item was added, we can be over the limit
	// by at most one item
	if (history.size() > COMBO_HISTORY_LENGTH)
		history.remove(COMBO_HISTORY_LENGTH);
}
/**
 * Return whether the user is allowed to enter a new container name or just
 * choose from existing ones.
 * <p>
 * Subclasses must implement this method.
 * </p>
 *
 * @return <code>true</code> if new ones are okay, and <code>false</code>
 *  if only existing ones are allowed
 */
protected abstract boolean allowNewContainerName();
/**
 * Creates a new label with a bold font.
 *
 * @param parent the parent control
 * @param text the label text
 * @return the new label control
 */
protected Label createBoldLabel(Composite parent, String text) {
	Label label = new Label(parent, SWT.FLAT);
	label.setFont(JFaceResources.getBannerFont());
	label.setText(text);
	GridData data = new GridData();
	data.verticalAlignment = GridData.FILL;
	data.horizontalAlignment = GridData.FILL;
	label.setLayoutData(data);
	return label;
}
/**
 * Creates the import/export options group controls.
 * <p>
 * The <code>WizardDataTransferPage</code> implementation of this method does
 * nothing. Subclasses wishing to define such components should reimplement
 * this hook method.
 * </p>
 *
 * @param parent the parent control
 */
protected void createOptionsGroup(Composite parent) {
}
/**
 * Creates a new label with a bold font.
 *
 * @param parent the parent control
 * @param text the label text
 * @return the new label control
 */
protected Label createPlainLabel(Composite parent, String text) {
	Label label = new Label(parent, SWT.FLAT);
	label.setText(text);
	GridData data = new GridData();
	data.verticalAlignment = GridData.FILL;
	data.horizontalAlignment = GridData.FILL;
	label.setLayoutData(data);
	return label;
}
/**
 * Creates a horizontal spacer line that fills the width of its container.
 *
 * @param parent the parent control
 */
protected void createSpacer(Composite parent) {
	Label spacer = new Label(parent, SWT.FLAT);
	GridData data = new GridData();
	data.horizontalAlignment = GridData.FILL;
	data.verticalAlignment = GridData.BEGINNING;
	spacer.setLayoutData(data);
}
/**
 * Returns whether this page is complete. This determination is made based upon
 * the current contents of this page's controls.  Subclasses wishing to include
 * their controls in this determination should override the hook methods 
 * <code>validateSourceGroup</code> and/or <code>validateOptionsGroup</code>.
 *
 * @return <code>true</code> if this page is complete, and <code>false</code> if
 *   incomplete
 * @see #validateSourceGroup
 * @see #validateOptionsGroup
 */
protected boolean determinePageCompletion() {
	boolean complete = validateSourceGroup()
		&& validateDestinationGroup()
		&& validateOptionsGroup();

	// Avoid draw flicker by not clearing the error
	// message unless all is valid.
	if (complete)
		setErrorMessage(null);
		
	return complete;
}
/**
 * Get a path from the supplied text widget.
 * @return org.eclipse.core.runtime.IPath
 */
protected IPath getPathFromText(Text textField) {
	return (new Path(textField.getText())).makeAbsolute();
}
/**
 * Queries the user to supply a container resource.
 *
 * @return the path to an existing or new container, or <code>null</code> if the
 *    user cancelled the dialog
 */
protected IPath queryForContainer(IContainer initialSelection, String msg) {
	ContainerSelectionDialog dialog =
		new ContainerSelectionDialog(getControl().getShell(), initialSelection, allowNewContainerName(), msg);
	dialog.open();
	Object[] result = dialog.getResult();
	if (result != null && result.length == 1) {
		return (IPath)result[0];
	}
	return null;
}
/**
 * The <code>WizardDataTransfer</code> implementation of this 
 * <code>IOverwriteQuery</code> method asks the user whether the existing 
 * resource at the given path should be overwritten.
 *
 * @param pathString 
 * @return the user's reply: one of <code>"YES"</code>, <code>"NO"</code>, <code>"ALL"</code>, 
 *   or <code>"CANCEL"</code>
 */
public String queryOverwrite(String pathString) {
	final MessageDialog dialog = 
		new MessageDialog(
			getContainer().getShell(),
			WorkbenchMessages.getString("Question"),  //$NON-NLS-1$
			null, 
			WorkbenchMessages.format("WizardDataTransfer.existsQuestion", new Object[] {pathString}),  //$NON-NLS-1$
			MessageDialog.QUESTION, 
			new String[] {
				IDialogConstants.YES_LABEL, 
				IDialogConstants.NO_LABEL, 
				IDialogConstants.YES_TO_ALL_LABEL, 
				IDialogConstants.CANCEL_LABEL}, 
			0); 
	String[] response = new String[] {YES, NO, ALL, CANCEL};
	//run in syncExec because callback is from an operation,
	//which is probably not running in the UI thread.
	getControl().getDisplay().syncExec(
		new Runnable() {
			public void run() {
				dialog.open();
			}
		});
	return dialog.getReturnCode() < 0 ? CANCEL : response[dialog.getReturnCode()];
}
/**
 * Displays a Yes/No question to the user with the specified message and returns
 * the user's response.
 *
 * @param message the question to ask
 * @return <code>true</code> for Yes, and <code>false</code> for No
 */
protected boolean queryYesNoQuestion(String message) {
	MessageDialog dialog = new MessageDialog(
		getContainer().getShell(),
		WorkbenchMessages.getString("Question"), //$NON-NLS-1$
		(Image)null,
		message,
		MessageDialog.NONE,
		new String[] {IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL},
		0);		// ensure yes is the default

	return dialog.open() == 0;
}
/**
 * Restores control settings that were saved in the previous instance of this
 * page.  
 * <p>
 * The <code>WizardDataTransferPage</code> implementation of this method does
 * nothing. Subclasses may override this hook method.
 * </p>
 */
protected void restoreWidgetValues() {
}
/**
 * Saves control settings that are to be restored in the next instance of
 * this page.  
 * <p>
 * The <code>WizardDataTransferPage</code> implementation of this method does
 * nothing. Subclasses may override this hook method.
 * </p>
 */
protected void saveWidgetValues() {
}
/**
 * Determine if the page is complete and update the page appropriately. 
 */
protected void updatePageCompletion() {
	boolean pageComplete = determinePageCompletion();
	setPageComplete(pageComplete);
	if (pageComplete) {
		setMessage(null);
	}
}
/**
 * Updates the enable state of this page's controls.
 * <p>
 * The <code>WizardDataTransferPage</code> implementation of this method does
 * nothing. Subclasses may extend this hook method.
 * </p>
 */
protected void updateWidgetEnablements() {
}
/**
 * Returns whether this page's destination specification controls currently all
 * contain valid values.
 * <p>
 * The <code>WizardDataTransferPage</code> implementation of this method returns
 * <code>true</code>. Subclasses may reimplement this hook method.
 * </p>
 *
 * @return <code>true</code> indicating validity of all controls in the 
 *   destination specification group
 */
protected boolean validateDestinationGroup() {
	return true;
}
/**
 * Returns whether this page's options group's controls currently all contain
 * valid values.
 * <p>
 * The <code>WizardDataTransferPage</code> implementation of this method returns
 * <code>true</code>. Subclasses may reimplement this hook method.
 * </p>
 *
 * @return <code>true</code> indicating validity of all controls in the options
 *   group
 */
protected boolean validateOptionsGroup() {
	return true;
}
/**
 * Returns whether this page's source specification controls currently all
 * contain valid values.
 * <p>
 * The <code>WizardDataTransferPage</code> implementation of this method returns
 * <code>true</code>. Subclasses may reimplement this hook method.
 * </p>
 *
 * @return <code>true</code> indicating validity of all controls in the 
 *   source specification group
 */
protected boolean validateSourceGroup() {
	return true;
}
}
