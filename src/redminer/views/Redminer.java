package redminer.views;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

import org.eclipse.nebula.widgets.ganttchart.GanttChart;
import org.eclipse.nebula.widgets.ganttchart.GanttEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TreeEditor;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.part.ViewPart;

import redminer.API;

import com.taskadapter.redmineapi.RedmineException;
import com.taskadapter.redmineapi.RedmineManager;
import com.taskadapter.redmineapi.bean.Issue;
import com.taskadapter.redmineapi.bean.IssueRelation;
import com.taskadapter.redmineapi.bean.User;

public class Redminer extends ViewPart {
	public static final String ID = "redminer.views.Redminer";
	private static String projectAddress = "http://localhost/redmine/projects/test";
	public static String redmineHost;
	public static String apiAccessKey = "6e2aba71da02acd7890484201b846ab6682ed52a";
	private static String login, password;
	private static String projectKey = "test";
	private static RedmineManager mgr;
	private static ArrayList<Issue> allIssues = new ArrayList<Issue>();
	private static ArrayList<Integer> numberIssue = new ArrayList<Integer>();
	private static ArrayList<GanttEvent> events = new ArrayList<GanttEvent>();
	private static HashMap<Integer, Issue> issues = new HashMap<Integer, Issue>();
	private static ArrayList<String> relations = new ArrayList<String>();
	private static Tree tree;
	public static GanttChart ganttChart;
	public static Group connectionGroup;
	public static Group tasksGroup;
	public static Group creationGroup;
	public static Composite composite;
	private final String SUBJECT = "Название";
	private final String PRIORITY = "Приоритет";
	private final String UPDATED = "Последнее обновление";
	private final String NUMBER = "№";
	private final static String PRIORITY_LOW = "Низкий";
	private final static String PRIORITY_NORMAL = "Нормальный";
	private final static String PRIORITY_HIGH = "Высокий";
	private final static String PRIORITY_URGENT = "Срочный";
	private final static String PRIORITY_IMMEDIATE = "Немедленный";
	private final String CREATE_ISSUE = "Создать задачу";
	private final String CONNECT = "Подключиться";
	private final String CONNECTION = "Подключение";
	private final String ISSUES = "Задачи";
	private final String ADDRESS = "Адрес проекта:";
	private final String API_ACCESS_KEY = "Ключ доступа API:";
	private final String DESCRIPTION = "Описание";
	private final String CANCEL = "Отмена";
	private final static String BLOCKED_BY = "заблокирована";
	private final static String BLOCKS = "блокирует";
	private final static String RELATED_TO = "связана с";
	private final static String DUPLICATED_BY = "дублирована";
	private final static String DUPLICATES = "дублирует";
	private final static String PRECEDES = "предшествует";
	private final static String FOLLOWS = "следует за";
	private final static String CONNECT_WITH_ISSUE = "Соединить с задачей";
	private final static String DELETE_ISSUE = "Удалить задачу";
	private final static String SUBTASK = "подзадача";
	private final String RELATION = "Отношение";
//	private final String CONNECTION_ERROR = "Ошибка подключения";
//	private final String CONNECTION_ERROR_MESSAGE = "Невозможно подключиться к заданному серверу с заданным API ключом";
	
	public Redminer() {}
	
