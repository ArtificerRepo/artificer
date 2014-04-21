/*
 * Copyright 2014 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.overlord.sramp.shell.commands.audit;

import javax.xml.namespace.QName;

import org.apache.commons.lang.StringUtils;
import org.jboss.aesh.cl.CommandDefinition;
import org.jboss.aesh.cl.Option;
import org.jboss.aesh.cl.validator.CommandValidator;
import org.jboss.aesh.cl.validator.CommandValidatorException;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.overlord.sramp.client.audit.AuditEntrySummary;
import org.overlord.sramp.client.audit.AuditResultSet;
import org.overlord.sramp.shell.ShellCommandConstants;
import org.overlord.sramp.shell.commands.core.AbstractCoreShellCommand;
import org.overlord.sramp.shell.i18n.Messages;


/**
 * Displays the audit trail for an artifact.
 *
 * @author eric.wittmann@redhat.com
 */
@CommandDefinition(name = ShellCommandConstants.Audit.AUDIT_COMMAND_SHOW_AUDIT_TRAIL, description = "Displays the audit trail for an artifact.", validator = ShowAuditTrailCommand.CustomValidator.class)
public class ShowAuditTrailCommand extends AbstractCoreShellCommand {

    private final static QName feedVarName = new QName("s-ramp", "feed"); //$NON-NLS-1$ //$NON-NLS-2$

    @Option(required = false, name = "feedIndex", hasValue = true, shortName = 'f')
    private Integer _feedIndex;

    @Option(required = false, name = "uuid", hasValue = true, shortName = 'u')
    private String _uuid;


    @Option(overrideRequired = true, name = "help", hasValue = false, shortName = 'h')
    private boolean _help;


    /**
     * Constructor.
     */
	public ShowAuditTrailCommand() {
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

        if (client == null) {
            print(Messages.i18n.format("MissingSRAMPConnection")); //$NON-NLS-1$
            return false;
        }
        BaseArtifactType artifact = getArtifact(_feedIndex, _uuid);
        String artifactUuid = null;
        if (artifact != null) { //$NON-NLS-1$
            artifactUuid = artifact.getUuid();
        } else { //$NON-NLS-1$
            artifactUuid = _uuid;
        }

        AuditResultSet auditTrail = client.getAuditTrailForArtifact(artifactUuid);
        QName artifactVarName = new QName("audit", "auditTrail"); //$NON-NLS-1$ //$NON-NLS-2$
        getContext().setVariable(artifactVarName, auditTrail);
        print(Messages.i18n.format("ShowAuditTrail.EntriesSummary", auditTrail.size())); //$NON-NLS-1$
        print("  Idx  " + Messages.i18n.format("AuditEntryLabel")); //$NON-NLS-1$ //$NON-NLS-2$
        print("  ---  -----------"); //$NON-NLS-1$
        int idx = 1;
        for (AuditEntrySummary auditEntrySummary : auditTrail) {
            print("  %1$3d  %2$s", idx++, auditEntrySummary.toString()); //$NON-NLS-1$
        }
        return true;
	}


    /* (non-Javadoc)
     * @see org.overlord.sramp.shell.BuiltInShellCommand#getName()
     */
    @Override
    public String getName() {
        return ShellCommandConstants.Audit.AUDIT_COMMAND_SHOW_AUDIT_TRAIL;
    }


    /**
     * Validates the Show Audit Trail Command
     * 
     * @author David Virgil Naranjo
     */
    public class CustomValidator implements CommandValidator<ShowAuditTrailCommand> {

        /**
         * Instantiates a new custom validator.
         */
        public CustomValidator() {

        }

        /* (non-Javadoc)
         * @see org.jboss.aesh.cl.validator.CommandValidator#validate(org.jboss.aesh.console.command.Command)
         */
        @Override
        public void validate(ShowAuditTrailCommand command) throws CommandValidatorException {
            if (StringUtils.isBlank(command.getUuid()) && command.getFeedIndex() == null) {
                throw new CommandValidatorException(Messages.i18n.format("Artifact.feed.no.option.selected"));

            } else if (!StringUtils.isBlank(command.getUuid()) && command.getFeedIndex() != null) {
                throw new CommandValidatorException(
                        Messages.i18n.format("Artifact.feed.both.option.selected"));
            }
        }

    }

    /**
     * Gets the feed index.
     *
     * @return the feed index
     */
    public Integer getFeedIndex() {
        return _feedIndex;
    }

    /**
     * Sets the feed index.
     *
     * @param feedIndex
     *            the new feed index
     */
    public void setFeedIndex(Integer feedIndex) {
        this._feedIndex = feedIndex;
    }

    /**
     * Gets the uuid.
     *
     * @return the uuid
     */
    public String getUuid() {
        return _uuid;
    }

    /**
     * Sets the uuid.
     *
     * @param uuid
     *            the new uuid
     */
    public void setUuid(String uuid) {
        this._uuid = uuid;
    }

    /* (non-Javadoc)
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

    /**
     * Gets the feedvarname.
     *
     * @return the feedvarname
     */
    public static QName getFeedvarname() {
        return feedVarName;
    }

}
