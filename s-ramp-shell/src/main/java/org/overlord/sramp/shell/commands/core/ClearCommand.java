package org.overlord.sramp.shell.commands.core;

import javax.xml.namespace.QName;

import org.jboss.aesh.cl.CommandDefinition;
import org.jboss.aesh.cl.Option;
import org.overlord.sramp.shell.ShellCommandConstants;

/**
 * Displays a summary of the current status, including what repository the user
 * is currently connected to (if any).
 * 
 * @author eric.wittmann@redhat.com
 */
@CommandDefinition(name = ShellCommandConstants.Sramp.S_RAMP_COMMAND_CLEAR, description = "Clear the S-RAMP session")
public class ClearCommand extends AbstractCoreShellCommand {

    @Option(overrideRequired = true, name = "help", hasValue = false, shortName = 'h')
    private boolean _help;

    /**
     * Constructor.
     */
    public ClearCommand() {
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

        QName artifactVarName = new QName("s-ramp", "artifact"); //$NON-NLS-1$ //$NON-NLS-2$
        QName feedVarName = new QName("s-ramp", "feed"); //$NON-NLS-1$ //$NON-NLS-2$

        getContext().removeVariable(artifactVarName);
        getContext().removeVariable(feedVarName);
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.overlord.sramp.shell.BuiltInShellCommand#getName()
     */
    @Override
    public String getName() {
        return ShellCommandConstants.Sramp.S_RAMP_COMMAND_CLEAR;
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