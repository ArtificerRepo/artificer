package org.overlord.sramp.shell;

import javax.xml.namespace.QName;

import org.junit.Assert;
import org.junit.Test;
import org.overlord.sramp.shell.aesh.AeshPopulator;
import org.overlord.sramp.shell.api.Arguments;
import org.overlord.sramp.shell.api.ShellCommand;
import org.overlord.sramp.shell.api.ShellContext;
import org.overlord.sramp.shell.commands.core.CreateArtifactCommand;

public class AeshPopulatorTest {

    private final ShellCommandFactory factory = new ShellCommandFactory();

    private final ShellContext context = new ShellContextImpl();

    private final AeshPopulator aeshPopulator = new AeshPopulator("/alias.txt");


    private void populateCreateCommand(String line) throws Exception {
        Arguments arguments = new Arguments(line);

        // The first argument is the qualified command name.
        QName commandName = arguments.removeCommandName();

        // Create the command.
        ShellCommand command = factory.createCommand(commandName);
        command.setContext(this.context);
        aeshPopulator.populateCommand(command, line);
        Assert.assertTrue(command instanceof CreateArtifactCommand);
        CreateArtifactCommand cac = (CreateArtifactCommand) command;
        Assert.assertNotNull(cac.getArtifactType());
        Assert.assertEquals("JavaArchive", cac.getArtifactName());
        Assert.assertNotNull(cac.getDescription());
    }

    @Test
    public void populateCommandTest() throws Exception {
        String line = "s-ramp:create --artifactType DtgovWorkflowQuery --name JavaArchive --description \"Query that is applied to all the JavaArchive Applications\"";
        populateCreateCommand(line);

    }

    @Test
    public void checkAlias() throws Exception {
        String line = "create --artifactType DtgovWorkflowQuery --name JavaArchive --description \"Query that is applied to all the JavaArchive Applications\"";
        populateCreateCommand(line);
    }
}
