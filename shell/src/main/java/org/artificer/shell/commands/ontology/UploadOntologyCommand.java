/*
 * Copyright 2012 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.artificer.shell.commands.ontology;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.artificer.client.ArtificerAtomApiClient;
import org.artificer.shell.i18n.Messages;
import org.artificer.shell.BuiltInShellCommand;
import org.artificer.shell.api.Arguments;
import org.artificer.shell.api.ShellContext;
import org.artificer.shell.api.SimpleShellContext;
import org.artificer.shell.util.FileNameCompleter;

/**
 * Uploads an ontology (S-RAMP OWL format) to the s-ramp repository.
 *
 * @author eric.wittmann@redhat.com
 */
public class UploadOntologyCommand extends BuiltInShellCommand {
    /**
     * Constructor.
     */
    public UploadOntologyCommand() {
    }

    /**
     * Main entry point - for use outside the interactive shell.
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            throw new Exception("Usage: " + UploadOntologyCommand.class + " [-file | -resource] [<filePath> | <resourcePath>]"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        String type = args[0];
        String path = args[1];
        StringBuilder argLine = new StringBuilder();
        boolean isResource = "-resource".equals(type); //$NON-NLS-1$
        if (isResource) {
            URL url = UploadOntologyCommand.class.getResource(path);
            if (url == null)
                throw new Exception("Could not find " + path + " on the classpath"); //$NON-NLS-1$ //$NON-NLS-2$
            argLine.append(url.toExternalForm());
        } else {
            File file = new File(path);
            if (!file.isFile())
                throw new FileNotFoundException(path);
            argLine.append(file.getCanonicalPath());
        }
        ArtificerAtomApiClient client = new ArtificerAtomApiClient("http://localhost:8080/artificer-server"); //$NON-NLS-1$
        QName clientVarName = new QName("s-ramp", "client"); //$NON-NLS-1$ //$NON-NLS-2$
        ShellContext context = new SimpleShellContext();
        context.setVariable(clientVarName, client);
        UploadOntologyCommand cmd = new UploadOntologyCommand();
        cmd.setArguments(new Arguments(argLine.toString()));
        cmd.setContext(context);
        cmd.execute();
    }

    @Override
    public boolean execute() throws Exception {
        String filePathArg = this.requiredArgument(0, Messages.i18n.format("UploadOntology.InvalidArgMsg.MissingPath")); //$NON-NLS-1$
        QName clientVarName = new QName("s-ramp", "client"); //$NON-NLS-1$ //$NON-NLS-2$
        ArtificerAtomApiClient client = (ArtificerAtomApiClient) getContext().getVariable(clientVarName);
        if (client == null) {
            print(Messages.i18n.format("MissingArtificerConnection")); //$NON-NLS-1$
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
                    print(Messages.i18n.format("UploadOntology.ReadingOntology", url.toExternalForm())); //$NON-NLS-1$
                    content = url.openStream();
                } else {
                    print(Messages.i18n.format("UploadOntology.CannotFind", filePathArg)); //$NON-NLS-1$
                }
            }
            client.uploadOntology(content);
            print(Messages.i18n.format("UploadOntology.SuccessfulUpload")); //$NON-NLS-1$
        } catch (Exception e) {
            print(Messages.i18n.format("UploadOntology.UploadFailed")); //$NON-NLS-1$
            print("\t" + e.getMessage()); //$NON-NLS-1$
            IOUtils.closeQuietly(content);
            return false;
        } finally {
            IOUtils.closeQuietly(content);
        }
        print("**********************************************************************"); //$NON-NLS-1$
        return true;
    }

    @Override
    public int tabCompletion(String lastArgument, List<CharSequence> candidates) {
        if (lastArgument == null)
            lastArgument = ""; //$NON-NLS-1$
        if (getArguments().isEmpty()) {
            FileNameCompleter delegate = new FileNameCompleter();
            return delegate.complete(lastArgument, lastArgument.length(), candidates);
        }
        return -1;
    }
}