	static void createParentNodes(TreeItem root) {
		HashSet<Integer> related_to = new HashSet<Integer>(),
				 precedes = new HashSet<Integer>(),
				 follows = new HashSet<Integer>(),
				 duplicates = new HashSet<Integer>(),
				 duplicated_by = new HashSet<Integer>(),
				 blocks = new HashSet<Integer>(),
				 blocked_by = new HashSet<Integer>(),
				 subTasks = new HashSet<Integer>();
		
		Issue issue = issues.get(Integer.parseInt(root.getText(1)));
		
		for (Issue issue1 : allIssues)
			if (issue.getId().equals(issue1.getParentId()))
				subTasks.add(issue1.getId());
		
		createNodes(root, subTasks, SUBTASK);
		
		ArrayList<IssueRelation> relis = (ArrayList<IssueRelation>) issue.getRelations();
		for (IssueRelation issueRelation : relis) {
			if (issueRelation.getType().equals("relates"))
				if (issueRelation.getIssueId() == issue.getId())
					related_to.add(issueRelation.getIssueToId());
				else
					related_to.add(issueRelation.getIssueId());
			if (issueRelation.getType().equals("blocks")) {
				if (issueRelation.getIssueId() == issue.getId())
					blocks.add(issueRelation.getIssueToId());
				else
					blocked_by.add(issueRelation.getIssueId());
			}
			if (issueRelation.getType().equals("duplicates")) {
				if (issueRelation.getIssueId() == issue.getId())
					duplicates.add(issueRelation.getIssueToId());
				else
					duplicated_by.add(issueRelation.getIssueId());
			}
			if (issueRelation.getType().equals("precedes")) {
				if (issueRelation.getIssueId() == issue.getId())
					precedes.add(issueRelation.getIssueToId());
				else
					follows.add(issueRelation.getIssueId());
			}
		}
		
		createNodes(root, blocked_by, BLOCKED_BY);
		createNodes(root, blocks, BLOCKS);
		createNodes(root, related_to, RELATED_TO);
		createNodes(root, duplicated_by, DUPLICATED_BY);
		createNodes(root, duplicates, DUPLICATES);
		createNodes(root, precedes, PRECEDES);
		createNodes(root, follows, FOLLOWS);
	}
	
	static void createNodes(TreeItem root, HashSet<Integer> set, String s) {
		for (Integer toId : set) {
			TreeItem item3 = new TreeItem(root, SWT.NONE);
			Issue issueTo = issues.get(toId);
			item3.setText(0, issueTo.getSubject());
			item3.setText(1, String.valueOf(toId));
			item3.setText(2, s);
			item3.setText(3, issueTo.getPriorityText());
//			item3.setText(4, issueTo.getUpdatedOn().toString());
		}
		set.clear();
	}

	public static void updateIssues() {
		allIssues.clear();
		issues.clear();
		numberIssue.clear();
		try {
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("include", "relations");
//			allIssues = API.getIssues(redmineHost, apiAccessKey);
			allIssues = (ArrayList<Issue>) mgr.getIssues(params);
		} catch (Exception e) {
			e.printStackTrace();
		}

		for (Issue issue : allIssues)
			issues.put(issue.getId(), issue);
		tree.removeAll();
		ganttChart.getGanttComposite().clearGanttEvents();
		
		while (numberIssue.size() != allIssues.size()) {
			for (Issue issue : allIssues) 
				if (!numberIssue.contains(issue.getId()))
					if (issue.getParentId() == null || numberIssue.contains(issue.getParentId())) {
						numberIssue.add(issue.getId());
						TreeItem item = new TreeItem(tree, SWT.NONE);
						String subject = issue.getSubject();
						String priorityS = issue.getPriorityText();
						String date = issue.getUpdatedOn().toString();
						item.setText(0, subject);
						item.setText(1, String.valueOf(issue.getId()));
						item.setText(3, priorityS);
						item.setText(4, date);
						createParentNodes(item);
					}
		}
		updateEvents();
	}
	
	private static String getPriorityText(Issue issue) {
		switch(issue.getPriorityId()){
		case 3: return PRIORITY_LOW;
		case 4: return PRIORITY_NORMAL;
		case 5: return PRIORITY_HIGH;
		case 6: return PRIORITY_URGENT;
		case 7: return PRIORITY_IMMEDIATE;
		default: return "";
		}
	}

	public static int getHeight(TreeItem root) {
		int res = 24;
		if (root.getExpanded()) {
			TreeItem[] items = root.getItems();
			for (TreeItem item : items)
				res += getHeight(item);
		}
		return res;
	}
	
