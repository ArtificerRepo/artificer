package cz.muni.fi.srampBrowserTestPugin;

import org.apache.log4j.BasicConfigurator;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(SWTBotJunit4ClassRunner.class)
public class BrowserUITest {

	private static SWTWorkbenchBot bot;

	@BeforeClass
	public static void beforeClass() throws Exception {
		BasicConfigurator.configure();

		bot = new SWTWorkbenchBot();

		bot.menu("Window").menu("Show View").menu("Other...").click();

		SWTBotShell shell = bot.shell("Show View");
		shell.activate();
		bot.tree().expandNode("S-RAMP").select("Repository Browser");
		bot.button("OK").click();

		bot.menu("Window").menu("Show View").menu("Other...").click();

		shell = bot.shell("Show View");
		shell.activate();
		bot.tree().expandNode("General").select("Navigator");
		bot.button("OK").click();

		bot.menu("File").menu("New").menu("Project...").click();
		bot.tree().getTreeItem("Project").select();
		bot.button("Next >").click();
		bot.textWithLabel("&Project name:").setText("Test");
		bot.button("Finish").click();

		bot.tree().getTreeItem("Test").select().contextMenu("New").menu("File")
				.click();
		bot.tree().getTreeItem("Test").select();
		bot.textWithLabel("File na&me:").setText("Test");
		bot.button("Finish").click();
		bot.styledText().setText("a");
		bot.toolbarButtonWithTooltip("Save (Ctrl+S)").click();

	}

	@Test
	public void repositoryBrowserTest() {
		bot.viewByTitle("Navigator").setFocus();

		bot.tree().getTreeItem("Test").getNode("Test").select()
				.contextMenu("Import to S-RAMP repository").click();
		bot.button("OK").click();

		bot.viewByTitle("Repository Browser").setFocus();
		bot.activeView().toolbarPushButton("ConnectToServer").click();
		bot.textWithLabel("Password:").setText("admin");
		bot.text(1).setText("a");
		bot.button("OK").click();

		// waiting to connecting
		bot.sleep(2000);

		bot.viewByTitle("Navigator").setFocus();

		bot.tree().getTreeItem("Test").getNode("Test").select()
				.contextMenu("Import to S-RAMP repository").click();
		bot.text().setText("file");
		bot.button("OK").click();

		bot.table().getTableItem("file").select()
				.contextMenu("Import to workspace").click();
		bot.table().getTableItem("Test").select();
		bot.button("OK").click();
		// waiting to importing
		bot.sleep(2000);

		bot.table().getTableItem("file").select().contextMenu("Delete").click();

	}

}