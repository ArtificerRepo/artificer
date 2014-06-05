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
package org.overlord.sramp.shell.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.overlord.sramp.shell.CompletionConstants;


/**
 * File name completer that undoes the Windows backslash mojo so that
 * the completer will work in most modern Windows shells, including
 * cygwin.
 *
 * @author eric.wittmann@redhat.com
 */
public class FileNameCompleter {

    /**
     * Instantiates a new file name completer.
     */
    public FileNameCompleter() {

    }

    private static final boolean OS_IS_WINDOWS;

    static {
        String os = getOsName();
        OS_IS_WINDOWS = os.contains("windows"); //$NON-NLS-1$
    }

    /**
     * Complete.
     *
     * @param buffer
     *            the buffer
     * @param cursor
     *            the cursor
     * @param candidates
     *            the candidates
     * @return the int
     */
    public int complete(String buffer, final int cursor, final List<CharSequence> candidates) {
        // buffer can be null
        if (candidates != null) {
            if (buffer == null) {
                buffer = ""; //$NON-NLS-1$
            }

            if (OS_IS_WINDOWS) {
                buffer = buffer.replace('/', '\\');
            }

            String translated = buffer;

            File homeDir = getUserHome();

            // Special character: ~ maps to the user's home directory
            if (translated.startsWith("~" + separator())) { //$NON-NLS-1$
                translated = homeDir.getPath() + translated.substring(1);
            } else if (translated.startsWith("~")) { //$NON-NLS-1$
                translated = homeDir.getParentFile().getAbsolutePath();
            } else if (!(translated.startsWith(separator()))) {
                String cwd = getUserDir().getAbsolutePath();
                translated = cwd + separator() + translated;
            }
            String lastPart = ""; //$NON-NLS-1$
            String firstPart = ""; //$NON-NLS-1$
            if (!translated.endsWith(File.separator)) {
                firstPart = translated.substring(0, translated.lastIndexOf(File.separator) + 1);
                lastPart = translated.substring(translated.lastIndexOf(File.separator) + 1);
            } else {
                firstPart = translated;
            }
            File file = new File(firstPart);
            final File dir;

            if (firstPart.endsWith(separator())) {
                dir = file;
            } else {
                dir = file.getParentFile();
            }

            File[] entries = dir == null ? new File[0] : dir.listFiles();

            matchFiles(buffer, firstPart + lastPart, entries, candidates);

            // Clean up the file candidates if we're in Windows - backslashes
            // become forward slashes. This
            // should still work in most Windows shells, but in particular fixes
            // a problem in cygwin
            // when this is *not* done (the cygwin shell will treat the
            // backlashes as escape sequences).
            if (isWindows()) {
                List<CharSequence> copy = new ArrayList<CharSequence>(candidates);
                candidates.clear();
                for (CharSequence candidate : copy) {
                    candidates.add(candidate.toString().replace("\\", "/")); //$NON-NLS-1$ //$NON-NLS-2$
                }
            }
        }
        return CompletionConstants.NO_APPEND_SEPARATOR;


    }

    /**
     * Separator.
     *
     * @return the string
     */
    protected String separator() {
        return File.separator;
    }


    /**
     * Gets the user dir.
     *
     * @return the user dir
     */
    protected File getUserDir() {
        return new File("."); //$NON-NLS-1$
    }

    /**
     * Match files.
     *
     * @param buffer
     *            the buffer
     * @param translated
     *            the translated
     * @param files
     *            the files
     * @param candidates
     *            the candidates
     * @return the int
     */
    protected void matchFiles(final String buffer, final String translated, final File[] files,
            final List<CharSequence> candidates) {

        for (File file : files) {
            if (file.getAbsolutePath().startsWith(translated)) {
                // CharSequence name = file.getName() + (matches == 1 &&
                // file.isDirectory() ? separator() : " ");
                if (StringUtils.isNotBlank(buffer)) {
                    String toAdd = file.getAbsolutePath().substring(
                            file.getAbsolutePath().lastIndexOf(buffer));
                    if (file.isDirectory()) {
                        toAdd += File.separator;
                    }
                    candidates.add(toAdd);
                } else {
                    String toAdd = file.getName();
                    if (file.isDirectory()) {
                        toAdd += File.separator;
                    }
                    candidates.add(toAdd);
                }

            }
        }

    }

    /**
     * Render.
     *
     * @param file
     *            the file
     * @param name
     *            the name
     * @return the char sequence
     */
    protected CharSequence render(final File file, final CharSequence name) {
        return name;
    }

    /**
     * Gets the user home.
     *
     * @return the user home
     */
    protected static File getUserHome() {
        return new File(System.getProperty("user.home")); //$NON-NLS-1$
    }

    /**
     * Gets the os name.
     *
     * @return the os name
     */
    protected static String getOsName() {
        return System.getProperty("os.name").toLowerCase(); //$NON-NLS-1$
    }

    /**
     * Checks if is windows.
     *
     * @return true, if is windows
     */
    public static boolean isWindows() {
        return getOsName().startsWith("windows"); //$NON-NLS-1$
    }

}