	public static void updateHeights() {
		TreeItem[] treeItems = tree.getItems();
		int[] heights = new int[treeItems.length];
		for (int i = 0; i < heights.length; ++i)
			heights[i] = getHeight(treeItems[i]);
		for (int i = 0; i < events.size(); ++i) {
			GanttEvent event = events.get(i);
			event.setFixedRowHeight(heights[i]);
			events.set(i, event);
		}
		ganttChart.redrawGanttChart();
		updateMenu();
	}
	
	public static void updateEvents() {
		ganttChart.getGanttComposite().clearGanttEvents();
		events.clear();
		
		for (int i = 0; i < numberIssue.size(); ++i) {
			int n = numberIssue.get(i);
			Issue issue = issues.get(n);
			Date start = issue.getStartDate() == null ? issue.getUpdatedOn() : issue.getStartDate();
			Date end  = issue.getDueDate() == null ? new Date() : issue.getDueDate();
			Calendar st = Calendar.getInstance();
			Calendar en = Calendar.getInstance();
			st.setTime(start);
			en.setTime(end);
			events.add(new GanttEvent(ganttChart, issue.getSubject(), st, en, issue.getDoneRatio()));
		}
		
		for (int i = 0; i < numberIssue.size(); ++i) {
			ArrayList<IssueRelation> relis = (ArrayList<IssueRelation>) issues.get(numberIssue.get(i)).getRelations();
			for (IssueRelation issueRelation : relis) {
				int iTo = numberIssue.indexOf(issueRelation.getIssueToId());
				ganttChart.addConnection(events.get(i), events.get(iTo));
				ganttChart.addConnection(events.get(iTo), events.get(i));
			}
		}
		ganttChart.redrawGanttChart();
	}
	
