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
package org.overlord.sramp.karaf.commands;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;
import java.util.UUID;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.felix.gogo.commands.Command;
import org.overlord.commons.codec.AesEncrypter;
import org.overlord.commons.karaf.commands.configure.AbstractConfigureCommand;

/**
 * @author Brett Meyer
 */
@Command(scope = "overlord:s-ramp", name = "configure")
public class ConfigureCommand extends AbstractConfigureCommand {

    @Override
    protected Object doExecute() throws Exception {
        super.doExecute();

        copyFile("sramp-modeshape-fuse.json", "sramp-modeshape.json"); //$NON-NLS-1$ //$NON-NLS-2$
        copyFile("sramp-ui.properties"); //$NON-NLS-1$

        String randomSrampJmsPassword = UUID.randomUUID().toString();

        Properties usersProperties = new Properties();
        File srcFile = new File(karafConfigPath + "users.properties"); //$NON-NLS-1$
        usersProperties.load(new FileInputStream(srcFile));
        String encryptedPassword = "{CRYPT}" + DigestUtils.sha256Hex(randomSrampJmsPassword) + "{CRYPT}"; //$NON-NLS-1$ //$NON-NLS-2$
        usersProperties.setProperty("srampjms", encryptedPassword); //$NON-NLS-1$
        usersProperties.store(new FileOutputStream(srcFile), ""); //$NON-NLS-1$

        // TODO: Host is currently hardcoded to "localhost" -- does that need to be configurable?
        Properties srampProperties = new Properties();
        srampProperties.load(this.getClass().getClassLoader().getResourceAsStream("/sramp.properties")); //$NON-NLS-1$
        encryptedPassword = "${crypt:" + AesEncrypter.encrypt(randomSrampJmsPassword) + "}"; //$NON-NLS-1$ //$NON-NLS-2$
        srampProperties.setProperty("sramp.config.events.jms.password", encryptedPassword); //$NON-NLS-1$
        File destFile = new File(karafConfigPath + "sramp.properties"); //$NON-NLS-1$
        srampProperties.store(new FileOutputStream(destFile), ""); //$NON-NLS-1$

        File dir = new File(karafConfigPath + "overlord-apps"); //$NON-NLS-1$
        if (!dir.exists()) {
            dir.mkdir();
        }
        copyFile("srampui-overlordapp.properties", "overlord-apps/srampui-overlordapp.properties"); //$NON-NLS-1$ //$NON-NLS-2$

        return null;
    }
}
