/*
 * Copyright 2012 JBoss Inc
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
package org.overlord.sramp.shell.commands.core;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.overlord.sramp.client.SrampAtomApiClient;
import org.overlord.sramp.common.ArtifactType;
import org.overlord.sramp.common.ArtifactTypeEnum;
import org.overlord.sramp.common.visitors.ArtifactVisitorHelper;
import org.overlord.sramp.shell.BuiltInShellCommand;
import org.overlord.sramp.shell.i18n.Messages;
import org.overlord.sramp.shell.util.FileNameCompleter;
import org.overlord.sramp.shell.util.PrintArtifactMetaDataVisitor;

import javax.xml.namespace.QName;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

/**
 * Uploads an artifact to the s-ramp repository.
 *
 * @author eric.wittmann@redhat.com
 */
public class UploadArtifactCommand extends BuiltInShellCommand {

    /**
     * Constructor.
     */
    public UploadArtifactCommand() {
    }

    /**
     * @see org.overlord.sramp.shell.api.shell.ShellCommand#execute()
     */
    @Override
    public boolean execute() throws Exception {
        String filePathArg = this.requiredArgument(0, Messages.i18n.format("Upload.InvalidArgMsg.LocalFile")); //$NON-NLS-1$
        String artifactTypeArg = this.optionalArgument(1);

        QName clientVarName = new QName("s-ramp", "client"); //$NON-NLS-1$ //$NON-NLS-2$
        SrampAtomApiClient client = (SrampAtomApiClient) getContext().getVariable(clientVarName);
        if (client == null) {
            print(Messages.i18n.format("MissingSRAMPConnection")); //$NON-NLS-1$
            return false;
        }
        InputStream content = null;
        try {

            File file = new File(filePathArg);
            if (file.exists()) {
                content = FileUtils.openInputStream(file);
            } else {
                URL url = this.getClass().getClassLoader().getResource(filePathArg);
                if (url != null) {
                    print(Messages.i18n.format("Upload.InvalidArgMsg.LocalFile")); //$NON-NLS-1$
                    content = url.openStream();
                } else {
                    print(Messages.i18n.format("Upload.InvalidArgMsg.LocalFile")); //$NON-NLS-1$
                }
            }
            ArtifactType artifactType = null;
            if (artifactTypeArg != null) {
                artifactType = ArtifactType.valueOf(artifactTypeArg);
                if (artifactType.isExtendedType()) {
                    artifactType = ArtifactType.ExtendedDocument(artifactType.getExtendedType());
                }
            }
            BaseArtifactType artifact = client.uploadArtifact(artifactType, content, file.getName());
            IOUtils.closeQuietly(content);

            // Put the artifact in the session as the active artifact
            QName artifactVarName = new QName("s-ramp", "artifact"); //$NON-NLS-1$ //$NON-NLS-2$
            getContext().setVariable(artifactVarName, artifact);
            print(Messages.i18n.format("Upload.Success")); //$NON-NLS-1$
            PrintArtifactMetaDataVisitor visitor = new PrintArtifactMetaDataVisitor();
            ArtifactVisitorHelper.visitArtifact(visitor, artifact);
        } catch (Exception e) {
            print(Messages.i18n.format("Upload.Failure")); //$NON-NLS-1$
            print("\t" + e.getMessage()); //$NON-NLS-1$
            IOUtils.closeQuietly(content);
            return false;
        }
        return true;
    }

    /**
     * @see org.overlord.sramp.shell.api.shell.AbstractShellCommand#tabCompletion(java.lang.String,
     *      java.util.List)
     */
    @Override
    public int tabCompletion(String lastArgument, List<CharSequence> candidates) {
        if (getArguments().isEmpty()) {
            if (lastArgument == null)
                lastArgument = ""; //$NON-NLS-1$
            FileNameCompleter delegate = new FileNameCompleter();
            return delegate.complete(lastArgument, lastArgument.length(), candidates);
        } else if (getArguments().size() == 1) {
            for (ArtifactTypeEnum t : ArtifactTypeEnum.values()) {
                String candidate = t.getType();
                if (lastArgument == null || candidate.startsWith(lastArgument)) {
                    candidates.add(candidate);
                }
            }
            return 0;
        }
        return -1;
    }

}
