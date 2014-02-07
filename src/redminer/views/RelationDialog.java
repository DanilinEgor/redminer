package redminer.views;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.taskadapter.redmineapi.bean.Issue;

public class RelationDialog {
	/**
	 * @uml.property  name="rELATION"
	 */
	private final String RELATION = "Отношение";
	/**
	 * @uml.property  name="relationComposites"
	 */
	ArrayList<Composite> relationComposites = new ArrayList<Composite>();
	/**
	 * @uml.property  name="res"
	 */
	String res = "no";
	
	public String showDialog(Issue draggedIssue, Issue issue) {
		open(draggedIssue, issue);
		return res;
	}
	
	private void open(Issue issue, Issue issueTo) {
		Display d = Display.getDefault();
	    final Shell shell = new Shell (d);
	    GridLayout gl = new GridLayout();
	    gl.numColumns = 3;
	    shell.setLayout(gl);

	    Label isLabel = new Label(shell, SWT.SHADOW_OUT);
	    isLabel.setText("Главная задача: №"+ issue.getId() + " " + issue.getSubject());
	    isLabel.pack();
	    isLabel.setLayoutData(new GridData(SWT.END, SWT.BEGINNING, true, true, 3, 1));
	    
	    Label isToLabel = new Label(shell, SWT.SHADOW_OUT);
	    isToLabel.setText("Дочерняя задача: №"+ issueTo.getId() + " " + issueTo.getSubject());
	    isToLabel.pack();
	    isToLabel.setLayoutData(new GridData(SWT.END, SWT.BEGINNING, true, true, 3, 1));
	    
	    Label issueLabel = new Label(shell, SWT.SHADOW_OUT);
	    issueLabel.setText(RELATION);
	    issueLabel.pack();
	    issueLabel.setLayoutData(new GridData(SWT.END, SWT.BEGINNING, true, true));
	    
		final Combo relationChoose = new Combo(shell, SWT.NONE);
		final String[] items = new String[] {"Связана с", "Блокирует", "Заблокирована", "Дублирует", "Дублирована", "Предшествует", "Следует за", "Подзадача"};
		relationChoose.setItems(items);
		relationChoose.setText(items[0]);
		
		Button buttonOK = new Button(shell, SWT.PUSH);
		buttonOK.setText("OK");
		buttonOK.pack();
		
		buttonOK.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				res = relationChoose.getText();
				if (res.equals(items[0])) res = "relates";
				else if (res.equals(items[1])) res = "blocks";
				else if (res.equals(items[2])) res = "blocked";
				else if (res.equals(items[3])) res = "duplicates";
				else if (res.equals(items[4])) res = "duplicated";
				else if (res.equals(items[5])) res = "precedes";
				else if (res.equals(items[6])) res = "follows";
				else if (res.equals(items[7])) res = "subtask";
				shell.close();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
	    Button buttonCancel = new Button(shell, SWT.PUSH);
	    buttonCancel.setText("Отмена");
	    buttonCancel.setLayoutData(new GridData(SWT.END, SWT.END, true, true, 4, 1));
	    buttonCancel.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				res = "no";
				shell.close();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	    
	    shell.pack();
	    shell.open();
	    while (!shell.isDisposed()) {
	        if (!d.readAndDispatch()) {
	            d.sleep();
	        }
	    }
	}
}