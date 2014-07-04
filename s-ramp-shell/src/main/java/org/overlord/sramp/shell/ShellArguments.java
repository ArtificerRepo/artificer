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
package org.overlord.sramp.shell;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.overlord.sramp.shell.i18n.Messages;

/**
 * Encapsulates all possible shell arguments.
 * 
 * @author Brett Meyer
 */
public class ShellArguments {

    private boolean simple = false;

    private String batchFilePath = null;

    private String logFilePath = null;

    private Map<String, String> propertiesFromFile = new HashMap<String, String>();;

    public ShellArguments(String[] args) {
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            boolean hasNext = args.length > i + 1;

            if (arg.equalsIgnoreCase("-simple")) { //$NON-NLS-1$
                simple = true;
            } else if (arg.equalsIgnoreCase("-f") && hasNext) { //$NON-NLS-1$
                i++;
                batchFilePath = args[i];
            } else if (arg.equals("-propertiesFile")) { //$NON-NLS-1$
                getPropertiesFromFile(arg);
            } else if (arg.equalsIgnoreCase("-l") && hasNext) { //$NON-NLS-1$
                i++;
                logFilePath = args[i];
            }
        }
    }

    /**
     * Gets the properties from a file.
     * 
     * @param filePath
     *            the file path
     * @throws ShellArgumentException
     *             the shell argument exception
     */
    private void getPropertiesFromFile(String filePath) {
        File f = new File(filePath);

        Properties props = new Properties();

        try {
            props.load(new FileInputStream(f));
        } catch (FileNotFoundException e) {
            System.out.println("Error: " + filePath + " " //$NON-NLS-1$ //$NON-NLS-2$
                    + Messages.i18n.format("InvalidArgMsg.propertiesFile.not.exist")); //$NON-NLS-1$
        } catch (IOException e) {
            System.out.println("Error: " + filePath + " " //$NON-NLS-1$ //$NON-NLS-2$
                    + Messages.i18n.format("InvalidArgMsg.propertiesFile.error.reading") + ": " //$NON-NLS-1$
                    + e.getMessage());
        }
        for (final String name : props.stringPropertyNames()) {
            propertiesFromFile.put(name, props.getProperty(name));
        }
    }

    /**
     * @return the simple
     */
    public boolean isSimple() {
        return simple;
    }

    /**
     * @return the batchFile
     */
    public boolean hasBatchFile() {
        return batchFilePath != null && batchFilePath.length() > 0;
    }

    /**
     * @return the batchFilePath
     */
    public String getBatchFilePath() {
        return batchFilePath;
    }

    /**
     * @return the properties
     */
    public Map<String, String> getPropertiesFromFile() {
        return propertiesFromFile;
    }

    /**
     * @return the logFile
     */
    public boolean hasLogFile() {
        return logFilePath != null && logFilePath.length() > 0;
    }

    /**
     * @return the logFilePath
     */
    public String getLogFilePath() {
        return logFilePath;
    }
}
