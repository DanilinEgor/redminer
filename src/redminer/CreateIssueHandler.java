package redminer;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.swt.layout.GridData;

import redminer.views.Redminer;

public class CreateIssueHandler extends AbstractHandler implements IHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		GridData gd = (GridData) Redminer.connectionGroup.getLayoutData();
		gd.exclude = true;
		Redminer.connectionGroup.setVisible(false);

		gd = (GridData) Redminer.tasksGroup.getLayoutData();
		gd.exclude = true;
		Redminer.tasksGroup.setVisible(false);
		
		gd = (GridData) Redminer.creationGroup.getLayoutData();
		gd.exclude = false;
		Redminer.creationGroup.setVisible(true);
		
		Redminer.composite.layout(true);
		Redminer.updateIssues();
		Redminer.updateEvents();
		Redminer.updateHeights();
		return null;
	}
	
	@Override
	public boolean isEnabled() {
		return !(Redminer.connectionGroup.getVisible());
	}
}
