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
package org.overlord.sramp.shell.util;

import java.util.ArrayList;
import java.util.List;

import jline.internal.Configuration;

/**
 * File name completer that undoes the Windows backslash mojo so that
 * the completer will work in most modern Windows shells, including
 * cygwin.
 *
 * @author eric.wittmann@redhat.com
 */
public class FileNameCompleter extends jline.console.completer.FileNameCompleter {

	/**
	 * Constructor.
	 */
	public FileNameCompleter() {
	}

	/**
	 * @see jline.console.completer.FileNameCompleter#complete(java.lang.String, int, java.util.List)
	 */
	@Override
	public int complete(String buffer, int cursor, List<CharSequence> candidates) {
		List<CharSequence> fileCandidates = new ArrayList<CharSequence>();
		int rval = super.complete(buffer, cursor, fileCandidates);
		// Clean up the file candidates if we're in Windows - backslashes become forward slashes.  This
		// should still work in most Windows shells, but in particular fixes a problem in cygwin
		// when this is *not* done (the cygwin shell will treat the backlashes as escape sequences).
		if (Configuration.isWindows()) {
			for (CharSequence candidate : fileCandidates) {
				candidates.add(candidate.toString().replace("\\", "/"));
			}
		}
		return rval;
	}

}
