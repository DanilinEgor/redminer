package redminer.views;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.nebula.widgets.grid.Grid;
import org.eclipse.nebula.widgets.grid.GridColumn;
import org.eclipse.nebula.widgets.grid.GridItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.taskadapter.redmineapi.RedmineAuthenticationException;
import com.taskadapter.redmineapi.RedmineException;
import com.taskadapter.redmineapi.RedmineManager;
import com.taskadapter.redmineapi.bean.Issue;
import com.taskadapter.redmineapi.bean.IssueRelation;

/**
 * This sample class demonstrates how to plug-in a new workbench view. The view
 * shows data obtained from the model. The sample creates a dummy model on the
 * fly, but a real implementation would connect to the model available either in
 * this or another plug-in (e.g. the workspace). The view is connected to the
 * model using a content provider.
 * <p>
 * The view uses a label provider to define how model objects should be
 * presented in the view. Each view can present the same model objects using
 * different labels and icons, if needed. Alternatively, a single label provider
 * can be shared between views in order to ensure that objects of the same type
 * are presented in the same way everywhere.
 * <p>
 */

public class Redminer extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "redminer.views.Redminer";

	private Grid grid;
	private Action action1;

	private static String redmineHost = "http://localhost/redmine/projects/test";
	private static String apiAccessKey = "6e2aba71da02acd7890484201b846ab6682ed52a";
	private static String projectKey = "test";
	private RedmineManager mgr;
	private ArrayList<Issue> allIssues;
	private HashSet<Pair<Integer, Integer>> relates, precedes, duplicates, blocks;
	
	/*
	 * The content provider class is responsible for providing objects to the
	 * view. It can wrap existing objects in adapters or simply return objects
	 * as-is. These objects may be sensitive to the current input of the view,
	 * or ignore it and always show the same content (like Task List, for
	 * example).
	 */

	/**
	 * The constructor.
	 */
	public Redminer() {
	}
	
	private String getPriorityString(int priority){
		String res = null;
		switch (priority) {
		case 3:
			res = "Low";
			break;
		case 4:
			res = "Normal";
			break;
		case 5:
			res = "High";
			break;
		case 6:
			res = "Urgent";
			break;
		case 7:
			res = "Immediate";
			break;

		default:
			break;
		}
		return res;
	}
	
	public void updateIssues() {
		allIssues = new ArrayList<Issue>();
		try {
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("include", "relations");
			allIssues = (ArrayList<Issue>) mgr.getIssues(params);
		} catch (RedmineAuthenticationException e) {
			e.printStackTrace();
		} catch (RedmineException e) {
			e.printStackTrace();
		}
		
		try {
			mgr.createRelation(5, 7, "blocks");
		} catch (RedmineException e1) {
			e1.printStackTrace();
		}
		
		relates = new HashSet<Pair<Integer,Integer>>();
		blocks = new HashSet<Pair<Integer,Integer>>();
		duplicates = new HashSet<Pair<Integer,Integer>>();
		precedes = new HashSet<Pair<Integer,Integer>>();
		
		for (Issue issue : allIssues) {
			ArrayList<IssueRelation> relis = (ArrayList<IssueRelation>) issue.getRelations();
			for (IssueRelation issueRelation : relis) {
				Pair<Integer, Integer> p = new Pair<Integer, Integer>(issueRelation.getIssueId(), issueRelation.getIssueToId());
				if (issueRelation.getType().equals("relates"))
					relates.add(p);
				if (issueRelation.getType().equals("blocks"))
					blocks.add(p);
				if (issueRelation.getType().equals("duplicates"))
					duplicates.add(p);
				if (issueRelation.getType().equals("precedes"))
					precedes.add(p);
			}
		}
		
		grid.removeAll();
		
		for (Issue issue : allIssues) {
			GridItem item1 = new GridItem(grid, SWT.NONE);
			String subject = issue.getSubject();
			int priority = issue.getPriorityId();
			String priorityS = getPriorityString(priority);
			String date = issue.getUpdatedOn().toString();
			String assignee = (issue.getAssignee() == null ? "-" : issue.getAssignee().toString());
			item1.setText(0, subject);
			item1.setText(1, priorityS);
			item1.setText(2, assignee);
			item1.setText(3, date);
			
			boolean created = false;
			GridItem item2 = null;
			for (Pair<Integer, Integer> pair : relates) {
				if (pair.getFirst() == issue.getId() || pair.getSecond() == issue.getId()) {
					int id = (issue.getId() == pair.getFirst() ? pair.getSecond() : pair.getFirst());
					String subject1 = null, priorityS1 = null, date1 = null, assignee1 = null;
					for (Issue issue1 : allIssues) {
						if (issue1.getId() == id) {
							subject1 = issue1.getSubject();
							int priority1 = issue1.getPriorityId();
							priorityS1 = getPriorityString(priority1);
							date1 = issue1.getUpdatedOn().toString();
							assignee1 = (issue1.getAssignee() == null ? "-" : issue1.getAssignee().toString());
						}
					}
					if (created == false) {
						item2 = new GridItem(item1, SWT.NONE);
						item2.setText("related to");
						created = true;
					}
					GridItem item3 = new GridItem(item2, SWT.NONE);
					item3.setText(0, subject1);
					item3.setText(1, priorityS1);
					item3.setText(2, assignee1);
					item3.setText(3, date1);
				}
			}
			
			boolean created1 = false, created2 = false;
			for (Pair<Integer, Integer> pair : blocks) {
				if (pair.getFirst() == issue.getId() || pair.getSecond() == issue.getId()) {
					int id = (issue.getId() == pair.getFirst() ? pair.getSecond() : pair.getFirst());
					String subject1 = null, priorityS1 = null, date1 = null, assignee1 = null;
					for (Issue issue1 : allIssues) {
						if (issue1.getId() == id) {
							subject1 = issue1.getSubject();
							int priority1 = issue1.getPriorityId();
							priorityS1 = getPriorityString(priority1);
							date1 = issue1.getUpdatedOn().toString();
							assignee1 = (issue1.getAssignee() == null ? "-" : issue1.getAssignee().toString());
						}
					}
					if (id == pair.getSecond() && created1 == false) {
						item2 = new GridItem(item1, SWT.NONE);
						item2.setText("blocks");
						created1 = true;
					}
					if (id == pair.getFirst() && created2 == false) {
						item2 = new GridItem(item1, SWT.NONE);
						item2.setText("blocked by");
						created2 = true;
					}
					GridItem item3 = new GridItem(item2, SWT.NONE);
					item3.setText(0, subject1);
					item3.setText(1, priorityS1);
					item3.setText(2, assignee1);
					item3.setText(3, date1);

				}
			}
			
			created1 = false;
			created2 = false;
			for (Pair<Integer, Integer> pair : duplicates) {
				if (pair.getFirst() == issue.getId() || pair.getSecond() == issue.getId()) {
					int id = (issue.getId() == pair.getFirst() ? pair.getSecond() : pair.getFirst());
					String subject1 = null, priorityS1 = null, date1 = null, assignee1 = null;
					for (Issue issue1 : allIssues) {
						if (issue1.getId() == id) {
							subject1 = issue1.getSubject();
							int priority1 = issue1.getPriorityId();
							priorityS1 = getPriorityString(priority1);
							date1 = issue1.getUpdatedOn().toString();
							assignee1 = (issue1.getAssignee() == null ? "-" : issue1.getAssignee().toString());
						}
					}
					if (id == pair.getSecond() && created1 == false) {
						item2 = new GridItem(item1, SWT.NONE);
						item2.setText("duplicates");
						created1 = true;
					}
					if (id == pair.getFirst() && created2 == false) {
						item2 = new GridItem(item1, SWT.NONE);
						item2.setText("duplicated by");
						created2 = true;
					}
					GridItem item3 = new GridItem(item2, SWT.NONE);
					item3.setText(0, subject1);
					item3.setText(1, priorityS1);
					item3.setText(2, assignee1);
					item3.setText(3, date1);
				}
			}
			
			created1 = false;
			created2 = false;
			for (Pair<Integer, Integer> pair : precedes) {
				if (pair.getFirst() == issue.getId() || pair.getSecond() == issue.getId()) {
					int id = (issue.getId() == pair.getFirst() ? pair.getSecond() : pair.getFirst());
					String subject1 = null, priorityS1 = null, date1 = null, assignee1 = null;
					for (Issue issue1 : allIssues) {
						if (issue1.getId() == id) {
							subject1 = issue1.getSubject();
							int priority1 = issue1.getPriorityId();
							priorityS1 = getPriorityString(priority1);
							date1 = issue1.getUpdatedOn().toString();
							assignee1 = (issue1.getAssignee() == null ? "-" : issue1.getAssignee().toString());
						}
					}
					if (id == pair.getSecond() && created1 == false) {
						item2 = new GridItem(item1, SWT.NONE);
						item2.setText("precedes");
						created1 = true;
					}
					if (id == pair.getFirst() && created2 == false) {
						item2 = new GridItem(item1, SWT.NONE);
						item2.setText("follows");
						created2 = true;
					}
					GridItem item3 = new GridItem(item2, SWT.NONE);
					item3.setText(0, subject1);
					item3.setText(1, priorityS1);
					item3.setText(2, assignee1);
					item3.setText(3, date1);
				}
			}
		}
	}
	
	

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public void createPartControl(final Composite parent) {

		GridLayout l = new GridLayout();
		parent.setLayout(l);

		GridData gd = new GridData();

		gd.horizontalAlignment = SWT.FILL;
		gd.verticalAlignment = SWT.FILL;
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = true;

		final Group connectionGroup = new Group(parent, SWT.NONE);
		connectionGroup.setText("Connection");
		connectionGroup.setLayoutData(gd);

		final Group tasksGroup = new Group(parent, SWT.NONE);
		tasksGroup.setText("Tasks");
		
		final Group creationGroup = new Group(parent, SWT.NONE);
		creationGroup.setText("Create Task");


		gd = new GridData();

		gd.horizontalAlignment = SWT.FILL;
		gd.verticalAlignment = SWT.FILL;
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = true;
		gd.exclude = true;

		tasksGroup.setLayoutData(gd);
		tasksGroup.setVisible(false);
		
		gd = new GridData();

		gd.horizontalAlignment = SWT.FILL;
		gd.verticalAlignment = SWT.FILL;
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = true;
		gd.exclude = true;
		
		creationGroup.setLayoutData(gd);
		creationGroup.setVisible(false);

		GridLayout gl = new GridLayout();
		gl.numColumns = 2;
		connectionGroup.setLayout(gl);
		gl = new GridLayout();
		gl.numColumns = 5;
		creationGroup.setLayout(gl);
		gl = new GridLayout();
		gl.numColumns = 5;
		tasksGroup.setLayout(gl);

		Label addressLabel = new Label(connectionGroup, SWT.NONE);
		addressLabel.setText("Address:");

		gd = new GridData();

		gd.horizontalAlignment = SWT.FILL;
		gd.verticalAlignment = SWT.FILL;
		gd.grabExcessHorizontalSpace = true;

		final Text addressText = new Text(connectionGroup, SWT.NONE);
		addressText.setLayoutData(gd);
		addressText.setText(redmineHost);

		Label keyLabel = new Label(connectionGroup, SWT.NONE);
		keyLabel.setText("API access key:");

		final Text accessKey = new Text(connectionGroup, SWT.NONE);
		accessKey.setLayoutData(gd);
		accessKey.setText(apiAccessKey);

		Button connectButton = new Button(connectionGroup, SWT.PUSH);
		connectButton.setText("Connect");
		gd = new GridData();
		connectButton.setLayoutData(gd);

		Button buttonGantt = new Button(tasksGroup, SWT.PUSH);
		buttonGantt.setText("Gantt");
		buttonGantt.pack();
		buttonGantt.setLayoutData(gd);

		final Button buttonReconnect = new Button(tasksGroup, SWT.PUSH);
		buttonReconnect.setText("Reconnect");
		buttonReconnect.pack();
		buttonReconnect.setLayoutData(gd);
		
		final Button buttonUpdate = new Button(tasksGroup, SWT.PUSH);
		buttonUpdate.setText("Update");
		buttonUpdate.pack();
		buttonUpdate.setLayoutData(gd);
		
		final Button buttonCreate = new Button(tasksGroup, SWT.PUSH);
		buttonCreate.setText("Create Issue");
		buttonCreate.setLayoutData(gd);
		
		final Button buttonDelete = new Button(tasksGroup, SWT.PUSH);
		buttonDelete.setText("Delete Issue");
		buttonDelete.setLayoutData(gd);
		
		gd = new GridData();
		gd.horizontalSpan = 5;
		gd.horizontalAlignment = SWT.FILL;
		gd.grabExcessHorizontalSpace = true;
		gd.verticalAlignment = SWT.FILL;
		gd.grabExcessVerticalSpace = true;
		
		grid = new Grid(tasksGroup, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
	    grid.setHeaderVisible(true);
	    GridColumn col1 = new GridColumn(grid, SWT.NONE);
	    col1.setTree(true);
	    col1.setText("Subject");
	    col1.setWidth(300);
	    GridColumn col2 = new GridColumn(grid, SWT.NONE);
	    col2.setText("Priority");
	    col2.setWidth(100);
	    GridColumn col3 = new GridColumn(grid, SWT.NONE);
	    col3.setText("Assigned to");
	    col3.setWidth(250);
	    GridColumn col4 = new GridColumn(grid, SWT.NONE);
	    col4.setText("Updated");
	    col4.setWidth(250);
	    
	    grid.setLayoutData(gd);
		
		gd = new GridData();

		gd.horizontalAlignment = SWT.FILL;
		gd.verticalAlignment = SWT.FILL;
		gd.horizontalSpan = 1;
		
		Label subjectLabel = new Label(creationGroup, SWT.NONE);
		subjectLabel.setText("Subject");
		subjectLabel.setLayoutData(gd);
		
		gd = new GridData();

		gd.horizontalAlignment = SWT.FILL;
		gd.verticalAlignment = SWT.FILL;
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalSpan = 4;
		
		final Text subjectText = new Text(creationGroup, SWT.NONE);
		subjectText.setLayoutData(gd);
				
		gd = new GridData();

		gd.horizontalAlignment = SWT.FILL;
		gd.verticalAlignment = SWT.FILL;
		gd.horizontalSpan = 1;
		
		Label descriptionLabel = new Label(creationGroup, SWT.NONE);
		descriptionLabel.setText("Description");
		descriptionLabel.setLayoutData(gd);

		gd = new GridData();

		gd.horizontalAlignment = SWT.FILL;
		gd.verticalAlignment = SWT.FILL;
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = true;
		gd.horizontalSpan = 4;
		
		final Text descriptionText = new Text(creationGroup, SWT.BORDER | SWT.V_SCROLL | SWT.WRAP | SWT.MULTI);
		descriptionText.setLayoutData(gd);
		
		gd = new GridData();

		gd.horizontalAlignment = SWT.FILL;
		gd.verticalAlignment = SWT.FILL;
		
		Label priorityLabel = new Label(creationGroup, SWT.NONE);
		priorityLabel.setText("Priority");
		priorityLabel.setLayoutData(gd);
		
		gd = new GridData();

		gd.horizontalAlignment = SWT.FILL;
		gd.verticalAlignment = SWT.FILL;
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalSpan = 1;
		
		final Combo priorityCombo = new Combo(creationGroup, SWT.READ_ONLY);
		priorityCombo.setItems(new String[]{"Low", "Normal", "High", "Urgent", "Immediate"});
		priorityCombo.setLayoutData(gd);
		priorityCombo.setText("Normal");
		
		gd = new GridData();

		gd.horizontalAlignment = SWT.FILL;
		gd.verticalAlignment = SWT.FILL;
		
		Label assigneeLabel = new Label(creationGroup, SWT.NONE);
		assigneeLabel.setText("Assigned to");
		assigneeLabel.setLayoutData(gd);
		
		gd = new GridData();

		gd.horizontalAlignment = SWT.FILL;
		gd.verticalAlignment = SWT.FILL;
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalSpan = 2;
		
		final Combo assigneeCombo = new Combo(creationGroup, SWT.READ_ONLY);
		assigneeCombo.setLayoutData(gd);
		
		final Button buttonCreateIssue = new Button(creationGroup, SWT.PUSH);
		buttonCreateIssue.setText("Create Issue");
		
		final Button buttonCancel = new Button(creationGroup, SWT.PUSH);
		buttonCancel.setText("Cancel");
		
		

		Listener bConnectListener = new Listener() {

			@Override
			public void handleEvent(Event event) {
				redmineHost = addressText.getText();
				apiAccessKey = accessKey.getText();
				mgr = new RedmineManager(redmineHost, apiAccessKey);
				GridData gd = (GridData) connectionGroup.getLayoutData();
				gd.exclude = true;
				connectionGroup.setVisible(false);
				
				gd = (GridData) creationGroup.getLayoutData();
				gd.exclude = true;
				creationGroup.setVisible(false);
				
				gd = (GridData) tasksGroup.getLayoutData();
				gd.exclude = false;
				tasksGroup.setVisible(true);
				
				parent.layout(true);
				
				updateIssues();
			}
		};

		connectButton.addListener(SWT.Selection, bConnectListener);

		Listener bGanttListener = new Listener() {
			@Override
			public void handleEvent(Event event) {
				HashSet<Pair<Integer, Integer>> relations = new HashSet<Pair<Integer,Integer>>();
				HashSet<Pair<Integer, Integer>> relations1 = new HashSet<Pair<Integer,Integer>>();
				for (Pair<Integer, Integer> pair : relates) {
					relations1.add(pair);
				}
				for (Pair<Integer, Integer> pair : blocks) {
					relations.add(pair);
				}
				for (Pair<Integer, Integer> pair : duplicates) {
					relations.add(pair);
				}
				for (Pair<Integer, Integer> pair : precedes) {
					relations.add(pair);
				}
				final GanttDiagram demo = new GanttDiagram(allIssues, relations, relations1);
			}
		};
		
		buttonGantt.addListener(SWT.Selection, bGanttListener);

		Listener bReconnectListener = new Listener() {

			@Override
			public void handleEvent(Event event) {
				GridData gd = (GridData) connectionGroup.getLayoutData();
				gd.exclude = false;
				connectionGroup.setVisible(true);

				gd = (GridData) tasksGroup.getLayoutData();
				gd.exclude = true;
				tasksGroup.setVisible(false);
				
				gd = (GridData) creationGroup.getLayoutData();
				gd.exclude = true;
				creationGroup.setVisible(false);

				parent.layout(true);
			}

		};

		buttonReconnect.addListener(SWT.Selection, bReconnectListener);
		
		Listener bCreateListener = new Listener() {
			
			@Override
			public void handleEvent(Event event) {
				/*
				 * ArrayList<User> users = null;
				try {
					users = (ArrayList<User>) mgr.getUsers();
				} catch (RedmineException e) {
					e.printStackTrace();
				}
				String[] usersS = new String[users.size()];
				int i = 0;
				for (User user : users) {
					usersS[i++] = user.getFullName();
				}
				
				assigneeCombo.setItems(usersS);
				assigneeCombo.setText(users.get(0).getFullName());
				 */
				assigneeCombo.setItems(new String[]{"Redmine Admin"});
				assigneeCombo.setText("Redmine Admin");
				
				GridData gd = (GridData) connectionGroup.getLayoutData();
				gd.exclude = true;
				connectionGroup.setVisible(false);

				gd = (GridData) tasksGroup.getLayoutData();
				gd.exclude = true;
				tasksGroup.setVisible(false);
				
				gd = (GridData) creationGroup.getLayoutData();
				gd.exclude = false;
				creationGroup.setVisible(true);
				
				parent.layout(true);
			}
		};
		
		buttonCreate.addListener(SWT.Selection, bCreateListener);
		
		Listener bCreateIssueListener = new Listener() {
			
			@Override
			public void handleEvent(Event event) {
				if (!subjectText.getText().isEmpty()) {
					GridData gd = (GridData) connectionGroup.getLayoutData();
					gd.exclude = true;
					connectionGroup.setVisible(false);

					gd = (GridData) tasksGroup.getLayoutData();
					gd.exclude = false;
					tasksGroup.setVisible(true);

					gd = (GridData) creationGroup.getLayoutData();
					gd.exclude = true;
					creationGroup.setVisible(false);

					String priority = priorityCombo.getText();
					int priorityId = 0;
					if (priority == "Low")
						priorityId = 3;
					if (priority == "Normal")
						priorityId = 4;
					if (priority == "High")
						priorityId = 5;
					if (priority == "Urgent")
						priorityId = 6;
					if (priority == "Immediate")
						priorityId = 7;

					Issue issue = new Issue();
					issue.setSubject(subjectText.getText());
					issue.setPriorityId(priorityId);
					issue.setDescription(descriptionText.getText());
					try {
						mgr.createIssue(projectKey, issue);
					} catch (RedmineException e) {
						e.printStackTrace();
					}

					updateIssues();
				}
			}
		};
		
		buttonCreateIssue.addListener(SWT.Selection, bCreateIssueListener);
		
		Listener bDeleteListener = new Listener() {
			
			@Override
			public void handleEvent(Event event) {
				GridItem[] items = grid.getSelection();
				if (items[0].getText(0) != "Tasks") {
					for (Issue issue : allIssues) {
						if (issue.getSubject().equals(items[0].getText(0)))
							try {
								mgr.deleteIssue(issue.getId());
							} catch (RedmineException e) {
								e.printStackTrace();
							}
					}
				}
				updateIssues();
			}
		};
		
		buttonDelete.addListener(SWT.Selection, bDeleteListener);
		
		Listener bUpdateListener = new Listener() {
			
			@Override
			public void handleEvent(Event event) {
				updateIssues();
			}
		};
		
		buttonUpdate.addListener(SWT.Selection, bUpdateListener);
		
		Listener bCancelListener = new Listener() {
			
			@Override
			public void handleEvent(Event event) {
				GridData gd = (GridData) connectionGroup.getLayoutData();
				gd.exclude = true;
				connectionGroup.setVisible(false);

				gd = (GridData) tasksGroup.getLayoutData();
				gd.exclude = false;
				tasksGroup.setVisible(true);
				
				gd = (GridData) creationGroup.getLayoutData();
				gd.exclude = true;
				creationGroup.setVisible(false);
				updateIssues();
			}
		};
		
		buttonCancel.addListener(SWT.Selection, bCancelListener);
		
		
		makeActions();
		hookContextMenu();
		contributeToActionBars();
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				Redminer.this.fillContextMenu(manager);
			}
		});
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(action1);
		manager.add(new Separator());
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(action1);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(action1);
	}

	private void makeActions() {
		action1 = new Action() {
			public void run() {
				HashSet<Pair<Integer, Integer>> relations = new HashSet<Pair<Integer,Integer>>();
				HashSet<Pair<Integer, Integer>> relations1 = new HashSet<Pair<Integer,Integer>>();
				for (Pair<Integer, Integer> pair : relates) {
					relations1.add(pair);
				}
				for (Pair<Integer, Integer> pair : blocks) {
					relations.add(pair);
				}
				for (Pair<Integer, Integer> pair : duplicates) {
					relations.add(pair);
				}
				for (Pair<Integer, Integer> pair : precedes) {
					relations.add(pair);
				}
				final GanttDiagram demo = new GanttDiagram(allIssues, relations, relations1);
			}
		};
		action1.setText("Show Gantt Diagram");
		action1.setToolTipText("Gantt Diagram");
		action1.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
//		tree.setFocus();
	}
}