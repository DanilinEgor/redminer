package redminer;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;

import redminer.views.Redminer;


public class RefreshHandler extends AbstractHandler implements IHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
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
