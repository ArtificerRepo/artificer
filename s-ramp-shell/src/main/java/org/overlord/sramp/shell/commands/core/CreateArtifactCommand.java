package org.overlord.sramp.shell.commands.core;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.commons.lang.StringUtils;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.overlord.sramp.atom.err.SrampAtomException;
import org.overlord.sramp.client.SrampAtomApiClient;
import org.overlord.sramp.client.SrampClientException;
import org.overlord.sramp.client.SrampClientQuery;
import org.overlord.sramp.client.query.QueryResultSet;
import org.overlord.sramp.common.ArtifactType;
import org.overlord.sramp.shell.BuiltInShellCommand;
import org.overlord.sramp.shell.api.InvalidCommandArgumentException;
import org.overlord.sramp.shell.i18n.Messages;

/**
 * @author David Virgil Naranjo
 *
 */
public class CreateArtifactCommand extends BuiltInShellCommand {

    private SrampAtomApiClient client;

    private BaseArtifactEnum determineArtifactType(String artifactType) {
        return BaseArtifactEnum.EXTENDED_ARTIFACT_TYPE;
    }
    /* (non-Javadoc)
     * @see org.overlord.sramp.shell.api.ShellCommand#execute()
     */
    @Override
    public boolean execute() throws Exception {
        String modeltype = this.requiredArgument(0, Messages.i18n.format("ArtifactModel.Mandatory"));
        String name = this.requiredArgument(1, Messages.i18n.format("ArtifactName.Mandatory"));
        String description=this.optionalArgument(2);

        String type="";
        String model="";
        String[] typeArray=modeltype.split("/");
        if(typeArray.length==1){
            throw new InvalidCommandArgumentException(0, Messages.i18n.format("ArtifactModel.NotCorrectFormat")); //$NON-NLS-1$
        }
        else{
            type = typeArray[typeArray.length - 1];
            for(int i=0;i<typeArray.length-1;i++){
                model += typeArray[i];
                if (i != typeArray.length - 2) {
                    model += "/";
                }
            }
        }
        QName clientVarName = new QName("s-ramp", "client"); //$NON-NLS-1$ //$NON-NLS-2$
        client = (SrampAtomApiClient) getContext().getVariable(clientVarName);
        if (client == null) {
            print(Messages.i18n.format("MissingSRAMPConnection")); //$NON-NLS-1$
            return false;
        }
        if (isUnique(name, modeltype)) {
            ArtifactType at = ArtifactType.valueOf(model, type, false);
            if (at.isDerived()) {
                throw new InvalidCommandArgumentException(0, Messages.i18n.format("ArtifactModel.isDerived")); //$NON-NLS-1$
            } else if (at.isDocument()) {
                throw new InvalidCommandArgumentException(0, Messages.i18n.format("ArtifactModel.isDocument"));
            } else {
                BaseArtifactType artifact = at.newArtifactInstance();
                artifact.setName(name);
                if (StringUtils.isNotBlank(description)) {
                    artifact.setDescription(description);
                }
                artifact = client.createArtifact(artifact);
                // Put the artifact in the session as the active artifact
                QName artifactVarName = new QName("s-ramp", "artifact"); //$NON-NLS-1$ //$NON-NLS-2$
                getContext().setVariable(artifactVarName, artifact);
            }
        } else {
            print(Messages.i18n.format("ArtifactModel.NameAlreadyExistInSramp", name, modeltype)); //$NON-NLS-1$
            return false;
        }


        print(Messages.i18n.format("CreateArtifactCommand.Successful", name)); //$NON-NLS-1$
        return true;
    }

    private boolean isUnique(String name, String model) {
        int page_size = 20;
        StringBuilder queryBuilder = new StringBuilder();
        // Initial query

        queryBuilder.append("/s-ramp/" + model); //$NON-NLS-1$

        List<String> criteria = new ArrayList<String>();
        List<Object> params = new ArrayList<Object>();

        criteria.add("fn:matches(@name, ?)"); //$NON-NLS-1$
        params.add(name.replace("*", ".*")); //$NON-NLS-1$ //$NON-NLS-2$

        queryBuilder.append("["); //$NON-NLS-1$
        queryBuilder.append(StringUtils.join(criteria, " and ")); //$NON-NLS-1$
        queryBuilder.append("]"); //$NON-NLS-1$

        // Create the query, and parameterize it
        SrampClientQuery query = client.buildQuery(queryBuilder.toString());
        for (Object param : params) {
            if (param instanceof String) {
                query.parameter((String) param);
            }
            if (param instanceof Calendar) {
                query.parameter((Calendar) param);
            }
        }
        QueryResultSet resultSet = null;
        try {
            resultSet = query.count(page_size + 1).query();
        } catch (SrampClientException e) {
            print(Messages.i18n.format("MissingSRAMPConnection")); //$NON-NLS-1$
            return false;
        } catch (SrampAtomException e) {
            print(Messages.i18n.format("MissingSRAMPConnection")); //$NON-NLS-1$
            return false;
        }
        if (resultSet.size() == 0) {
            return true;
        }

        return false;
    }
}
