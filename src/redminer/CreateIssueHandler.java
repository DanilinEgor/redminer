package redminer;

import java.util.ArrayList;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.swt.layout.GridData;

import com.taskadapter.redmineapi.bean.User;

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
		
		try {
			ArrayList<User> users = API.getUsers(Redminer.redmineHost, Redminer.apiAccessKey);
			for (User user : users) {
				System.out.println(user.toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
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
