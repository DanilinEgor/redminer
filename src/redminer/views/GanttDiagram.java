package redminer.views;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;

import org.eclipse.nebula.widgets.ganttchart.GanttChart;
import org.eclipse.nebula.widgets.ganttchart.GanttEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Shell;

import com.taskadapter.redmineapi.bean.Issue;

public class GanttDiagram{

	private static final long serialVersionUID = 1L;
	public static final String ID = "redminer.views.GanttDiagram";
	
    public GanttDiagram(ArrayList<Issue> issues, HashSet<Pair<Integer, Integer>> relations, HashSet<Pair<Integer, Integer>> relations1) {
		Shell shell = new Shell ();
		shell.setText("Gantt Chart");
		shell.setSize(600, 500);		
		shell.setLayout(new FillLayout());
		
		GanttChart ganttChart = new GanttChart(shell, SWT.NONE);
		
		GanttEvent[] events = new GanttEvent[issues.get(0).getId()+1];
		
		for (Issue issue : issues) {
			Date start = issue.getStartDate() == null ? issue.getUpdatedOn() : issue.getStartDate();
			Date end  = issue.getDueDate() == null ? new Date() : issue.getDueDate();
			Calendar st = Calendar.getInstance();
			Calendar en = Calendar.getInstance();
			st.setTime(start);
			en.setTime(end);
			events[issue.getId()] = new GanttEvent(ganttChart, issue.getSubject(), st, en, issue.getDoneRatio());
		}
		for (Pair<Integer,Integer> pair : relations) {
			int first = pair.getFirst();
			int second = pair.getSecond();
			ganttChart.addConnection(events[first], events[second]);
		}
		for (Pair<Integer,Integer> pair : relations) {
			int first = pair.getFirst();
			int second = pair.getSecond();
			ganttChart.addConnection(events[first], events[second]);
			ganttChart.addConnection(events[second], events[first]);
		}
		
		shell.open();
    }
}