package redminer.views;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TreeEditor;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

import redminer.API;

import com.taskadapter.redmineapi.RedmineException;
import com.taskadapter.redmineapi.RedmineManager;
import com.taskadapter.redmineapi.bean.Issue;
import com.taskadapter.redmineapi.bean.IssueRelation;

public class IssueDialog {
	ArrayList<Composite> relationComposites = new ArrayList<Composite>();
	Set<Integer> tasks = new HashSet<Integer>();
	public void open(final String redmineHost, final String apiAccessKey, final String login, final String password, final Issue issue, final HashMap<Integer, Issue> issues, final ArrayList<Integer> numberIssue, final RedmineManager mgr) {
		Display d = Display.getDefault();
	    final Shell shell = new Shell (d);
	    GridLayout gl = new GridLayout();
	    gl.numColumns = 4;
	    shell.setLayout(gl);
	    
	    Label issueNumber = new Label(shell, SWT.SHADOW_OUT);
	    issueNumber.setText("№"+issue.getId().toString());
	    issueNumber.pack();
	    issueNumber.setLayoutData(new GridData(SWT.END, SWT.BEGINNING, true, true));
	    
	    FontData fontData = issueNumber.getFont().getFontData()[0];
	    Font font = new Font(d, new FontData(fontData.getName(), fontData.getHeight(), SWT.BOLD));
	    issueNumber.setFont(font);
	    
	    Label issueSubject = new Label(shell, SWT.NONE);
	    issueSubject.setText(issue.getSubject());
	    issueSubject.pack();
	    issueSubject.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, true, 3, 1));
	    
	    fontData = issueSubject.getFont().getFontData()[0];
	    font = new Font(d, new FontData(fontData.getName(), fontData.getHeight(), SWT.BOLD));
	    issueSubject.setFont(font);
	    
	    Label priority = new Label(shell, SWT.NONE);
	    priority.setText("Приоритет:");
	    priority.pack();
	    priority.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, true));
	    
	    Label issuePriority = new Label(shell, SWT.NONE);
	    issuePriority.setText(issue.getPriorityText());
	    issuePriority.pack();
	    issuePriority.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, true));
	    if (issue.getStartDate() != null) { 
		    Label startDate = new Label(shell, SWT.NONE);
		    startDate.setText("Дата начала:");
		    startDate.pack();
		    startDate.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, true));
		    
		    Label issueStartDate = new Label(shell, SWT.NONE);
		    issueStartDate.setText(issue.getStartDate().toString());
		    issueStartDate.pack();
		    issueStartDate.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, true));
	    }
	    Label updateDate = new Label(shell, SWT.NONE);
	    updateDate.setText("Обновлена:");
	    updateDate.pack();
	    updateDate.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, true));
	    
	    Label issueUpdateDate = new Label(shell, SWT.NONE);
	    issueUpdateDate.setText(issue.getUpdatedOn().toString());
	    issueUpdateDate.pack();
	    issueUpdateDate.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, true));
	    
	    Label relatedIssues = new Label(shell, SWT.NONE);
	    relatedIssues.setText("Зависимые задачи:");
	    relatedIssues.pack();
	    relatedIssues.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, true, 4, 1));
	    
	    fontData = relatedIssues.getFont().getFontData()[0];
	    font = new Font(d, new FontData(fontData.getName(), fontData.getHeight(), SWT.BOLD));
	    relatedIssues.setFont(font);
	    
	    
	    final Tree tree = new Tree(shell, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
	    tree.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, true, 4, 1));
	    tree.setHeaderVisible(true);
	    
	    TreeColumn col0 = new TreeColumn(tree, SWT.NONE);
	    col0.setText("Название");
	    col0.setWidth(200);
	    TreeColumn col1 = new TreeColumn(tree, SWT.NONE);
	    col1.setText("№");
	    col1.setWidth(50);
	    TreeColumn col2 = new TreeColumn(tree, SWT.NONE);
	    col2.setText("Отношение");
	    col2.setWidth(200);
	    TreeColumn col3 = new TreeColumn(tree, SWT.NONE);
	    col3.setText("");
	    col3.setWidth(50);
    
	    for (Integer num : numberIssue) {
	    	final Issue is = issues.get(num);
	    	if (is.getParentId() == issue.getId()) {
				tasks.add(num);
				final TreeItem item = new TreeItem(tree, SWT.NONE);
				String relation = "Подзадача ";
				String subject = is.getSubject();
				item.setText(0, subject);
				item.setText(1, String.valueOf(is.getId()));
				item.setText(2, relation);
				final Button button = new Button(tree, SWT.PUSH);
				button.setText("-");
				button.pack();
				TreeEditor editor1 = new TreeEditor(tree);
				editor1.horizontalAlignment = SWT.RIGHT;
			    editor1.grabHorizontal = true;
			    editor1.minimumWidth = 50;
			    editor1.setEditor(button, item, 3);
				button.addSelectionListener(new SelectionListener() {
					
					@Override
					public void widgetSelected(SelectionEvent e) {
						try {
							item.dispose();
							button.dispose();
							shell.layout(true);
							API.deleteParent(redmineHost, apiAccessKey, login, password, is.getId());
							Redminer.updateIssues();
							shell.close();
							IssueDialog d = new IssueDialog();
							d.open(redmineHost, apiAccessKey, login, password, issue, issues, numberIssue, mgr);
						} catch (Exception e2) {
							e2.printStackTrace();
						}
					}
					
					@Override
					public void widgetDefaultSelected(SelectionEvent e) {}
				});
			}
		}
	    
	    final ArrayList<IssueRelation> relis = (ArrayList<IssueRelation>) issue.getRelations();
	    for (final IssueRelation issueRelation : relis) {
	    	System.out.println(issueRelation.getId());
	    	tasks.add(issueRelation.getIssueToId() == issue.getId() ? issueRelation.getIssueId() : issueRelation.getIssueToId());
	    	String relation = null;
			if (issueRelation.getType().equals("relates"))
				relation = "Связана с ";
			else if (issueRelation.getType().equals("blocks"))
				if (issueRelation.getIssueId() == issue.getId())
					relation = "Блокирует ";
				else
					relation = "Заблокирована ";
			else if (issueRelation.getType().equals("duplicates"))
				if (issueRelation.getIssueId() == issue.getId())
					relation = "Дублирует ";
				else
					relation = "Дублирована ";
			else if (issueRelation.getType().equals("precedes"))
				if (issueRelation.getIssueId() == issue.getId())
					relation = "Предшествует ";
				else
					relation = "Следует за ";
			final TreeItem item = new TreeItem(tree, SWT.NONE);
			Issue is = (issueRelation.getIssueId() == issue.getId() ? issues.get(issueRelation.getIssueToId()) : issues.get(issueRelation.getIssueId()));
			String subject = is.getSubject();
			item.setText(0, subject);
			item.setText(1, String.valueOf(is.getId()));
			item.setText(2, relation);
			final Button button = new Button(tree, SWT.PUSH);
			button.setText("-");
			button.pack();
			TreeEditor editor1 = new TreeEditor(tree);
			editor1.horizontalAlignment = SWT.RIGHT;
		    editor1.grabHorizontal = true;
		    editor1.minimumWidth = 50;
		    editor1.setEditor(button, item, 3);
		    button.addSelectionListener(new SelectionListener() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					try {
						item.dispose();
						button.dispose();
						shell.layout(true);
//						mgr.deleteRelation(issueRelation.getId());
						API.deleteRelation(redmineHost, apiAccessKey, login, password, issueRelation.getIssueId(), issueRelation.getId());
						Redminer.updateIssues();
						shell.close();
						IssueDialog d = new IssueDialog();
						d.open(redmineHost, apiAccessKey, login, password, issue, issues, numberIssue, mgr);
					} catch (Exception e2) {
						e2.printStackTrace();
					}
				}
				
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {}
			});
		}
	    tree.pack();
	    
		final Composite newRelationComposite = new Composite(shell, SWT.NONE);
		newRelationComposite.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, true, 4, 1));
		GridData gd1 = (GridData)newRelationComposite.getLayoutData();
		gd1.exclude = true;
		newRelationComposite.setLayout(new GridLayout(3, false));
		newRelationComposite.setVisible(false);
		
		final Combo relationChoose = new Combo(newRelationComposite, SWT.NONE);
		final String[] items = new String[] {"Связана с", "Блокирует", "Заблокирована", "Дублирует", "Дублирована", "Предшествует", "Следует за", "Подзадача"};
		relationChoose.setItems(items);
		relationChoose.setText(items[0]);
		
		final Combo issueChoose = new Combo(newRelationComposite, SWT.NONE);
		String[] issueItems = new String[issues.size()-1-tasks.size()];
		for (int i = 0, j = 0; i < numberIssue.size(); ++i) {
			if (!tasks.contains(numberIssue.get(i))) {
				Issue issue1 = issues.get(numberIssue.get(i));
				if (!issue1.getSubject().equals(issue.getSubject()))
					issueItems[j++] = "№" + issue1.getId() + " " + issue1.getSubject();
			}
		}	
		issueChoose.setItems(issueItems);
		if (issueItems.length != 0) 
			issueChoose.setText(issueItems[0]);
		
		Button buttonNewRelationOK = new Button(newRelationComposite, SWT.PUSH);
		buttonNewRelationOK.setText("OK");
		buttonNewRelationOK.pack();
		
		final Button buttonAdd = new Button(shell, SWT.PUSH);
		
		buttonNewRelationOK.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				final int issueTo = Integer.parseInt(issueChoose.getText().substring(1, issueChoose.getText().indexOf(' ')));
				String rel = relationChoose.getText();
				final Issue is = issues.get(issueTo);
				if (rel.equals(items[7])) {
					is.setParentId(issue.getId());
					try {
						mgr.update(is);
						tasks.add(is.getId());
						Redminer.updateIssues();
						shell.close();
						IssueDialog d = new IssueDialog();
						d.open(redmineHost, apiAccessKey, login, password, issue, issues, numberIssue, mgr);
					} catch (RedmineException e1) {
						e1.printStackTrace();
					}
				}
				else {
					if (rel.equals(items[0])) rel = "relates";
					else if (rel.equals(items[1])) rel = "blocks";
					else if (rel.equals(items[2])) rel = "blocked";
					else if (rel.equals(items[3])) rel = "duplicates";
					else if (rel.equals(items[4])) rel = "duplicated";
					else if (rel.equals(items[5])) rel = "precedes";
					else if (rel.equals(items[6])) rel = "follows";
					try {
//						mgr.createRelation(issue.getId(), issueTo, rel);
						API.createRelation(redmineHost, apiAccessKey, login, password, issue.getId(), issueTo, rel);
						Redminer.updateIssues();
						shell.close();
						IssueDialog d = new IssueDialog();
						d.open(redmineHost, apiAccessKey, login, password, issue, issues, numberIssue, mgr);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}

				GridData gd1 = (GridData)newRelationComposite.getLayoutData();
				gd1.exclude = true;
				newRelationComposite.pack();
				newRelationComposite.setVisible(false);
				
				gd1 = (GridData)buttonAdd.getLayoutData();
				gd1.exclude = false;
				buttonAdd.setVisible(true);
				
				shell.layout(true);
				
				shell.pack();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
	    
	    buttonAdd.setText("+");
	    buttonAdd.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, true, 4, 1));
	    
	    buttonAdd.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				GridData gd = (GridData)newRelationComposite.getLayoutData();
				gd.exclude = false;
				newRelationComposite.pack();
				newRelationComposite.setVisible(true);
				shell.layout(true);
				gd = (GridData)buttonAdd.getLayoutData();
				gd.exclude = true;
				buttonAdd.setVisible(false);
				shell.pack();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	    
	    Button buttonCancel = new Button(shell, SWT.PUSH);
	    buttonCancel.setText("Закрыть");
	    buttonCancel.setLayoutData(new GridData(SWT.END, SWT.END, true, true, 4, 1));
	    buttonCancel.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				Redminer.updateIssues();
				Redminer.updateEvents();
				Redminer.updateHeights();
				shell.close();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	    
	    tree.pack();
	    shell.layout(true);
	    shell.pack();
	    shell.setLocation(200, 200);
	    shell.open();
	    while (!shell.isDisposed()) {
	        if (!d.readAndDispatch()) {
	            d.sleep();
	        }
	    }
	}
}