	public static void updateMenu() {
		final Menu menu = new Menu(tree);
		tree.setMenu(menu);
		MenuItem conItem = new MenuItem(menu, SWT.CASCADE);
		conItem.setText(CONNECT_WITH_ISSUE);
		MenuItem delItem = new MenuItem(menu, SWT.NONE);
		delItem.setText(DELETE_ISSUE);
		delItem.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				TreeItem[] items = tree.getSelection();
				String idToDelete = items[0].getText(1);
				int idDel = Integer.parseInt(idToDelete);
				for (Issue issue : allIssues)
					if (issue.getId().equals(idDel))
						try {
							mgr.deleteIssue(issue.getId());
						} catch (RedmineException e) {
							e.printStackTrace();
						}
				updateIssues();
				updateEvents();
				updateHeights();
			}
		});
		/*
		TreeItem selectedItem = tree.getSelection()[0];
		TreeItem[] selItems = selectedItem.getItems();
		ArrayList<String> nums = new ArrayList<String>();
		for (TreeItem treeItem : selItems)
			nums.add(treeItem.getText(1));
		nums.add(selectedItem.getText(1));
		*/
		Menu conMenu =  new Menu(menu);
		conItem.setMenu(conMenu);
		TreeItem[] items = tree.getItems();
		for (final TreeItem treeItem : items) {
//			if (nums.contains(treeItem.getText(1))) continue;
			final MenuItem issueItem = new MenuItem(conMenu, SWT.CASCADE);
			issueItem.setText("№" + treeItem.getText(1) + " " + treeItem.getText(0));
			Menu issueMenu = new Menu(conMenu);
			issueItem.setMenu(issueMenu);
			for (String s : relations) {
				MenuItem item = new MenuItem(issueMenu, SWT.PUSH);
				item.setText(s);
				String rel = null;
				if (s.equals(RELATED_TO)) rel = "relates";
				else if (s.equals(BLOCKED_BY)) rel = "blocked";
				else if (s.equals(BLOCKS)) rel = "blocks";
				else if (s.equals(DUPLICATED_BY)) rel = "duplicated";
				else if (s.equals(DUPLICATES)) rel = "duplicates";
				else if (s.equals(PRECEDES)) rel = "precedes";
				else if (s.equals(FOLLOWS)) rel = "follows";
				else if (s.equals(SUBTASK)) rel = "subtask";
				final String res = rel;
				item.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event event) {
						if (!res.equals("subtask"))
							try {
								API.createRelation(redmineHost, apiAccessKey, login, password, Integer.parseInt(tree.getSelection()[0].getText(1)), Integer.parseInt(treeItem.getText(1)), res);
							} catch (Exception e) {
								e.printStackTrace();
							}
						else {
							Issue issue = issues.get(Integer.parseInt(treeItem.getText(1))); 
							issue.setParentId(Integer.parseInt(tree.getSelection()[0].getText(1)));
							try {
								mgr.update(issue);
							} catch (RedmineException e) {
								e.printStackTrace();
							}
						}
						updateIssues();
					}
				});
			}
		}
	}

	public void createPartControl(final Composite parent) {
		composite = new Composite(parent, SWT.NONE);

		relations.add(RELATED_TO);
		relations.add(BLOCKED_BY);
		relations.add(BLOCKS);
		relations.add(PRECEDES);
		relations.add(FOLLOWS);
		relations.add(DUPLICATED_BY);
		relations.add(DUPLICATES);
		relations.add(SUBTASK);
		
		GridLayout l = new GridLayout();
		composite.setLayout(l);

		GridData gd = new GridData();

		gd.horizontalAlignment = SWT.FILL;
		gd.verticalAlignment = SWT.FILL;
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = true;

		connectionGroup = new Group(composite, SWT.NONE);
		connectionGroup.setText(CONNECTION);
		connectionGroup.setLayoutData(gd);

		tasksGroup = new Group(composite, SWT.NONE);
		tasksGroup.setText(ISSUES);
		
		creationGroup = new Group(composite, SWT.NONE);
		creationGroup.setText(CREATE_ISSUE);


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
		gl.numColumns = 4;
		creationGroup.setLayout(gl);
		gl = new GridLayout(2, true);
		tasksGroup.setLayout(gl);
		
		gd = new GridData();

		gd.horizontalAlignment = SWT.FILL;
		gd.verticalAlignment = SWT.FILL;
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalSpan = 2;

		Group authGroup = new Group(connectionGroup, SWT.NONE);
		authGroup.setLayout(new RowLayout(SWT.VERTICAL));
		authGroup.setText("Способ аутентификации");
		authGroup.setLayoutData(gd);
		
		Button lp = new Button(authGroup, SWT.RADIO);
		lp.setText("Логин/пароль");
		lp.setSelection(true);
		
		final Button aak = new Button(authGroup, SWT.RADIO);
		aak.setText("API access key");

		Label addressLabel = new Label(connectionGroup, SWT.NONE);
		addressLabel.setText(ADDRESS);

		gd = new GridData();

		gd.horizontalAlignment = SWT.FILL;
		gd.verticalAlignment = SWT.FILL;
		gd.grabExcessHorizontalSpace = true;

		final Text addressText = new Text(connectionGroup, SWT.NONE);
		addressText.setLayoutData(gd);
		addressText.setText(projectAddress);

		gd = new GridData();

		gd.verticalAlignment = SWT.FILL;
		gd.exclude = true;
		
		final Label keyLabel = new Label(connectionGroup, SWT.NONE);
		keyLabel.setLayoutData(gd);
		keyLabel.setVisible(false);
		keyLabel.setText(API_ACCESS_KEY);
		
		gd = new GridData();
		gd.horizontalAlignment = SWT.FILL;
		gd.verticalAlignment = SWT.FILL;
		gd.grabExcessHorizontalSpace = true;
		gd.exclude = true;

		
		final Text accessKey = new Text(connectionGroup, SWT.NONE);
		accessKey.setLayoutData(gd);
		accessKey.setText(apiAccessKey);
		accessKey.setVisible(false);
		
		gd = new GridData();

		gd.horizontalAlignment = SWT.FILL;
		gd.verticalAlignment = SWT.FILL;
		gd.grabExcessHorizontalSpace = true;
		
		final Label loginLabel = new Label(connectionGroup, SWT.NONE);
//		loginLabel.setLayoutData(gd);
		loginLabel.setText("Логин:");

		final Text loginText = new Text(connectionGroup, SWT.NONE);
		loginText.setLayoutData(gd);
		loginText.setText("admin");
		
		final Label passLabel = new Label(connectionGroup, SWT.NONE);
//		passLabel.setLayoutData(gd);
		passLabel.setText("Пароль:");

		final Text passText = new Text(connectionGroup, SWT.NONE);
		passText.setLayoutData(gd);
		passText.setText("admin");
		
		lp.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				GridData gd = (GridData)loginLabel.getLayoutData();
				gd.exclude = false;
				loginLabel.setVisible(true);
				
				gd = (GridData)loginText.getLayoutData();
				gd.exclude = false;
				loginText.setVisible(true);
				
				gd = (GridData)passLabel.getLayoutData();
				gd.exclude = false;
				passLabel.setVisible(true);
				
				gd = (GridData)passText.getLayoutData();
				gd.exclude = false;
				passText.setVisible(true);
				
				gd = (GridData)accessKey.getLayoutData();
				gd.exclude = true;
				accessKey.setVisible(false);
				
				gd = (GridData)keyLabel.getLayoutData();
				gd.exclude = true;
				keyLabel.setVisible(false);
				connectionGroup.layout(true);
			}
		});
		
		aak.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				GridData gd = (GridData)loginLabel.getLayoutData();
				gd.exclude = true;
				loginLabel.setVisible(false);
				
				gd = (GridData)loginText.getLayoutData();
				gd.exclude = true;
				loginText.setVisible(false);
				
				gd = (GridData)passLabel.getLayoutData();
				gd.exclude = true;
				passLabel.setVisible(false);
				
				gd = (GridData)passText.getLayoutData();
				gd.exclude = true;
				passText.setVisible(false);
				
				gd = (GridData)accessKey.getLayoutData();
				gd.exclude = false;
				accessKey.setVisible(true);
				
				gd = (GridData)keyLabel.getLayoutData();
				gd.exclude = false;
				keyLabel.setVisible(true);
				connectionGroup.layout(true);
			}
		});
		
		

		Button connectButton = new Button(connectionGroup, SWT.PUSH);
		connectButton.setText(CONNECT);
		gd = new GridData();
		connectButton.setLayoutData(gd);

		
		gd = new GridData();
		gd.horizontalSpan = 2;
		gd.horizontalAlignment = SWT.FILL;
		gd.verticalAlignment = SWT.FILL;
		Composite t = new Composite(tasksGroup, SWT.NONE);
		t.setLayoutData(gd);
		t.setLayout(new RowLayout());
		gd = new GridData();
		gd.grabExcessHorizontalSpace = true;
		gd.verticalAlignment = SWT.FILL;
		gd.grabExcessVerticalSpace = true;
		
		tree = new Tree(tasksGroup, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
	    tree.setHeaderVisible(true);
	    
	    TreeColumn col0 = new TreeColumn(tree, SWT.NONE);
	    col0.setText(SUBJECT);
	    col0.setWidth(200);
	    TreeColumn col1 = new TreeColumn(tree, SWT.NONE);
	    col1.setText(NUMBER);
	    col1.setWidth(50);
	    TreeColumn col2 = new TreeColumn(tree, SWT.NONE);
	    col2.setText(RELATION);
	    col2.setWidth(200);
	    TreeColumn col3 = new TreeColumn(tree, SWT.NONE);
	    col3.setText(PRIORITY);
	    col3.setWidth(100);
	    TreeColumn col4 = new TreeColumn(tree, SWT.NONE);
	    col4.setText(UPDATED);
	    col4.setWidth(200);
	    
	    tree.setLayoutData(gd);

	    gd = new GridData();

		gd.horizontalAlignment = SWT.FILL;
		gd.verticalAlignment = SWT.FILL;
	    
		ganttChart = new GanttChart(tasksGroup, SWT.NONE);
	    ganttChart.setLayoutData(gd);
	    
		gd = new GridData();

		gd.horizontalAlignment = SWT.FILL;
		gd.verticalAlignment = SWT.FILL;
		gd.horizontalSpan = 1;
		
		Label subjectLabel = new Label(creationGroup, SWT.NONE);
		subjectLabel.setText(SUBJECT);
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
		descriptionLabel.setText(DESCRIPTION);
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
		priorityLabel.setText(PRIORITY);
		priorityLabel.setLayoutData(gd);
		
		gd = new GridData();

		gd.horizontalAlignment = SWT.FILL;
		gd.verticalAlignment = SWT.FILL;
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalSpan = 1;
		
		final Combo priorityCombo = new Combo(creationGroup, SWT.READ_ONLY);
		priorityCombo.setItems(new String[]{PRIORITY_LOW, PRIORITY_NORMAL, PRIORITY_HIGH, PRIORITY_URGENT, PRIORITY_IMMEDIATE});
		priorityCombo.setLayoutData(gd);
		priorityCombo.setText(PRIORITY_NORMAL);
		
		gd = new GridData();

		gd.horizontalAlignment = SWT.FILL;
		gd.verticalAlignment = SWT.FILL;
		
		gd = new GridData();

		gd.horizontalAlignment = SWT.FILL;
		gd.verticalAlignment = SWT.FILL;
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalSpan = 2;
		
		final Button buttonCreateIssue = new Button(creationGroup, SWT.PUSH);
		buttonCreateIssue.setText(CREATE_ISSUE);
		
		final Button buttonCancel = new Button(creationGroup, SWT.PUSH);
		buttonCancel.setText(CANCEL);
		
		Transfer[] types = new Transfer[] { TextTransfer.getInstance() };
		final TreeItem dragItem[] = new TreeItem[1];
		DragSource source = new DragSource(tree, DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_LINK);
		source.setTransfer(types);
		source.addDragListener(new DragSourceListener() {
			
			@Override
			public void dragStart(DragSourceEvent event) {
				if (tree.getSelectionCount() > 1)
					event.doit = false;
				else {
					dragItem[0] = tree.getSelection()[0];
					event.doit = true;
				}
			}
			
			@Override
			public void dragSetData(DragSourceEvent event) {
				event.data = tree.getSelection()[0].getText(1);
			}
			
			@Override
			public void dragFinished(DragSourceEvent event) {
				dragItem[0] = null;
			}
		});
		
		DropTarget target = new DropTarget(tree, DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_LINK);
		target.setTransfer(types);		
		target.addDropListener(new DropTargetAdapter() {
			public void drop(DropTargetEvent event){
				Issue draggedIssue = issues.get(Integer.parseInt((String)event.data));
				TreeItem item = (TreeItem)event.item;
				TreeItem[] childItems = item.getItems();
				boolean out = false;
				for (TreeItem treeItem : childItems)
					if (treeItem.getText(1).equals(draggedIssue.getId().toString())) out = true;
				if (!out && (Integer.parseInt(((TreeItem)event.item).getText(1)) != draggedIssue.getId())) {
					String[] itemText = new String[tree.getColumnCount()];
					String rel = new RelationDialog().showDialog(draggedIssue, issues.get(Integer.parseInt(((TreeItem)event.item).getText(1))));
					if (!rel.equals("no")) {
						TreeItem newItem = new TreeItem((TreeItem)event.item, SWT.NONE);
						String rel1 = null;
						if (rel.equals("relates")) rel1 = RELATED_TO;
						else if (rel.equals("blocked")) rel1 = BLOCKED_BY;
						else if (rel.equals("blocks")) rel1 = BLOCKS;
						else if (rel.equals("duplicated")) rel1 = DUPLICATED_BY;
						else if (rel.equals("duplicates")) rel1 = DUPLICATES;
						else if (rel.equals("precedes")) rel1 = PRECEDES;
						else if (rel.equals("follows")) rel1 = FOLLOWS;
						else if (rel.equals("subtask")) rel1 = SUBTASK;
						itemText[0] = draggedIssue.getSubject();
						itemText[1] = String.valueOf(draggedIssue.getId());
						itemText[2] = rel1;
						itemText[3] = draggedIssue.getPriorityText();
						itemText[4] = draggedIssue.getUpdatedOn().toString();
						if (rel.equals("subtask")) {
							draggedIssue.setParentId(Integer.parseInt(((TreeItem)event.item).getText(1)));
							try {
								mgr.update(draggedIssue);
							} catch (RedmineException e) {
								e.printStackTrace();
							}
						}
						else {
							try {
								API.createRelation(redmineHost, apiAccessKey, login, password, draggedIssue.getId(), Integer.parseInt(((TreeItem)event.item).getText(1)), rel);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						newItem.setText(itemText);
						
						
						TreeItem items[] = tree.getItems();
						TreeItem parentItem = null;
						for (TreeItem treeItem : items) {
							if (treeItem.getText(1).equals(draggedIssue.getId().toString())) {parentItem = treeItem; break;} 
						}
						TreeItem newItem1 = new TreeItem(parentItem, SWT.NONE);
						if (rel.equals("relates")) rel = RELATED_TO;
						else if (rel.equals("blocked")) rel = BLOCKS;
						else if (rel.equals("blocks")) rel = BLOCKED_BY;
						else if (rel.equals("duplicated")) rel = DUPLICATES;
						else if (rel.equals("duplicates")) rel = DUPLICATED_BY;
						else if (rel.equals("precedes")) rel = FOLLOWS;
						else if (rel.equals("follows")) rel = PRECEDES;
						itemText[0] = item.getText(0);
						itemText[1] = item.getText(1);
						itemText[2] = rel;
						itemText[3] = item.getText(3);
						itemText[4] = item.getText(4);
						newItem1.setText(itemText);
						
						createParentNodes(newItem);
						createParentNodes(newItem1);
						
						Display.getCurrent().asyncExec(new Runnable() {
							@Override
							public void run() {
								updateEvents();
								updateHeights();
							}
						});
					}
				}
			};
		});
		
		
		tree.addListener(SWT.Expand, new Listener() {
			@Override
			public void handleEvent(Event event) {
				Display d = Display.getCurrent();
				TreeItem item = (TreeItem)event.item;
				TreeItem[] items = item.getItems();
				for (TreeItem treeItem : items)
					createParentNodes(treeItem);
				d.asyncExec(new Runnable() {
					@Override
					public void run() {
						updateEvents();
						updateHeights();
					}
				});
			}
		});
		tree.addListener(SWT.Collapse, new Listener() {
			@Override
			public void handleEvent(Event event) {
				Display d = Display.getCurrent();
				TreeItem item = (TreeItem)event.item;
				TreeItem[] items = item.getItems();
				for (TreeItem treeItem : items) {
					TreeItem[] items1 = treeItem.getItems();
					for (TreeItem treeItem1 : items1) {
						treeItem1.dispose();
					}
				}
				d.asyncExec(new Runnable() {
					@Override
					public void run() {
						updateEvents();
						updateHeights();
					}
				});
			}
		});
		
		tree.addTraverseListener(new TraverseListener() {
			
			@Override
			public void keyTraversed(TraverseEvent e) {
				if (e.detail == 32) System.out.println("ctrl <-");
				if (e.detail == 64) System.out.println("ctrl ->");
			}
		});
		
	    final TreeEditor editor = new TreeEditor(tree);
	    editor.horizontalAlignment = SWT.LEFT;
	    editor.grabHorizontal = true;
	    
	    tree.addKeyListener(new KeyAdapter() {
	      public void keyPressed(KeyEvent event) {
	        if (event.keyCode == SWT.F2 && tree.getSelectionCount() == 1) {
	          final TreeItem item = tree.getSelection()[0];

	          final Text text = new Text(tree, SWT.NONE);
	          text.setText(item.getText());
	          text.selectAll();
	          text.setFocus();

	          text.addFocusListener(new FocusAdapter() {
	            public void focusLost(FocusEvent event) {
	              item.setText(text.getText());
	              Issue issue = issues.get(Integer.parseInt(item.getText(1)));
	              issue.setSubject(text.getText());
	              try {
					mgr.update(issue);
				} catch (RedmineException e) {
					e.printStackTrace();
				}
	              text.dispose();
	            }
	          });

	          text.addKeyListener(new KeyAdapter() {
	            public void keyPressed(KeyEvent event) {
	              switch (event.keyCode) {
	              case SWT.CR:
	                item.setText(text.getText());
	                Issue issue = issues.get(Integer.parseInt(item.getText(1)));
		            issue.setSubject(text.getText());
		            try {
						mgr.update(issue);
					} catch (RedmineException e) {
						e.printStackTrace();
					}
	              case SWT.ESC:
	                text.dispose();
	                break;
	              }
	            }
	          });

	          editor.setEditor(text, item);
	        }
	        else if (event.keyCode == SWT.F3 && tree.getSelectionCount() == 1) {
	        	TreeItem item = tree.getSelection()[0];
				Issue issue = issues.get(Integer.parseInt(item.getText(1)));
				IssueDialog d = new IssueDialog();
				d.open(redmineHost, apiAccessKey, login, password, issue, issues, numberIssue, mgr);
	        }
	      }
	    });
		
		
		
		Listener bConnectListener = new Listener() {

			@Override
			public void handleEvent(Event event) {
				projectAddress = addressText.getText();
				redmineHost = projectAddress.substring(0, projectAddress.substring(0, projectAddress.lastIndexOf('/')).lastIndexOf('/'));
				if (aak.getSelection() == true)
					apiAccessKey = accessKey.getText();
				else {
					login = loginText.getText();
					password = passText.getText();
				}
				
				mgr = new RedmineManager(projectAddress, apiAccessKey);
				
				GridData gd = (GridData) connectionGroup.getLayoutData();
				gd.exclude = true;
				connectionGroup.setVisible(false);
				
				gd = (GridData) creationGroup.getLayoutData();
				gd.exclude = true;
				creationGroup.setVisible(false);
				
				gd = (GridData) tasksGroup.getLayoutData();
				gd.exclude = false;
				tasksGroup.setVisible(true);
				
				composite.layout(true);

				try {
					ArrayList<User> users = (ArrayList<User>) mgr.getUsers();
					for (User user : users) {
						System.out.println(user);
					}
				} catch (RedmineException e) {
					e.printStackTrace();
				}
				
				
				updateIssues();
				updateEvents();
				updateMenu();
			}
		};

		connectButton.addListener(SWT.Selection, bConnectListener);

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
					if (priority == PRIORITY_LOW)
						priorityId = 3;
					if (priority == PRIORITY_NORMAL)
						priorityId = 4;
					if (priority == PRIORITY_HIGH)
						priorityId = 5;
					if (priority == PRIORITY_URGENT)
						priorityId = 6;
					if (priority == PRIORITY_IMMEDIATE)
						priorityId = 7;

					Issue issue = new Issue();
					issue.setSubject(subjectText.getText());
					issue.setPriorityId(priorityId);
					issue.setDescription(descriptionText.getText());
					issue.setStartDate(new Date());
					try {
						mgr.createIssue(projectKey, issue);
					} catch (RedmineException e) {
						e.printStackTrace();
					}
					
					subjectText.setText("");
					descriptionText.setText("");
					priorityCombo.setText(PRIORITY_NORMAL);

					updateIssues();
					updateEvents();
					updateHeights();
				}
			}
		};
		
		buttonCreateIssue.addListener(SWT.Selection, bCreateIssueListener);
		
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
			}
		};
		
		buttonCancel.addListener(SWT.Selection, bCancelListener);
	}
	
	@Override
	public void setFocus() {}
	
}
