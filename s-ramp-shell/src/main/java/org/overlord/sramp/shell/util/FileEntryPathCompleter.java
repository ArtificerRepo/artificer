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
import java.util.List;

import org.overlord.sramp.atom.archive.SrampArchive;
import org.overlord.sramp.atom.archive.SrampArchiveEntry;
import org.overlord.sramp.atom.archive.SrampArchiveException;
import org.overlord.sramp.shell.CompletionConstants;

/**
 * File Entry Name completer
 * 
 * @author David Virgil Naranjo
 */
public class FileEntryPathCompleter {

    private final SrampArchive archive;

    /**
     * Instantiates a new file entry path completer.
     *
     * @param archive
     *            the archive
     */
    public FileEntryPathCompleter(SrampArchive archive) {
        this.archive = archive;
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
        if(archive!=null){
            try {
                for(SrampArchiveEntry entry:archive.getEntries()){
                    if(entry.getPath().startsWith(buffer)){
                        if (entry.getPath().indexOf(File.separator, buffer.length()) != -1) {
                            candidates.add(entry.getPath().substring(0,
                                    entry.getPath().indexOf(File.separator, buffer.length()) + 1));
                        } else if (!entry.getPath().equals(buffer)) {
                            candidates.add(entry.getPath());
                        }

                    }
                }
            } catch (SrampArchiveException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        if (candidates.size() == 1 && candidates.get(0).toString().endsWith(File.separator)) {
            return CompletionConstants.NO_APPEND_SEPARATOR;
        } else {
            return -1;
        }

    }

}
