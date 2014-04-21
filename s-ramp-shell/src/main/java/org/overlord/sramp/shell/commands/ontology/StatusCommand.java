package org.overlord.sramp.shell.commands.ontology;

import java.util.List;

import org.jboss.aesh.cl.CommandDefinition;
import org.jboss.aesh.cl.Option;
import org.overlord.sramp.shell.ShellCommandConstants;
import org.overlord.sramp.shell.i18n.Messages;

/**
 * Displays a summary of the current status, including what repository the user
 * is currently connected to (if any).
 *
 * @author eric.wittmann@redhat.com
 */
@CommandDefinition(name = ShellCommandConstants.Ontology.ONTOLOGY_COMMAND_STATUS, description = "Displays a summary of the current ontology status")
public class StatusCommand extends AbstractOntologyCommand {

    @Option(overrideRequired = true, name = "help", hasValue = false, shortName = 'h')
    private boolean _help;

    /**
     * Constructor.
     */
    public StatusCommand() {
    }

    /**
     * Execute.
     *
     * @return true, if successful
     * @throws Exception
     *             the exception
     * @see org.overlord.sramp.shell.api.shell.ShellCommand#execute()
     */
    @Override
    public boolean execute() throws Exception {
        super.execute();

        List feed = (List) getContext().getVariable(feedVarName);

        if (feed == null) {
            print(Messages.i18n.format("Ontology.Status.Status1")); //$NON-NLS-1$
        } else {
            print(Messages.i18n.format("Ontology.Status.Status2", feed.size())); //$NON-NLS-1$
        }
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.overlord.sramp.shell.BuiltInShellCommand#getName()
     */
    @Override
    public String getName() {
        return ShellCommandConstants.Ontology.ONTOLOGY_COMMAND_STATUS;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.overlord.sramp.shell.BuiltInShellCommand#isHelp()
     */
    @Override
    public boolean isHelp() {
        return _help;
    }

    /**
     * Sets the help.
     *
     * @param help
     *            the new help
     */
    public void setHelp(boolean help) {
        this._help = help;
    }

}
