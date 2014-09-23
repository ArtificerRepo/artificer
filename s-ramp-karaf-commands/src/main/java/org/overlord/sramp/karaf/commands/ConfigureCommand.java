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
import org.overlord.sramp.karaf.commands.i18n.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Brett Meyer
 */
@Command(scope = "overlord:s-ramp", name = "configure")
public class ConfigureCommand extends AbstractConfigureCommand {

    private static final Logger logger = LoggerFactory.getLogger(ConfigureCommand.class);

    @Override
    protected Object doExecute() throws Exception {
        logger.info(Messages.getString("configure.command.executed")); //$NON-NLS-1$

        super.doExecute();

        logger.debug(Messages.getString("configure.command.copying.files")); //$NON-NLS-1$
        copyFile("sramp-modeshape.json"); //$NON-NLS-1$
        copyFile("sramp-ui.properties"); //$NON-NLS-1$

        logger.debug(Messages.getString("configure.command.copying.files.end")); //$NON-NLS-1$
        String randomSrampJmsPassword = UUID.randomUUID().toString();

        logger.debug(Messages.getString("configure.command.adding.jms.user")); //$NON-NLS-1$
        Properties usersProperties = new Properties();
        File srcFile = new File(karafConfigPath + "users.properties"); //$NON-NLS-1$
        usersProperties.load(new FileInputStream(srcFile));
        // Adding the jms user to the users.properties
        String encryptedPassword = "{CRYPT}" + DigestUtils.sha256Hex(randomSrampJmsPassword) + "{CRYPT}"; //$NON-NLS-1$ //$NON-NLS-2$
        usersProperties.setProperty(ConfigureConstants.SRAMP_EVENTS_JMS_DEFAULT_USER, encryptedPassword); //$NON-NLS-1$
        logger.debug(Messages.getString("configure.command.adding.user.end")); //$NON-NLS-1$

        // Adding to the admin user the sramp grants:
        String adminUser = (String) usersProperties.get("admin");
        adminUser += ",admin.sramp"; //$NON-NLS-1$
        usersProperties.setProperty("admin", adminUser); //$NON-NLS-1$

        logger.debug(Messages.getString("configure.command.modify.admin.roles")); //$NON-NLS-1$
        // Storing the users.properties changes
        usersProperties.store(new FileOutputStream(srcFile), ""); //$NON-NLS-1$


        // TODO: Host is currently hardcoded to "localhost" -- does that need to be configurable?
        logger.debug(Messages.getString("configure.command.modifying.sramp.properties")); //$NON-NLS-1$
        Properties srampProperties = new Properties();
        srampProperties.load(this.getClass().getClassLoader().getResourceAsStream("/sramp.properties")); //$NON-NLS-1$
        encryptedPassword = "${crypt:" + AesEncrypter.encrypt(randomSrampJmsPassword) + "}"; //$NON-NLS-1$ //$NON-NLS-2$
        srampProperties.setProperty(ConfigureConstants.SRAMP_EVENTS_JMS_USER, ConfigureConstants.SRAMP_EVENTS_JMS_DEFAULT_USER); //$NON-NLS-1$
        srampProperties.setProperty(ConfigureConstants.SRAMP_EVENTS_JMS_PASSWORD, encryptedPassword); //$NON-NLS-1$
        File destFile = new File(karafConfigPath + ConfigureConstants.SRAMP_PROPERTIES_FILE_NAME); //$NON-NLS-1$
        srampProperties.store(new FileOutputStream(destFile), ""); //$NON-NLS-1$
        String message = Messages.format("configure.command.new.user.added", ConfigureConstants.SRAMP_EVENTS_JMS_DEFAULT_USER); //$NON-NLS-1$
        logger.info(message); //$NON-NLS-1$
        System.out.println(message);
        File dir = new File(karafConfigPath + "overlord-apps"); //$NON-NLS-1$
        if (!dir.exists()) {
            dir.mkdir();
        }
        copyFile("overlord-apps/srampui-overlordapp.properties"); //$NON-NLS-1$
        logger.info(Messages.getString("configure.command.end.execution")); //$NON-NLS-1$
        return null;
    }
}
