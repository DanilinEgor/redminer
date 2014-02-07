package redminer;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.swt.layout.GridData;

import redminer.views.Redminer;


public class ReconnectHandler extends AbstractHandler implements IHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		GridData gd = (GridData) Redminer.connectionGroup.getLayoutData();
		gd.exclude = false;
		Redminer.connectionGroup.setVisible(true);

		gd = (GridData) Redminer.tasksGroup.getLayoutData();
		gd.exclude = true;
		Redminer.tasksGroup.setVisible(false);
		
		gd = (GridData) Redminer.creationGroup.getLayoutData();
		gd.exclude = true;
		Redminer.creationGroup.setVisible(false);

		Redminer.composite.layout(true);
		Redminer.ganttChart.getGanttComposite().clearGanttEvents();
		return null;
	}

	@Override
	public boolean isEnabled() {
		return !(Redminer.connectionGroup.getVisible());
	}
}
